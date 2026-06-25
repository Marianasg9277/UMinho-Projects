package com.example.loginapi.repository.titulos;

import com.example.loginapi.model.titulos.HistoricoValidacao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.titulos.Passe;


public interface HistoricoValidacaoRepository extends JpaRepository<HistoricoValidacao, Long> {

    @Query("SELECT h FROM HistoricoValidacao h WHERE h.cliente.id = :clienteId ORDER BY h.dataValidacao DESC")
    List<HistoricoValidacao> findAllByClienteIdOrderByDataValidacaoDesc(
            @Param("clienteId") Long clienteId
    );

    @Query("SELECT h.transacao.id FROM HistoricoValidacao h WHERE h.cliente.id = :clienteId AND h.transacao IS NOT NULL")
    List<Long> findTransacaoIdsByClienteId(@Param("clienteId") Long clienteId);

    @Query("SELECT h.passe.id FROM HistoricoValidacao h WHERE h.cliente.id = :clienteId AND h.passe IS NOT NULL")
    List<Long> findPasseIdsByClienteId(@Param("clienteId") Long clienteId);

    List<HistoricoValidacao> findTop200ByOrderByDataValidacaoDesc();

    // ── Métodos para Fase 2 (Validar TT / Histórico filtrado) ─────────────────

    @Query("SELECT h FROM HistoricoValidacao h WHERE h.cliente.id = :clienteId AND h.sucesso = true ORDER BY h.dataValidacao DESC")
    List<HistoricoValidacao> findAllByClienteIdAndSucessoTrueOrderByDataValidacaoDesc(
            @Param("clienteId") Long clienteId
    );

    List<HistoricoValidacao> findTop5ByPasseIdAndSucessoTrueOrderByDataValidacaoDesc(Long passeId);

    List<HistoricoValidacao> findTop5ByTransacaoIdAndSucessoTrueOrderByDataValidacaoDesc(Long transacaoId);

    // ── Métodos para Fiscalização QR ──────────────────────────────────────────

    @Query("SELECT h FROM HistoricoValidacao h WHERE h.passe.id = :passeId AND h.linha.id = :linhaId AND h.sucesso = true ORDER BY h.dataValidacao DESC")
    List<HistoricoValidacao> findSuccessfulByPasseIdAndLinhaId(
            @Param("passeId") Long passeId, @Param("linhaId") Long linhaId);

    @Query("SELECT h FROM HistoricoValidacao h WHERE h.transacao.id = :transacaoId AND h.linha.id = :linhaId AND h.sucesso = true ORDER BY h.dataValidacao DESC")
    List<HistoricoValidacao> findSuccessfulByTransacaoIdAndLinhaId(
            @Param("transacaoId") Long transacaoId, @Param("linhaId") Long linhaId);

    List<HistoricoValidacao> findTop5ByPasseIdOrderByDataValidacaoDesc(Long passeId);

    List<HistoricoValidacao> findTop5ByTransacaoIdOrderByDataValidacaoDesc(Long transacaoId);
}
