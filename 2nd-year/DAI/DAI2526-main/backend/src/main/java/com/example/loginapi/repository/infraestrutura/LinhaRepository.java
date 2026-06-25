package com.example.loginapi.repository.infraestrutura;

import com.example.loginapi.model.infraestrutura.Linha;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LinhaRepository extends JpaRepository<Linha, Long> {
    Optional<Linha> findByNumero(String numero);
    Optional<Linha> findByGtfsRouteId(String gtfsRouteId);

    List<Linha> findByAtivoTrueOrderByNumeroAsc();
    List<Linha> findByAtivoOrderByNumeroAsc(boolean ativo);
    List<Linha> findAllByOrderByNumeroAsc();

    boolean existsByNumeroAndIdNot(String numero, Long id);
}
