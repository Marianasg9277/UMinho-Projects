package com.example.loginapi.controller.publicas;

import com.example.loginapi.dto.RotaLinhaImportRequest;
import com.example.loginapi.dto.RotaLinhaPontoResponse;
import com.example.loginapi.service.frota.RotaLinhaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.loginapi.model.infraestrutura.Linha;


@RestController
@RequestMapping("/api/mapa")
@Tag(name = "Mapa – Rotas", description = "Endpoints de importação e consulta de rotas de linhas")
public class MapaController {

    @Autowired
    private RotaLinhaService rotaLinhaService;

    @Value("${app.simulator.api-key}")
    private String apiKey;

    // ── POST /api/mapa/rotas/importar ───────────────────────────────────────────
    // Protegido por header X-SIMULATOR-API-KEY.

    @PostMapping("/rotas/importar")
    @Operation(summary = "Importar pontos de rota a partir de KML processado")
    public ResponseEntity<?> importarRota(
            @RequestHeader(value = "X-SIMULATOR-API-KEY", required = false) String chave,
            @RequestBody RotaLinhaImportRequest request) {

        // ── Validar API Key ────────────────────────────────────────────────────
        if (chave == null || !chave.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Chave de simulação inválida ou ausente",
                                 "error", "UNAUTHORIZED"));
        }

        try {
            int total = rotaLinhaService.importarRota(request);
            return ResponseEntity.ok(Map.of(
                    "message", "Rota importada com sucesso",
                    "pontosImportados", total,
                    "linhaId", request.getLinhaId(),
                    "sentido", request.getSentido()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", ex.getMessage(), "error", "BAD_REQUEST"));
        }
    }

    // ── GET /api/mapa/rotas/{linhaId}/{sentido} ─────────────────────────────────
    // Público — usado pelo frontend do mapa para consultar a rota.

    @GetMapping("/rotas/{linhaId}/{sentido}")
    @Operation(summary = "Obter pontos da rota de uma linha num dado sentido")
    public List<RotaLinhaPontoResponse> obterRota(
            @PathVariable Long linhaId,
            @PathVariable String sentido) {
        return rotaLinhaService.obterRota(linhaId, sentido);
    }
}
