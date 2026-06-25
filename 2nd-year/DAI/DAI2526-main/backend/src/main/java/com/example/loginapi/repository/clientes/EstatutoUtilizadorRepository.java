package com.example.loginapi.repository.clientes;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.clientes.EstatutoUtilizador;
import com.example.loginapi.model.clientes.enums.EstadoEstatutoUtilizador;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import com.example.loginapi.model.clientes.Utilizador;


public interface EstatutoUtilizadorRepository extends JpaRepository<EstatutoUtilizador, Long> {

    /** Estatuto ativo atual do utilizador (deve haver no máximo um). */
    Optional<EstatutoUtilizador> findByClienteAndEstado(Cliente cliente, EstadoEstatutoUtilizador estado);

    /** Todos os estatutos (histórico) de um utilizador. */
    List<EstatutoUtilizador> findByClienteOrderByCriadoEmDesc(Cliente cliente);

    /** Verifica se já existe estatuto ativo de um tipo específico. */
    Optional<EstatutoUtilizador> findByClienteAndTipoEstatutoAndEstado(
            Cliente cliente, TipoEstatuto tipoEstatuto, EstadoEstatutoUtilizador estado);
}
