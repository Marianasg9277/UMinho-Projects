package com.example.loginapi.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.loginapi.model.Cliente;
import com.example.loginapi.model.Utilizador;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUtilizador(Utilizador utilizador);
}
