package com.example.loginapi.repository.infraestrutura;

import com.example.loginapi.model.infraestrutura.Paragem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParagemRepository extends JpaRepository<Paragem, Long> {
    Optional<Paragem> findByGtfsStopId(String gtfsStopId);
    List<Paragem> findAllByAtivo(Boolean ativo);
    boolean existsByGtfsStopIdAndIdNot(String gtfsStopId, Long id);
}
