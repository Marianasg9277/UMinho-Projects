package com.example.loginapi.repository.pagamentos;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.Conta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ContaRepository extends JpaRepository<Conta, Long> {
    Optional<Conta> findByCliente(Cliente cliente);
}
