package com.example.loginapi.service.frota;

import com.example.loginapi.dto.RotaLinhaImportRequest;
import com.example.loginapi.dto.RotaLinhaPontoResponse;
import com.example.loginapi.model.infraestrutura.RotaLinhaPonto;
import com.example.loginapi.repository.infraestrutura.RotaLinhaPontoRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import com.example.loginapi.model.infraestrutura.Linha;


@Service
public class RotaLinhaService {

    @Autowired
    private RotaLinhaPontoRepository repository;

    /**
     * Importa (ou reimporta) os pontos de uma rota.
     * Apaga os pontos anteriores da mesma linha/sentido e insere os novos.
     */
    @Transactional
    public int importarRota(RotaLinhaImportRequest request) {
        if (request.getLinhaId() == null || request.getSentido() == null) {
            throw new IllegalArgumentException("linhaId e sentido são obrigatórios");
        }
        if (request.getPontos() == null || request.getPontos().isEmpty()) {
            throw new IllegalArgumentException("Lista de pontos vazia");
        }

        // Apagar apenas a mesma linha/sentido
        repository.deleteByLinhaIdAndSentido(request.getLinhaId(), request.getSentido());

        // Inserir novos pontos
        List<RotaLinhaPonto> entidades = new ArrayList<>();
        int ordem = 0;
        for (RotaLinhaImportRequest.PontoDTO p : request.getPontos()) {
            entidades.add(new RotaLinhaPonto(
                    request.getLinhaId(),
                    request.getSentido(),
                    ordem++,
                    p.getLatitude(),
                    p.getLongitude()
            ));
        }
        repository.saveAll(entidades);
        return entidades.size();
    }

    /**
     * Devolve os pontos de uma rota ordenados por 'ordem'.
     * Retorna lista vazia se a rota não existir na BD.
     */
    public List<RotaLinhaPontoResponse> obterRota(Long linhaId, String sentido) {
        return repository.findByLinhaIdAndSentidoOrderByOrdemAsc(linhaId, sentido)
                .stream()
                .map(p -> new RotaLinhaPontoResponse(p.getOrdem(), p.getLatitude(), p.getLongitude()))
                .collect(Collectors.toList());
    }
}
