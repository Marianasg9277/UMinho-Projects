package com.example.loginapi.service;

import com.example.loginapi.service.pagamentos.SaldoCompraService;

import com.example.loginapi.service.titulos.PasseService;

import com.example.loginapi.service.titulos.CompraPasseService;

import com.example.loginapi.service.pagamentos.PagamentoService;

import com.example.loginapi.service.clientes.ContaService;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.Conta;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.pagamentos.enums.TipoMovimentoConta;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaldoCompraServiceTest {

    @Mock private ContaService contaService;
    @Mock private PagamentoService pagamentoService;
    @Mock private CompraPasseService compraPasseService;
    @Mock private PasseService passeService;

    @InjectMocks private SaldoCompraService saldoCompraService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(saldoCompraService, "compraPasseService", compraPasseService);
    }

    // ─── Bilhete ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Bilhete com saldo suficiente deve debitar e confirmar pagamento")
    void bilheteComSaldoSuficiente_debitaEConfirma() {
        Cliente cliente = new Cliente();

        Transacao transacao = new Transacao();
        transacao.setPreco(new BigDecimal("1.50"));

        Conta conta = new Conta();
        conta.setSaldo(new BigDecimal("10.00"));

        Transacao resultado = new Transacao();
        resultado.setPreco(new BigDecimal("1.50"));

        when(contaService.obterOuCriarConta(cliente)).thenReturn(conta);
        when(pagamentoService.confirmarPagamentoTransacao(transacao, "SALDO_CONTA")).thenReturn(resultado);

        Transacao retorno = saldoCompraService.pagarBilheteComSaldo(cliente, transacao);

        assertSame(resultado, retorno);
        verify(contaService).debitar(eq(conta), eq(new BigDecimal("1.50")),
                eq(TipoMovimentoConta.COMPRA_BILHETE), anyString(), isNull());
        verify(pagamentoService).confirmarPagamentoTransacao(transacao, "SALDO_CONTA");
    }

    @Test
    @DisplayName("Bilhete com saldo insuficiente deve lançar exceção sem debitar")
    void bilheteComSaldoInsuficiente_lancaExcecao() {
        Cliente cliente = new Cliente();

        Transacao transacao = new Transacao();
        transacao.setPreco(new BigDecimal("5.00"));

        Conta conta = new Conta();
        conta.setSaldo(new BigDecimal("1.00"));

        when(contaService.obterOuCriarConta(cliente)).thenReturn(conta);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> saldoCompraService.pagarBilheteComSaldo(cliente, transacao));

        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        verify(contaService, never()).debitar(any(), any(), any(), any(), any());
        verify(pagamentoService, never()).confirmarPagamentoTransacao(any(), any());
    }

    // ─── Passe ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Passe com saldo suficiente deve debitar e confirmar pagamento")
    void passeComSaldoSuficiente_debitaESimula() {
        Cliente cliente = new Cliente();
        Long passeId = 7L;

        Passe passe = new Passe();
        passe.setPrecoAplicado(new BigDecimal("25.00"));

        Conta conta = new Conta();
        conta.setSaldo(new BigDecimal("50.00"));

        Pagamento pagamento = new Pagamento();

        when(passeService.obterPasse(passeId)).thenReturn(Optional.of(passe));
        when(contaService.obterOuCriarConta(cliente)).thenReturn(conta);
        when(compraPasseService.simularPagamento(passeId, "SALDO_CONTA")).thenReturn(pagamento);

        Pagamento retorno = saldoCompraService.pagarPasseComSaldo(cliente, passeId);

        assertSame(pagamento, retorno);
        verify(contaService).debitar(eq(conta), eq(new BigDecimal("25.00")),
                eq(TipoMovimentoConta.COMPRA_PASSE), anyString(), isNull());
        verify(compraPasseService).simularPagamento(passeId, "SALDO_CONTA");
    }

    @Test
    @DisplayName("Passe com saldo insuficiente deve lançar exceção sem debitar")
    void passeComSaldoInsuficiente_lancaExcecao() {
        Cliente cliente = new Cliente();
        Long passeId = 7L;

        Passe passe = new Passe();
        passe.setPrecoAplicado(new BigDecimal("25.00"));

        Conta conta = new Conta();
        conta.setSaldo(new BigDecimal("5.00"));

        when(passeService.obterPasse(passeId)).thenReturn(Optional.of(passe));
        when(contaService.obterOuCriarConta(cliente)).thenReturn(conta);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> saldoCompraService.pagarPasseComSaldo(cliente, passeId));

        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        verify(contaService, never()).debitar(any(), any(), any(), any(), any());
        verify(compraPasseService, never()).simularPagamento(any(), any());
    }
}
