package com.example.loginapi.controller.backoffice;

import com.example.loginapi.dto.TipoBilheteRequest;
import com.example.loginapi.dto.TipoBilheteResponse;
import com.example.loginapi.model.titulos.TipoBilhete;
import com.example.loginapi.repository.titulos.TipoBilheteRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/precario")
@Tag(name = "Admin – Preçário", description = "Gestão de tipos de bilhete (ADMIN e GESTOR_SERVICOS)")
public class AdminPrecarioController {

    @Autowired
    private TipoBilheteRepository tipoBilheteRepo;

    @GetMapping("/tipos")
    @Operation(summary = "Listar tipos de bilhete (admin)")
    public ResponseEntity<List<TipoBilheteResponse>> listar(
            @RequestParam(required = false) Boolean ativo) {
        List<TipoBilhete> lista = (ativo != null)
                ? tipoBilheteRepo.findByAtivoOrderByCategoriaAscNomeAsc(ativo)
                : tipoBilheteRepo.findAllByOrderByCategoriaAscNomeAsc();
        return ResponseEntity.ok(lista.stream()
                .map(TipoBilheteResponse::from)
                .collect(Collectors.toList()));
    }

    @PostMapping("/tipos")
    @Operation(summary = "Criar tipo de bilhete")
    public ResponseEntity<?> criar(@Valid @RequestBody TipoBilheteRequest req) {
        try {
            TipoBilhete t = new TipoBilhete();
            t.setNome(req.getNome().trim());
            t.setCategoria(TipoBilhete.Categoria.valueOf(req.getCategoria().toUpperCase()));
            t.setPreco(req.getPreco());
            t.setValidadeHoras(req.getValidadeHoras() != null ? req.getValidadeHoras() : 2);
            t.setDescricao(req.getDescricao() != null ? req.getDescricao().trim() : null);
            t.setAtivo(true);
            return ResponseEntity.status(201).body(TipoBilheteResponse.from(tipoBilheteRepo.save(t)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Categoria inválida. Use AVULSO, MENSAL ou ZAPPING."));
        }
    }

    @PutMapping("/tipos/{id}")
    @Operation(summary = "Atualizar tipo de bilhete")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @Valid @RequestBody TipoBilheteRequest req) {
        TipoBilhete t = tipoBilheteRepo.findById(id).orElse(null);
        if (t == null) return ResponseEntity.notFound().build();
        try {
            t.setNome(req.getNome().trim());
            t.setCategoria(TipoBilhete.Categoria.valueOf(req.getCategoria().toUpperCase()));
            t.setPreco(req.getPreco());
            if (req.getValidadeHoras() != null) t.setValidadeHoras(req.getValidadeHoras());
            t.setDescricao(req.getDescricao() != null ? req.getDescricao().trim() : null);
            return ResponseEntity.ok(TipoBilheteResponse.from(tipoBilheteRepo.save(t)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Categoria inválida. Use AVULSO, MENSAL ou ZAPPING."));
        }
    }

    @DeleteMapping("/tipos/{id}")
    @Operation(summary = "Desativar tipo de bilhete (soft delete)")
    public ResponseEntity<?> desativar(@PathVariable Long id) {
        TipoBilhete t = tipoBilheteRepo.findById(id).orElse(null);
        if (t == null) return ResponseEntity.notFound().build();
        t.setAtivo(false);
        tipoBilheteRepo.save(t);
        return ResponseEntity.ok(Map.of("message", "Tipo de bilhete desativado."));
    }

    @PatchMapping("/tipos/{id}/reativar")
    @Operation(summary = "Reativar tipo de bilhete desativado")
    public ResponseEntity<?> reativar(@PathVariable Long id) {
        TipoBilhete t = tipoBilheteRepo.findById(id).orElse(null);
        if (t == null) return ResponseEntity.notFound().build();
        t.setAtivo(true);
        tipoBilheteRepo.save(t);
        return ResponseEntity.ok(Map.of("message", "Tipo de bilhete reativado."));
    }
}
