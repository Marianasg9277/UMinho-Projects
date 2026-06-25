package com.example.loginapi.repository.infraestrutura;

import com.example.loginapi.model.infraestrutura.Horario;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.loginapi.model.infraestrutura.Linha;


public interface HorarioRepository extends JpaRepository<Horario, Long> {

    /** Usado pelo endpoint público GET /api/autocarros. */
    List<Horario> findAllByOrderByMinutosAteAsc();

    /** Listagem admin geral: ordena por número de linha e depois por minutos. */
    @Query("SELECT h FROM Horario h ORDER BY h.linha.numero ASC, h.minutosAte ASC")
    List<Horario> findAllOrderedByLinhaAndMinutos();

    /** Listagem admin filtrada por linha, ordenada por minutos. */
    @Query("SELECT h FROM Horario h WHERE h.linha.id = :linhaId ORDER BY h.minutosAte ASC")
    List<Horario> findAllByLinhaIdOrdered(@Param("linhaId") Long linhaId);
}
