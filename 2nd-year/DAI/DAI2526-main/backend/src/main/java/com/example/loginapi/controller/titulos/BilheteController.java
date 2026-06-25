package com.example.loginapi.controller.titulos;

import com.example.loginapi.dto.BilheteResponse;
import com.example.loginapi.dto.ComprarBilheteAutenticadoRequest;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.repository.infraestrutura.CoroaRepository;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import com.example.loginapi.repository.titulos.TipoBilheteRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.service.autenticacao.AuthService;
import com.example.loginapi.service.pagamentos.PagamentoService;
import com.example.loginapi.service.pagamentos.SaldoCompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.titulos.TipoBilhete;


/**
 * Controller para gestão de bilhetes de utilizadores autenticados.
 *
 * Endpoints públicos (guest) continuam em PagamentoController.
 * Este controller exige autenticação.
 */
@RestController
@RequestMapping("/api/user/bilhetes")
@Tag(name = "Bilhetes", description = "Compra e consulta de bilhetes avulso por utilizador autenticado")
public class BilheteController {

    @Autowired private AuthService authService;
    @Autowired private PagamentoService pagamentoService;
    @Autowired private SaldoCompraService saldoCompraService;
    @Autowired private TransacaoRepository transacaoRepo;
    @Autowired private TipoBilheteRepository tipoBilheteRepo;
    @Autowired private LinhaRepository linhaRepo;
    @Autowired private CoroaRepository coroaRepo;

    // ─────────────────────────────────────────────────────────────────────────
    // Listar bilhetes do utilizador
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listar todos os bilhetes do utilizador autenticado")
    public ResponseEntity<List<BilheteResponse>> listar(Authentication auth) {
        Cliente cliente = getCliente(auth);
        List<Transacao> transacoes = transacaoRepo.findByClienteOrderByDataCompraDesc(cliente);
        return ResponseEntity.ok(transacoes.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Comprar bilhete (utilizador autenticado)
    // ─────────────────────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Comprar bilhete avulso (utilizador autenticado)")
    public ResponseEntity<?> comprar(@Valid @RequestBody ComprarBilheteAutenticadoRequest req,
                                      Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);

            var tipoBilhete = tipoBilheteRepo.findById(req.getTipoBilheteId())
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de bilhete não encontrado."));

            com.example.loginapi.model.infraestrutura.Linha linha = null;
            if (req.getLinhaId() != null) {
                linha = linhaRepo.findById(req.getLinhaId())
                        .orElseThrow(() -> new IllegalArgumentException("Linha não encontrada."));
            }

            Transacao t = new Transacao();
            t.setCliente(cliente);
            t.setTipoBilhete(tipoBilhete);
            t.setLinha(linha);
            // Prioridade: coroaId explícito do request; fallback: coroa do tipo de bilhete
            if (req.getCoroaId() != null) {
                t.setCoroaId(req.getCoroaId());
            } else if (tipoBilhete.getCoroa() != null) {
                t.setCoroaId(tipoBilhete.getCoroa().getId());
            }
            t.setPreco(tipoBilhete.getPreco());
            t.setDataCompra(LocalDateTime.now());
            t.setEstadoPagamento(EstadoPagamento.NOT_STARTED);
            t.setCodigoQr("BILHETE-PENDENTE-" + UUID.randomUUID().toString().replace("-", "").toUpperCase());

            Transacao saved = transacaoRepo.save(t);

            String metodo = req.getMetodo() != null ? req.getMetodo() : "CARTAO";
            Transacao pago;
            if ("SALDO_CONTA".equals(metodo)) {
                pago = saldoCompraService.pagarBilheteComSaldo(cliente, saved);
            } else {
                pago = pagamentoService.confirmarPagamentoTransacao(saved, metodo);
            }

            return ResponseEntity.ok(toResponse(pago));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Verificar validade de um bilhete
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/{id}/validade")
    @Operation(summary = "Verificar se um bilhete está dentro do prazo de validade")
    public ResponseEntity<?> verificarValidade(@PathVariable Long id, Authentication auth) {
        Cliente cliente = getCliente(auth);
        return transacaoRepo.findById(id)
                .filter(t -> t.getCliente() != null && t.getCliente().getId().equals(cliente.getId()))
                .map(t -> {
                    boolean dentroDosPrazo = t.getValidoAte() == null
                            || !LocalDateTime.now().isAfter(t.getValidoAte());
                    boolean valido = t.getEstadoPagamento() == EstadoPagamento.PAID && dentroDosPrazo;
                    return ResponseEntity.ok(Map.of(
                            "bilheteId", t.getId(),
                            "codigoQr", t.getCodigoQr(),
                            "valido", valido,
                            "validoAte", t.getValidoAte() != null ? t.getValidoAte().toString() : null,
                            "estadoPagamento", t.getEstadoPagamento().name()
                    ));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private Cliente getCliente(Authentication auth) {
        Cliente c = authService.getClienteByEmail(auth.getName());
        if (c == null) throw new IllegalStateException("Perfil de cliente não encontrado.");
        return c;
    }

    private BilheteResponse toResponse(Transacao t) {
        BilheteResponse r = new BilheteResponse();
        r.setId(t.getId());
        r.setTipoBilheteNome(t.getTipoBilhete().getNome());
        r.setLinhaNome(t.getLinha() != null
                ? t.getLinha().getOrigem() + " → " + t.getLinha().getDestino()
                : null);
        r.setQuantia(t.getPreco());
        r.setEstadoPagamento(t.getEstadoPagamento() != null ? t.getEstadoPagamento().name() : null);
        r.setCodigoQr(t.getCodigoQr());
        r.setFaturaNumero(t.getFaturaNumero());
        r.setDataCompra(t.getDataCompra() != null ? t.getDataCompra().toString() : null);
        r.setValidoAte(t.getValidoAte() != null ? t.getValidoAte().toString() : null);
        // Coroa pelo id (lazy resolve)
        if (t.getCoroaId() != null) {
            coroaRepo.findById(t.getCoroaId())
                    .ifPresent(c -> r.setCoroaNome(c.getNome()));
        }
        return r;
    }
}
