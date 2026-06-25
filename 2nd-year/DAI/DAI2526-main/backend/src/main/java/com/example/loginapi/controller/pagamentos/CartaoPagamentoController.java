package com.example.loginapi.controller.pagamentos;

import com.example.loginapi.dto.CartaoPagamentoRequest;
import com.example.loginapi.dto.CartaoPagamentoResponse;
import com.example.loginapi.model.pagamentos.CartaoPagamento;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.service.autenticacao.AuthService;
import com.example.loginapi.service.pagamentos.CartaoPagamentoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.pagamentos.Conta;


@RestController
@RequestMapping("/api/user/cartoes")
@Tag(name = "Cartões de Pagamento", description = "Gestão de cartões de pagamento associados à conta")
public class CartaoPagamentoController {

    @Autowired private CartaoPagamentoService cartaoService;
    @Autowired private AuthService authService;

    @GetMapping
    @Operation(summary = "Listar cartões ativos do utilizador autenticado")
    public ResponseEntity<List<CartaoPagamentoResponse>> listar(Authentication auth) {
        Cliente cliente = getCliente(auth);
        List<CartaoPagamentoResponse> lista = cartaoService.listarCartoes(cliente)
                .stream().map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    @Operation(summary = "Associar novo cartão de pagamento à conta")
    public ResponseEntity<?> associar(@RequestBody CartaoPagamentoRequest req, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            CartaoPagamento cartao = cartaoService.associarCartao(cliente, req);
            return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(cartao));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/predefinido")
    @Operation(summary = "Definir cartão como método de pagamento predefinido")
    public ResponseEntity<?> definirPredefinido(@PathVariable Long id, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            CartaoPagamento cartao = cartaoService.definirPredefinido(id, cliente);
            return ResponseEntity.ok(toResponse(cartao));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Remover (desativar) cartão de pagamento")
    public ResponseEntity<?> remover(@PathVariable Long id, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            cartaoService.removerCartao(id, cliente);
            return ResponseEntity.ok(Map.of("message", "Cartão removido com sucesso."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Cliente getCliente(Authentication auth) {
        Cliente cliente = authService.getClienteByEmail(auth.getName());
        if (cliente == null) throw new IllegalStateException("Perfil de cliente não encontrado.");
        return cliente;
    }

    private CartaoPagamentoResponse toResponse(CartaoPagamento c) {
        CartaoPagamentoResponse r = new CartaoPagamentoResponse();
        r.setId(c.getId());
        r.setNomeTitular(c.getNomeTitular());
        r.setUltimos4Digitos(c.getUltimos4Digitos());
        r.setBandeira(c.getBandeira());
        r.setMesValidade(c.getMesValidade());
        r.setAnoValidade(c.getAnoValidade());
        r.setPredefinido(c.isPredefinido());
        r.setCriadoEm(c.getCriadoEm().toString());
        return r;
    }
}
