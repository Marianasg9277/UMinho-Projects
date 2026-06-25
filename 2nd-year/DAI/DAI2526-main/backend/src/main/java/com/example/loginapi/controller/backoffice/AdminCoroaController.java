package com.example.loginapi.controller.backoffice;

import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.repository.infraestrutura.CoroaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/coroas")
@Tag(name = "Admin – Coroas", description = "Gestão de coroas/zonas tarifárias (ADMIN e GESTOR_SERVICOS)")
public class AdminCoroaController {

    @Autowired
    private CoroaRepository coroaRepo;

    @GetMapping
    @Operation(summary = "Listar coroas (admin) — suporta ?ativo=true/false")
    public ResponseEntity<List<Coroa>> listar(@RequestParam(required = false) Boolean ativo) {
        List<Coroa> lista = coroaRepo.findAll();
        if (ativo != null) {
            lista = lista.stream().filter(c -> c.isAtivo() == ativo).collect(Collectors.toList());
        }
        return ResponseEntity.ok(lista);
    }

    @PostMapping
    @Operation(summary = "Criar nova coroa")
    public ResponseEntity<?> criar(@RequestBody Map<String, Object> body) {
        try {
            String nome = body.getOrDefault("nome", "").toString().trim();
            if (nome.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "O nome é obrigatório."));
            }
            Coroa c = new Coroa();
            c.setNome(nome);
            if (body.containsKey("descricao") && body.get("descricao") != null) {
                c.setDescricao(body.get("descricao").toString().trim());
            }
            c.setAtivo(true);
            return ResponseEntity.status(201).body(coroaRepo.save(c));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    @Operation(summary = "Atualizar coroa")
    public ResponseEntity<?> atualizar(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Coroa c = coroaRepo.findById(id).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        String nome = body.getOrDefault("nome", "").toString().trim();
        if (nome.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "O nome é obrigatório."));
        }
        c.setNome(nome);
        if (body.containsKey("descricao") && body.get("descricao") != null) {
            c.setDescricao(body.get("descricao").toString().trim());
        }
        return ResponseEntity.ok(coroaRepo.save(c));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Desativar coroa (soft delete — ativo=false)")
    public ResponseEntity<?> desativar(@PathVariable Long id) {
        Coroa c = coroaRepo.findById(id).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        c.setAtivo(false);
        coroaRepo.save(c);
        return ResponseEntity.ok(Map.of("message", "Coroa desativada."));
    }

    @PatchMapping("/{id}/reativar")
    @Operation(summary = "Reativar coroa desativada")
    public ResponseEntity<?> reativar(@PathVariable Long id) {
        Coroa c = coroaRepo.findById(id).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        c.setAtivo(true);
        coroaRepo.save(c);
        return ResponseEntity.ok(Map.of("message", "Coroa reativada."));
    }
}
