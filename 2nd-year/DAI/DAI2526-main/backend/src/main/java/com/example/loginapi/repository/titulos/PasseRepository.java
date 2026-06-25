package com.example.loginapi.repository.titulos;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.titulos.Passe;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PasseRepository extends JpaRepository<Passe, Long> {
    List<Passe> findByClienteOrderByCriadoEmDesc(Cliente cliente);
}
