package com.example.loginapi.controller.cliente;

import com.example.loginapi.dto.CarregamentoSaldoRequest;
import com.example.loginapi.dto.CarregamentoSaldoResponse;
import com.example.loginapi.dto.ContaResponse;
import com.example.loginapi.dto.MovimentoContaResponse;
import com.example.loginapi.model.pagamentos.Conta;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.service.autenticacao.AuthService;
import com.example.loginapi.service.clientes.ContaService;
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
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.pagamentos.MovimentoConta;


@RestController
@RequestMapping("/api/user/conta")
@Tag(name = "Conta", description = "Consulta de saldo e movimentos da conta do cliente")
public class ContaController {

    @Autowired private ContaService contaService;
    @Autowired private AuthService authService;

    @GetMapping
    @Operation(summary = "Obter saldo da conta (cria conta automaticamente se não existir)")
    public ResponseEntity<?> obterConta(Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            Conta conta = contaService.obterOuCriarConta(cliente);
            return ResponseEntity.ok(toContaResponse(conta));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/carregamentos")
    @Operation(summary = "Carregar saldo da conta via pagamento simulado")
    public ResponseEntity<?> carregarSaldo(@RequestBody CarregamentoSaldoRequest req, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            CarregamentoSaldoResponse resp = contaService.processarCarregamento(cliente, req);
            return ResponseEntity.ok(resp);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.PAYMENT_REQUIRED)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Erro interno. Tente novamente."));
        }
    }

    @GetMapping("/movimentos")
    @Operation(summary = "Listar movimentos da conta, ordenados do mais recente para o mais antigo")
    public ResponseEntity<?> listarMovimentos(Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            Conta conta = contaService.obterOuCriarConta(cliente);
            List<MovimentoContaResponse> movimentos = contaService.listarMovimentos(conta)
                    .stream().map(this::toMovimentoResponse).collect(Collectors.toList());
            return ResponseEntity.ok(movimentos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    private Cliente getCliente(Authentication auth) {
        Cliente cliente = authService.getClienteByEmail(auth.getName());
        if (cliente == null) throw new IllegalStateException("Perfil de cliente não encontrado.");
        return cliente;
    }

    private ContaResponse toContaResponse(Conta c) {
        ContaResponse r = new ContaResponse();
        r.setId(c.getId());
        r.setSaldo(c.getSaldo());
        r.setCriadoEm(c.getCriadoEm().toString());
        r.setAtualizadoEm(c.getAtualizadoEm().toString());
        return r;
    }

    private MovimentoContaResponse toMovimentoResponse(com.example.loginapi.model.pagamentos.MovimentoConta m) {
        MovimentoContaResponse r = new MovimentoContaResponse();
        r.setId(m.getId());
        r.setTipo(m.getTipo().name());
        r.setValor(m.getValor());
        r.setSaldoAntes(m.getSaldoAntes());
        r.setSaldoDepois(m.getSaldoDepois());
        r.setDescricao(m.getDescricao());
        r.setCriadoEm(m.getCriadoEm().toString());
        return r;
    }
}
