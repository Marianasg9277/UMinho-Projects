package com.example.loginapi.repository.clientes;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.clientes.Utilizador;
import jakarta.persistence.LockModeType;
import com.example.loginapi.service.titulos.PasseService;


public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByUtilizador(Utilizador utilizador);
    Optional<Cliente> findByNif(String nif);
    Optional<Cliente> findByNumeroCartaoCidadao(String numeroCartaoCidadao);

    /**
     * Acquires a pessimistic write lock (SELECT ... FOR UPDATE) on the clientes row.
     * Used in PasseService.obterOuGerarQrToken() to serialize concurrent QR generation
     * for the same account, ensuring the "one active QR per account" rule holds under
     * concurrent requests.
     * To remove the concurrency guard in the future, delete the call-site in PasseService.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Cliente c WHERE c.id = :id")
    Optional<Cliente> findByIdForUpdate(@Param("id") Long id);
}