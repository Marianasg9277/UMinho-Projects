package com.example.loginapi.service.titulos;

import com.example.loginapi.dto.FiscalizacaoValidacaoItemDTO;
import com.example.loginapi.dto.FiscalizacaoVerificacaoResponse;
import com.example.loginapi.model.colaboradores.Colaborador;
import com.example.loginapi.model.colaboradores.enums.TipoColaborador;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.model.titulos.HistoricoValidacao;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.PasseQrToken;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.titulos.enums.EstadoOperacionalPasse;
import com.example.loginapi.repository.colaboradores.ColaboradorRepository;
import com.example.loginapi.repository.titulos.HistoricoValidacaoRepository;
import com.example.loginapi.repository.titulos.PasseQrTokenRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class FiscalizacaoQrService {

    @Autowired private ColaboradorRepository colaboradorRepo;
    @Autowired private PasseQrTokenRepository qrTokenRepo;
    @Autowired private TransacaoRepository transacaoRepo;
    @Autowired private HistoricoValidacaoRepository historicoRepo;

    @Transactional(readOnly = true)
    public FiscalizacaoVerificacaoResponse verificarQr(String codigo, Authentication authentication) {

        // 1. Utilizador autenticado
        String email = authentication != null ? authentication.getName() : null;
        if (email == null) {
            return erroSimples("NAO_AUTENTICADO", "Utilizador não autenticado.");
        }

        // 2. Colaborador ativo
        Optional<Colaborador> optColab = colaboradorRepo.findByEmailAndAtivoTrue(email);
        if (optColab.isEmpty()) {
            return erroSimples("NAO_FISCALIZADOR", "Utilizador não encontrado como colaborador ativo.");
        }
        Colaborador colab = optColab.get();

        // 3. Role FISCALIZADOR
        if (colab.getTipoColaborador() != TipoColaborador.FISCALIZADOR) {
            return erroSimples("NAO_FISCALIZADOR", "Utilizador autenticado não é um fiscalizador.");
        }

        // 4. Linha atribuída
        Linha linhaFiscalizador = colab.getLinhaAtual();
        if (linhaFiscalizador == null) {
            return erroSimples("SEM_LINHA_ATRIBUIDA", "Fiscalizador sem linha atribuída. Contacte o supervisor.");
        }
        String nomeLinha = formatarNomeLinha(linhaFiscalizador);

        // 5. Identificar QR
        Optional<PasseQrToken> optToken = qrTokenRepo.findByToken(codigo);
        if (optToken.isPresent()) {
            return verificarPasse(optToken.get(), linhaFiscalizador, nomeLinha);
        }

        Optional<Transacao> optTransacao = transacaoRepo.findByCodigoQr(codigo);
        if (optTransacao.isPresent()) {
            return verificarBilhete(optTransacao.get(), linhaFiscalizador, nomeLinha);
        }

        FiscalizacaoVerificacaoResponse resp = erroSimples("QR_DESCONHECIDO", "Código QR não reconhecido.");
        resp.setLinhaFiscalizador(nomeLinha);
        return resp;
    }

    // ── Verificação de passe ──────────────────────────────────────────────────

    private FiscalizacaoVerificacaoResponse verificarPasse(PasseQrToken qrToken, Linha linhaFiscalizador, String nomeLinha) {
        FiscalizacaoVerificacaoResponse resp = new FiscalizacaoVerificacaoResponse();
        resp.setTipoTitulo("PASSE");
        resp.setLinhaFiscalizador(nomeLinha);

        Passe passe = qrToken.getPasse();
        if (passe == null) {
            resp.setValido(false);
            resp.setMotivoInvalidade("PASSE_INVALIDO");
            resp.setMensagem("Passe associado ao QR não encontrado.");
            return resp;
        }

        // Dados do titular
        if (passe.getCliente() != null) {
            resp.setTitular(passe.getCliente().getNomeCompleto());
            if (passe.getCliente().getFotoPassePath() != null) {
                resp.setTemFotoPasse(true);
                resp.setFotoTitularUrl("/api/validar/passes/" + passe.getId() + "/foto");
            }
        }
        if (passe.getTipoPasse() != null)          resp.setTipoPasse(passe.getTipoPasse().getNome());
        if (passe.getCoroa() != null)              resp.setCoroa(passe.getCoroa().getNome());
        if (passe.getTipoEstatutoAplicado() != null) resp.setEstatuto(passe.getTipoEstatutoAplicado().name());
        if (passe.getDataInicio() != null)         resp.setValidadeInicio(passe.getDataInicio().toString());
        if (passe.getDataFim() != null)            resp.setValidadeFim(passe.getDataFim().toString());
        if (passe.getEstadoOperacional() != null)  resp.setEstado(passe.getEstadoOperacional().name());

        // Verificar token
        if (qrToken.getRevogadoEm() != null) {
            resp.setValido(false);
            resp.setMotivoInvalidade("TOKEN_REVOGADO");
            resp.setMensagem("Token QR revogado.");
            resp.setUltimasValidacoes(obterUltimasValidacoesPasse(passe.getId()));
            return resp;
        }
        if (Instant.now().isAfter(qrToken.getExpiraEm())) {
            resp.setValido(false);
            resp.setMotivoInvalidade("TOKEN_EXPIRADO");
            resp.setMensagem("Código QR expirado. Peça ao passageiro que gere um novo QR.");
            resp.setUltimasValidacoes(obterUltimasValidacoesPasse(passe.getId()));
            return resp;
        }

        // Verificar estado operacional do passe (sem escrever na BD)
        EstadoOperacionalPasse estadoEfetivo = derivarEstadoOperacional(passe);
        resp.setEstado(estadoEfetivo.name());
        if (estadoEfetivo != EstadoOperacionalPasse.ACTIVE) {
            String motivo = switch (estadoEfetivo) {
                case FALTA_RENOVAR -> "PASSE_EXPIRADO";
                case INACTIVE      -> "PASSE_INATIVO";
                default            -> "PASSE_INVALIDO";
            };
            String msg = switch (estadoEfetivo) {
                case FALTA_RENOVAR -> "Passe expirado. O passageiro precisa de renovar.";
                case INACTIVE      -> "Passe inativo ou com pagamento pendente.";
                default            -> "Passe inválido.";
            };
            resp.setValido(false);
            resp.setMotivoInvalidade(motivo);
            resp.setMensagem(msg);
            resp.setUltimasValidacoes(obterUltimasValidacoesPasse(passe.getId()));
            return resp;
        }

        // Passe estruturalmente válido — verificar histórico de validação
        return avaliarHistoricoValidacao(resp, passe.getId(), null, linhaFiscalizador);
    }

    // ── Verificação de bilhete ────────────────────────────────────────────────

    private FiscalizacaoVerificacaoResponse verificarBilhete(Transacao transacao, Linha linhaFiscalizador, String nomeLinha) {
        FiscalizacaoVerificacaoResponse resp = new FiscalizacaoVerificacaoResponse();
        resp.setTipoTitulo("BILHETE");
        resp.setLinhaFiscalizador(nomeLinha);

        if (transacao.getTipoBilhete() != null) resp.setTipoBilhete(transacao.getTipoBilhete().getNome());
        if (transacao.getDataCompra() != null)  resp.setValidadeInicio(transacao.getDataCompra().toLocalDate().toString());
        if (transacao.getValidoAte() != null)   resp.setValidadeFim(transacao.getValidoAte().toString());
        if (transacao.getEstadoPagamento() != null) resp.setEstado(transacao.getEstadoPagamento().name());

        if (transacao.getCliente() != null)           resp.setTitular(transacao.getCliente().getNomeCompleto());
        else if (transacao.getGuestNome() != null)    resp.setTitular(transacao.getGuestNome());

        EstadoPagamento estado = transacao.getEstadoPagamento();
        if (estado != EstadoPagamento.PAID && estado != EstadoPagamento.USED) {
            resp.setValido(false);
            resp.setMotivoInvalidade("BILHETE_NAO_PAGO");
            resp.setMensagem("Bilhete não pago ou cancelado.");
            resp.setUltimasValidacoes(obterUltimasValidacoesBilhete(transacao.getId()));
            return resp;
        }

        // Bilhete pago/usado → verificar histórico de validação
        return avaliarHistoricoValidacao(resp, null, transacao.getId(), linhaFiscalizador);
    }

    // ── Avaliação do histórico de validação ───────────────────────────────────

    private FiscalizacaoVerificacaoResponse avaliarHistoricoValidacao(
            FiscalizacaoVerificacaoResponse resp, Long passeId, Long transacaoId, Linha linhaFiscalizador) {

        // Buscar última validação com sucesso na linha do fiscalizador
        List<HistoricoValidacao> validacoesNaLinha = passeId != null
                ? historicoRepo.findSuccessfulByPasseIdAndLinhaId(passeId, linhaFiscalizador.getId())
                : historicoRepo.findSuccessfulByTransacaoIdAndLinhaId(transacaoId, linhaFiscalizador.getId());

        // Buscar todas as últimas 5 validações (para contexto)
        List<HistoricoValidacao> ultimas = passeId != null
                ? historicoRepo.findTop5ByPasseIdOrderByDataValidacaoDesc(passeId)
                : historicoRepo.findTop5ByTransacaoIdOrderByDataValidacaoDesc(transacaoId);

        resp.setUltimasValidacoes(mapearUltimas(ultimas));

        if (!validacoesNaLinha.isEmpty()) {
            // Validação encontrada na linha do fiscalizador
            HistoricoValidacao ultima = validacoesNaLinha.get(0);
            resp.setValido(true);
            resp.setValidadoNaLinhaAtual(true);
            resp.setDataHoraValidacao(ultima.getDataValidacao());
            if (ultima.getLinha() != null) {
                resp.setLinhaValidacao(formatarNomeLinha(ultima.getLinha()));
            }
            resp.setMensagem("Fiscalização OK. Título válido e validado nesta linha.");
            return resp;
        }

        // Sem validação nesta linha — verificar se foi validado noutras linhas
        List<HistoricoValidacao> validacoesNoutrasLinhas = passeId != null
                ? historicoRepo.findTop5ByPasseIdAndSucessoTrueOrderByDataValidacaoDesc(passeId)
                : historicoRepo.findTop5ByTransacaoIdAndSucessoTrueOrderByDataValidacaoDesc(transacaoId);

        if (!validacoesNoutrasLinhas.isEmpty()) {
            HistoricoValidacao outra = validacoesNoutrasLinhas.get(0);
            resp.setValido(false);
            resp.setValidadoNaLinhaAtual(false);
            resp.setMotivoInvalidade("VALIDADO_NOUTRA_LINHA");
            if (outra.getLinha() != null) {
                resp.setLinhaValidacao(formatarNomeLinha(outra.getLinha()));
                resp.setMensagem("Título validado noutra linha: " + formatarNomeLinha(outra.getLinha()) + ".");
            } else {
                resp.setMensagem("Título validado noutra linha.");
            }
            resp.setDataHoraValidacao(outra.getDataValidacao());
            return resp;
        }

        // Sem qualquer validação registada
        resp.setValido(false);
        resp.setValidadoNaLinhaAtual(false);
        resp.setMotivoInvalidade("SEM_VALIDACAO_REGISTADA");
        resp.setMensagem("Título válido mas sem validação registada. O passageiro não foi validado pelo motorista.");
        return resp;
    }

    // ── Utilitários ───────────────────────────────────────────────────────────

    private EstadoOperacionalPasse derivarEstadoOperacional(Passe passe) {
        LocalDate hoje = LocalDate.now();
        if (passe.getDataFim() != null && hoje.isAfter(passe.getDataFim())) {
            return EstadoOperacionalPasse.FALTA_RENOVAR;
        }
        if (passe.getDataInicio() != null && hoje.isBefore(passe.getDataInicio())) {
            return EstadoOperacionalPasse.INACTIVE;
        }
        return passe.getEstadoOperacional() != null ? passe.getEstadoOperacional() : EstadoOperacionalPasse.INACTIVE;
    }

    private List<FiscalizacaoValidacaoItemDTO> obterUltimasValidacoesPasse(Long passeId) {
        return mapearUltimas(historicoRepo.findTop5ByPasseIdOrderByDataValidacaoDesc(passeId));
    }

    private List<FiscalizacaoValidacaoItemDTO> obterUltimasValidacoesBilhete(Long transacaoId) {
        return mapearUltimas(historicoRepo.findTop5ByTransacaoIdOrderByDataValidacaoDesc(transacaoId));
    }

    private List<FiscalizacaoValidacaoItemDTO> mapearUltimas(List<HistoricoValidacao> registos) {
        return registos.stream().map(h -> {
            FiscalizacaoValidacaoItemDTO item = new FiscalizacaoValidacaoItemDTO();
            item.setDataHora(h.getDataValidacao());
            item.setSucesso(h.getSucesso());
            item.setMotivoRejeicao(h.getMotivoRejeicao());
            item.setDetalhes(h.getDetalhes());
            if (h.getLinha() != null) {
                item.setLinha(formatarNomeLinha(h.getLinha()));
            }
            return item;
        }).toList();
    }

    private String formatarNomeLinha(Linha linha) {
        if (linha == null) return null;
        String numero = linha.getNumero() != null ? linha.getNumero() : "";
        String nome   = linha.getNome()   != null ? linha.getNome()   : "";
        return ("Linha " + numero + (nome.isBlank() ? "" : " — " + nome)).trim();
    }

    private FiscalizacaoVerificacaoResponse erroSimples(String motivo, String mensagem) {
        FiscalizacaoVerificacaoResponse resp = new FiscalizacaoVerificacaoResponse();
        resp.setValido(false);
        resp.setMotivoInvalidade(motivo);
        resp.setMensagem(mensagem);
        return resp;
    }
}
