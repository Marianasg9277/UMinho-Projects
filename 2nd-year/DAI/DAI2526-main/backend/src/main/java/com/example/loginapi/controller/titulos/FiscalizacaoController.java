package com.example.loginapi.controller.titulos;

import com.example.loginapi.dto.FiscalizacaoQrResponse;
import com.example.loginapi.dto.FiscalizacaoVerificacaoResponse;
import com.example.loginapi.dto.ValidacaoRegistadaDTO;
import com.example.loginapi.model.titulos.HistoricoValidacao;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.PasseQrToken;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.repository.titulos.HistoricoValidacaoRepository;
import com.example.loginapi.repository.titulos.PasseQrTokenRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.service.titulos.FiscalizacaoQrService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.example.loginapi.model.infraestrutura.Linha;


/**
 * Endpoints de fiscalização — todos read-only.
 * Não consome bilhetes, não grava histórico, não altera passes nem transações.
 */
@RestController
@RequestMapping("/api/fiscalizacao")
public class FiscalizacaoController {

    @Autowired private PasseQrTokenRepository qrTokenRepo;
    @Autowired private TransacaoRepository transacaoRepo;
    @Autowired private HistoricoValidacaoRepository historicoRepo;
    @Autowired private FiscalizacaoQrService fiscalizacaoQrService;

    // ── Endpoint principal de fiscalização QR ────────────────────────────────

    @GetMapping("/qr/{codigo}/verificar")
    @Transactional(readOnly = true)
    public ResponseEntity<FiscalizacaoVerificacaoResponse> verificarQr(
            @PathVariable String codigo,
            Authentication authentication) {
        return ResponseEntity.ok(fiscalizacaoQrService.verificarQr(codigo, authentication));
    }

    // ── Endpoint: lista de validações registadas ──────────────────────────────

    @GetMapping("/validacoes")
    @Transactional(readOnly = true)
    public ResponseEntity<List<ValidacaoRegistadaDTO>> listarValidacoes() {
        List<HistoricoValidacao> registos = historicoRepo.findTop200ByOrderByDataValidacaoDesc();
        List<ValidacaoRegistadaDTO> dtos = registos.stream()
                .map(this::mapToDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    private ValidacaoRegistadaDTO mapToDto(HistoricoValidacao h) {
        ValidacaoRegistadaDTO dto = new ValidacaoRegistadaDTO();
        dto.setId(h.getId());
        dto.setDataValidacao(h.getDataValidacao());
        dto.setTipoTitulo(h.getTipoTitulo() != null ? h.getTipoTitulo().name() : null);
        dto.setTipoDescricao(h.getTipoDescricao());

        // Resultado e motivo — usados diretamente dos campos sucesso e motivoRejeicao
        dto.setResultado(Boolean.TRUE.equals(h.getSucesso()));
        dto.setMotivoRejeicao(h.getMotivoRejeicao());

        // Linha (FetchType.EAGER em HistoricoValidacao — sempre carregada)
        if (h.getLinha() != null) {
            dto.setLinha(h.getLinha().getNumero() + " — " + h.getLinha().getNome());
        }

        // Titular (LAZY — carregado dentro da transação @Transactional)
        if (h.getCliente() != null) {
            dto.setNomeTitular(h.getCliente().getNomeCompleto());
        }

        // Enriquecimento de PASSE (LAZY — seguro dentro da transação)
        if (h.getPasse() != null) {
            Passe p = h.getPasse();
            if (p.getTipoPasse() != null)           dto.setTipoPasse(p.getTipoPasse().getNome());
            if (p.getCoroa() != null)               dto.setCoroa(p.getCoroa().getNome());
            if (p.getTipoEstatutoAplicado() != null) dto.setEstatuto(p.getTipoEstatutoAplicado().name());
            if (p.getDataInicio() != null)          dto.setValidadeInicio(p.getDataInicio().toString());
            if (p.getDataFim() != null)             dto.setValidadeFim(p.getDataFim().toString());
            if (p.getEstadoOperacional() != null)   dto.setEstadoPasse(p.getEstadoOperacional().name());
        }

        // Enriquecimento de BILHETE (LAZY — seguro dentro da transação)
        if (h.getTransacao() != null) {
            Transacao t = h.getTransacao();
            if (t.getTipoBilhete() != null)   dto.setTipoBilhete(t.getTipoBilhete().getNome());
            if (t.getDataCompra() != null)    dto.setDataCompra(t.getDataCompra().toLocalDate().toString());
            if (t.getValidoAte() != null)     dto.setValidadeFim(t.getValidoAte().toLocalDate().toString());
            if (t.getEstadoPagamento() != null) dto.setEstadoBilhete(t.getEstadoPagamento().name());
            // Titular de compra guest (fallback)
            if (dto.getNomeTitular() == null && t.getGuestNome() != null) {
                dto.setNomeTitular(t.getGuestNome());
            }
            // Linha da transação (fallback se historico.linha for null)
            if (dto.getLinha() == null && t.getLinha() != null) {
                dto.setLinha(t.getLinha().getNumero() + " — " + t.getLinha().getNome());
            }
        }

        return dto;
    }

    // ── Endpoint legado: consulta por QR (mantido, sem uso pela página) ───────

    @GetMapping("/qr/{codigo}")
    @Transactional(readOnly = true)
    public ResponseEntity<FiscalizacaoQrResponse> inspecionarQr(@PathVariable String codigo) {
        Optional<PasseQrToken> optToken = qrTokenRepo.findByToken(codigo);
        if (optToken.isPresent()) {
            return ResponseEntity.ok(inspecionarPasse(optToken.get()));
        }

        Optional<Transacao> optTransacao = transacaoRepo.findByCodigoQr(codigo);
        if (optTransacao.isPresent()) {
            return ResponseEntity.ok(inspecionarBilhete(optTransacao.get()));
        }

        FiscalizacaoQrResponse resp = new FiscalizacaoQrResponse();
        resp.setValido(false);
        resp.setMensagem("Código QR não reconhecido.");
        resp.setMotivoInvalidade("TOKEN_NAO_ENCONTRADO");
        return ResponseEntity.ok(resp);
    }

    private FiscalizacaoQrResponse inspecionarPasse(PasseQrToken qrToken) {
        FiscalizacaoQrResponse resp = new FiscalizacaoQrResponse();
        resp.setTipoTitulo("PASSE");

        Passe passe = qrToken.getPasse();
        if (passe == null) {
            resp.setValido(false);
            resp.setMensagem("Passe associado não encontrado.");
            resp.setMotivoInvalidade("PASSE_INVALIDO");
            return resp;
        }

        if (passe.getCliente() != null) resp.setNomeTitular(passe.getCliente().getNomeCompleto());
        if (passe.getTipoPasse() != null) resp.setTipoPasse(passe.getTipoPasse().getNome());
        if (passe.getCoroa() != null) resp.setCoroa(passe.getCoroa().getNome());
        if (passe.getTipoEstatutoAplicado() != null) resp.setEstatuto(passe.getTipoEstatutoAplicado().name());
        if (passe.getDataInicio() != null) resp.setValidadeInicio(passe.getDataInicio().toString());
        if (passe.getDataFim() != null) resp.setValidadeFim(passe.getDataFim().toString());

        if (qrToken.getRevogadoEm() != null) {
            resp.setValido(false); resp.setEstado("REVOGADO");
            resp.setMotivoInvalidade("TOKEN_REVOGADO"); resp.setMensagem("Token QR revogado.");
            return resp;
        }
        if (Instant.now().isAfter(qrToken.getExpiraEm())) {
            resp.setValido(false); resp.setEstado("TOKEN_EXPIRADO");
            resp.setMotivoInvalidade("TOKEN_EXPIRADO"); resp.setMensagem("QR expirado.");
            return resp;
        }

        LocalDate hoje = LocalDate.now();
        if (passe.getDataFim() != null && hoje.isAfter(passe.getDataFim())) {
            resp.setValido(false); resp.setEstado("FALTA_RENOVAR");
            resp.setMotivoInvalidade("PASSE_EXPIRADO"); resp.setMensagem("Passe expirado.");
            return resp;
        }
        if (passe.getDataInicio() != null && hoje.isBefore(passe.getDataInicio())) {
            resp.setValido(false); resp.setEstado("INACTIVE");
            resp.setMotivoInvalidade("PASSE_NAO_INICIADO"); resp.setMensagem("Passe ainda não iniciado.");
            return resp;
        }

        if (passe.getEstadoOperacional() != null) {
            resp.setEstado(passe.getEstadoOperacional().name());
            switch (passe.getEstadoOperacional()) {
                case ACTIVE      -> { resp.setValido(true);  resp.setMensagem("Passe válido."); }
                case FALTA_RENOVAR -> { resp.setValido(false); resp.setMotivoInvalidade("PASSE_EXPIRADO"); resp.setMensagem("Passe expirado."); }
                case INACTIVE    -> { resp.setValido(false); resp.setMotivoInvalidade("PASSE_INATIVO"); resp.setMensagem("Passe inativo."); }
                default          -> { resp.setValido(false); resp.setMotivoInvalidade("PASSE_INVALIDO"); resp.setMensagem("Passe inválido."); }
            }
        } else {
            resp.setValido(false); resp.setMotivoInvalidade("PASSE_INVALIDO"); resp.setMensagem("Passe inválido.");
        }
        return resp;
    }

    private FiscalizacaoQrResponse inspecionarBilhete(Transacao transacao) {
        FiscalizacaoQrResponse resp = new FiscalizacaoQrResponse();
        resp.setTipoTitulo("BILHETE");

        if (transacao.getTipoBilhete() != null) resp.setTipoBilhete(transacao.getTipoBilhete().getNome());
        if (transacao.getLinha() != null) resp.setLinha(transacao.getLinha().getOrigem() + " → " + transacao.getLinha().getDestino());
        if (transacao.getDataCompra() != null) resp.setDataCompra(transacao.getDataCompra().toLocalDate().toString());
        if (transacao.getValidoAte() != null) resp.setValidadeFim(transacao.getValidoAte().toLocalDate().toString());
        if (transacao.getCliente() != null) resp.setNomeTitular(transacao.getCliente().getNomeCompleto());
        else if (transacao.getGuestNome() != null) resp.setNomeTitular(transacao.getGuestNome());

        resp.setEstado(transacao.getEstadoPagamento() != null ? transacao.getEstadoPagamento().name() : "DESCONHECIDO");

        if (transacao.getEstadoPagamento() == EstadoPagamento.USED) {
            resp.setValido(false); resp.setMotivoInvalidade("BILHETE_JA_USADO"); resp.setMensagem("Bilhete já utilizado.");
            return resp;
        }
        if (transacao.getEstadoPagamento() != EstadoPagamento.PAID) {
            resp.setValido(false); resp.setMotivoInvalidade("BILHETE_NAO_PAGO"); resp.setMensagem("Bilhete não pago.");
            return resp;
        }
        if (transacao.getValidoAte() != null && LocalDateTime.now().isAfter(transacao.getValidoAte())) {
            resp.setValido(false); resp.setMotivoInvalidade("BILHETE_EXPIRADO");
            resp.setMensagem("Bilhete expirado em " + transacao.getValidoAte().toLocalDate() + ".");
            return resp;
        }

        resp.setValido(true);
        resp.setMensagem("Bilhete válido.");
        return resp;
    }
}
