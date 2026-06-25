package com.example.loginapi.repository.infraestrutura;

import com.example.loginapi.model.infraestrutura.LinhaParagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import com.example.loginapi.model.infraestrutura.Paragem;
import com.example.loginapi.model.infraestrutura.Linha;


public interface LinhaParagemRepository extends JpaRepository<LinhaParagem, Long> {

    /**
     * Devolve o percurso ordenado de uma linha, com JOIN FETCH para evitar N+1.
     * Mantido para compatibilidade com código existente.
     */
    @Query("SELECT lp FROM LinhaParagem lp JOIN FETCH lp.paragem WHERE lp.linha.id = :linhaId ORDER BY lp.ordem ASC")
    List<LinhaParagem> findByLinhaIdOrderByOrdemAsc(@Param("linhaId") Long linhaId);

    /**
     * Devolve o percurso ordenado de uma linha para um sentido específico.
     */
    @Query("SELECT lp FROM LinhaParagem lp JOIN FETCH lp.paragem WHERE lp.linha.id = :linhaId AND lp.sentido = :sentido ORDER BY lp.ordem ASC")
    List<LinhaParagem> findByLinhaIdAndSentidoOrderByOrdemAsc(@Param("linhaId") Long linhaId, @Param("sentido") String sentido);

    /**
     * Remove o percurso simplificado existente da linha antes de importar o GTFS real.
     */
    @Modifying
    @Query("DELETE FROM LinhaParagem lp WHERE lp.linha.id = :linhaId")
    void deleteByLinhaId(@Param("linhaId") Long linhaId);

    /**
     * Remove todos os registos de uma paragem numa linha, independentemente do sentido.
     * Devolve o número de registos eliminados (0 se a associação não existia).
     */
    @Modifying
    @Query("DELETE FROM LinhaParagem lp WHERE lp.linha.id = :linhaId AND lp.paragem.id = :paragemId")
    int deleteByLinhaIdAndParagemId(@Param("linhaId") Long linhaId, @Param("paragemId") Long paragemId);

    /** Verifica duplicado para um sentido não-null. */
    boolean existsByLinhaIdAndParagemIdAndSentido(Long linhaId, Long paragemId, String sentido);

    /** Verifica duplicado quando sentido é null. */
    boolean existsByLinhaIdAndParagemIdAndSentidoIsNull(Long linhaId, Long paragemId);
}
