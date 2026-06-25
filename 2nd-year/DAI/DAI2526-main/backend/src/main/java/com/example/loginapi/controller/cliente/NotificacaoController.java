package com.example.loginapi.controller.cliente;

import com.example.loginapi.model.comunicacao.Notificacao;
import com.example.loginapi.service.comunicacao.NotificacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.clientes.Cliente;


/**
 * Endpoints for the authenticated user's notifications.
 * Accessible to all authenticated users under /api/user/notificacoes.
 */
@RestController
@RequestMapping("/api/user/notificacoes")
@Tag(name = "Notificações", description = "Gestão de notificações do utilizador autenticado")
public class NotificacaoController {

    @Autowired
    private NotificacaoService notificacaoService;

    @GetMapping
    @Operation(summary = "Listar notificações do utilizador autenticado",
               description = "Inclui notificações pessoais e broadcast. Ordenadas por data decrescente.")
    @ApiResponse(responseCode = "200", description = "Lista de notificações")
    public ResponseEntity<List<Notificacao>> listar(Authentication auth) {
        List<Notificacao> lista = notificacaoService.listarParaUtilizador(auth.getName());
        return ResponseEntity.ok(lista);
    }

    @GetMapping("/count")
    @Operation(summary = "Número de notificações não lidas")
    public ResponseEntity<Map<String, Long>> count(Authentication auth) {
        long count = notificacaoService.contarNaoLidasParaUtilizador(auth.getName());
        return ResponseEntity.ok(Map.of("naoLidas", count));
    }

    @PatchMapping("/{id}/lida")
    @Operation(summary = "Marcar notificação como lida")
    public ResponseEntity<?> marcarLida(@PathVariable Long id, Authentication auth) {
        boolean ok = notificacaoService.marcarComoLida(id, auth.getName());
        if (!ok) return ResponseEntity.status(403).body(Map.of("message", "Não autorizado ou não encontrado"));
        return ResponseEntity.ok(Map.of("message", "Notificação marcada como lida"));
    }

    @PatchMapping("/todas-lidas")
    @Operation(summary = "Marcar todas as notificações do utilizador como lidas")
    public ResponseEntity<?> marcarTodasLidas(Authentication auth) {
        notificacaoService.marcarTodasComoLidas(auth.getName());
        return ResponseEntity.ok(Map.of("message", "Todas as notificações marcadas como lidas"));
    }
}
