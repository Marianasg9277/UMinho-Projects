package com.example.loginapi.controller.backoffice;

import com.example.loginapi.dto.CriarLinhaRequest;
import com.example.loginapi.dto.LinhaParagemRequest;
import com.example.loginapi.dto.LinhaParagemResponse;
import com.example.loginapi.dto.RotaLinhaImportRequest;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import com.example.loginapi.service.infraestrutura.LinhaParagemService;
import com.example.loginapi.service.frota.RotaLinhaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import com.example.loginapi.model.infraestrutura.Paragem;


/**
 * Admin-only endpoints for line (linha) management.
 * All routes under /api/admin/** are protected by SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/linhas")
@Tag(name = "Admin – Linhas", description = "Gestão administrativa de linhas (UC4.4.x)")
public class AdminLinhaController {

    @Autowired
    private LinhaParagemService linhaParagemService;

    @Autowired
    private LinhaRepository linhaRepository;

    @Autowired
    private RotaLinhaService rotaLinhaService;

    // ── UC4.4.x Listar Linhas (admin) ─────────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listar linhas (admin) — suporta ?ativo=true/false")
    public ResponseEntity<List<Linha>> listarLinhas(
            @RequestParam(required = false) Boolean ativo) {
        List<Linha> lista = (ativo != null)
                ? linhaRepository.findByAtivoOrderByNumeroAsc(ativo)
                : linhaRepository.findAllByOrderByNumeroAsc();
        return ResponseEntity.ok(lista);
    }

    // Criar Linha

    @PostMapping
    @Operation(summary = "Criar nova linha")
    public ResponseEntity<?> criarLinha(@RequestBody CriarLinhaRequest dto) {
        try {
            if (linhaRepository.findByNumero(dto.getNumero()).isPresent()) {
                return ResponseEntity.status(409).body(Map.of("message",
                        "Já existe uma linha com o número " + dto.getNumero() + "."));
            }

            Linha linha = new Linha();
            linha.setNumero(dto.getNumero());
            linha.setNome(dto.getNome());
            linha.setOrigem(dto.getOrigem());
            linha.setDestino(dto.getDestino());
            linha.setNumParagens(dto.getNumParagens());
            linha.setDuracaoMin(dto.getDuracaoMin());
            if (dto.getGtfsRouteId() != null && !dto.getGtfsRouteId().isBlank()) {
                linha.setGtfsRouteId(dto.getGtfsRouteId());
            }
            if (dto.getCor() != null && !dto.getCor().isBlank()) {
                linha.setCor(dto.getCor());
            }

            Linha saved = linhaRepository.save(linha);
            return ResponseEntity.status(201).body(Map.of(
                    "message", "Linha criada com sucesso.",
                    "id", saved.getId(),
                    "numero", saved.getNumero()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── UC4.4.2 Alterar Linha ─────────────────────────────────────────────────

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar dados de uma linha existente")
    public ResponseEntity<?> atualizarLinha(@PathVariable Long id, @RequestBody CriarLinhaRequest dto) {
        Linha linha = linhaRepository.findById(id).orElse(null);
        if (linha == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            if (dto.getNumero() == null || dto.getNumero().isBlank()) {
                return ResponseEntity.badRequest().body(Map.of("message", "O número da linha é obrigatório."));
            }
            if (linhaRepository.existsByNumeroAndIdNot(dto.getNumero().trim(), id)) {
                return ResponseEntity.status(409).body(Map.of("message",
                        "Já existe outra linha com o número " + dto.getNumero().trim() + "."));
            }
            linha.setNumero(dto.getNumero().trim());
            if (dto.getNome() != null && !dto.getNome().isBlank()) linha.setNome(dto.getNome().trim());
            if (dto.getOrigem() != null && !dto.getOrigem().isBlank()) linha.setOrigem(dto.getOrigem().trim());
            if (dto.getDestino() != null && !dto.getDestino().isBlank()) linha.setDestino(dto.getDestino().trim());
            if (dto.getNumParagens() > 0) linha.setNumParagens(dto.getNumParagens());
            if (dto.getDuracaoMin() > 0) linha.setDuracaoMin(dto.getDuracaoMin());
            if (dto.getCor() != null && !dto.getCor().isBlank()) linha.setCor(dto.getCor().trim());
            Linha saved = linhaRepository.save(linha);
            return ResponseEntity.ok(Map.of(
                    "message", "Linha atualizada com sucesso.",
                    "id", saved.getId(),
                    "numero", saved.getNumero()
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── UC4.4.3 Eliminar Linha (soft delete) ─────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar linha (soft delete — ativo=false)")
    public ResponseEntity<?> desativarLinha(@PathVariable Long id) {
        Linha linha = linhaRepository.findById(id).orElse(null);
        if (linha == null) return ResponseEntity.notFound().build();
        linha.setAtivo(false);
        linhaRepository.save(linha);
        return ResponseEntity.ok(Map.of("message", "Linha desativada."));
    }

    @PatchMapping("/{id}/reativar")
    @Operation(summary = "Reativar linha desativada")
    public ResponseEntity<?> reativarLinha(@PathVariable Long id) {
        Linha linha = linhaRepository.findById(id).orElse(null);
        if (linha == null) return ResponseEntity.notFound().build();
        linha.setAtivo(true);
        linhaRepository.save(linha);
        return ResponseEntity.ok(Map.of("message", "Linha reativada."));
    }

    // Importar Rota KML para Linha existente

    @PostMapping("/{linhaId}/rota")
    @Operation(summary = "Importar rota (pontos KML) para uma linha")
    public ResponseEntity<?> importarRota(
            @PathVariable Long linhaId,
            @RequestBody RotaLinhaImportRequest request) {
        try {
            request.setLinhaId(linhaId);
            int total = rotaLinhaService.importarRota(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Rota importada com sucesso.",
                    "pontosImportados", total,
                    "sentido", request.getSentido()
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── UC4.4.1 Associar Paragem a Linha ─────────────────────────────────────

    @PostMapping("/{linhaId}/paragens")
    @Operation(summary = "Associar uma paragem existente a uma linha")
    public ResponseEntity<?> associarParagem(
            @PathVariable Long linhaId,
            @Valid @RequestBody LinhaParagemRequest dto) {
        try {
            LinhaParagemResponse response = linhaParagemService.associarParagem(linhaId, dto);
            return ResponseEntity.status(201).body(response);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        }
    }

    // ── UC4.4.5 Desassociar Paragem de Linha ──────────────────────────────────

    @DeleteMapping("/{linhaId}/paragens/{paragemId}")
    @Operation(summary = "Remover associação entre uma linha e uma paragem (todos os sentidos)")
    public ResponseEntity<?> desassociarParagem(
            @PathVariable Long linhaId,
            @PathVariable Long paragemId) {
        try {
            int removidos = linhaParagemService.desassociarParagem(linhaId, paragemId);
            return ResponseEntity.ok(Map.of(
                    "message", "Associação removida com sucesso.",
                    "registosRemovidos", removidos
            ));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }
}
