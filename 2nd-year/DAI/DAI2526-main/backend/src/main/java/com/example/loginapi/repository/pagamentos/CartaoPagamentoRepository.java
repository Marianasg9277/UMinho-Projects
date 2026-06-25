package com.example.loginapi.repository.pagamentos;

import com.example.loginapi.model.pagamentos.CartaoPagamento;
import com.example.loginapi.model.clientes.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartaoPagamentoRepository extends JpaRepository<CartaoPagamento, Long> {

    List<CartaoPagamento> findByClienteAndAtivoTrueOrderByCriadoEmDesc(Cliente cliente);

    Optional<CartaoPagamento> findByIdAndCliente(Long id, Cliente cliente);

    long countByClienteAndAtivoTrue(Cliente cliente);

    @Modifying
    @Query("UPDATE CartaoPagamento c SET c.predefinido = false WHERE c.cliente = :cliente AND c.ativo = true")
    void limparPredefinidosDoCliente(@Param("cliente") Cliente cliente);
}
