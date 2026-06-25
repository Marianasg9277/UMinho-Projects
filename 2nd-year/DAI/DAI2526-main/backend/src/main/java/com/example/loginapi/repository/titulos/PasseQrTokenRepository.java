package com.example.loginapi.repository.titulos;

import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.PasseQrToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.service.titulos.PasseService;
import com.example.loginapi.model.pagamentos.Conta;


public interface PasseQrTokenRepository extends JpaRepository<PasseQrToken, Long> {

    Optional<PasseQrToken> findByToken(String token);

    @Query("SELECT t FROM PasseQrToken t WHERE t.passe = :passe AND t.revogadoEm IS NULL AND t.expiraEm > :agora ORDER BY t.expiraEm DESC")
    List<PasseQrToken> findActivosByPasse(@Param("passe") Passe passe, @Param("agora") Instant agora);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE PasseQrToken t SET t.revogadoEm = :agora WHERE t.passe = :passe AND t.revogadoEm IS NULL")
    void revogarTodosDoPass(@Param("passe") Passe passe, @Param("agora") Instant agora);

    /**
     * Revoga todos os tokens activos de passes da mesma conta que NÃO sejam o passe indicado.
     * Chamado ao gerar (ou reutilizar) um QR para garantir a regra temporária:
     * só um passe por conta pode ter QR activo de cada vez.
     * Para remover a restrição no futuro, basta apagar esta chamada em PasseService.
     */
    @Modifying(clearAutomatically = true)
    @Query("UPDATE PasseQrToken t SET t.revogadoEm = :agora " +
           "WHERE t.revogadoEm IS NULL " +
           "AND t.passe IN (" +
           "  SELECT p FROM Passe p WHERE p.cliente.id = :clienteId AND p.id <> :passeId" +
           ")")
    void revogarTokensDeOutrosPassesDaConta(
            @Param("clienteId") Long clienteId,
            @Param("passeId") Long passeId,
            @Param("agora") Instant agora);
}
