package com.example.loginapi.controller.backoffice;

import com.example.loginapi.model.frota.Autocarro;
import com.example.loginapi.model.frota.AutocarroEstado;
import com.example.loginapi.repository.frota.AutocarroEstadoRepository;
import com.example.loginapi.repository.frota.AutocarroRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin/autocarros")
@Tag(name = "Admin – Estado Autocarro", description = "Gestão de estados operacionais de autocarros (manutenção)")
public class AutocarroEstadoAdminController {

    private static final Set<String> ESTADOS_VALIDOS =
            Set.of("ARMAZENADO", "EM_TRANSITO", "EM_SERVICO", "AVARIADO", "MANUTENCAO");

    @Autowired private AutocarroRepository autocarroRepo;
    @Autowired private AutocarroEstadoRepository estadoRepo;

    /**
     * Transições disponíveis para o admin:
     *   Qualquer estado normal → AVARIADO  : body {"acao": "DECLARAR_AVARIA"}
     *   AVARIADO   → MANUTENCAO            : body {"acao": "INICIAR_MANUTENCAO"}
     *   MANUTENCAO → ARMAZENADO            : body {"acao": "REPARADO"}
     *   MANUTENCAO → AVARIADO              : body {"acao": "MANUTENCAO_CHEIA"}
     */
    @PatchMapping("/{codigoAutocarro}/operacional")
    @Operation(summary = "Gerir transições de manutenção do autocarro (admin)")
    public ResponseEntity<?> gerir(
            @PathVariable String codigoAutocarro,
            @RequestBody Map<String, String> body) {

        String acao = body.get("acao");
        if (acao == null || acao.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Campo 'acao' obrigatório. Valores aceites: INICIAR_MANUTENCAO, REPARADO, MANUTENCAO_CHEIA"));
        }

        Autocarro autocarro = autocarroRepo.findByCodigo(codigoAutocarro).orElse(null);
        if (autocarro == null) {
            return ResponseEntity.notFound().build();
        }

        AutocarroEstado estado = estadoRepo.findByAutocarroId(autocarro.getId())
                .orElseGet(() -> {
                    AutocarroEstado novo = new AutocarroEstado();
                    novo.setAutocarro(autocarro);
                    return novo;
                });

        switch (acao.toUpperCase()) {
            case "DECLARAR_AVARIA":
                if ("MANUTENCAO".equals(estado.getEstado())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("erro", "Não é possível declarar avaria quando em MANUTENCAO. Use MANUTENCAO_CHEIA."));
                }
                estado.setEstado("AVARIADO");
                estado.setSubEstado(null);
                estado.setInicioServico(null);
                estado.setSentidoAtual(null);
                break;
            case "INICIAR_MANUTENCAO":
                if (!"AVARIADO".equals(estado.getEstado())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("erro", "Só pode iniciar manutenção a partir do estado AVARIADO. Estado atual: " + estado.getEstado()));
                }
                estado.setEstado("MANUTENCAO");
                estado.setSubEstado(null);
                break;
            case "REPARADO":
                if (!"MANUTENCAO".equals(estado.getEstado())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("erro", "Só pode marcar como reparado a partir do estado MANUTENCAO. Estado atual: " + estado.getEstado()));
                }
                estado.setEstado("ARMAZENADO");
                estado.setSubEstado(null);
                break;
            case "MANUTENCAO_CHEIA":
                if (!"MANUTENCAO".equals(estado.getEstado())) {
                    return ResponseEntity.badRequest()
                            .body(Map.of("erro", "Só pode marcar manutenção cheia a partir do estado MANUTENCAO. Estado atual: " + estado.getEstado()));
                }
                estado.setEstado("AVARIADO");
                estado.setSubEstado(null);
                break;
            default:
                return ResponseEntity.badRequest()
                        .body(Map.of("erro", "Ação inválida. Valores aceites: INICIAR_MANUTENCAO, REPARADO, MANUTENCAO_CHEIA"));
        }

        estadoRepo.save(estado);
        return ResponseEntity.ok(Map.of(
                "codigoAutocarro", codigoAutocarro,
                "estadoAtual", estado.getEstado()
        ));
    }

    /**
     * Override directo do estado operacional pelo admin — sem restrições de máquina de estados.
     * EM_SERVICO exige sentido (IDA ou VOLTA) e define inicioServico = agora.
     */
    @PatchMapping("/{codigoAutocarro}/estado-operacional")
    @Operation(summary = "Alterar estado operacional directamente (admin)")
    public ResponseEntity<?> alterarEstadoOperacional(
            @PathVariable String codigoAutocarro,
            @RequestBody Map<String, String> body) {

        String novoEstado = body.get("estado");
        if (novoEstado == null || !ESTADOS_VALIDOS.contains(novoEstado.toUpperCase())) {
            return ResponseEntity.badRequest().body(Map.of("erro",
                    "Campo 'estado' inválido. Valores aceites: ARMAZENADO, EM_TRANSITO, EM_SERVICO, AVARIADO, MANUTENCAO"));
        }
        novoEstado = novoEstado.toUpperCase();

        String sentido = body.get("sentido") != null ? body.get("sentido").toUpperCase() : null;
        if ("EM_SERVICO".equals(novoEstado) && (sentido == null || (!sentido.equals("IDA") && !sentido.equals("VOLTA")))) {
            return ResponseEntity.badRequest().body(Map.of("erro",
                    "Campo 'sentido' obrigatório quando estado = EM_SERVICO. Valores aceites: IDA, VOLTA"));
        }

        Autocarro autocarro = autocarroRepo.findByCodigo(codigoAutocarro).orElse(null);
        if (autocarro == null) return ResponseEntity.notFound().build();

        if (Boolean.FALSE.equals(autocarro.getAtivo())) {
            return ResponseEntity.badRequest().body(Map.of("erro",
                    "Autocarro inativo. Ative o veículo antes de alterar o estado operacional."));
        }

        AutocarroEstado estado = estadoRepo.findByAutocarroId(autocarro.getId())
                .orElseGet(() -> {
                    AutocarroEstado novo = new AutocarroEstado();
                    novo.setAutocarro(autocarro);
                    return novo;
                });

        if ("EM_SERVICO".equals(novoEstado)) {
            estado.setEstado("EM_SERVICO");
            estado.setInicioServico(LocalDateTime.now());
            estado.setSentidoAtual(sentido);
            estado.setSubEstado("PONTUAL");
            estado.setOcupacao(0);
        } else {
            estado.setEstado(novoEstado);
            estado.setSubEstado(null);
            estado.setInicioServico(null);
            estado.setSentidoAtual(null);
        }

        estado.setControloManual(true);
        estadoRepo.save(estado);
        return ResponseEntity.ok(Map.of(
                "codigoAutocarro", codigoAutocarro,
                "estadoAtual", estado.getEstado(),
                "sentidoAtual", estado.getSentidoAtual() != null ? estado.getSentidoAtual() : "",
                "controloManual", "true"
        ));
    }

    /**
     * Máquina de estados completa — transições permitidas pelo diagrama.
     *
     * Transições suportadas:
     *   ARMAZENADO  + A_CIRCULAR          → EM_TRANSITO
     *   ARMAZENADO  + MANTER              → MANUTENCAO
     *   ARMAZENADO  + AVARIAR             → AVARIADO
     *   EM_TRANSITO + A_CIRCULAR          → EM_SERVICO  (sentido obrigatório)
     *   EM_TRANSITO + ULTIMA_PARAGEM_CAIS → ARMAZENADO
     *   EM_TRANSITO + AVARIAR             → AVARIADO
     *   EM_SERVICO  + TERMINAR_SERVICO    → EM_TRANSITO
     *   EM_SERVICO  + AVARIAR             → AVARIADO
     *   AVARIADO    + REPARAR             → MANUTENCAO
     *   AVARIADO    + MANUTENCAO_CHEIA    → ARMAZENADO
     *   MANUTENCAO  + REPARADO            → ARMAZENADO
     *   MANUTENCAO  + MANTER              → ARMAZENADO
     *
     * Qualquer outra combinação devolve 400.
     */
    @PatchMapping("/{codigoAutocarro}/transicao-operacional")
    @Operation(summary = "Executar transição de estado operacional (máquina de estados completa)")
    public ResponseEntity<?> transicaoOperacional(
            @PathVariable String codigoAutocarro,
            @RequestBody Map<String, String> body) {

        String acao = body.get("acao");
        if (acao == null || acao.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("erro", "Campo 'acao' obrigatório."));
        }
        acao = acao.toUpperCase();

        String sentido = body.get("sentido") != null ? body.get("sentido").toUpperCase() : null;

        Autocarro autocarro = autocarroRepo.findByCodigo(codigoAutocarro).orElse(null);
        if (autocarro == null) return ResponseEntity.notFound().build();

        if (Boolean.FALSE.equals(autocarro.getAtivo())) {
            return ResponseEntity.badRequest().body(Map.of("erro",
                    "Autocarro inativo. Ative o veículo antes de alterar o estado operacional."));
        }

        AutocarroEstado estado = estadoRepo.findByAutocarroId(autocarro.getId())
                .orElseGet(() -> {
                    AutocarroEstado novo = new AutocarroEstado();
                    novo.setAutocarro(autocarro);
                    return novo;
                });

        String estadoAtual = estado.getEstado() != null ? estado.getEstado() : "ARMAZENADO";
        String novoEstado;

        switch (estadoAtual + "+" + acao) {

            // ── ARMAZENADO ─────────────────────────────────────────────────────
            case "ARMAZENADO+A_CIRCULAR":
                novoEstado = "EM_TRANSITO";
                break;
            case "ARMAZENADO+MANTER":
                novoEstado = "MANUTENCAO";
                break;
            case "ARMAZENADO+AVARIAR":
                novoEstado = "AVARIADO";
                break;

            // ── EM_TRANSITO ────────────────────────────────────────────────────
            case "EM_TRANSITO+A_CIRCULAR":
                if (sentido == null || (!sentido.equals("IDA") && !sentido.equals("VOLTA"))) {
                    return ResponseEntity.badRequest().body(Map.of("erro",
                            "Campo 'sentido' obrigatório para entrar em serviço. Valores aceites: IDA, VOLTA"));
                }
                novoEstado = "EM_SERVICO";
                break;
            case "EM_TRANSITO+ULTIMA_PARAGEM_CAIS":
                novoEstado = "ARMAZENADO";
                break;
            case "EM_TRANSITO+AVARIAR":
                novoEstado = "AVARIADO";
                break;

            // ── EM_SERVICO ─────────────────────────────────────────────────────
            case "EM_SERVICO+TERMINAR_SERVICO":
                novoEstado = "EM_TRANSITO";
                break;
            case "EM_SERVICO+AVARIAR":
                novoEstado = "AVARIADO";
                break;

            // ── AVARIADO ───────────────────────────────────────────────────────
            case "AVARIADO+REPARAR":
                novoEstado = "MANUTENCAO";
                break;
            case "AVARIADO+MANUTENCAO_CHEIA":
                novoEstado = "ARMAZENADO";
                break;

            // ── MANUTENCAO ─────────────────────────────────────────────────────
            case "MANUTENCAO+REPARADO":
            case "MANUTENCAO+MANTER":
                novoEstado = "ARMAZENADO";
                break;

            // ── Transição inválida ─────────────────────────────────────────────
            default:
                return ResponseEntity.badRequest().body(Map.of("erro",
                        "Transição inválida: ação '" + acao + "' não é permitida no estado '" + estadoAtual + "'."));
        }

        // Aplicar novo estado com regras de dados
        if ("EM_SERVICO".equals(novoEstado)) {
            estado.setEstado("EM_SERVICO");
            estado.setInicioServico(LocalDateTime.now());
            estado.setSentidoAtual(sentido);
            estado.setSubEstado("PONTUAL");
            estado.setOcupacao(0);
        } else {
            estado.setEstado(novoEstado);
            estado.setSubEstado(null);
            estado.setInicioServico(null);
            estado.setSentidoAtual(null);
        }

        estado.setControloManual(true);
        estadoRepo.save(estado);

        return ResponseEntity.ok(Map.of(
                "codigoAutocarro", codigoAutocarro,
                "estadoAnterior", estadoAtual,
                "acao", acao,
                "estadoAtual", novoEstado,
                "sentidoAtual", estado.getSentidoAtual() != null ? estado.getSentidoAtual() : "",
                "controloManual", "true"
        ));
    }

    /**
     * Volta ao modo automático — o script pode voltar a controlar o estado operacional.
     */
    @PatchMapping("/{codigoAutocarro}/controlo-automatico")
    @Operation(summary = "Voltar ao modo automático (script controla estado)")
    public ResponseEntity<?> retomarAutomatico(@PathVariable String codigoAutocarro) {
        Autocarro autocarro = autocarroRepo.findByCodigo(codigoAutocarro).orElse(null);
        if (autocarro == null) return ResponseEntity.notFound().build();

        AutocarroEstado estado = estadoRepo.findByAutocarroId(autocarro.getId())
                .orElseGet(() -> {
                    AutocarroEstado novo = new AutocarroEstado();
                    novo.setAutocarro(autocarro);
                    return novo;
                });

        estado.setControloManual(false);
        estadoRepo.save(estado);
        return ResponseEntity.ok(Map.of(
                "codigoAutocarro", codigoAutocarro,
                "controloManual", "false"
        ));
    }
}
