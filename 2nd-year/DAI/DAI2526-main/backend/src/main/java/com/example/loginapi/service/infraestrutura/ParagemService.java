package com.example.loginapi.service.infraestrutura;

import com.example.loginapi.dto.ParagemRequest;
import com.example.loginapi.dto.ParagemResponse;
import com.example.loginapi.model.infraestrutura.Paragem;
import com.example.loginapi.repository.infraestrutura.ParagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ParagemService {

    @Autowired
    private ParagemRepository paragemRepo;

    // ── UC4.5.1 Criar ─────────────────────────────────────────────────────────

    @Transactional
    public ParagemResponse criar(ParagemRequest req) {
        validarCoordenadas(req.getLatitude(), req.getLongitude());
        String gtfsId = sanitizarGtfsStopId(req.getGtfsStopId());
        if (gtfsId != null && paragemRepo.findByGtfsStopId(gtfsId).isPresent()) {
            throw new IllegalStateException("Já existe uma paragem com gtfsStopId '" + gtfsId + "'.");
        }
        Paragem p = new Paragem();
        p.setNome(req.getNome().trim());
        p.setLatitude(req.getLatitude());
        p.setLongitude(req.getLongitude());
        p.setGtfsStopId(gtfsId);
        p.setZoneId(req.getZoneId() != null ? req.getZoneId().trim() : null);
        p.setAtivo(true);
        return toResponse(paragemRepo.save(p));
    }

    // ── UC4.5.2 Consultar ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ParagemResponse> listar(Boolean ativo) {
        List<Paragem> paragens = ativo != null
                ? paragemRepo.findAllByAtivo(ativo)
                : paragemRepo.findAll();
        return paragens.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ParagemResponse> consultar(Long id) {
        return paragemRepo.findById(id).map(this::toResponse);
    }

    // ── UC4.5.3 Atualizar ─────────────────────────────────────────────────────

    @Transactional
    public Optional<ParagemResponse> atualizar(Long id, ParagemRequest req) {
        Optional<Paragem> opt = paragemRepo.findById(id);
        if (opt.isEmpty()) return Optional.empty();

        validarCoordenadas(req.getLatitude(), req.getLongitude());
        String gtfsId = sanitizarGtfsStopId(req.getGtfsStopId());
        if (gtfsId != null && paragemRepo.existsByGtfsStopIdAndIdNot(gtfsId, id)) {
            throw new IllegalStateException("Já existe outra paragem com gtfsStopId '" + gtfsId + "'.");
        }

        Paragem p = opt.get();
        p.setNome(req.getNome().trim());
        p.setLatitude(req.getLatitude());
        p.setLongitude(req.getLongitude());
        // Preservar gtfsStopId existente se a edição manual não enviar o campo
        if (gtfsId != null) {
            p.setGtfsStopId(gtfsId);
        }
        p.setZoneId(req.getZoneId() != null ? req.getZoneId().trim() : null);
        // ativo não é alterado aqui — controlado exclusivamente pelo DELETE
        return Optional.of(toResponse(paragemRepo.save(p)));
    }

    // ── UC4.5.4 Eliminar (lógico) ─────────────────────────────────────────────

    @Transactional
    public boolean eliminar(Long id) {
        Optional<Paragem> opt = paragemRepo.findById(id);
        if (opt.isEmpty()) return false;
        Paragem p = opt.get();
        p.setAtivo(false);
        paragemRepo.save(p);
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void validarCoordenadas(Double lat, Double lon) {
        if (lat != null && (lat < -90.0 || lat > 90.0)) {
            throw new IllegalArgumentException("Latitude inválida. Deve estar entre -90 e 90.");
        }
        if (lon != null && (lon < -180.0 || lon > 180.0)) {
            throw new IllegalArgumentException("Longitude inválida. Deve estar entre -180 e 180.");
        }
    }

    private String sanitizarGtfsStopId(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return raw.trim();
    }

    private ParagemResponse toResponse(Paragem p) {
        ParagemResponse r = new ParagemResponse();
        r.setId(p.getId());
        r.setNome(p.getNome());
        r.setLatitude(p.getLatitude());
        r.setLongitude(p.getLongitude());
        r.setGtfsStopId(p.getGtfsStopId());
        r.setZoneId(p.getZoneId());
        r.setAtivo(p.getAtivo());
        return r;
    }
}
