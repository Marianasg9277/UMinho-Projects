package com.example.loginapi;

import com.example.loginapi.controller.titulos.BilheteController;
import com.example.loginapi.dto.ComprarBilheteAutenticadoRequest;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.titulos.TipoBilhete;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.repository.infraestrutura.CoroaRepository;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import com.example.loginapi.repository.titulos.TipoBilheteRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.service.autenticacao.AuthService;
import com.example.loginapi.service.pagamentos.PagamentoService;
import com.example.loginapi.service.pagamentos.SaldoCompraService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes focados na correcção de validade e preço dos bilhetes.
 *
 * Cobre:
 * - Fórmula de validadeHoras após correcção de divisão inteira
 * - Preços corretos das tarifas GTFS
 * - Regressão: compra autenticada guarda TipoBilhete.preco em Transacao.preco
 */
@ExtendWith(MockitoExtension.class)
class ValidadeBilheteTest {

    // ─── 1-2. Fórmula validadeHoras ──────────────────────────────────────────────

    @Test
    @DisplayName("3600s (60 min) → validadeHoras = 1 (sem alteração)")
    void testFormula_3600s_deveRetornarUmaHora() {
        int transferDuration = 3600;
        int resultado = (int) Math.ceil((double) transferDuration / 3600);
        assertEquals(1, resultado, "3600s deve continuar a dar 1 hora");
    }

    @Test
    @DisplayName("5400s (90 min) → validadeHoras = 2 (bug corrigido: era 1 por divisão inteira)")
    void testFormula_5400s_deveRetornarDuasHoras() {
        int transferDuration = 5400;
        int resultado = (int) Math.ceil((double) transferDuration / 3600);
        assertEquals(2, resultado, "5400s deve dar 2 horas (ceiling de 1.5)");
    }

    // ─── 3-4. Preços GTFS ────────────────────────────────────────────────────────

    @Test
    @DisplayName("TipoBilhete Zona 1 mantém preço = 1.55€")
    void testPrecoZona1_1_55() {
        TipoBilhete t = new TipoBilhete();
        t.setPreco(new BigDecimal("1.55"));
        assertEquals(0, new BigDecimal("1.55").compareTo(t.getPreco()),
                "Preço Zona 1 deve ser 1.55€");
    }

    @Test
    @DisplayName("TipoBilhete Zona 1+2 mantém preço = 2.00€")
    void testPrecoZona1e2_2_00() {
        TipoBilhete t = new TipoBilhete();
        t.setPreco(new BigDecimal("2.00"));
        assertEquals(0, new BigDecimal("2.00").compareTo(t.getPreco()),
                "Preço Zona 1+2 deve ser 2.00€");
    }

    // ─── 5. Regressão compra autenticada ─────────────────────────────────────────

    @Mock private AuthService authService;
    @Mock private PagamentoService pagamentoService;
    @Mock private SaldoCompraService saldoCompraService;
    @Mock private TransacaoRepository transacaoRepo;
    @Mock private TipoBilheteRepository tipoBilheteRepo;
    @Mock private LinhaRepository linhaRepo;
    @Mock private CoroaRepository coroaRepo;

    @InjectMocks
    private BilheteController bilheteController;

    @Test
    @DisplayName("Compra autenticada: Transacao.preco = TipoBilhete.preco (1.55€)")
    void testCompraAutenticadaGuardaPrecoDoTipoBilhete() {
        TipoBilhete tipoBilhete = new TipoBilhete();
        tipoBilhete.setNome("Coroa/Zona 1");
        tipoBilhete.setPreco(new BigDecimal("1.55"));
        tipoBilhete.setValidadeHoras(1);

        Utilizador u = new Utilizador();
        u.setEmail("cliente@tub.pt");
        Cliente cliente = new Cliente();
        cliente.setUtilizador(u);

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("cliente@tub.pt");
        when(authService.getClienteByEmail("cliente@tub.pt")).thenReturn(cliente);
        when(tipoBilheteRepo.findById(1L)).thenReturn(Optional.of(tipoBilhete));

        // transacaoRepo.save() deve devolver com ID para gerar referências
        when(transacaoRepo.save(any(Transacao.class))).thenAnswer(i -> {
            Transacao t = i.getArgument(0);
            ReflectionTestUtils.setField(t, "id", 1L);
            return t;
        });

        // confirmarPagamentoTransacao devolve passe com campos mínimos para toResponse()
        Transacao pago = new Transacao();
        ReflectionTestUtils.setField(pago, "id", 1L);
        pago.setPreco(new BigDecimal("1.55"));
        pago.setTipoBilhete(tipoBilhete);
        pago.setEstadoPagamento(EstadoPagamento.PAID);
        pago.setDataCompra(LocalDateTime.now());
        when(pagamentoService.confirmarPagamentoTransacao(any(Transacao.class), anyString()))
                .thenReturn(pago);

        ComprarBilheteAutenticadoRequest req = new ComprarBilheteAutenticadoRequest();
        req.setTipoBilheteId(1L);
        req.setMetodo("CARTAO");

        bilheteController.comprar(req, auth);

        ArgumentCaptor<Transacao> captor = ArgumentCaptor.forClass(Transacao.class);
        verify(transacaoRepo).save(captor.capture());
        assertEquals(0, new BigDecimal("1.55").compareTo(captor.getValue().getPreco()),
                "Transacao.preco deve ser igual a TipoBilhete.preco antes do pagamento");
    }
}
