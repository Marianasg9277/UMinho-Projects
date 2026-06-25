package com.example.loginapi.repository.comunicacao;

import com.example.loginapi.model.comunicacao.Notificacao;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.example.loginapi.model.clientes.Utilizador;


public interface NotificacaoRepository extends JpaRepository<Notificacao, Long> {
    /** Notificações do utilizador + notificações broadcast (destino null) */
    List<Notificacao> findByUtilizadorDestinoOrUtilizadorDestinoIsNullOrderByDataCriacaoDesc(String email);
    List<Notificacao> findByUtilizadorDestinoAndLidaFalseOrUtilizadorDestinoIsNullAndLidaFalseOrderByDataCriacaoDesc(String email);
    long countByUtilizadorDestinoAndLidaFalse(String email);
    long countByUtilizadorDestinoIsNullAndLidaFalse();
}
