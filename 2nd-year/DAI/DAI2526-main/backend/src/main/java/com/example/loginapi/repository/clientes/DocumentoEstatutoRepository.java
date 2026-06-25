package com.example.loginapi.repository.clientes;

import com.example.loginapi.model.clientes.DocumentoEstatuto;
import com.example.loginapi.model.clientes.PedidoEstatuto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DocumentoEstatutoRepository extends JpaRepository<DocumentoEstatuto, Long> {
    List<DocumentoEstatuto> findByPedido(PedidoEstatuto pedido);
}
