package com.example.loginapi.service.frota;

import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.infraestrutura.LinhaParagem;
import com.example.loginapi.model.infraestrutura.Paragem;
import com.example.loginapi.model.titulos.TipoBilhete;
import com.example.loginapi.repository.infraestrutura.LinhaParagemRepository;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import com.example.loginapi.repository.infraestrutura.ParagemRepository;
import com.example.loginapi.repository.titulos.TipoBilheteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import com.example.loginapi.model.infraestrutura.Coroa;


/**
 * Importa dados reais GTFS fornecidos em src/main/resources/gtfs.
 *
 * Fonte de verdade nesta fase:
 * - routes.txt -> linhas
 * - stops.txt -> paragens
 * - fare_attributes.txt -> tipos_bilhete avulso
 * - trips.txt + stop_times.txt -> linha_paragens, num_paragens e duracao_min
 * - shapes.txt -> rota_linha_ponto
 *
 * A importação é idempotente: atualiza registos existentes quando encontra o
 * identificador GTFS correspondente, e cria os que ainda não existem.
 */
@Service
public class GtfsImportService {

    private static final Logger log = LoggerFactory.getLogger(GtfsImportService.class);

    private static final String[] CORES = {
            "#ef4444", "#3b82f6", "#10b981", "#f59e0b", "#8b5cf6", "#ec4899", "#0077b6"
    };

    private final LinhaRepository linhaRepository;
    private final ParagemRepository paragemRepository;
    private final TipoBilheteRepository tipoBilheteRepository;
    private final LinhaParagemRepository linhaParagemRepository;
    private final JdbcTemplate jdbcTemplate;

    public GtfsImportService(
            LinhaRepository linhaRepository,
            ParagemRepository paragemRepository,
            TipoBilheteRepository tipoBilheteRepository,
            LinhaParagemRepository linhaParagemRepository,
            JdbcTemplate jdbcTemplate
    ) {
        this.linhaRepository = linhaRepository;
        this.paragemRepository = paragemRepository;
        this.tipoBilheteRepository = tipoBilheteRepository;
        this.linhaParagemRepository = linhaParagemRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public void importarDadosBase() {
        importarLinhas();
        importarParagens();
        importarTarifarioAvulso();
        prepararTabelaLinhaParagensParaGtfs();
        prepararTabelaRotaLinhaPontoParaGtfs();
        importarPercursosLinhas();
    }

    @Transactional
    public void importarLinhas() {
        List<Map<String, String>> rows = readCsv("gtfs/routes.txt");
        int criadas = 0;
        int atualizadas = 0;
        int index = 0;

        for (Map<String, String> row : rows) {
            String routeId = clean(row.get("route_id"));
            String numero = clean(row.get("route_short_name"));
            String nome = clean(row.get("route_long_name"));
            if (isBlank(routeId) || isBlank(numero) || isBlank(nome)) continue;

            Optional<Linha> byGtfs = linhaRepository.findByGtfsRouteId(routeId);
            Optional<Linha> byNumero = byGtfs.isPresent() ? Optional.empty() : linhaRepository.findByNumero(numero);

            Linha linha = byGtfs.or(() -> byNumero).orElseGet(Linha::new);
            boolean nova = linha.getId() == null;
            boolean tinhaGtfsAntes = !isBlank(linha.getGtfsRouteId());

            linha.setGtfsRouteId(routeId);
            linha.setNumero(numero);
            linha.setNome(toDisplayName(nome));

            String[] origemDestino = splitOrigemDestino(nome);
            linha.setOrigem(toDisplayName(origemDestino[0]));
            linha.setDestino(toDisplayName(origemDestino[1]));

            if (isBlank(linha.getCor())) {
                linha.setCor(CORES[index % CORES.length]);
            }

            // Se era uma linha antiga/demo sem gtfsRouteId, remover métricas inventadas.
            // Métricas reais serão calculadas a partir de trips.txt/stop_times.txt.
            if (nova || !tinhaGtfsAntes) {
                linha.setNumParagens(0);
                linha.setDuracaoMin(0);
            }

            linhaRepository.save(linha);
            if (nova) criadas++; else atualizadas++;
            index++;
        }

        log.info("GTFS routes.txt importado: {} linhas criadas, {} atualizadas", criadas, atualizadas);
    }

    @Transactional
    public void importarParagens() {
        List<Map<String, String>> rows = readCsv("gtfs/stops.txt");
        int criadas = 0;
        int atualizadas = 0;

        for (Map<String, String> row : rows) {
            String stopId = clean(row.get("stop_id"));
            String nome = clean(row.get("stop_name"));
            if (isBlank(stopId) || isBlank(nome)) continue;

            Paragem paragem = paragemRepository.findByGtfsStopId(stopId).orElseGet(Paragem::new);
            boolean nova = paragem.getId() == null;

            paragem.setGtfsStopId(stopId);
            paragem.setNome(nome.trim());
            paragem.setLatitude(parseDouble(row.get("stop_lat")));
            paragem.setLongitude(parseDouble(row.get("stop_lon")));
            paragem.setZoneId(clean(row.get("zone_id")));
            paragem.setAtivo(true);

            paragemRepository.save(paragem);
            if (nova) criadas++; else atualizadas++;
        }

        log.info("GTFS stops.txt importado: {} paragens criadas, {} atualizadas", criadas, atualizadas);
    }

    @Transactional
    public void importarTarifarioAvulso() {
        List<Map<String, String>> rows = readCsv("gtfs/fare_attributes.txt");
        int criados = 0;
        int preservados = 0;

        for (Map<String, String> row : rows) {
            String fareId = clean(row.get("fare_id"));
            if (isBlank(fareId)) continue;

            String nome = nomeTarifa(fareId);
            TipoBilhete tipo = tipoBilheteRepository.findByGtfsFareId(fareId)
                    .or(() -> tipoBilheteRepository.findByNome(nome))
                    .or(() -> legacyTipoBilhete(fareId))
                    .orElseGet(TipoBilhete::new);
            boolean novo = tipo.getId() == null;

            Integer transferDuration = parseInteger(row.get("transfer_duration"));
            Integer transfers = parseInteger(row.get("transfers"));

            tipo.setGtfsFareId(fareId);
            tipo.setNome(nome);
            tipo.setCategoria(TipoBilhete.Categoria.AVULSO);
            tipo.setTransferDuration(transferDuration);
            tipo.setTransfers(transfers);
            tipo.setValidadeHoras(transferDuration != null
                    ? (int) Math.ceil((double) transferDuration / 3600)
                    : 1);
            tipo.setDescricao(descricaoTarifa(fareId, transfers, transferDuration));

            if (novo) {
                tipo.setPreco(parseBigDecimal(row.get("price")));
                log.info("GTFS tipo de bilhete criado: {} — {}€", nome, row.get("price"));
                criados++;
            } else {
                log.debug("GTFS tipo de bilhete existente — preço preservado: {}", nome);
                preservados++;
            }
            tipoBilheteRepository.save(tipo);
        }

        log.info("GTFS fare_attributes.txt importado: {} tipos criados, {} preservados (preço não alterado)", criados, preservados);
    }

    /**
     * Remove a constraint antiga (linha_id, ordem), porque o GTFS passa a guardar
     * uma sequência por sentido. Com IDA e VOLTA, a ordem 1 é legítima nos dois
     * sentidos, por isso a unicidade correta é (linha_id, sentido, ordem).
     *
     * O schema.sql também tem esta correção, mas no projeto atual o
     * spring.sql.init.mode está desativado. Executar aqui garante compatibilidade
     * com bases de dados já existentes.
     */
    private void prepararTabelaLinhaParagensParaGtfs() {
        try {
            jdbcTemplate.execute("ALTER TABLE linha_paragens ADD COLUMN IF NOT EXISTS sentido VARCHAR(20)");
            jdbcTemplate.execute("ALTER TABLE linha_paragens DROP CONSTRAINT IF EXISTS unique_ordem_por_linha");
            jdbcTemplate.execute("CREATE UNIQUE INDEX IF NOT EXISTS uq_linha_paragens_linha_sentido_ordem " +
                    "ON linha_paragens (linha_id, sentido, ordem) " +
                    "WHERE sentido IS NOT NULL");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_linha_paragens_linha_sentido_ordem " +
                    "ON linha_paragens (linha_id, sentido, ordem)");
        } catch (Exception ex) {
            log.warn("Não foi possível preparar constraints de linha_paragens para GTFS: {}", ex.getMessage());
        }
    }

    private void prepararTabelaRotaLinhaPontoParaGtfs() {
        try {
            jdbcTemplate.execute("ALTER TABLE rota_linha_ponto ADD COLUMN IF NOT EXISTS shape_id VARCHAR(80)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_rota_linha_ponto_linha_sentido_ordem " +
                    "ON rota_linha_ponto (linha_id, sentido, ordem)");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_rota_linha_ponto_shape_id " +
                    "ON rota_linha_ponto (shape_id)");
        } catch (Exception ex) {
            log.warn("Não foi possível preparar rota_linha_ponto para GTFS shapes: {}", ex.getMessage());
        }
    }

    /**
     * Importa uma viagem representativa por linha e por direction_id.
     *
     * A tabela linha_paragens atual representa um percurso simplificado. Por isso
     * escolhemos a viagem com mais paragens para cada par route_id + direction_id.
     * O endpoint existente devolve IDA por defeito, evitando misturar ida e volta.
     */
    @Transactional
    public void importarPercursosLinhas() {
        List<Map<String, String>> trips = readCsv("gtfs/trips.txt");
        List<Map<String, String>> stopTimes = readCsv("gtfs/stop_times.txt");
        if (trips.isEmpty() || stopTimes.isEmpty()) {
            log.warn("GTFS trips.txt/stop_times.txt ausentes ou vazios; linha_paragens não foi importada");
            return;
        }

        Map<String, List<StopTimeRow>> stopTimesByTrip = new HashMap<>();
        for (Map<String, String> row : stopTimes) {
            String tripId = clean(row.get("trip_id"));
            String stopId = clean(row.get("stop_id"));
            Integer sequence = parseInteger(row.get("stop_sequence"));
            if (isBlank(tripId) || isBlank(stopId) || sequence == null) continue;

            String arrival = firstNonBlank(row.get("arrival_time"), row.get("departure_time"));
            String departure = firstNonBlank(row.get("departure_time"), row.get("arrival_time"));
            StopTimeRow stopTime = new StopTimeRow(
                    tripId,
                    stopId,
                    sequence,
                    parseGtfsTimeToMinutes(arrival),
                    parseGtfsTimeToMinutes(departure)
            );
            stopTimesByTrip.computeIfAbsent(tripId, ignored -> new ArrayList<>()).add(stopTime);
        }

        Map<String, TripCandidate> bestTripByRouteAndDirection = new LinkedHashMap<>();
        for (Map<String, String> row : trips) {
            String routeId = clean(row.get("route_id"));
            String tripId = clean(row.get("trip_id"));
            String directionId = clean(row.get("direction_id"));
            String shapeId = clean(row.get("shape_id"));
            if (isBlank(routeId) || isBlank(tripId)) continue;

            List<StopTimeRow> tripStopTimes = stopTimesByTrip.getOrDefault(tripId, List.of());
            if (tripStopTimes.isEmpty()) continue;

            String sentido = toSentido(directionId);
            String key = routeId + "|" + sentido;
            TripCandidate candidate = new TripCandidate(routeId, tripId, sentido, shapeId, tripStopTimes);
            TripCandidate current = bestTripByRouteAndDirection.get(key);
            if (current == null || candidate.isBetterThan(current)) {
                bestTripByRouteAndDirection.put(key, candidate);
            }
        }

        Map<String, Paragem> paragensByGtfsId = new HashMap<>();
        for (Paragem paragem : paragemRepository.findAll()) {
            if (!isBlank(paragem.getGtfsStopId())) {
                paragensByGtfsId.put(paragem.getGtfsStopId(), paragem);
            }
        }

        int linhasProcessadas = 0;
        int registosCriados = 0;
        int linhasSemParagens = 0;

        for (Linha linha : linhaRepository.findAll()) {
            if (isBlank(linha.getGtfsRouteId())) continue;

            TripCandidate ida = bestTripByRouteAndDirection.get(linha.getGtfsRouteId() + "|IDA");
            TripCandidate volta = bestTripByRouteAndDirection.get(linha.getGtfsRouteId() + "|VOLTA");
            TripCandidate principal = ida != null ? ida : volta;

            if (principal == null) {
                linhasSemParagens++;
                continue;
            }

            linhaParagemRepository.deleteByLinhaId(linha.getId());
            linhaParagemRepository.flush();

            int criadasLinha = 0;
            if (ida != null) criadasLinha += criarLinhaParagens(linha, ida, paragensByGtfsId);
            if (volta != null) criadasLinha += criarLinhaParagens(linha, volta, paragensByGtfsId);

            List<StopTimeRow> principalOrdenada = principal.sortedStopTimes();
            linha.setNumParagens(principalOrdenada.size());
            linha.setDuracaoMin(calcularDuracaoMin(principalOrdenada));
            linhaRepository.save(linha);

            registosCriados += criadasLinha;
            linhasProcessadas++;
        }

        log.info("GTFS trips/stop_times importado: {} linhas processadas, {} linha_paragens criadas, {} linhas sem paragens", linhasProcessadas, registosCriados, linhasSemParagens);

        importarShapesRotas(bestTripByRouteAndDirection);
    }

    /**
     * Importa o traçado real GTFS para rota_linha_ponto.
     *
     * Usamos o shape_id da viagem representativa já escolhida em
     * trips.txt/stop_times.txt. Assim, o mapa passa a desenhar a rota real do
     * GTFS, em vez de ligar apenas as paragens em linha reta.
     */
    private void importarShapesRotas(Map<String, TripCandidate> bestTripByRouteAndDirection) {
        List<Map<String, String>> shapeRows = readCsv("gtfs/shapes.txt");
        if (shapeRows.isEmpty()) {
            log.warn("GTFS shapes.txt ausente ou vazio; rota_linha_ponto não foi importada");
            return;
        }

        Map<String, List<ShapePointRow>> shapesById = new HashMap<>();
        for (Map<String, String> row : shapeRows) {
            String shapeId = clean(row.get("shape_id"));
            Double lat = parseDouble(row.get("shape_pt_lat"));
            Double lon = parseDouble(row.get("shape_pt_lon"));
            Integer sequence = parseInteger(row.get("shape_pt_sequence"));
            if (isBlank(shapeId) || lat == null || lon == null || sequence == null) continue;
            shapesById.computeIfAbsent(shapeId, ignored -> new ArrayList<>())
                    .add(new ShapePointRow(shapeId, lat, lon, sequence));
        }

        int linhasComShape = 0;
        int pontosCriados = 0;
        int shapesNaoEncontrados = 0;

        for (Linha linha : linhaRepository.findAll()) {
            if (isBlank(linha.getGtfsRouteId())) continue;

            TripCandidate ida = bestTripByRouteAndDirection.get(linha.getGtfsRouteId() + "|IDA");
            TripCandidate volta = bestTripByRouteAndDirection.get(linha.getGtfsRouteId() + "|VOLTA");

            boolean importouLinha = false;
            if (ida != null && !isBlank(ida.shapeId())) {
                int criados = importarShapeParaLinha(linha, ida.sentido(), ida.shapeId(), shapesById);
                pontosCriados += criados;
                importouLinha = importouLinha || criados > 0;
                if (criados == 0) shapesNaoEncontrados++;
            }
            if (volta != null && !isBlank(volta.shapeId())) {
                int criados = importarShapeParaLinha(linha, volta.sentido(), volta.shapeId(), shapesById);
                pontosCriados += criados;
                importouLinha = importouLinha || criados > 0;
                if (criados == 0) shapesNaoEncontrados++;
            }
            if (importouLinha) linhasComShape++;
        }

        log.info("GTFS shapes.txt importado: {} linhas com rota, {} pontos em rota_linha_ponto, {} shapes não encontrados",
                linhasComShape, pontosCriados, shapesNaoEncontrados);
    }

    private int importarShapeParaLinha(Linha linha, String sentido, String shapeId, Map<String, List<ShapePointRow>> shapesById) {
        List<ShapePointRow> pontos = shapesById.getOrDefault(shapeId, List.of()).stream()
                .sorted(Comparator.comparing(ShapePointRow::sequence))
                .toList();
        if (pontos.isEmpty()) return 0;

        Long existentes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM rota_linha_ponto WHERE linha_id = ? AND sentido = ? AND shape_id = ?",
                Long.class,
                linha.getId(),
                sentido,
                shapeId
        );
        if (existentes != null && existentes == pontos.size()) {
            return 0;
        }

        jdbcTemplate.update("DELETE FROM rota_linha_ponto WHERE linha_id = ? AND sentido = ?", linha.getId(), sentido);

        final int batchSize = 500;
        int total = 0;
        for (int from = 0; from < pontos.size(); from += batchSize) {
            List<ShapePointRow> batch = pontos.subList(from, Math.min(from + batchSize, pontos.size()));
            jdbcTemplate.batchUpdate(
                    "INSERT INTO rota_linha_ponto (linha_id, sentido, ordem, latitude, longitude, shape_id) VALUES (?, ?, ?, ?, ?, ?)",
                    batch,
                    batch.size(),
                    (ps, point) -> {
                        ps.setLong(1, linha.getId());
                        ps.setString(2, sentido);
                        ps.setInt(3, point.sequence());
                        ps.setDouble(4, point.latitude());
                        ps.setDouble(5, point.longitude());
                        ps.setString(6, shapeId);
                    }
            );
            total += batch.size();
        }
        return total;
    }

    private int criarLinhaParagens(Linha linha, TripCandidate trip, Map<String, Paragem> paragensByGtfsId) {
        List<StopTimeRow> ordered = trip.sortedStopTimes();
        if (ordered.isEmpty()) return 0;

        Integer startMinutes = ordered.stream()
                .map(StopTimeRow::effectiveTimeMinutes)
                .filter(minutes -> minutes != null)
                .findFirst()
                .orElse(null);

        int criadas = 0;
        int ordem = 1;
        for (StopTimeRow stopTime : ordered) {
            Paragem paragem = paragensByGtfsId.get(stopTime.stopId());
            if (paragem == null) continue;

            LinhaParagem linhaParagem = new LinhaParagem();
            linhaParagem.setLinha(linha);
            linhaParagem.setParagem(paragem);
            linhaParagem.setSentido(trip.sentido());
            linhaParagem.setOrdem(ordem++);
            linhaParagem.setMinutosDesdeInicio(calcularMinutosDesdeInicio(startMinutes, stopTime.effectiveTimeMinutes()));
            linhaParagemRepository.save(linhaParagem);
            criadas++;
        }
        return criadas;
    }

    private int calcularMinutosDesdeInicio(Integer startMinutes, Integer currentMinutes) {
        if (startMinutes == null || currentMinutes == null) return 0;
        return Math.max(0, currentMinutes - startMinutes);
    }

    private int calcularDuracaoMin(List<StopTimeRow> stopTimes) {
        Integer first = null;
        Integer last = null;
        for (StopTimeRow stopTime : stopTimes) {
            Integer minutes = stopTime.effectiveTimeMinutes();
            if (minutes == null) continue;
            if (first == null) first = minutes;
            last = minutes;
        }
        if (first == null || last == null) return 0;
        return Math.max(0, last - first);
    }

    private String toSentido(String directionId) {
        return "1".equals(clean(directionId)) ? "VOLTA" : "IDA";
    }

    private Optional<TipoBilhete> legacyTipoBilhete(String fareId) {
        if ("1_coroa-zona_1".equals(fareId)) return tipoBilheteRepository.findByNome("Coroa 1");
        if ("1_coroa-zona_2".equals(fareId)) return tipoBilheteRepository.findByNome("Coroa 2");
        return Optional.empty();
    }

    private List<Map<String, String>> readCsv(String classpathLocation) {
        ClassPathResource resource = new ClassPathResource(classpathLocation);
        if (!resource.exists()) {
            log.warn("Ficheiro GTFS não encontrado no classpath: {}", classpathLocation);
            return List.of();
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String headerLine = reader.readLine();
            if (headerLine == null) return List.of();

            List<String> headers = parseCsvLine(headerLine).stream()
                    .map(this::clean)
                    .toList();
            List<Map<String, String>> rows = new ArrayList<>();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                List<String> values = parseCsvLine(line);
                Map<String, String> row = new HashMap<>();
                for (int i = 0; i < headers.size(); i++) {
                    row.put(headers.get(i), i < values.size() ? clean(values.get(i)) : "");
                }
                rows.add(row);
            }
            return rows;
        } catch (IOException ex) {
            throw new IllegalStateException("Erro ao ler ficheiro GTFS: " + classpathLocation, ex);
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        values.add(current.toString());
        return values;
    }

    private String[] splitOrigemDestino(String routeLongName) {
        String[] parts = routeLongName.split("\\s+-\\s+", 2);
        if (parts.length == 2) return new String[] { parts[0], parts[1] };
        return new String[] { routeLongName, routeLongName };
    }

    private String toDisplayName(String value) {
        if (value == null) return "";
        String trimmed = value.trim();
        if (trimmed.isEmpty()) return trimmed;
        String lower = trimmed.toLowerCase();
        StringBuilder result = new StringBuilder(lower.length());
        boolean capitalizeNext = true;
        for (int i = 0; i < lower.length(); i++) {
            char c = lower.charAt(i);
            if (Character.isLetter(c) && capitalizeNext) {
                result.append(Character.toTitleCase(c));
                capitalizeNext = false;
            } else {
                result.append(c);
                capitalizeNext = c == ' ' || c == '/' || c == '-' || c == '(' || c == '.';
            }
        }
        return result.toString();
    }

    private String nomeTarifa(String fareId) {
        boolean transbordo = fareId.toLowerCase().contains("transbordo");
        String base;
        if (fareId.contains("zona_1!2")) base = "Coroa/Zona 1+2";
        else if (fareId.contains("zona_1")) base = "Coroa/Zona 1";
        else if (fareId.contains("zona_2")) base = "Coroa/Zona 2";
        else base = fareId;
        return transbordo ? base + " com transbordo" : base;
    }

    private String descricaoTarifa(String fareId, Integer transfers, Integer transferDuration) {
        StringBuilder descricao = new StringBuilder("Fonte GTFS: ").append(fareId);
        if (transfers != null && transfers > 0) descricao.append("; permite transbordo");
        if (transferDuration != null) descricao.append("; validade ").append(transferDuration / 60).append(" min");
        return descricao.toString();
    }

    private String firstNonBlank(String first, String second) {
        if (!isBlank(first)) return clean(first);
        return clean(second);
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Double parseDouble(String value) {
        if (isBlank(value)) return null;
        return Double.parseDouble(value.trim());
    }

    private Integer parseInteger(String value) {
        if (isBlank(value)) return null;
        return Integer.parseInt(value.trim());
    }

    private BigDecimal parseBigDecimal(String value) {
        if (isBlank(value)) return BigDecimal.ZERO;
        return new BigDecimal(value.trim());
    }

    private Integer parseGtfsTimeToMinutes(String value) {
        if (isBlank(value)) return null;
        String[] parts = value.trim().split(":");
        if (parts.length < 2) return null;
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        return hours * 60 + minutes;
    }

    private record StopTimeRow(String tripId, String stopId, Integer sequence, Integer arrivalMinutes, Integer departureMinutes) {
        Integer effectiveTimeMinutes() {
            return departureMinutes != null ? departureMinutes : arrivalMinutes;
        }
    }

    private record ShapePointRow(String shapeId, Double latitude, Double longitude, Integer sequence) {}

    private record TripCandidate(String routeId, String tripId, String sentido, String shapeId, List<StopTimeRow> stopTimes) {
        List<StopTimeRow> sortedStopTimes() {
            return stopTimes.stream()
                    .sorted(Comparator.comparing(StopTimeRow::sequence))
                    .toList();
        }

        boolean isBetterThan(TripCandidate other) {
            int thisStops = stopTimes.size();
            int otherStops = other.stopTimes.size();
            if (thisStops != otherStops) return thisStops > otherStops;
            return duration() > other.duration();
        }

        int duration() {
            List<StopTimeRow> ordered = sortedStopTimes();
            Integer first = null;
            Integer last = null;
            for (StopTimeRow row : ordered) {
                Integer minutes = row.effectiveTimeMinutes();
                if (minutes == null) continue;
                if (first == null) first = minutes;
                last = minutes;
            }
            if (first == null || last == null) return 0;
            return Math.max(0, last - first);
        }
    }
}
