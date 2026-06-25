package com.example.loginapi.repository.frota;

import com.example.loginapi.model.frota.AutocarroUltimaPosicao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.frota.Autocarro;


public interface AutocarroUltimaPosicaoRepository extends JpaRepository<AutocarroUltimaPosicao, Long> {

    Optional<AutocarroUltimaPosicao> findByAutocarroId(Long autocarroId);

    @Query("SELECT p FROM AutocarroUltimaPosicao p " +
           "JOIN FETCH p.autocarro a " +
           "JOIN FETCH p.linha l " +
           "WHERE a.ativo = true")
    List<AutocarroUltimaPosicao> findAllAtivas();

    @Query("SELECT p FROM AutocarroUltimaPosicao p " +
           "JOIN FETCH p.autocarro a " +
           "JOIN FETCH p.linha l " +
           "WHERE a.ativo = true AND l.id = :linhaId")
    List<AutocarroUltimaPosicao> findAllAtivasByLinhaId(@Param("linhaId") Long linhaId);
}
