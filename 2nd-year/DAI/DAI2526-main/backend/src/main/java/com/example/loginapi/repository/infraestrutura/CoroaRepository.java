package com.example.loginapi.repository.infraestrutura;

import com.example.loginapi.model.infraestrutura.Coroa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CoroaRepository extends JpaRepository<Coroa, Long> {
    List<Coroa> findByAtivoTrue();
    Optional<Coroa> findByNome(String nome);
}
