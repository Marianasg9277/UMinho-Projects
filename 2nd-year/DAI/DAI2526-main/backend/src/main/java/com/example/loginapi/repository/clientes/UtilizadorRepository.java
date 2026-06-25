package com.example.loginapi.repository.clientes;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.loginapi.model.clientes.Utilizador;

import java.util.Optional;

public interface UtilizadorRepository extends JpaRepository<Utilizador, Long> {
    Optional<Utilizador> findByEmail(String email);
}