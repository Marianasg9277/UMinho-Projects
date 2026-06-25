package com.example.loginapi.repository.titulos;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface TransacaoRepository extends JpaRepository<Transacao, Long> {
    List<Transacao> findByClienteOrderByDataCompraDesc(Cliente cliente);
    List<Transacao> findByClienteAndEstadoPagamentoOrderByDataCompraDesc(Cliente cliente, EstadoPagamento estadoPagamento);
    List<Transacao> findByClienteAndEstadoPagamentoInOrderByDataCompraDesc(Cliente cliente, List<EstadoPagamento> estados);
    Optional<Transacao> findByCodigoQr(String codigoQr);
    List<Transacao> findByClienteAndEstadoPagamentoNotOrderByDataCompraDesc(Cliente cliente, EstadoPagamento estadoPagamento);
}
