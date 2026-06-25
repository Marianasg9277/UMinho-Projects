package com.example.loginapi.repository.pagamentos;

import com.example.loginapi.model.pagamentos.Conta;
import com.example.loginapi.model.pagamentos.MovimentoConta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimentoContaRepository extends JpaRepository<MovimentoConta, Long> {
    List<MovimentoConta> findByContaOrderByCriadoEmDesc(Conta conta);
}
