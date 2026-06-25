package com.example.loginapi.service.frota;

import com.example.loginapi.dto.AutocarroPosicaoRequest;
import com.example.loginapi.dto.AutocarroPosicaoResponse;
import com.example.loginapi.model.frota.Autocarro;
import com.example.loginapi.model.frota.AutocarroUltimaPosicao;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.frota.AutocarroEstado;
import com.example.loginapi.model.infraestrutura.LinhaParagem;
import com.example.loginapi.repository.frota.AutocarroEstadoRepository;
import com.example.loginapi.repository.frota.AutocarroRepository;
import com.example.loginapi.repository.frota.AutocarroUltimaPosicaoRepository;
import com.example.loginapi.repository.infraestrutura.LinhaParagemRepository;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.example.loginapi.model.infraestrutura.Paragem;


@Service
public class SimulacaoAutocarroService {

    private static final Logger log = LoggerFactory.getLogger(SimulacaoAutocarroService.class);

    // Estados que podem ser mantidos como "anterior válido" no fallback
    private static final Set<String> ESTADOS_NORMAIS = Set.of("ARMAZENADO", "EM_TRANSITO", "EM_SERVICO");

    @Autowired private AutocarroRepository autocarroRepo;
    @Autowired private AutocarroUltimaPosicaoRepository posicaoRepo;
    @Autowired private LinhaRepository linhaRepo;
    @Autowired private AutocarroEstadoRepository estadoRepo;
    @Autowired private LinhaParagemRepository linhaParagemRepo;

    /**
     * Recebe uma posição de autocarro, cria-o se necessário e guarda/atualiza a última posição.
     */
    @Transactional
    public AutocarroPosicaoResponse atualizarPosicao(AutocarroPosicaoRequest req) {

        // ── Validações ─────────────────────────────────────────────────────────
        if (req.getCodigoAutocarro() == null || req.getCodigoAutocarro().isBlank()) {
            throw new IllegalArgumentException("codigoAutocarro é obrigatório");
        }
        if (req.getLinhaId() == null) {
            throw new IllegalArgumentException("linhaId é obrigatório");
        }
        if (req.getLatitude() == null || req.getLatitude() < -90 || req.getLatitude() > 90) {
            throw new IllegalArgumentException("latitude deve estar entre -90 e 90");
        }
        if (req.getLongitude() == null || req.getLongitude() < -180 || req.getLongitude() > 180) {
            throw new IllegalArgumentException("longitude deve estar entre -180 e 180");
        }
        if (req.getVelocidade() != null && req.getVelocidade() < 0) {
            throw new IllegalArgumentException("velocidade não pode ser negativa");
        }
        if (req.getDirecao() != null && (req.getDirecao() < 0 || req.getDirecao() > 359)) {
            throw new IllegalArgumentException("direcao deve estar entre 0 e 359");
        }
        if (req.getTimestampReportado() == null) {
            throw new IllegalArgumentException("timestampReportado é obrigatório");
        }

        // ── Validar que a linha existe ─────────────────────────────────────────
        Linha linha = linhaRepo.findById(req.getLinhaId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Linha com id " + req.getLinhaId() + " não encontrada"));

        // ── Criar ou reutilizar autocarro ──────────────────────────────────────
        Autocarro autocarro = autocarroRepo.findByCodigo(req.getCodigoAutocarro())
                .orElseGet(() -> {
                    log.info("Criar novo autocarro: {}", req.getCodigoAutocarro());
                    Autocarro novo = new Autocarro();
                    novo.setCodigo(req.getCodigoAutocarro());
                    novo.setNome(req.getNome());
                    novo.setLinha(linha);
                    novo.setAtivo(true);
                    return autocarroRepo.save(novo);
                });

        // Rejeitar update para autocarro inativo
        if (Boolean.FALSE.equals(autocarro.getAtivo())) {
            log.info("POST de posição ignorado para autocarro inativo: {}", autocarro.getCodigo());
            throw new IllegalArgumentException("Autocarro inativo: " + autocarro.getCodigo()
                    + ". Ative o veículo no backoffice antes de retomar a simulação.");
        }

        // Atualizar nome se vier preenchido e for diferente
        if (req.getNome() != null && !req.getNome().equals(autocarro.getNome())) {
            autocarro.setNome(req.getNome());
            autocarroRepo.save(autocarro);
        }

        // ── Guardar / atualizar última posição ─────────────────────────────────
        AutocarroUltimaPosicao posicao = posicaoRepo.findByAutocarroId(autocarro.getId())
                .orElseGet(() -> {
                    AutocarroUltimaPosicao nova = new AutocarroUltimaPosicao();
                    nova.setAutocarro(autocarro);
                    return nova;
                });

        posicao.setLinha(linha);
        posicao.setLatitude(req.getLatitude());
        posicao.setLongitude(req.getLongitude());
        posicao.setVelocidade(req.getVelocidade());
        posicao.setDirecao(req.getDirecao());
        posicao.setTimestampReportado(req.getTimestampReportado());
        posicao.setRecebidoEm(Instant.now());

        posicaoRepo.save(posicao);

        // ── Guardar / atualizar estado e ocupação ──────────────────────────────
        AutocarroEstado estado = estadoRepo.findByAutocarroId(autocarro.getId())
                .orElseGet(() -> {
                    AutocarroEstado novo = new AutocarroEstado();
                    novo.setAutocarro(autocarro);
                    return novo;
                });

        // Controlo manual: só actualiza posição GPS; estado operacional protegido pelo admin
        if (estado.isControloManual()) {
            log.debug("Controlo manual activo — update de posição apenas: {}", autocarro.getCodigo());
            return toResponse(posicao, autocarro, linha);
        }

        // Capturar sub-estado anterior antes de qualquer alteração (necessário para histerese)
        String subEstadoAnterior = estado.getSubEstado();

        // Determinar estado operacional final com protecção de AVARIADO/MANUTENCAO
        String estadoFinal = determinarEstadoOperacional(
                estado.getEstado(),
                req.getEstado(),
                req.getVelocidade(),
                req.getInicioServico() != null,
                req.getSentido() != null && !req.getSentido().isBlank());
        estado.setEstado(estadoFinal);

        // Limpar sub-estado quando não está em serviço; quando EM_SERVICO, o backend calcula
        if (!"EM_SERVICO".equals(estadoFinal)) {
            estado.setSubEstado(null);
        }
        if (req.getOcupacao() != null) {
            estado.setOcupacao(req.getOcupacao());
        }
        if (req.getCapacidade() != null && req.getCapacidade() > 0) {
            estado.setCapacidade(req.getCapacidade());
        }

        // Guardar/limpar inicioServico e sentidoAtual com base no estado final
        if ("EM_SERVICO".equals(estadoFinal)) {
            if (req.getInicioServico() != null) {
                estado.setInicioServico(req.getInicioServico());
            }
            if (req.getSentido() != null && !req.getSentido().isBlank()) {
                estado.setSentidoAtual(req.getSentido());
            }
            // Backend calcula subEstado e ignora o valor enviado pelo script
            estado.setSubEstado(calcularSubEstado(estado, posicao, linha.getId(), subEstadoAnterior));
        } else {
            estado.setInicioServico(null);
            estado.setSentidoAtual(null);
        }

        estadoRepo.save(estado);

        log.debug("Posição atualizada: {} → [{}, {}]",
                autocarro.getCodigo(), req.getLatitude(), req.getLongitude());

        return toResponse(posicao, autocarro, linha);
    }

    /**
     * Devolve posições atuais de autocarros ativos, opcionalmente filtrado por linha.
     */
    @Transactional(readOnly = true)
    public List<AutocarroPosicaoResponse> listarPosicoes(Long linhaId) {
        List<AutocarroUltimaPosicao> posicoes;

        if (linhaId != null) {
            posicoes = posicaoRepo.findAllAtivasByLinhaId(linhaId);
        } else {
            posicoes = posicaoRepo.findAllAtivas();
        }

        return posicoes.stream()
                .map(p -> toResponse(p, p.getAutocarro(), p.getLinha()))
                .collect(Collectors.toList());
    }

    // ── Mapper ─────────────────────────────────────────────────────────────────

    private AutocarroPosicaoResponse toResponse(AutocarroUltimaPosicao p,
                                                 Autocarro autocarro,
                                                 Linha linha) {
        AutocarroPosicaoResponse resp = new AutocarroPosicaoResponse();
        resp.setCodigoAutocarro(autocarro.getCodigo());
        resp.setNome(autocarro.getNome());
        resp.setLinhaId(linha.getId());
        resp.setLinhaNome(linha.getNome());
        resp.setLatitude(p.getLatitude());
        resp.setLongitude(p.getLongitude());
        resp.setVelocidade(p.getVelocidade());
        resp.setDirecao(p.getDirecao());
        resp.setTimestampReportado(p.getTimestampReportado());
        resp.setRecebidoEm(p.getRecebidoEm());

        // ── Preencher estado, ocupação e dados de viagem ───────────────────────
        estadoRepo.findByAutocarroId(autocarro.getId()).ifPresent(est -> {
            resp.setEstado(est.getEstado());
            resp.setSubEstado(est.getSubEstado());
            resp.setOcupacao(est.getOcupacao());
            resp.setCapacidade(est.getCapacidade());
            if (est.getCapacidade() != null && est.getCapacidade() > 0 && est.getOcupacao() != null) {
                double pct = (est.getOcupacao() * 100.0) / est.getCapacidade();
                resp.setPercentagemOcupacao(Math.round(pct * 10.0) / 10.0);
            }
            resp.setInicioServico(est.getInicioServico());
            resp.setSentidoAtual(est.getSentidoAtual());
            resp.setControloManual(est.isControloManual());
        });

        // ── Calcular ETA para a próxima paragem ────────────────────────────────
        // Só calcula se o autocarro tiver velocidade > 0. Se velocidade = 0 ou
        // null, o autocarro está parado e não é possível estimar chegada.
        if (p.getVelocidade() != null && p.getVelocidade() > 0) {
            List<LinhaParagem> paragens = linhaParagemRepo.findByLinhaIdOrderByOrdemAsc(linha.getId());
            if (!paragens.isEmpty()) {
                LinhaParagem maisProxima = null;
                double menorDistancia = Double.MAX_VALUE;
                for (LinhaParagem lp : paragens) {
                    if (lp.getParagem().getLatitude() == null || lp.getParagem().getLongitude() == null) continue;
                    double dist = haversineKm(
                            p.getLatitude(), p.getLongitude(),
                            lp.getParagem().getLatitude(), lp.getParagem().getLongitude());
                    if (dist < menorDistancia) {
                        menorDistancia = dist;
                        maisProxima = lp;
                    }
                }
                if (maisProxima != null) {
                    double etaHoras = menorDistancia / p.getVelocidade();
                    int etaMinutos = (int) Math.max(1, Math.round(etaHoras * 60));
                    resp.setProximaParagemNome(maisProxima.getParagem().getNome());
                    resp.setEtaMinutos(etaMinutos);
                }
            }
        }

        return resp;
    }

    /**
     * Determina o estado operacional final do autocarro.
     *
     * Protecção de estados manuais:
     * - MANUTENCAO: nunca sobrescrito por POST de posição (admin-only).
     * - AVARIADO:   nunca sobrescrito por EM_SERVICO/EM_TRANSITO/ARMAZENADO via POST de posição.
     *
     * Para estados normais, segue esta ordem:
     * 1. Aceitar estado do request se válido e com dados mínimos.
     * 2. EM_SERVICO requer inicioServico + sentido; sem eles degrada para EM_TRANSITO.
     * 3. MANUTENCAO no request é rejeitado (admin-only).
     * 4. Fallback por velocidade: > 1 km/h → EM_TRANSITO.
     * 5. Manter estado anterior normal se existir.
     * 6. ARMAZENADO como último recurso (não inferido por velocidade = 0).
     */
    private String determinarEstadoOperacional(String estadoAtualBD,
                                                String estadoRequest,
                                                Double velocidade,
                                                boolean temInicioServico,
                                                boolean temSentido) {
        // ── Protecção de MANUTENCAO ────────────────────────────────────────────
        if ("MANUTENCAO".equals(estadoAtualBD)) {
            return "MANUTENCAO";
        }

        // ── Protecção de AVARIADO ──────────────────────────────────────────────
        // Só o fluxo admin pode fazer sair de AVARIADO (via INICIAR_MANUTENCAO)
        if ("AVARIADO".equals(estadoAtualBD)) {
            return "AVARIADO";
        }

        // ── Processar estado pedido no request ─────────────────────────────────
        if (estadoRequest != null && !estadoRequest.isBlank()) {
            switch (estadoRequest) {
                case "ARMAZENADO":
                case "EM_TRANSITO":
                    return estadoRequest;
                case "EM_SERVICO":
                    // Validar dados mínimos para pontualidade; sem eles degradar para EM_TRANSITO
                    if (temInicioServico && temSentido) return "EM_SERVICO";
                    log.debug("EM_SERVICO pedido sem inicioServico/sentido — a usar EM_TRANSITO");
                    return "EM_TRANSITO";
                case "AVARIADO":
                    // Avaria declarada pelo script — aceitar
                    return "AVARIADO";
                case "MANUTENCAO":
                    // MANUTENCAO via POST de posição é inválido: só o admin pode iniciar manutenção
                    log.warn("Estado MANUTENCAO rejeitado em POST de posição — use endpoint admin.");
                    break;   // fallthrough para inferência
                default:
                    log.warn("Estado desconhecido no request: {}", estadoRequest);
                    break;   // fallthrough para inferência
            }
        }

        // ── Fallback por velocidade (request sem estado válido) ─────────────────
        double vel = (velocidade != null) ? velocidade : 0.0;
        if (vel > 1.0) return "EM_TRANSITO";

        // ── Manter estado anterior normal ──────────────────────────────────────
        if (estadoAtualBD != null && ESTADOS_NORMAIS.contains(estadoAtualBD)) {
            return estadoAtualBD;
        }

        // ── Último recurso (não inferido por velocidade 0) ─────────────────────
        return "ARMAZENADO";
    }

    /**
     * Calcula o sub-estado de pontualidade (PONTUAL / ATRASADO / ADIANTADO) com base
     * na posição actual, tempo decorrido desde o início do serviço e o horário previsto
     * das paragens (LinhaParagem.minutosDesdeInicio).
     *
     * Se alguma pré-condição falhar devolve subEstadoAnterior (quando não null/blank)
     * ou PONTUAL — evita apagar um estado real por falta momentânea de dados.
     */
    private String calcularSubEstado(AutocarroEstado estado,
                                     AutocarroUltimaPosicao posicao,
                                     Long linhaId,
                                     String subEstadoAnterior) {

        String fallback = (subEstadoAnterior != null && !subEstadoAnterior.isBlank())
                ? subEstadoAnterior : "PONTUAL";

        // ── Pré-condições ──────────────────────────────────────────────────────
        if (estado.getInicioServico() == null)                              return fallback;
        if (estado.getSentidoAtual() == null || estado.getSentidoAtual().isBlank()) return fallback;
        if (posicao.getLatitude() == null || posicao.getLongitude() == null) return fallback;

        // Proteger contra inicioServico no futuro (relógio desalinhado)
        LocalDateTime agora = LocalDateTime.now();
        if (estado.getInicioServico().isAfter(agora))                       return fallback;

        double tempoDecorridoMin = ChronoUnit.SECONDS.between(estado.getInicioServico(), agora) / 60.0;

        // ── Paragens do sentido actual ─────────────────────────────────────────
        List<LinhaParagem> paragens = linhaParagemRepo
                .findByLinhaIdAndSentidoOrderByOrdemAsc(linhaId, estado.getSentidoAtual());
        if (paragens.isEmpty())                                             return fallback;

        // ── Próxima paragem esperada ───────────────────────────────────────────
        // Primeira paragem cujo minutosDesdeInicio >= tempo decorrido e com coordenadas
        LinhaParagem proximaParagem = null;
        for (LinhaParagem lp : paragens) {
            if (lp.getParagem().getLatitude() == null || lp.getParagem().getLongitude() == null) continue;
            if (lp.getMinutosDesdeInicio() >= tempoDecorridoMin) {
                proximaParagem = lp;
                break;
            }
        }
        // Se já passou todas as paragens previstas, usar a última com coordenadas
        if (proximaParagem == null) {
            for (int i = paragens.size() - 1; i >= 0; i--) {
                LinhaParagem lp = paragens.get(i);
                if (lp.getParagem().getLatitude() != null && lp.getParagem().getLongitude() != null) {
                    proximaParagem = lp;
                    break;
                }
            }
        }
        if (proximaParagem == null)                                         return fallback;

        // ── Distância e ETA real ───────────────────────────────────────────────
        double distanciaKm = haversineKm(
                posicao.getLatitude(),  posicao.getLongitude(),
                proximaParagem.getParagem().getLatitude(),
                proximaParagem.getParagem().getLongitude());

        double minutosRestantesPrevistos = proximaParagem.getMinutosDesdeInicio() - tempoDecorridoMin;
        double velocidade = (posicao.getVelocidade() != null) ? posicao.getVelocidade() : 0.0;

        if (velocidade < 1.0) {
            // Sem velocidade útil: ETA não é calculável.
            // Marcar ATRASADO apenas se o tempo previsto já passou há mais de 5 min.
            if (minutosRestantesPrevistos < -5.0) return "ATRASADO";
            return fallback;
        }

        double etaRealMin = (distanciaKm / velocidade) * 60.0;   // sem risco de /0 (velocidade >= 1)
        double desvioMin  = etaRealMin - minutosRestantesPrevistos;

        // ── Classificar com histerese ──────────────────────────────────────────
        if ("ATRASADO".equals(subEstadoAnterior)) {
            if (desvioMin <= -2.0) return "ADIANTADO";
            if (desvioMin <   3.0) return "PONTUAL";   // saída suavizada de ATRASADO
            return "ATRASADO";
        }
        if ("ADIANTADO".equals(subEstadoAnterior)) {
            if (desvioMin >=  5.0) return "ATRASADO";
            if (desvioMin >  -1.0) return "PONTUAL";   // saída suavizada de ADIANTADO
            return "ADIANTADO";
        }
        // Estado neutro (PONTUAL ou null)
        if (desvioMin >=  5.0) return "ATRASADO";
        if (desvioMin <= -2.0) return "ADIANTADO";
        return "PONTUAL";
    }

    private double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                 + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                 * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    }
}
