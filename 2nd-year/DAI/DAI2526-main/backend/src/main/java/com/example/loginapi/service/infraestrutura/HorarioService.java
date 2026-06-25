package com.example.loginapi.service.infraestrutura;

import com.example.loginapi.dto.HorarioRequest;
import com.example.loginapi.dto.HorarioResponse;
import com.example.loginapi.model.infraestrutura.Horario;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.repository.infraestrutura.HorarioRepository;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.loginapi.model.infraestrutura.Paragem;


@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepo;

    @Autowired
    private LinhaRepository linhaRepo;

    // ── UC4.6.1 Criar ─────────────────────────────────────────────────────────

    @Transactional
    public HorarioResponse criar(HorarioRequest req) {
        Linha linha = linhaRepo.findById(req.getLinhaId())
                .orElseThrow(() -> new NoSuchElementException("Linha não encontrada: id=" + req.getLinhaId()));

        String paragem = validarParagem(req.getParagem());

        Horario h = new Horario();
        h.setLinha(linha);
        h.setParagem(paragem);
        h.setMinutosAte(req.getMinutosAte());
        return toResponse(horarioRepo.save(h));
    }

    // ── UC4.6.2 Consultar ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<HorarioResponse> listar(Long linhaId) {
        List<Horario> horarios = linhaId != null
                ? horarioRepo.findAllByLinhaIdOrdered(linhaId)
                : horarioRepo.findAllOrderedByLinhaAndMinutos();
        return horarios.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<HorarioResponse> consultar(Long id) {
        return horarioRepo.findById(id).map(this::toResponse);
    }

    // ── UC4.6.3 Atualizar ─────────────────────────────────────────────────────

    @Transactional
    public Optional<HorarioResponse> atualizar(Long id, HorarioRequest req) {
        Optional<Horario> opt = horarioRepo.findById(id);
        if (opt.isEmpty()) return Optional.empty();

        Linha linha = linhaRepo.findById(req.getLinhaId())
                .orElseThrow(() -> new NoSuchElementException("Linha não encontrada: id=" + req.getLinhaId()));

        String paragem = validarParagem(req.getParagem());

        Horario h = opt.get();
        h.setLinha(linha);
        h.setParagem(paragem);
        h.setMinutosAte(req.getMinutosAte());
        return Optional.of(toResponse(horarioRepo.save(h)));
    }

    // ── UC4.6.4 Eliminar ─────────────────────────────────────────────────────

    @Transactional
    public boolean eliminar(Long id) {
        if (!horarioRepo.existsById(id)) return false;
        horarioRepo.deleteById(id);
        return true;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String validarParagem(String raw) {
        String trimmed = raw != null ? raw.trim() : "";
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException("A paragem não pode ser vazia.");
        }
        return trimmed;
    }

    private HorarioResponse toResponse(Horario h) {
        HorarioResponse r = new HorarioResponse();
        r.setId(h.getId());
        r.setLinhaId(h.getLinha().getId());
        r.setLinhaNumero(h.getLinha().getNumero());
        r.setLinhaNome(h.getLinha().getNome());
        r.setParagem(h.getParagem());
        r.setMinutosAte(h.getMinutosAte());
        return r;
    }
}
