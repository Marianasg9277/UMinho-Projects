package com.example.loginapi.repository.pagamentos;

import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PagamentoRepository extends JpaRepository<Pagamento, Long> {
    Optional<Pagamento> findFirstByPasseOrderByCriadoEmDesc(Passe passe);
    List<Pagamento> findByEstado(EstadoPagamento estado);
}
