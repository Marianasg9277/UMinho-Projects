package com.example.loginapi;

import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.TipoPasse;
import com.example.loginapi.model.titulos.enums.EstadoComercialPasse;
import com.example.loginapi.model.titulos.enums.EstadoOperacionalPasse;
import com.example.loginapi.repository.clientes.ClienteRepository;
import com.example.loginapi.repository.pagamentos.PagamentoRepository;
import com.example.loginapi.repository.titulos.PasseQrTokenRepository;
import com.example.loginapi.repository.titulos.PasseRepository;
import com.example.loginapi.service.comunicacao.NotificacaoService;
import com.example.loginapi.service.titulos.PasseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para validação de estado operacional de passes.
 *
 * Cobre:
 * - Passe ACTIVE (pago e dentro do período)
 * - Passe expirado → FALTA_RENOVAR
 * - Passe CANCELLED → INVALIDO
 * - Passe PENDING_PAYMENT → INACTIVE
 */
@ExtendWith(MockitoExtension.class)
class PasseValidacaoTest {

    @Mock private PasseRepository passeRepo;
    @Mock private PagamentoRepository pagamentoRepo;
    @Mock private PasseQrTokenRepository qrTokenRepo;
    @Mock private ClienteRepository clienteRepo;
    @Mock private NotificacaoService notificacaoService;

    @InjectMocks
    private PasseService passeService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passeService, "diasAvisoExpiracao", 7);
    }

    private Passe criarPasseBase() {
        TipoPasse tipo = new TipoPasse();
        tipo.setNome("Mensal");
        tipo.setDuracaoDias(30);

        Passe passe = new Passe();
        passe.setTipoPasse(tipo);
        passe.setEstadoComercial(EstadoComercialPasse.PAID);
        passe.setEstadoOperacional(EstadoOperacionalPasse.ACTIVE);
        passe.setDataInicio(LocalDate.now().minusDays(5));
        passe.setDataFim(LocalDate.now().plusDays(25));
        return passe;
    }

    @Test
    @DisplayName("Passe pago e dentro do prazo deve ser ACTIVE")
    void testPasseAtivo() {
        Passe passe = criarPasseBase();

        Passe resultado = passeService.atualizarEstadoOperacionalSeNecessario(passe);

        assertEquals(EstadoOperacionalPasse.ACTIVE, resultado.getEstadoOperacional());
        assertTrue(passeService.estaValido(resultado));
    }

    @Test
    @DisplayName("Passe pago mas com data fim ultrapassada deve ser FALTA_RENOVAR")
    void testPasseExpirado() {
        Passe passe = criarPasseBase();
        passe.setDataFim(LocalDate.now().minusDays(1)); // expirou ontem
        when(passeRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        Passe resultado = passeService.atualizarEstadoOperacionalSeNecessario(passe);

        assertEquals(EstadoOperacionalPasse.FALTA_RENOVAR, resultado.getEstadoOperacional());
        assertFalse(passeService.estaValido(resultado));
    }

    @Test
    @DisplayName("Passe cancelado deve ser INVALIDO")
    void testPasseCancelado() {
        Passe passe = criarPasseBase();
        passe.setEstadoComercial(EstadoComercialPasse.CANCELLED);
        passe.setEstadoOperacional(EstadoOperacionalPasse.INVALIDO);

        Passe resultado = passeService.atualizarEstadoOperacionalSeNecessario(passe);

        assertEquals(EstadoOperacionalPasse.INVALIDO, resultado.getEstadoOperacional());
        assertFalse(passeService.estaValido(resultado));
    }

    @Test
    @DisplayName("Passe pendente de pagamento deve ser INACTIVE")
    void testPassePendente() {
        Passe passe = criarPasseBase();
        passe.setEstadoComercial(EstadoComercialPasse.PENDING_PAYMENT);
        passe.setEstadoOperacional(EstadoOperacionalPasse.INACTIVE);

        Passe resultado = passeService.atualizarEstadoOperacionalSeNecessario(passe);

        assertEquals(EstadoOperacionalPasse.INACTIVE, resultado.getEstadoOperacional());
        assertFalse(passeService.estaValido(resultado));
    }

    @Test
    @DisplayName("estaValido deve retornar false para passe com data de início no futuro")
    void testPasseNaoIniciadoNaoEhValido() {
        Passe passe = criarPasseBase();
        passe.setDataInicio(LocalDate.now().plusDays(1)); // ainda não começou
        passe.setDataFim(LocalDate.now().plusDays(31));

        assertFalse(passeService.estaValido(passe));
    }
}
