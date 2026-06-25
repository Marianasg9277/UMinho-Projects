package com.example.loginapi.controller.titulos;

import com.example.loginapi.dto.ValidacaoQrResponse;
import com.example.loginapi.model.colaboradores.Colaborador;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.titulos.HistoricoValidacao;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.PasseQrToken;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.titulos.enums.EstadoComercialPasse;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.model.colaboradores.enums.TipoColaborador;
import com.example.loginapi.repository.colaboradores.ColaboradorRepository;
import com.example.loginapi.repository.infraestrutura.CoroaRepository;
import com.example.loginapi.repository.titulos.HistoricoValidacaoRepository;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import com.example.loginapi.repository.titulos.PasseQrTokenRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.service.titulos.FotoPasseService;
import com.example.loginapi.service.titulos.PasseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.example.loginapi.model.pagamentos.Pagamento;


@RestController
@RequestMapping("/api/validar")
@Tag(name = "Validação QR", description = "Validação de códigos QR de passes e bilhetes")
public class ValidacaoController {

    @Autowired private PasseService passeService;
    @Autowired private FotoPasseService fotoPasseService;
    @Autowired private PasseQrTokenRepository qrTokenRepo;
    @Autowired private TransacaoRepository transacaoRepo;
    @Autowired private HistoricoValidacaoRepository historicoValidacaoRepo;
    @Autowired private LinhaRepository linhaRepo;
    @Autowired private CoroaRepository coroaRepo;
    @Autowired private ColaboradorRepository colaboradorRepo;

    // ── Endpoint principal ────────────────────────────────────────────────────

    @GetMapping("/qr/{codigo}")
    @Transactional
    @Operation(summary = "Validar código QR — suporta Passes e Bilhetes")
    public ResponseEntity<ValidacaoQrResponse> validarQr(
            @PathVariable String codigo,
            @RequestParam Long coroaId,
            @RequestParam(required = false) Long linhaId,
            Authentication authentication) {

        // 1. Resolver coroa — obrigatório
        Coroa coroa = coroaRepo.findById(coroaId).orElse(null);
        if (coroa == null) {
            return ResponseEntity.badRequest().body(
                    new ValidacaoQrResponse(false, "COROA_INVALIDA", "Coroa não reconhecida."));
        }

        // 2. Resolver linha — alocação com fallback manual
        String email = authentication != null ? authentication.getName() : null;
        Linha linhaResolvida = resolverLinha(email, linhaId);
        if (linhaResolvida == null) {
            ValidacaoQrResponse resp = new ValidacaoQrResponse(
                    false, "SEM_LINHA_ALOCADA",
                    "Sem linha alocada. Selecione a linha manualmente.");
            resp.setRequiresLinhaManual(true);
            return ResponseEntity.status(422).body(resp);
        }

        // 3. Identificar QR como passe
        Optional<PasseQrToken> optToken = qrTokenRepo.findByToken(codigo);
        if (optToken.isPresent()) {
            return ResponseEntity.ok(validarPasseComToken(optToken.get(), linhaResolvida, coroa));
        }

        // 4. Identificar QR como bilhete
        Optional<Transacao> optTransacao = transacaoRepo.findByCodigoQr(codigo);
        if (optTransacao.isPresent()) {
            return ResponseEntity.ok(validarBilhete(optTransacao.get(), linhaResolvida, coroa));
        }

        // 5. Código desconhecido
        return ResponseEntity.ok(new ValidacaoQrResponse(false, "TOKEN_NAO_ENCONTRADO", "Código QR não reconhecido."));
    }

    // ── Foto do titular ───────────────────────────────────────────────────────

    @GetMapping("/passes/{passeId}/foto")
    @Transactional(readOnly = true)
    @Operation(summary = "Servir foto do titular de um passe — uso exclusivo de motorista/fiscalizador")
    public ResponseEntity<byte[]> obterFotoTitularPasse(@PathVariable Long passeId) {
        Passe passe = passeService.obterPasse(passeId).orElse(null);
        if (passe == null) return ResponseEntity.notFound().build();

        Cliente cliente = passe.getCliente();
        if (cliente == null || cliente.getFotoPassePath() == null) {
            return ResponseEntity.notFound().build();
        }

        try {
            byte[] imagem = fotoPasseService.lerFoto(cliente);
            String contentType = fotoPasseService.inferirContentType(cliente.getFotoPassePath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("private, no-store");
            return ResponseEntity.ok().headers(headers).body(imagem);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    // ── Validação de passe ────────────────────────────────────────────────────

    private ValidacaoQrResponse validarPasseComToken(PasseQrToken qrToken, Linha linha, Coroa coroaSelecionada) {
        Instant agora = Instant.now();

        Passe passe = qrToken.getPasse();
        if (passe == null) {
            return new ValidacaoQrResponse(false, "PASSE_INVALIDO", "Passe associado não encontrado.");
        }

        passeService.atualizarEstadoOperacionalSeNecessario(passe);

        ValidacaoQrResponse resp = new ValidacaoQrResponse();
        resp.setTipoDocumento("PASSE");
        resp.setEstadoOperacional(passe.getEstadoOperacional().name());
        resp.setCoroaValidada(coroaSelecionada.getNome());
        resp.setLinhaAlocada(formatarNomeLinha(linha));

        if (passe.getTipoPasse() != null)  resp.setTipoPasseNome(passe.getTipoPasse().getNome());
        if (passe.getCoroa() != null)      resp.setCoroaNome(passe.getCoroa().getNome());
        if (passe.getDataInicio() != null) resp.setDataInicio(passe.getDataInicio().toString());
        if (passe.getDataFim() != null)    resp.setDataFim(passe.getDataFim().toString());

        if (passe.getCliente() != null) {
            resp.setNomeTitular(passe.getCliente().getNomeCompleto());
            if (passe.getCliente().getFotoPassePath() != null) {
                resp.setTemFotoPasse(true);
                resp.setFotoTitularUrl("/api/validar/passes/" + passe.getId() + "/foto");
            }
        }

        // Token revogado
        if (qrToken.getRevogadoEm() != null) {
            resp.setValido(false);
            resp.setMotivoRejeicao("TOKEN_REVOGADO");
            resp.setMensagem("Token QR revogado.");
            registarHistoricoPasse(passe, linha, coroaSelecionada, false, "TOKEN_REVOGADO");
            return resp;
        }

        // Token expirado
        if (agora.isAfter(qrToken.getExpiraEm())) {
            resp.setValido(false);
            resp.setMotivoRejeicao("TOKEN_EXPIRADO");
            resp.setMensagem("QR expirado. Gere um novo código.");
            registarHistoricoPasse(passe, linha, coroaSelecionada, false, "TOKEN_EXPIRADO");
            return resp;
        }

        // Estado operacional
        switch (passe.getEstadoOperacional()) {
            case ACTIVE -> {} // prosseguir para verificação de coroa
            case FALTA_RENOVAR -> {
                resp.setValido(false);
                resp.setMotivoRejeicao("PASSE_EXPIRADO");
                resp.setMensagem("Passe expirado. É necessário renovar.");
                registarHistoricoPasse(passe, linha, coroaSelecionada, false, "PASSE_EXPIRADO");
                return resp;
            }
            case INACTIVE -> {
                boolean pago = passe.getEstadoComercial() == EstadoComercialPasse.PAID;
                String motivo = pago ? "PASSE_NAO_INICIADO" : "PASSE_INATIVO";
                String msg    = pago ? "Passe ainda não iniciado." : "Passe inativo — pagamento pendente.";
                resp.setValido(false);
                resp.setMotivoRejeicao(motivo);
                resp.setMensagem(msg);
                registarHistoricoPasse(passe, linha, coroaSelecionada, false, motivo);
                return resp;
            }
            default -> {
                resp.setValido(false);
                resp.setMotivoRejeicao("PASSE_INVALIDO");
                resp.setMensagem("Passe inválido.");
                registarHistoricoPasse(passe, linha, coroaSelecionada, false, "PASSE_INVALIDO");
                return resp;
            }
        }

        // Verificar coroa — apenas quando passe está ACTIVE
        if (passe.getCoroa() != null && !tituloCobreCoroa(passe.getCoroa(), coroaSelecionada)) {
            resp.setValido(false);
            resp.setMotivoRejeicao("COROA_INSUFICIENTE");
            resp.setMensagem("O passe (" + passe.getCoroa().getNome()
                    + ") não é válido na " + coroaSelecionada.getNome() + ".");
            registarHistoricoPasse(passe, linha, coroaSelecionada, false, "COROA_INSUFICIENTE");
            return resp;
        }

        resp.setValido(true);
        resp.setMensagem("Passe válido.");
        registarHistoricoPasse(passe, linha, coroaSelecionada, true, null);
        return resp;
    }

    // ── Validação de bilhete ──────────────────────────────────────────────────

    private ValidacaoQrResponse validarBilhete(Transacao transacao, Linha linha, Coroa coroaSelecionada) {
        ValidacaoQrResponse resp = new ValidacaoQrResponse();
        resp.setTipoDocumento("BILHETE");
        resp.setCoroaValidada(coroaSelecionada.getNome());
        resp.setLinhaAlocada(formatarNomeLinha(linha));

        if (transacao.getTipoBilhete() != null) resp.setTipoPasseNome(transacao.getTipoBilhete().getNome());
        if (transacao.getDataCompra() != null)  resp.setDataInicio(transacao.getDataCompra().toLocalDate().toString());
        if (transacao.getCliente() != null)     resp.setNomeTitular(transacao.getCliente().getNomeCompleto());
        else if (transacao.getGuestNome() != null) resp.setNomeTitular(transacao.getGuestNome());

        // Coroa do título (se existir) — para exibição
        if (transacao.getCoroaId() != null) {
            coroaRepo.findById(transacao.getCoroaId())
                    .ifPresent(c -> resp.setCoroaNome(c.getNome()));
        }

        EstadoPagamento estado = transacao.getEstadoPagamento();

        // 1. Bilhete sem pagamento confirmado
        if (estado != EstadoPagamento.PAID && estado != EstadoPagamento.USED) {
            resp.setValido(false);
            resp.setMotivoRejeicao("BILHETE_NAO_PAGO");
            resp.setMensagem("Bilhete não pago ou inválido.");
            registarHistoricoBilhete(transacao, linha, coroaSelecionada, false, "BILHETE_NAO_PAGO");
            return resp;
        }

        LocalDateTime agora = LocalDateTime.now();

        // 2. Calcular janela de validade sem modificar a entidade ainda
        //    (só persiste se todas as verificações passarem)
        LocalDateTime primeiraValidacaoCalc = transacao.getPrimeiraValidacaoEm();
        LocalDateTime validoAteCalc = transacao.getValidoAte();
        boolean isPrimeiraValidacao = false;

        if (primeiraValidacaoCalc == null && validoAteCalc == null) {
            int horas = transacao.getTipoBilhete() != null
                    ? transacao.getTipoBilhete().getValidadeHoras() : 2;
            primeiraValidacaoCalc = agora;
            validoAteCalc = agora.plusHours(horas);
            isPrimeiraValidacao = true;
        }

        if (validoAteCalc != null) resp.setDataFim(validoAteCalc.toString());

        // 3. Validade expirada
        if (validoAteCalc != null && agora.isAfter(validoAteCalc)) {
            resp.setValido(false);
            resp.setMotivoRejeicao("BILHETE_EXPIRADO");
            resp.setMensagem("Bilhete expirado. A validade terminou em " + validoAteCalc.toLocalDate() + ".");
            registarHistoricoBilhete(transacao, linha, coroaSelecionada, false, "BILHETE_EXPIRADO");
            return resp;
        }

        // 4. Verificar coroa
        if (transacao.getCoroaId() != null) {
            Coroa coroaTitulo = coroaRepo.findById(transacao.getCoroaId()).orElse(null);
            if (coroaTitulo != null && !tituloCobreCoroa(coroaTitulo, coroaSelecionada)) {
                resp.setValido(false);
                resp.setMotivoRejeicao("COROA_INSUFICIENTE");
                resp.setMensagem("O bilhete (" + coroaTitulo.getNome()
                        + ") não é válido na " + coroaSelecionada.getNome() + ".");
                registarHistoricoBilhete(transacao, linha, coroaSelecionada, false, "COROA_INSUFICIENTE");
                return resp;
            }
        }

        // 5. Todas as verificações passaram — persistir estado
        if (isPrimeiraValidacao) {
            transacao.setPrimeiraValidacaoEm(primeiraValidacaoCalc);
            transacao.setValidoAte(validoAteCalc);
        }
        transacao.setEstadoPagamento(EstadoPagamento.USED); // idempotente
        transacaoRepo.save(transacao);

        registarHistoricoBilhete(transacao, linha, coroaSelecionada, true, null);
        resp.setValido(true);
        resp.setEstadoOperacional(EstadoPagamento.USED.name());
        resp.setMensagem("Bilhete válido.");
        return resp;
    }

    // ── Resolução de linha ────────────────────────────────────────────────────

    private Linha resolverLinha(String email, Long linhaIdFallback) {
        if (email != null) {
            Optional<Colaborador> optColab = colaboradorRepo.findByEmailAndAtivoTrue(email);
            if (optColab.isPresent()) {
                Colaborador c = optColab.get();
                if (c.getTipoColaborador() == TipoColaborador.MOTORISTA
                        && c.getAutocarro() != null
                        && c.getAutocarro().getLinha() != null) {
                    return c.getAutocarro().getLinha();
                }
            }
        }
        if (linhaIdFallback != null) {
            return linhaRepo.findById(linhaIdFallback).orElse(null);
        }
        return null;
    }

    // ── Regra de coroa ────────────────────────────────────────────────────────

    /**
     * Devolve true se o título (com a sua coroa) é válido na coroa selecionada.
     * Coroa 2 cobre Coroa 1 e Coroa 2; Coroa 1 cobre apenas Coroa 1.
     * O número é extraído do nome ("Coroa 2" → 2); fallback para o id.
     */
    private boolean tituloCobreCoroa(Coroa titulo, Coroa selecionada) {
        return extrairNumeroCoroa(titulo) >= extrairNumeroCoroa(selecionada);
    }

    private int extrairNumeroCoroa(Coroa coroa) {
        if (coroa == null) return 0;
        String nome = coroa.getNome();
        if (nome != null) {
            Matcher m = Pattern.compile("\\d+").matcher(nome);
            if (m.find()) {
                try { return Integer.parseInt(m.group()); } catch (NumberFormatException ignored) {}
            }
        }
        return coroa.getId() != null ? coroa.getId().intValue() : 0;
    }

    // ── Registo em histórico ──────────────────────────────────────────────────

    private void registarHistoricoPasse(Passe passe, Linha linha, Coroa coroa,
                                         boolean sucesso, String motivoRejeicao) {
        if (passe == null || passe.getCliente() == null) return;

        HistoricoValidacao h = new HistoricoValidacao();
        h.setCliente(passe.getCliente());
        h.setDataValidacao(LocalDateTime.now());
        h.setTipoTitulo(HistoricoValidacao.TipoTitulo.PASSE);
        h.setPasse(passe);
        h.setLinha(linha);
        h.setCoroa(coroa);
        h.setSucesso(sucesso);
        h.setMotivoRejeicao(motivoRejeicao);
        h.setTipoDescricao(passe.getTipoPasse() != null ? passe.getTipoPasse().getNome() : "Passe");

        // Detalhes descritivos limpos — sem prefixos técnicos
        StringBuilder detalhes = new StringBuilder();
        if (passe.getCoroa() != null) detalhes.append(passe.getCoroa().getNome());
        if (coroa != null) {
            if (detalhes.length() > 0) detalhes.append(" | ");
            detalhes.append("Zona validada: ").append(coroa.getNome());
        }
        h.setDetalhes(detalhes.length() > 0 ? detalhes.toString() : null);

        historicoValidacaoRepo.save(h);
    }

    private void registarHistoricoBilhete(Transacao transacao, Linha linha, Coroa coroa,
                                           boolean sucesso, String motivoRejeicao) {
        if (transacao == null || transacao.getCliente() == null) return;

        HistoricoValidacao h = new HistoricoValidacao();
        h.setCliente(transacao.getCliente());
        h.setDataValidacao(LocalDateTime.now());
        h.setTipoTitulo(HistoricoValidacao.TipoTitulo.BILHETE);
        h.setTransacao(transacao);
        h.setLinha(linha);
        h.setCoroa(coroa);
        h.setSucesso(sucesso);
        h.setMotivoRejeicao(motivoRejeicao);
        h.setTipoDescricao(transacao.getTipoBilhete() != null ? transacao.getTipoBilhete().getNome() : "Bilhete");
        h.setDetalhes(null); // sem prefixos técnicos

        historicoValidacaoRepo.save(h);
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

    private String formatarNomeLinha(Linha linha) {
        if (linha == null) return null;
        String numero = linha.getNumero() != null ? linha.getNumero() : "";
        String nome   = linha.getNome() != null ? linha.getNome() : "";
        return ("Linha " + numero + (nome.isBlank() ? "" : " — " + nome)).trim();
    }
}
