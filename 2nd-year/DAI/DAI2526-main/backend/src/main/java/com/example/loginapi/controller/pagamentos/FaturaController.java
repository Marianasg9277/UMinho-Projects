package com.example.loginapi.controller.pagamentos;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.repository.pagamentos.PagamentoRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.service.autenticacao.AuthService;
import com.example.loginapi.service.pagamentos.FaturaService;
import com.example.loginapi.service.titulos.PasseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import com.example.loginapi.dto.PagamentoItemResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import com.example.loginapi.model.infraestrutura.Linha;


@RestController
@RequestMapping("/api/user/faturas")
@Tag(name = "Faturas", description = "Listagem e download de faturas")
public class FaturaController {

    @Autowired private AuthService authService;
    @Autowired private PasseService passeService;
    @Autowired private FaturaService faturaService;
    @Autowired private TransacaoRepository transacaoRepo;

    @GetMapping
    @Operation(summary = "Listar faturas emitidas (passes e bilhetes pagos)")
    public ResponseEntity<List<PagamentoItemResponse>> listar(Authentication auth) {
        Cliente cliente = authService.getClienteByEmail(auth.getName());
        List<PagamentoItemResponse> faturas = new ArrayList<>();

        // ── Passes pagos com fatura ──────────────────────────────────────────
        for (Passe p : passeService.listarPasses(cliente)) {
            String faturaNumero = passeService.obterFaturaNumero(p);
            if (faturaNumero == null || faturaNumero.isBlank()) continue;

            PagamentoItemResponse item = new PagamentoItemResponse();
            item.setTipoObjeto("PASSE");
            item.setObjetoId(p.getId());
            item.setTitulo(p.getTipoPasse().getNome() + " — " + p.getCoroa().getNome());
            item.setDetalhe(p.getDataInicio() != null
                    ? "De " + p.getDataInicio() + " a " + p.getDataFim()
                    : "Datas não definidas");
            item.setQuantia(p.getPrecoAplicado());
            item.setEstadoPagamento(p.getEstadoComercial().name());
            item.setFaturaNumero(faturaNumero);
            faturas.add(item);
        }

        // ── Bilhetes pagos ou já usados com fatura ───────────────────────────
        for (Transacao t : transacaoRepo.findByClienteAndEstadoPagamentoInOrderByDataCompraDesc(
                cliente, java.util.Arrays.asList(EstadoPagamento.PAID, EstadoPagamento.USED))) {
            if (t.getFaturaNumero() == null || t.getFaturaNumero().isBlank()) continue;

            PagamentoItemResponse item = new PagamentoItemResponse();
            item.setTipoObjeto("BILHETE");
            item.setObjetoId(t.getId());
            item.setTitulo(t.getTipoBilhete().getNome());
            item.setDetalhe(t.getLinha() != null
                    ? t.getLinha().getOrigem() + " → " + t.getLinha().getDestino()
                    : "Sem linha");
            item.setQuantia(t.getPreco());
            item.setEstadoPagamento(EstadoPagamento.PAID.name());
            item.setFaturaNumero(t.getFaturaNumero());
            faturas.add(item);
        }

        return ResponseEntity.ok(faturas);
    }

    /**
     * Download de fatura em PDF (passe).
     * Exemplo: GET /api/user/faturas/passe/42/download
     */
    @GetMapping("/passe/{passeId}/download")
    @Operation(summary = "Download PDF de fatura de passe")
    public ResponseEntity<?> downloadFaturaPasse(@PathVariable Long passeId, Authentication auth) {
        Cliente cliente = authService.getClienteByEmail(auth.getName());
        Optional<Passe> opt = passeService.obterPasse(passeId)
                .filter(p -> p.getCliente().getId().equals(cliente.getId()));

        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Passe passe = opt.get();

        String faturaNumero = passeService.obterFaturaNumero(passe);
        if (faturaNumero == null) {
            return ResponseEntity.badRequest().body("Fatura não disponível para este passe.");
        }

        byte[] pdf = faturaService.gerarPdfFaturaPasse(passe, obterPagamentoPasse(passe));
        if (pdf == null) {
            // Fallback para HTML
            String html = faturaService.gerarHtmlFaturaPasse(passe, obterPagamentoPasse(passe));
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html.getBytes());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"fatura-" + faturaNumero + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Download de fatura em PDF (bilhete autenticado).
     * Exemplo: GET /api/user/faturas/bilhete/10/download
     */
    @GetMapping("/bilhete/{transacaoId}/download")
    @Operation(summary = "Download PDF de fatura de bilhete")
    public ResponseEntity<?> downloadFaturaBilhete(@PathVariable Long transacaoId, Authentication auth) {
        Cliente cliente = authService.getClienteByEmail(auth.getName());
        Optional<Transacao> opt = transacaoRepo.findById(transacaoId)
                .filter(t -> t.getCliente() != null && t.getCliente().getId().equals(cliente.getId()));

        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Transacao transacao = opt.get();

        if (transacao.getFaturaNumero() == null) {
            return ResponseEntity.badRequest().body("Fatura não disponível para este bilhete.");
        }

        byte[] pdf = faturaService.gerarPdfFaturaBilhete(transacao);
        if (pdf == null) {
            String html = faturaService.gerarHtmlFaturaBilhete(transacao);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html.getBytes());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"fatura-" + transacao.getFaturaNumero() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    /**
     * Consulta de fatura de bilhete guest pelo código QR.
     * Não requer autenticação (rota pública via SecurityConfig).
     */
    @GetMapping("/guest/{codigoQr}")
    @Operation(summary = "Obter fatura de bilhete guest pelo código QR (público)")
    public ResponseEntity<?> faturaGuest(@PathVariable String codigoQr) {
        Optional<Transacao> opt = transacaoRepo.findByCodigoQr(codigoQr);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Transacao t = opt.get();
        if (t.getGuestEmail() == null) {
            return ResponseEntity.badRequest().body("Este código não corresponde a um bilhete guest.");
        }
        String html = faturaService.gerarHtmlFaturaBilhete(t);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html.getBytes());
    }

    /**
     * Download de fatura guest em PDF pelo código QR.
     * Rota pública via SecurityConfig.
     */
    @GetMapping("/guest/{codigoQr}/download")
    @Operation(summary = "Download PDF de fatura guest pelo código QR (público)")
    public ResponseEntity<?> downloadFaturaGuest(@PathVariable String codigoQr) {
        Optional<Transacao> opt = transacaoRepo.findByCodigoQr(codigoQr);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();
        Transacao t = opt.get();

        if (t.getFaturaNumero() == null) {
            return ResponseEntity.badRequest().body("Fatura não disponível para este bilhete.");
        }

        byte[] pdf = faturaService.gerarPdfFaturaBilhete(t);
        if (pdf == null) {
            String html = faturaService.gerarHtmlFaturaBilhete(t);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html.getBytes());
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"fatura-" + t.getFaturaNumero() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    // Helper: obtém o Pagamento associado a um passe (necessário para FaturaService)
    @Autowired
    private PagamentoRepository pagamentoRepository;

    private Pagamento obterPagamentoPasse(Passe passe) {
        return pagamentoRepository.findFirstByPasseOrderByCriadoEmDesc(passe)
                .orElseThrow(() -> new IllegalStateException("Pagamento não encontrado para passe id=" + passe.getId()));
    }
}
