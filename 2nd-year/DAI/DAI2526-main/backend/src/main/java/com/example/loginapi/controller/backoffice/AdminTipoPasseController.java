package com.example.loginapi.controller.backoffice;

import com.example.loginapi.model.titulos.TipoPasse;
import com.example.loginapi.repository.titulos.TipoPasseRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.loginapi.model.titulos.Passe;


@RestController
@RequestMapping("/api/admin/tipos-passe")
@Tag(name = "Admin – Tipos de Passe", description = "Gestão de tipos de passe (ADMIN e GESTOR_SERVICOS)")
public class AdminTipoPasseController {

    @Autowired
    private TipoPasseRepository tipoPasseRepo;

    @GetMapping
    @Operation(summary = "Listar tipos de passe (admin) — suporta ?ativo=true/false")
    public ResponseEntity<List<TipoPasse>> listar(@RequestParam(required = false) Boolean ativo) {
        List<TipoPasse> lista = tipoPasseRepo.findAll();
        if (ativo != null) {
            lista = lista.stream().filter(t -> t.isAtivo() == ativo).collect(Collectors.toList());
        }
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    @Operation(summary = "Criar novo tipo de passe")
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        try {
            String nome = body.getOrDefault("nome", "").toString().trim();
            if (nome.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "O nome é obrigatório."));
            }
            Object duracaoObj = body.get("duracaoDias");
            if (duracaoObj == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "A duração em dias é obrigatória."));
            }
            int duracaoDias = Integer.parseInt(duracaoObj.toString());
            if (duracaoDias < 1) {
                return ResponseEntity.badRequest().body(Map.of("message", "A duração deve ser superior a zero."));
            }
            TipoPasse t = new TipoPasse();
            t.setNome(nome);
            t.setDuracaoDias(duracaoDias);
            if (body.containsKey("descricao") && body.get("descricao") != null) {
                t.setDescricao(body.get("descricao").toString().trim());
            }
            t.setAtivo(true);
            return ResponseEntity.status(201).body(tipoPasseRepo.save(t));
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Duração inválida."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar tipo de passe")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        TipoPasse t = tipoPasseRepo.findById(id).orElse(null);
        if (t == null) return ResponseEntity.notFound().build();
        String nome = body.getOrDefault("nome", "").toString().trim();
        if (nome.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "O nome é obrigatório."));
        }
        t.setNome(nome);
        if (body.containsKey("duracaoDias") && body.get("duracaoDias") != null) {
            int duracaoDias = Integer.parseInt(body.get("duracaoDias").toString());
            if (duracaoDias < 1) {
                return ResponseEntity.badRequest().body(Map.of("message", "A duração deve ser superior a zero."));
            }
            t.setDuracaoDias(duracaoDias);
        }
        if (body.containsKey("descricao") && body.get("descricao") != null) {
            t.setDescricao(body.get("descricao").toString().trim());
        }
        return ResponseEntity.ok(tipoPasseRepo.save(t));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar tipo de passe (soft delete — ativo=false)")
    public ResponseEntity<?> desativar(@PathVariable Long id) {
        TipoPasse t = tipoPasseRepo.findById(id).orElse(null);
        if (t == null) return ResponseEntity.notFound().build();
        t.setAtivo(false);
        tipoPasseRepo.save(t);
        return ResponseEntity.ok(Map.of("message", "Tipo de passe desativado."));
    }

    @PatchMapping("/{id}/reativar")
    @Operation(summary = "Reativar tipo de passe desativado")
    public ResponseEntity<?> reativar(@PathVariable Long id) {
        TipoPasse t = tipoPasseRepo.findById(id).orElse(null);
        if (t == null) return ResponseEntity.notFound().build();
        t.setAtivo(true);
        tipoPasseRepo.save(t);
        return ResponseEntity.ok(Map.of("message", "Tipo de passe reativado."));
    }
}
