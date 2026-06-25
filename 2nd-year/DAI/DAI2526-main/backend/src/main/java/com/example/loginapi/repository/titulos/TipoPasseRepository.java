package com.example.loginapi.repository.titulos;

import com.example.loginapi.model.titulos.TipoPasse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipoPasseRepository extends JpaRepository<TipoPasse, Long> {
    List<TipoPasse> findByAtivoTrue();
}
