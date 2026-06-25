package com.example.loginapi.repository.titulos;

import com.example.loginapi.model.titulos.TipoBilhete;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoBilheteRepository extends JpaRepository<TipoBilhete, Long> {
    Optional<TipoBilhete> findByGtfsFareId(String gtfsFareId);
    Optional<TipoBilhete> findByNome(String nome);

    List<TipoBilhete> findByAtivoTrueOrderByCategoriaAscNomeAsc();
    List<TipoBilhete> findByAtivoOrderByCategoriaAscNomeAsc(boolean ativo);
    List<TipoBilhete> findAllByOrderByCategoriaAscNomeAsc();
}
