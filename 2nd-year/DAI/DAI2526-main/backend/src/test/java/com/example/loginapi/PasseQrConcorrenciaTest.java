package com.example.loginapi;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.TipoPasse;
import com.example.loginapi.model.titulos.PasseQrToken;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.infraestrutura.RegraPreco;
import com.example.loginapi.model.titulos.enums.EstadoComercialPasse;
import com.example.loginapi.model.titulos.enums.EstadoOperacionalPasse;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import com.example.loginapi.repository.clientes.UtilizadorRepository;
import com.example.loginapi.repository.clientes.ClienteRepository;
import com.example.loginapi.repository.titulos.TipoPasseRepository;
import com.example.loginapi.repository.infraestrutura.CoroaRepository;
import com.example.loginapi.repository.titulos.PasseRepository;
import com.example.loginapi.repository.titulos.PasseQrTokenRepository;
import com.example.loginapi.service.titulos.PasseService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Concurrency test: two simultaneous QR generation requests for different passes
 * of the same account must result in exactly ONE active token (not two).
 *
 * Requires a running PostgreSQL database (uses the real DB, not H2).
 * Run with: mvn test -Dtest=PasseQrConcorrenciaTest
 */
@Disabled("Requires live PostgreSQL database — run manually with DB connection")
@SpringBootTest
class PasseQrConcorrenciaTest {

    @Autowired private PasseService passeService;
    @Autowired private UtilizadorRepository utilizadorRepo;
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private TipoPasseRepository tipoPasseRepo;
    @Autowired private CoroaRepository coroaRepo;
    @Autowired private PasseRepository passeRepo;
    @Autowired private PasseQrTokenRepository qrTokenRepo;

    // IDs kept for cleanup
    private Long clienteId;
    private Long passeAId;
    private Long passeBId;
    private Long utilizadorId;
    private Long tipoPasseId;
    private Long coroaId;

    @BeforeEach
    void setup() {
        // Use random unique values to avoid conflicts with existing DB data
        String uid = UUID.randomUUID().toString().replace("-", "").substring(0, 8);

        Utilizador u = new Utilizador();
        u.setEmail("teste-concorrencia-" + uid + "@test.local");
        u.setPassword("hash-placeholder");
        u = utilizadorRepo.save(u);
        utilizadorId = u.getId();

        Cliente c = new Cliente();
        c.setUtilizador(u);
        c.setPerfil("NORMAL");
        c.setNome("Teste");
        c.setSobrenome("Concorrencia");
        c.setDataNascimento(LocalDate.of(1990, 1, 1));
        c.setMorada("Rua de Teste, 1");
        c.setNif("9" + uid); // NIF precisa de ter tipicamente 9 digitos, 'uid' tem 8
        c.setTelefone("912345678");
        c.setNumeroCartaoCidadao("CC-" + uid);
        c.setDigitoVerificacaoCartaoCidadao("0");
        c = clienteRepo.save(c);
        clienteId = c.getId();

        TipoPasse tp = new TipoPasse();
        tp.setNome("Mensal-teste-" + uid);
        tp.setDuracaoDias(30);
        tp.setAtivo(true);
        tp = tipoPasseRepo.save(tp);
        tipoPasseId = tp.getId();

        Coroa cr = new Coroa();
        cr.setNome("Coroa-teste-" + uid);
        cr.setAtivo(true);
        cr = coroaRepo.save(cr);
        coroaId = cr.getId();

        Passe a = buildPasseActivo(c, tp, cr, "PASSE-A-" + uid);
        a = passeRepo.save(a);
        passeAId = a.getId();

        Passe b = buildPasseActivo(c, tp, cr, "PASSE-B-" + uid);
        b = passeRepo.save(b);
        passeBId = b.getId();
    }

    @AfterEach
    void cleanup() {
        // Delete in FK-safe order: tokens → passes → cliente → utilizador → lookup tables
        if (passeAId != null) qrTokenRepo.findAll().stream()
                .filter(t -> t.getPasse().getId().equals(passeAId) || t.getPasse().getId().equals(passeBId))
                .map(PasseQrToken::getId)
                .forEach(qrTokenRepo::deleteById);
        if (passeAId != null) passeRepo.deleteById(passeAId);
        if (passeBId != null) passeRepo.deleteById(passeBId);
        if (clienteId != null) clienteRepo.deleteById(clienteId);
        if (utilizadorId != null) utilizadorRepo.deleteById(utilizadorId);
        if (tipoPasseId != null) tipoPasseRepo.deleteById(tipoPasseId);
        if (coroaId != null) coroaRepo.deleteById(coroaId);
    }

    // ── Test ─────────────────────────────────────────────────────────────────────

    @Test
    void duasChamadasSimultaneas_deveResultarEmApenasUmTokenActivo() throws Exception {
        Passe passeA = passeRepo.findById(passeAId).orElseThrow();
        Passe passeB = passeRepo.findById(passeBId).orElseThrow();

        // CyclicBarrier ensures both threads reach the starting line before either proceeds
        CyclicBarrier barrier = new CyclicBarrier(2);
        AtomicReference<Throwable> errorA = new AtomicReference<>();
        AtomicReference<Throwable> errorB = new AtomicReference<>();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS); // wait for both threads to be ready
                passeService.obterOuGerarQrToken(passeA);
            } catch (Throwable t) {
                errorA.set(t);
            }
        });

        executor.submit(() -> {
            try {
                barrier.await(5, TimeUnit.SECONDS);
                passeService.obterOuGerarQrToken(passeB);
            } catch (Throwable t) {
                errorB.set(t);
            }
        });

        executor.shutdown();
        boolean finished = executor.awaitTermination(15, TimeUnit.SECONDS);
        assertTrue(finished, "Threads did not complete within timeout");

        // Neither thread should have thrown
        assertNull(errorA.get(), "Thread A threw: " + errorA.get());
        assertNull(errorB.get(), "Thread B threw: " + errorB.get());

        // Count active tokens across both passes — must be exactly 1
        Instant now = Instant.now();
        List<PasseQrToken> activosA = qrTokenRepo.findActivosByPasse(passeA, now);
        List<PasseQrToken> activosB = qrTokenRepo.findActivosByPasse(passeB, now);
        int total = activosA.size() + activosB.size();

        assertEquals(1, total,
                "Expected exactly 1 active token across both passes, found " + total +
                " (passeA=" + activosA.size() + ", passeB=" + activosB.size() + ")");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────────

    private Passe buildPasseActivo(Cliente c, TipoPasse tp, Coroa cr, String codigoQr) {
        Passe p = new Passe();
        p.setCliente(c);
        p.setTipoPasse(tp);
        p.setCoroa(cr);
        p.setTipoEstatutoAplicado(TipoEstatuto.SEM_ESTATUTO);
        p.setPrecoAplicado(new BigDecimal("30.00"));
        p.setEstadoComercial(EstadoComercialPasse.PAID);
        p.setEstadoOperacional(EstadoOperacionalPasse.ACTIVE);
        p.setCodigoQr(codigoQr);
        p.setDataInicio(LocalDate.now());
        p.setDataFim(LocalDate.now().plusDays(30));
        return p;
    }
}
