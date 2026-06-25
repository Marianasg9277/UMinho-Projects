package com.example.loginapi.controller.backoffice;

import com.example.loginapi.dto.HorarioRequest;
import com.example.loginapi.dto.HorarioResponse;
import com.example.loginapi.service.infraestrutura.HorarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import com.example.loginapi.model.infraestrutura.Horario;


/**
 * Admin-only endpoints for schedule (horario) management.
 * All routes under /api/admin/** are protected by SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/horarios")
@Tag(name = "Admin – Horários", description = "Gestão administrativa de horários (UC4.6.x)")
public class AdminHorarioController {

    @Autowired
    private HorarioService horarioService;

    // ── UC4.6.1 Criar ─────────────────────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Criar novo horário")
    public ResponseEntity<?> criar(@Valid @RequestBody HorarioRequest req) {
        try {
            HorarioResponse resp = horarioService.criar(req);
            return ResponseEntity.status(201).body(resp);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── UC4.6.2 Listar ────────────────────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listar horários (todos por omissão; filtrar com ?linhaId=)")
    public List<HorarioResponse> listar(@RequestParam(required = false) Long linhaId) {
        return horarioService.listar(linhaId);
    }

    // ── UC4.6.2 Consultar por ID ──────────────────────────────────────────────

    @GetMapping("/{id}")
    @Operation(summary = "Consultar horário por ID")
    public ResponseEntity<?> consultar(@PathVariable Long id) {
        return horarioService.consultar(id)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // ── UC4.6.3 Atualizar ─────────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar horário")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody HorarioRequest req) {
        try {
            return horarioService.atualizar(id, req)
                    .<ResponseEntity<?>>map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── UC4.6.4 Eliminar ──────────────────────────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar horário (eliminação física)")
    public ResponseEntity<?> eliminar(@PathVariable Long id) {
        if (!horarioService.eliminar(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(Map.of("message", "Horário eliminado com sucesso."));
    }
}
