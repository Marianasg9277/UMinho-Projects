package com.example.loginapi.controller.backoffice;

import com.example.loginapi.dto.AutocarroCreateRequest;
import com.example.loginapi.dto.AutocarroEstadoRequest;
import com.example.loginapi.dto.AutocarroResponse;
import com.example.loginapi.model.frota.Autocarro;
import com.example.loginapi.repository.frota.AutocarroRepository;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.loginapi.model.infraestrutura.Linha;


/**
 * Admin-only endpoints for vehicle (autocarro) consultation.
 * All routes under /api/admin/** are protected by SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin")
@Tag(name = "Admin – Veículos", description = "Consulta de veículos/autocarros (backoffice)")
public class AdminAutocarroController {

    @Autowired private AutocarroRepository autocarroRepo;
    @Autowired private LinhaRepository linhaRepo;

    // ── Listar todos ─────────────────────────────────────────────────────────

    @GetMapping("/autocarros")
    @Operation(summary = "Listar todos os autocarros")
    @Transactional(readOnly = true)
    public List<AutocarroResponse> listar() {
        return autocarroRepo.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Detalhe por ID ───────────────────────────────────────────────────────

    @GetMapping("/autocarros/{id}")
    @Operation(summary = "Detalhe de um autocarro por ID")
    @Transactional(readOnly = true)
    public ResponseEntity<AutocarroResponse> detalhe(@PathVariable Long id) {
        return autocarroRepo.findById(id)
                .map(a -> ResponseEntity.ok(toResponse(a)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Criar ────────────────────────────────────────────────────────────────

    @PostMapping("/autocarros")
    @Operation(summary = "Adicionar um novo autocarro")
    @Transactional
    public ResponseEntity<?> criar(@Valid @RequestBody AutocarroCreateRequest req) {
        if (autocarroRepo.findByCodigo(req.getCodigo()).isPresent()) {
            return ResponseEntity.status(409)
                    .body(Map.of("message", "Já existe um autocarro com o código '" + req.getCodigo() + "'."));
        }
        var linha = linhaRepo.findById(req.getLinhaId());
        if (linha.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Linha não encontrada."));
        }
        Autocarro a = new Autocarro();
        a.setCodigo(req.getCodigo().trim());
        a.setNome(req.getNome() != null ? req.getNome().trim() : null);
        a.setAtivo(req.getAtivo() != null ? req.getAtivo() : true);
        a.setLinha(linha.get());
        return ResponseEntity.status(201).body(toResponse(autocarroRepo.save(a)));
    }

    // ── Atualizar estado ─────────────────────────────────────────────────────

    @PatchMapping("/autocarros/{id}/estado")
    @Operation(summary = "Atualizar estado ativo/inativo de um autocarro")
    @Transactional
    public ResponseEntity<?> atualizarEstado(@PathVariable Long id,
                                              @Valid @RequestBody AutocarroEstadoRequest req) {
        return autocarroRepo.findById(id)
                .map(a -> {
                    a.setAtivo(req.getAtivo());
                    return ResponseEntity.ok(toResponse(autocarroRepo.save(a)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Remover (lógico) ─────────────────────────────────────────────────────

    @DeleteMapping("/autocarros/{id}")
    @Operation(summary = "Remoção lógica de um autocarro (ativo=false)")
    @Transactional
    public ResponseEntity<?> remover(@PathVariable Long id) {
        return autocarroRepo.findById(id)
                .map(a -> {
                    a.setAtivo(false);
                    return ResponseEntity.ok(toResponse(autocarroRepo.save(a)));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    // ── Alocar/Desalocar Linha ────────────────────────────────────────────────

    @PatchMapping("/autocarros/{id}/linha")
    @Transactional
    @Operation(summary = "Alocar autocarro a linha (ou desalocar passando linhaId=null)")
    public ResponseEntity<?> alocarLinha(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Autocarro autocarro = autocarroRepo.findById(id).orElse(null);
        if (autocarro == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Autocarro não encontrado: id=" + id));
        }
        if (!Boolean.TRUE.equals(autocarro.getAtivo())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Autocarro inativo."));
        }

        Object linhaIdRaw = body.get("linhaId");
        if (linhaIdRaw == null) {
            autocarro.setLinha(null);
            autocarroRepo.save(autocarro);
            return ResponseEntity.ok(Map.of(
                    "autocarroId", autocarro.getId(),
                    "linhaId", "",
                    "linhaNome", ""
            ));
        }

        Long linhaId;
        try {
            linhaId = Long.parseLong(linhaIdRaw.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "linhaId inválido."));
        }

        Linha linha = linhaRepo.findById(linhaId).orElse(null);
        if (linha == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Linha não encontrada: id=" + linhaId));
        }

        autocarro.setLinha(linha);
        autocarroRepo.save(autocarro);

        return ResponseEntity.ok(Map.of(
                "autocarroId", autocarro.getId(),
                "linhaId", linha.getId(),
                "linhaNome", "Linha " + linha.getNumero() + " — " + linha.getNome()
        ));
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private AutocarroResponse toResponse(Autocarro a) {
        AutocarroResponse r = new AutocarroResponse();
        r.setId(a.getId());
        r.setCodigo(a.getCodigo());
        r.setNome(a.getNome());
        r.setAtivo(a.getAtivo());
        if (a.getLinha() != null) {
            r.setLinhaNumero(a.getLinha().getNumero());
            r.setLinhaNome(a.getLinha().getNome());
        }
        r.setCriadoEm(a.getCriadoEm());
        r.setAtualizadoEm(a.getAtualizadoEm());
        return r;
    }
}
