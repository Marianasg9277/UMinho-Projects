package com.example.loginapi.controller.backoffice;

import com.example.loginapi.dto.ParagemRequest;
import com.example.loginapi.dto.ParagemResponse;
import com.example.loginapi.service.infraestrutura.ParagemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.loginapi.model.infraestrutura.Paragem;


/**
 * Admin-only endpoints for stop (paragem) management.
 * All routes under /api/admin/** are protected by SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/paragens")
@Tag(name = "Admin – Paragens", description = "Gestão administrativa de paragens (UC4.5.x)")
public class AdminParagemController {

    @Autowired
    private ParagemService paragemService;

    // ── UC4.5.1 Criar ─────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Criar nova paragem")
    public ResponseEntity<?> criar(@Valid @RequestBody ParagemRequest req) {
        try {
            ParagemResponse resp = paragemService.criar(req);
            return ResponseEntity.status(201).body(resp);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── UC4.5.2 Listar ────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listar paragens (todas por omissão; filtrar com ?ativo=true ou ?ativo=false)")
    public List<ParagemResponse> listar(@RequestParam(required = false) Boolean ativo) {
        return paragemService.listar(ativo);
    }

    // ── UC4.5.2 Consultar por ID ──────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Consultar paragem por ID")
    public ResponseEntity<?> consultar(@PathVariable Long id) {
        return paragemService.consultar(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── UC4.5.3 Atualizar ─────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados de uma paragem (o campo ativo não é alterável aqui)")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody ParagemRequest req) {
        try {
            return paragemService.atualizar(id, req)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── UC4.5.4 Eliminar (lógico) ─────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar paragem (eliminação lógica: ativo=false)")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!paragemService.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Paragem desativada com sucesso."));
    }
}
