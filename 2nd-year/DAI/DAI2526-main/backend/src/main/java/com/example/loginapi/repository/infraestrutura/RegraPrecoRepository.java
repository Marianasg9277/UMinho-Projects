package com.example.loginapi.repository.infraestrutura;

import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.infraestrutura.RegraPreco;
import com.example.loginapi.model.titulos.TipoPasse;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RegraPrecoRepository extends JpaRepository<RegraPreco, Long> {

    /** Encontra a regra ativa vigente para a combinação exata. */
    @Query("SELECT r FROM RegraPreco r WHERE r.tipoEstatuto = :estatuto " +
           "AND r.tipoPasse = :tipoPasse AND r.coroa = :coroa " +
           "AND r.ativo = true AND r.dataInicioVigencia <= :data " +
           "AND (r.dataFimVigencia IS NULL OR r.dataFimVigencia >= :data) " +
           "ORDER BY r.dataInicioVigencia DESC")
    List<RegraPreco> findVigentes(@Param("estatuto") TipoEstatuto estatuto,
                                   @Param("tipoPasse") TipoPasse tipoPasse,
                                   @Param("coroa") Coroa coroa,
                                   @Param("data") LocalDate data);

    /** Todas as regras ativas vigentes para um dado estatuto (para listar opções). */
    @Query("SELECT r FROM RegraPreco r WHERE r.tipoEstatuto = :estatuto " +
           "AND r.ativo = true AND r.dataInicioVigencia <= :data " +
           "AND (r.dataFimVigencia IS NULL OR r.dataFimVigencia >= :data)")
    List<RegraPreco> findVigentesPorEstatuto(@Param("estatuto") TipoEstatuto estatuto,
                                              @Param("data") LocalDate data);

    /** Todas as regras ativas e vigentes (uso público — preçário). */
    @Query("SELECT r FROM RegraPreco r " +
           "WHERE r.ativo = true " +
           "AND r.dataInicioVigencia <= :data " +
           "AND (r.dataFimVigencia IS NULL OR r.dataFimVigencia >= :data) " +
           "AND r.tipoPasse IS NOT NULL AND r.coroa IS NOT NULL " +
           "ORDER BY r.tipoEstatuto ASC, r.tipoPasse.nome ASC, r.coroa.nome ASC")
    List<RegraPreco> findTodasVigentes(@Param("data") LocalDate data);

    /** Todas as regras (admin). */
    List<RegraPreco> findAllByOrderByCriadoEmDesc();
}
