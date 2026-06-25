package com.example.loginapi.controller.backoffice;

import com.example.loginapi.dto.ColaboradorRequest;
import com.example.loginapi.dto.ColaboradorResponse;
import com.example.loginapi.model.frota.Autocarro;
import com.example.loginapi.model.colaboradores.Colaborador;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.colaboradores.enums.TipoColaborador;
import com.example.loginapi.repository.frota.AutocarroRepository;
import com.example.loginapi.repository.colaboradores.ColaboradorRepository;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import com.example.loginapi.service.colaboradores.ColaboradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Admin-only endpoints for collaborator management (UC4.3.x).
 * All routes under /api/admin/** are protected by SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/colaboradores")
@Tag(name = "Admin – Colaboradores", description = "Gestão administrativa de colaboradores (UC4.3.x)")
public class AdminColaboradorController {

    @Autowired
    private ColaboradorService colaboradorService;

    @Autowired
    private ColaboradorRepository colaboradorRepo;

    @Autowired
    private AutocarroRepository autocarroRepo;

    @Autowired
    private LinhaRepository linhaRepo;

    // ── UC4.3.1 Adicionar Colaborador ─────────────────────────────────────────

    @PostMapping
    @Operation(summary = "Criar novo colaborador")
    public ResponseEntity<?> criar(@Valid @RequestBody ColaboradorRequest dto) {
        try {
            ColaboradorResponse response = colaboradorService.criar(dto);
            return ResponseEntity.status(201).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body(Map.of("message", e.getMessage()));
        }
    }

    // ── UC4.3.2 Consultar Colaboradores ──────────────────────────────────────

    @GetMapping
    @Operation(summary = "Listar colaboradores (filtros opcionais: ?tipo= e ?ativo=)")
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Boolean ativo) {
        try {
            List<ColaboradorResponse> lista = colaboradorService.listar(tipo, ativo);
            return ResponseEntity.ok(lista);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consultar colaborador por ID")
    public ResponseEntity<?> consultar(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(colaboradorService.consultar(id));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    // ── UC4.3.5 Remover Colaborador (lógico) ─────────────────────────────────

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar colaborador (eliminação lógica)")
    public ResponseEntity<?> desativar(@PathVariable Long id) {
        try {
            colaboradorService.desativar(id);
            return ResponseEntity.ok(Map.of("message", "Colaborador desativado com sucesso."));
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    // ── Alocação: MOTORISTA → Autocarro ──────────────────────────────────────

    @PatchMapping("/{id}/autocarro")
    @Transactional
    @Operation(summary = "Alocar motorista a autocarro (ou remover alocação passando autocarroId=null)")
    public ResponseEntity<?> alocarAutocarro(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Colaborador colaborador = colaboradorRepo.findById(id).orElse(null);
        if (colaborador == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Colaborador não encontrado."));
        }
        if (colaborador.getTipoColaborador() != TipoColaborador.MOTORISTA) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    "Este endpoint é exclusivo para colaboradores do tipo MOTORISTA. Tipo atual: "
                            + colaborador.getTipoColaborador()));
        }

        Object autocarroIdRaw = body.get("autocarroId");
        if (autocarroIdRaw == null) {
            colaborador.setAutocarro(null);
            colaboradorRepo.save(colaborador);
            return ResponseEntity.ok(Map.of(
                    "colaboradorId", colaborador.getId(),
                    "autocarroCodigo", "",
                    "linhaInferida", ""
            ));
        }

        Long autocarroId;
        try {
            autocarroId = Long.parseLong(autocarroIdRaw.toString());
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "autocarroId inválido."));
        }

        Autocarro autocarro = autocarroRepo.findById(autocarroId).orElse(null);
        if (autocarro == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Autocarro não encontrado: id=" + autocarroId));
        }

        colaborador.setAutocarro(autocarro);
        colaboradorRepo.save(colaborador);

        Linha linha = autocarro.getLinha();
        String linhaDesc = linha != null
                ? "Linha " + linha.getNumero() + " — " + linha.getNome()
                : "";

        return ResponseEntity.ok(Map.of(
                "colaboradorId", colaborador.getId(),
                "autocarroCodigo", autocarro.getCodigo(),
                "linhaInferida", linhaDesc
        ));
    }

    // ── Alocação: FISCALIZADOR → Linha ────────────────────────────────────────

    @PatchMapping("/{id}/linha")
    @Transactional
    @Operation(summary = "Alocar fiscalizador a linha (ou remover alocação passando linhaId=null)")
    public ResponseEntity<?> alocarLinha(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body) {

        Colaborador colaborador = colaboradorRepo.findById(id).orElse(null);
        if (colaborador == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Colaborador não encontrado."));
        }
        if (colaborador.getTipoColaborador() != TipoColaborador.FISCALIZADOR) {
            return ResponseEntity.badRequest().body(Map.of("message",
                    "Este endpoint é exclusivo para colaboradores do tipo FISCALIZADOR. Tipo atual: "
                            + colaborador.getTipoColaborador()));
        }

        Object linhaIdRaw = body.get("linhaId");
        if (linhaIdRaw == null) {
            colaborador.setLinhaAtual(null);
            colaboradorRepo.save(colaborador);
            return ResponseEntity.ok(Map.of(
                    "colaboradorId", colaborador.getId(),
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

        colaborador.setLinhaAtual(linha);
        colaboradorRepo.save(colaborador);

        return ResponseEntity.ok(Map.of(
                "colaboradorId", colaborador.getId(),
                "linhaId", linha.getId(),
                "linhaNome", "Linha " + linha.getNumero() + " — " + linha.getNome()
        ));
    }
}
