package com.example.loginapi.controller.backoffice;

import com.example.loginapi.dto.AutocarroPosicaoRequest;
import com.example.loginapi.dto.AutocarroPosicaoResponse;
import com.example.loginapi.service.frota.SimulacaoAutocarroService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import com.example.loginapi.model.frota.Autocarro;


@RestController
@Tag(name = "Simulação – Autocarros", description = "Endpoints de simulação de posição de autocarros")
public class SimulacaoAutocarroController {

    @Autowired
    private SimulacaoAutocarroService service;

    @Value("${app.simulator.api-key}")
    private String apiKey;

    // ── POST /api/simulacao/autocarros/posicao ───────────────────────────────
    // Protegido por header X-SIMULATOR-API-KEY (não usa sessão/cookie).

    @PostMapping("/api/simulacao/autocarros/posicao")
    @Operation(summary = "Enviar posição de autocarro (usado por script de simulação)")
    public ResponseEntity<?> atualizarPosicao(
            @RequestHeader(value = "X-SIMULATOR-API-KEY", required = false) String chave,
            @RequestBody AutocarroPosicaoRequest request) {

        // ── Validar API Key ────────────────────────────────────────────────────
        if (chave == null || !chave.equals(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Chave de simulação inválida ou ausente",
                                 "error", "UNAUTHORIZED"));
        }

        try {
            AutocarroPosicaoResponse response = service.atualizarPosicao(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", ex.getMessage(), "error", "BAD_REQUEST"));
        }
    }

    // ── GET /api/autocarros/posicoes ─────────────────────────────────────────
    // Público — usado pelo frontend do mapa para consultar posições atuais.

    @GetMapping("/api/autocarros/posicoes")
    @Operation(summary = "Listar posições atuais de autocarros")
    public List<AutocarroPosicaoResponse> listarPosicoes(
            @RequestParam(value = "linhaId", required = false) Long linhaId) {
        return service.listarPosicoes(linhaId);
    }
}
