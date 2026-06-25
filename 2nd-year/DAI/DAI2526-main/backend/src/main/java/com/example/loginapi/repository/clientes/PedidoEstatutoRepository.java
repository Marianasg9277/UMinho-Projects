package com.example.loginapi.repository.clientes;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.clientes.PedidoEstatuto;
import com.example.loginapi.model.clientes.enums.EstadoPedidoEstatuto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import com.example.loginapi.model.clientes.Utilizador;


public interface PedidoEstatutoRepository extends JpaRepository<PedidoEstatuto, Long> {

    /** Pedidos do utilizador, mais recentes primeiro. */
    List<PedidoEstatuto> findByClienteOrderByCriadoEmDesc(Cliente cliente);

    /** Pedidos num determinado estado (para backoffice). */
    List<PedidoEstatuto> findByEstadoOrderByCriadoEmAsc(EstadoPedidoEstatuto estado);

    /** Pedidos em estados que requerem revisão (PENDING_APPROVAL, UNDER_REVIEW). */
    List<PedidoEstatuto> findByEstadoInOrderByCriadoEmAsc(List<EstadoPedidoEstatuto> estados);

    // ── Queries com JOIN FETCH para evitar LazyInitializationException ─────────

    /**
     * Todos os pedidos com cliente + utilizador carregados, mais recentes primeiro.
     * Usado pelo backoffice (admin) para evitar sessão lazy fora de contexto.
     */
    @Query("SELECT DISTINCT p FROM PedidoEstatuto p " +
           "JOIN FETCH p.cliente c " +
           "JOIN FETCH c.utilizador " +
           "LEFT JOIN FETCH p.documentos " +
           "ORDER BY p.criadoEm DESC")
    List<PedidoEstatuto> findAllComClienteOrderByDesc();

    /**
     * Pedidos filtrados por estado com JOIN FETCH.
     */
    @Query("SELECT DISTINCT p FROM PedidoEstatuto p " +
           "JOIN FETCH p.cliente c " +
           "JOIN FETCH c.utilizador " +
           "LEFT JOIN FETCH p.documentos " +
           "WHERE p.estado = :estado " +
           "ORDER BY p.criadoEm ASC")
    List<PedidoEstatuto> findComClienteByEstado(@Param("estado") EstadoPedidoEstatuto estado);

    /**
     * Pedido por ID com JOIN FETCH.
     */
    @Query("SELECT p FROM PedidoEstatuto p " +
           "JOIN FETCH p.cliente c " +
           "JOIN FETCH c.utilizador " +
           "LEFT JOIN FETCH p.documentos " +
           "WHERE p.id = :id")
    Optional<PedidoEstatuto> findByIdComCliente(@Param("id") Long id);

    /** Todos os pedidos, mais recentes primeiro (simples, sem fetch). */
    List<PedidoEstatuto> findAllByOrderByCriadoEmDesc();

    /** Contagem de pedidos pendentes — badge do backoffice. */
    @Query("SELECT COUNT(p) FROM PedidoEstatuto p WHERE p.estado IN :estados")
    long countByEstadoIn(@Param("estados") List<EstadoPedidoEstatuto> estados);

    /** Atalho para contar pedidos em PENDING_APPROVAL + UNDER_REVIEW. */
    default long countPedidosPendentes() {
        return countByEstadoIn(
            List.of(EstadoPedidoEstatuto.PENDING_APPROVAL, EstadoPedidoEstatuto.UNDER_REVIEW)
        );
    }
}
