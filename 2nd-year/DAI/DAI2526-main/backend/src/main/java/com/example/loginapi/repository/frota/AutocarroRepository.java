package com.example.loginapi.repository.frota;

import com.example.loginapi.model.frota.Autocarro;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AutocarroRepository extends JpaRepository<Autocarro, Long> {

    Optional<Autocarro> findByCodigo(String codigo);
}
