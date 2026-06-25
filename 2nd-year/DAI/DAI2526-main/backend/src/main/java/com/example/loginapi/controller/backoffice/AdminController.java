package com.example.loginapi.controller.backoffice;

import com.example.loginapi.model.backoffice.AuditLog;
import com.example.loginapi.model.comunicacao.Aviso;
import com.example.loginapi.model.clientes.enums.EstadoPedidoEstatuto;
import com.example.loginapi.repository.clientes.ClienteRepository;
import com.example.loginapi.repository.comunicacao.AvisoRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.repository.comunicacao.NotificacaoRepository;
import com.example.loginapi.repository.clientes.PedidoEstatutoRepository;
import com.example.loginapi.service.clientes.AuditLogService;
import com.example.loginapi.service.comunicacao.NotificacaoService;
import com.example.loginapi.model.comunicacao.Notificacao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Admin-only endpoints.
 * All routes under /api/admin/** are protected by SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Endpoints de gestão exclusivos para administradores")
public class AdminController {

    @Autowired private ClienteRepository clienteRepo;
    @Autowired private AvisoRepository avisoRepo;
    @Autowired private TransacaoRepository transacaoRepo;
    @Autowired private AuditLogService auditLogService;
    @Autowired private NotificacaoService notificacaoService;
    @Autowired private NotificacaoRepository notificacaoRepo;
    @Autowired private PedidoEstatutoRepository pedidoEstatutoRepo;

    // ── Stats ────────────────────────────────────────────────────────────────

    @GetMapping("/stats")
    @Operation(summary = "Estatísticas gerais do sistema")
    public Map<String, Long> getStats() {
        long pedidosPendentes = pedidoEstatutoRepo.countPedidosPendentes();
        // Map.of suporta no máximo 10 entradas
        return Map.of(
            "utilizadores", clienteRepo.count(),
            "avisos", avisoRepo.count(),
            "notificacoes", notificacaoRepo.count(),
            "transacoes", transacaoRepo.count(),
            "pedidosPendentes", pedidosPendentes
        );
    }

    // ── Audit Logs ───────────────────────────────────────────────────────────

    @GetMapping("/logs")
    @Operation(summary = "Lista logs de auditoria (mais recentes primeiro)")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista devolvida com sucesso"),
        @ApiResponse(responseCode = "403", description = "Acesso negado")
    })
    public List<AuditLog> getLogs(
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String acao,
            @RequestParam(required = false) Boolean sucesso) {

        if (username != null && !username.isBlank())
            return auditLogService.filtrarPorUtilizador(username);
        if (acao != null && !acao.isBlank())
            return auditLogService.filtrarPorAcao(acao);
        if (sucesso != null)
            return auditLogService.filtrarPorSucesso(sucesso);
        return auditLogService.listarTodos();
    }

    // ── Avisos CRUD ──────────────────────────────────────────────────────────

    @PostMapping("/avisos")
    @Operation(summary = "Criar novo aviso")
    public ResponseEntity<Aviso> criarAviso(@RequestBody Aviso aviso,
                                             Authentication auth,
                                             HttpServletRequest req) {
        if (aviso.getDataHora() == null) aviso.setDataHora(LocalDateTime.now());
        Aviso saved = avisoRepo.save(aviso);

        String user = auth != null ? auth.getName() : "anonymous";
        auditLogService.registar(user, "ADMIN", "AVISO_CRIADO",
            "avisos", "Aviso criado: " + saved.getTitulo(), true, req);
        notificacaoService.criarBroadcast(
            "Novo aviso: " + saved.getTitulo(),
            saved.getDescricao(), Notificacao.Tipo.AVISO);
        return ResponseEntity.ok(saved);
    }

    @PutMapping("/avisos/{id}")
    @Operation(summary = "Atualizar aviso existente")
    public ResponseEntity<?> atualizarAviso(@PathVariable Long id,
                                              @RequestBody Aviso aviso,
                                              Authentication auth,
                                              HttpServletRequest req) {
        Optional<Aviso> opt = avisoRepo.findById(id);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Aviso existing = opt.get();
        existing.setTitulo(aviso.getTitulo());
        existing.setDescricao(aviso.getDescricao());
        existing.setTipo(aviso.getTipo());
        existing.setNovo(aviso.isNovo());
        if (aviso.getDataHora() != null) existing.setDataHora(aviso.getDataHora());
        avisoRepo.save(existing);

        String user = auth != null ? auth.getName() : "anonymous";
        auditLogService.registar(user, "ADMIN", "AVISO_EDITADO",
            "avisos", "Aviso #" + id + " atualizado", true, req);
        return ResponseEntity.ok(existing);
    }

    @DeleteMapping("/avisos/{id}")
    @Operation(summary = "Apagar aviso")
    public ResponseEntity<?> apagarAviso(@PathVariable Long id,
                                          Authentication auth,
                                          HttpServletRequest req) {
        if (!avisoRepo.existsById(id)) return ResponseEntity.notFound().build();
        avisoRepo.deleteById(id);

        String user = auth != null ? auth.getName() : "anonymous";
        auditLogService.registar(user, "ADMIN", "AVISO_APAGADO",
            "avisos", "Aviso #" + id + " eliminado", true, req);
        return ResponseEntity.ok(Map.of("message", "Aviso eliminado"));
    }
}
