package com.example.loginapi.service.infraestrutura;

import com.example.loginapi.dto.LinhaParagemRequest;
import com.example.loginapi.dto.LinhaParagemResponse;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.infraestrutura.LinhaParagem;
import com.example.loginapi.model.infraestrutura.Paragem;
import com.example.loginapi.repository.infraestrutura.LinhaParagemRepository;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import com.example.loginapi.repository.infraestrutura.ParagemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@Service
public class LinhaParagemService {

    @Autowired
    private LinhaParagemRepository linhaParagemRepo;

    @Autowired
    private LinhaRepository linhaRepo;

    @Autowired
    private ParagemRepository paragemRepo;

    private static final Set<String> SENTIDOS_VALIDOS = Set.of("IDA", "VOLTA");

    /**
     * UC4.4.1 — Associa uma paragem existente e ativa a uma linha.
     * Normaliza sentido (blank → null, lowercase → uppercase).
     * Recalcula Linha.numParagens após inserção.
     *
     * @throws NoSuchElementException  se linha ou paragem não existirem
     * @throws IllegalArgumentException se paragem inativa, sentido inválido ou duplicado
     */
    @Transactional
    public LinhaParagemResponse associarParagem(Long linhaId, LinhaParagemRequest dto) {
        Linha linha = linhaRepo.findById(linhaId)
                .orElseThrow(() -> new NoSuchElementException("Linha não encontrada: id=" + linhaId));

        Paragem paragem = paragemRepo.findById(dto.getParagemId())
                .orElseThrow(() -> new NoSuchElementException("Paragem não encontrada: id=" + dto.getParagemId()));

        if (!Boolean.TRUE.equals(paragem.getAtivo())) {
            throw new IllegalArgumentException("A paragem id=" + dto.getParagemId() + " está inativa.");
        }

        String sentido = normalizarSentido(dto.getSentido());

        if (sentido != null && !SENTIDOS_VALIDOS.contains(sentido)) {
            throw new IllegalArgumentException("Sentido inválido: '" + sentido + "'. Use IDA, VOLTA ou omita o campo.");
        }

        boolean duplicado = sentido == null
                ? linhaParagemRepo.existsByLinhaIdAndParagemIdAndSentidoIsNull(linhaId, paragem.getId())
                : linhaParagemRepo.existsByLinhaIdAndParagemIdAndSentido(linhaId, paragem.getId(), sentido);

        if (duplicado) {
            String sentidoDesc = sentido != null ? "sentido=" + sentido : "sentido=null";
            throw new IllegalStateException(
                    "Já existe associação entre linha " + linhaId + " e paragem " + paragem.getId()
                    + " com " + sentidoDesc + ".");
        }

        LinhaParagem lp = new LinhaParagem();
        lp.setLinha(linha);
        lp.setParagem(paragem);
        lp.setOrdem(dto.getOrdem());
        lp.setMinutosDesdeInicio(dto.getMinutosDesdeInicio());
        lp.setSentido(sentido);
        lp = linhaParagemRepo.save(lp);

        recalcularNumParagens(linha);

        return toResponse(lp, linha, paragem);
    }

    private String normalizarSentido(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return raw.trim().toUpperCase();
    }

    private LinhaParagemResponse toResponse(LinhaParagem lp, Linha linha, Paragem paragem) {
        LinhaParagemResponse r = new LinhaParagemResponse();
        r.setId(lp.getId());
        r.setLinhaId(linha.getId());
        r.setLinhaNumero(linha.getNumero());
        r.setLinhaNome(linha.getNome());
        r.setParagemId(paragem.getId());
        r.setParagemNome(paragem.getNome());
        r.setOrdem(lp.getOrdem());
        r.setMinutosDesdeInicio(lp.getMinutosDesdeInicio());
        r.setSentido(lp.getSentido());
        return r;
    }

    /**
     * UC4.4.5 — Remove todos os registos LinhaParagem para o par (linhaId, paragemId),
     * independentemente do sentido (IDA, VOLTA ou null).
     * Recalcula e persiste Linha.numParagens após a remoção.
     *
     * @return número de registos LinhaParagem eliminados
     * @throws NoSuchElementException se linha ou paragem não existirem
     * @throws IllegalStateException  se a associação não existir
     */
    @Transactional
    public int desassociarParagem(Long linhaId, Long paragemId) {
        Linha linha = linhaRepo.findById(linhaId)
                .orElseThrow(() -> new NoSuchElementException("Linha não encontrada: id=" + linhaId));

        if (!paragemRepo.existsById(paragemId)) {
            throw new NoSuchElementException("Paragem não encontrada: id=" + paragemId);
        }

        int removidos = linhaParagemRepo.deleteByLinhaIdAndParagemId(linhaId, paragemId);

        if (removidos == 0) {
            throw new IllegalStateException(
                    "Não existe associação entre a linha " + linhaId + " e a paragem " + paragemId + ".");
        }

        recalcularNumParagens(linha);

        return removidos;
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Recalcula Linha.numParagens após alteração do percurso.
     * Lógica: IDA se existir → VOLTA se existir → total restante sem filtro.
     */
    private void recalcularNumParagens(Linha linha) {
        List<LinhaParagem> ida = linhaParagemRepo
                .findByLinhaIdAndSentidoOrderByOrdemAsc(linha.getId(), "IDA");
        if (!ida.isEmpty()) {
            linha.setNumParagens(ida.size());
            linhaRepo.save(linha);
            return;
        }

        List<LinhaParagem> volta = linhaParagemRepo
                .findByLinhaIdAndSentidoOrderByOrdemAsc(linha.getId(), "VOLTA");
        if (!volta.isEmpty()) {
            linha.setNumParagens(volta.size());
            linhaRepo.save(linha);
            return;
        }

        // Retrocompatibilidade: dados sem campo sentido
        List<LinhaParagem> todos = linhaParagemRepo
                .findByLinhaIdOrderByOrdemAsc(linha.getId());
        linha.setNumParagens(todos.size());
        linhaRepo.save(linha);
    }
}
