package com.example.loginapi.controller.backoffice;

import com.example.loginapi.model.infraestrutura.RegraPreco;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import com.example.loginapi.repository.infraestrutura.CoroaRepository;
import com.example.loginapi.repository.infraestrutura.RegraPrecoRepository;
import com.example.loginapi.repository.titulos.TipoPasseRepository;
import com.example.loginapi.service.infraestrutura.PricingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.titulos.Passe;


@RestController
@RequestMapping("/api/admin/pricing")
@Tag(name = "Admin – Pricing", description = "Gestão de regras de preço (admin only)")
public class AdminPricingController {

    @Autowired private PricingService pricingService;
    @Autowired private RegraPrecoRepository regraPrecoRepo;
    @Autowired private TipoPasseRepository tipoPasseRepo;
    @Autowired private CoroaRepository coroaRepo;

    @GetMapping("/regras")
    @Operation(summary = "Listar todas as regras de preço")
    public ResponseEntity<List<RegraPreco>> listarRegras() {
        return ResponseEntity.ok(pricingService.listarTodasRegras());
    }

    @PostMapping("/regras")
    @Operation(summary = "Criar nova regra de preço")
    public ResponseEntity<?> criarRegra(@RequestBody Map<String, Object> body) {
        try {
            RegraPreco regra = new RegraPreco();
            regra.setTipoEstatuto(TipoEstatuto.valueOf((String) body.get("tipoEstatuto")));
            regra.setTipoPasse(tipoPasseRepo.findById(Long.valueOf(body.get("tipoPasseId").toString()))
                    .orElseThrow(() -> new IllegalArgumentException("Tipo de passe não encontrado.")));
            regra.setCoroa(coroaRepo.findById(Long.valueOf(body.get("coroaId").toString()))
                    .orElseThrow(() -> new IllegalArgumentException("Coroa não encontrada.")));
            regra.setPreco(new BigDecimal(body.get("preco").toString()));
            regra.setDataInicioVigencia(LocalDate.parse(body.get("dataInicioVigencia").toString()));
            if (body.containsKey("dataFimVigencia") && body.get("dataFimVigencia") != null) {
                regra.setDataFimVigencia(LocalDate.parse(body.get("dataFimVigencia").toString()));
            }
            regra.setAtivo(true);
            return ResponseEntity.ok(pricingService.criarRegra(regra));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PatchMapping("/regras/{id}/preco")
    @Operation(summary = "Atualizar apenas o preço de uma regra existente")
    public ResponseEntity<?> atualizarPrecoRegra(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        try {
            RegraPreco regra = regraPrecoRepo.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Regra de preço não encontrada."));
            if (!body.containsKey("preco") || body.get("preco") == null)
                return ResponseEntity.badRequest().body(Map.of("message", "Campo 'preco' obrigatório."));
            BigDecimal novoPreco = new BigDecimal(body.get("preco").toString());
            if (novoPreco.compareTo(BigDecimal.ZERO) < 0)
                return ResponseEntity.badRequest().body(Map.of("message", "O preço não pode ser negativo."));
            regra.setPreco(novoPreco);
            return ResponseEntity.ok(regraPrecoRepo.save(regra));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Preço inválido."));
        }
    }

    @DeleteMapping("/regras/{id}")
    @Operation(summary = "Desativar regra de preço")
    public ResponseEntity<?> desativarRegra(@PathVariable Long id) {
        try {
            pricingService.desativarRegra(id);
            return ResponseEntity.ok(Map.of("message", "Regra desativada."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}
