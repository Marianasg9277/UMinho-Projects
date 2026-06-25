package com.example.loginapi.repository.infraestrutura;

import com.example.loginapi.model.infraestrutura.RotaLinhaPonto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import com.example.loginapi.model.infraestrutura.Linha;


@Repository
public interface RotaLinhaPontoRepository extends JpaRepository<RotaLinhaPonto, Long> {

    /** Devolve todos os pontos de uma rota, ordenados pela coluna 'ordem'. */
    List<RotaLinhaPonto> findByLinhaIdAndSentidoOrderByOrdemAsc(Long linhaId, String sentido);

    /** Apaga apenas os pontos da mesma linha/sentido (para reimportação). */
    @Transactional
    void deleteByLinhaIdAndSentido(Long linhaId, String sentido);

    /** Apaga todos os pontos GTFS de uma linha antes de reimportar shapes. */
    @Transactional
    void deleteByLinhaId(Long linhaId);
}
