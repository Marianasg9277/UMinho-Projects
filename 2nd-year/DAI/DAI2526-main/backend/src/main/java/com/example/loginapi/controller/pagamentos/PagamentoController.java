package com.example.loginapi.controller.pagamentos;

import com.example.loginapi.dto.BilheteResponse;
import com.example.loginapi.dto.CompraGuestRequestDTO;
import com.example.loginapi.dto.PagamentoItemResponse;
import com.example.loginapi.dto.PagamentoRequest;
import com.example.loginapi.dto.PagamentoResultadoResponse;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.service.autenticacao.AuthService;
import com.example.loginapi.service.titulos.CompraPasseService;
import com.example.loginapi.service.pagamentos.FaturaService;
import com.example.loginapi.service.pagamentos.PagamentoService;
import com.example.loginapi.service.titulos.PasseService;
import com.example.loginapi.service.titulos.QrCodeService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.infraestrutura.Linha;


@RestController
@RequestMapping("/api")
public class PagamentoController {

    @Autowired private AuthService authService;
    @Autowired private PasseService passeService;
    @Autowired private CompraPasseService compraPasseService;
    @Autowired private PagamentoService pagamentoService;
    @Autowired private FaturaService faturaService;
    @Autowired private QrCodeService qrCodeService;
    @Autowired private TransacaoRepository transacaoRepo;

    @GetMapping("/user/pagamentos")
    public ResponseEntity<List<PagamentoItemResponse>> listar(Authentication auth) {
        Cliente cliente = authService.getClienteByEmail(auth.getName());
        List<PagamentoItemResponse> itens = new ArrayList<>();

        for (Passe p : passeService.listarPasses(cliente)) {
            PagamentoItemResponse i = new PagamentoItemResponse();
            i.setTipoObjeto("PASSE");
            i.setObjetoId(p.getId());
            i.setTitulo(p.getTipoPasse().getNome() + " — " + p.getCoroa().getNome());
            i.setDetalhe(p.getEstadoComercial().name());
            i.setQuantia(p.getPrecoAplicado());
            i.setEstadoPagamento(p.getEstadoComercial().name());
            i.setFaturaNumero(passeService.obterFaturaNumero(p));
            itens.add(i);
        }

        for (Transacao t : transacaoRepo.findByClienteAndEstadoPagamentoInOrderByDataCompraDesc(
                cliente, List.of(EstadoPagamento.PAID, EstadoPagamento.USED))) {
            PagamentoItemResponse i = new PagamentoItemResponse();
            i.setTipoObjeto("BILHETE");
            i.setObjetoId(t.getId());
            i.setTitulo(t.getTipoBilhete().getNome());
            i.setDetalhe(t.getLinha() != null ? t.getLinha().getOrigem() + " → " + t.getLinha().getDestino() : "Sem linha");
            i.setQuantia(t.getPreco());
            i.setEstadoPagamento(t.getEstadoPagamento() != null ? t.getEstadoPagamento().name() : EstadoPagamento.NOT_STARTED.name());
            i.setFaturaNumero(t.getFaturaNumero());
            itens.add(i);
        }

        return ResponseEntity.ok(itens);
    }

    @PostMapping("/user/pagamentos/pagar")
    public ResponseEntity<?> pagar(@Valid @RequestBody PagamentoRequest req, Authentication auth) {
        try {
            Cliente cliente = authService.getClienteByEmail(auth.getName());
            String tipoObjeto = req.getTipoObjeto().trim().toUpperCase(Locale.ROOT);

            if ("PASSE".equals(tipoObjeto)) {
                Passe passe = passeService.obterPasse(req.getObjetoId())
                        .orElseThrow(() -> new IllegalArgumentException("Passe não encontrado."));
                if (!passe.getCliente().getId().equals(cliente.getId())) {
                    throw new IllegalArgumentException("Passe não encontrado.");
                }
                String metodo = req.getMetodo() != null ? req.getMetodo() : "CARTAO";
                Pagamento pagamento = compraPasseService.simularPagamento(passe.getId(), metodo);
                PagamentoResultadoResponse resp = new PagamentoResultadoResponse();
                resp.setSuccess(true);
                resp.setTipoObjeto("PASSE");
                resp.setObjetoId(passe.getId());
                resp.setEstadoPagamento(pagamento.getEstado().name());
                resp.setFaturaNumero(pagamento.getFaturaNumero());
                resp.setMensagem("Pagamento do passe concluído via " + metodo + ".");
                return ResponseEntity.ok(resp);
            }

            if ("BILHETE".equals(tipoObjeto)) {
                Transacao t = transacaoRepo.findById(req.getObjetoId())
                        .orElseThrow(() -> new IllegalArgumentException("Bilhete não encontrado."));
                if (t.getCliente() == null || !t.getCliente().getId().equals(cliente.getId())) {
                    throw new IllegalArgumentException("Bilhete não encontrado.");
                }
                String metodo = req.getMetodo() != null ? req.getMetodo() : "CARTAO";
                Transacao updated = pagamentoService.confirmarPagamentoTransacao(t, metodo);
                PagamentoResultadoResponse resp = new PagamentoResultadoResponse();
                resp.setSuccess(true);
                resp.setTipoObjeto("BILHETE");
                resp.setObjetoId(updated.getId());
                resp.setEstadoPagamento(updated.getEstadoPagamento().name());
                resp.setFaturaNumero(updated.getFaturaNumero());
                resp.setMensagem("Pagamento do bilhete concluído via " + metodo + ".");
                return ResponseEntity.ok(resp);
            }

            return ResponseEntity.badRequest().body(Map.of("message", "tipoObjeto inválido. Use PASSE ou BILHETE."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/pagamentos/guest/bilhete")
    public ResponseEntity<?> comprarBilheteGuest(@Valid @RequestBody CompraGuestRequestDTO dto) {
        try {
            Transacao transacao = pagamentoService.comprarBilheteGuest(dto);

            BilheteResponse resp = new BilheteResponse();
            resp.setId(transacao.getId());
            resp.setTipoBilheteNome(transacao.getTipoBilhete().getNome());
            resp.setLinhaNome(transacao.getLinha() != null
                    ? transacao.getLinha().getOrigem() + " → " + transacao.getLinha().getDestino()
                    : null);
            resp.setQuantia(transacao.getPreco());
            resp.setEstadoPagamento(transacao.getEstadoPagamento().name());
            resp.setCodigoQr(transacao.getCodigoQr());
            resp.setFaturaNumero(transacao.getFaturaNumero());
            resp.setDataCompra(transacao.getDataCompra().toString());
            resp.setValidoAte(transacao.getValidoAte() != null ? transacao.getValidoAte().toString() : null);

            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /**
     * Download do PDF do bilhete para utilizador guest.
     * Autenticação mínima: o NIF fornecido tem de corresponder ao da transação.
     */
    @GetMapping("/pagamentos/guest/bilhete/{id}/pdf")
    public ResponseEntity<byte[]> downloadBilheteGuestPdf(
            @PathVariable Long id,
            @RequestParam String nif) {
        try {
            Transacao t = transacaoRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Bilhete não encontrado."));

            if (t.getGuestNif() == null || !t.getGuestNif().equals(nif.trim())) {
                return ResponseEntity.status(403).build();
            }
            if (t.getEstadoPagamento() != EstadoPagamento.PAID
                    && t.getEstadoPagamento() != EstadoPagamento.USED) {
                return ResponseEntity.status(403).build();
            }

            byte[] qrPng = null;
            try { qrPng = qrCodeService.gerarPng(t.getCodigoQr(), 200, 200); } catch (Exception ignore) {}
            byte[] pdf = faturaService.gerarPdfBilhete(t, qrPng);
            if (pdf == null) return ResponseEntity.internalServerError().build();

            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition",
                            "attachment; filename=\"bilhete-" + t.getId() + ".pdf\"")
                    .body(pdf);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
