package com.example.loginapi.service;

import com.example.loginapi.service.clientes.ContaService;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.Conta;
import com.example.loginapi.model.pagamentos.MovimentoConta;
import com.example.loginapi.model.pagamentos.enums.TipoMovimentoConta;
import com.example.loginapi.repository.pagamentos.ContaRepository;
import com.example.loginapi.repository.pagamentos.MovimentoContaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaServiceTest {

    @Mock private ContaRepository contaRepo;
    @Mock private MovimentoContaRepository movimentoRepo;
    @InjectMocks private ContaService contaService;

    private Cliente cliente;
    private Conta conta;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();

        conta = new Conta();
        conta.setCliente(cliente);
        conta.setSaldo(BigDecimal.ZERO);
    }

    // ─── Criação automática ───────────────────────────────────────────────────

    @Test
    @DisplayName("Deve criar conta automaticamente se não existir")
    void criarContaAutomaticamente() {
        when(contaRepo.findByCliente(cliente)).thenReturn(Optional.empty());
        when(contaRepo.save(any(Conta.class))).thenAnswer(i -> i.getArgument(0));

        Conta resultado = contaService.obterOuCriarConta(cliente);

        assertNotNull(resultado);
        assertEquals(BigDecimal.ZERO, resultado.getSaldo());
        verify(contaRepo).save(any(Conta.class));
    }

    @Test
    @DisplayName("Deve devolver conta existente sem criar nova")
    void devolveCotaExistente() {
        conta.setSaldo(new BigDecimal("10.00"));
        when(contaRepo.findByCliente(cliente)).thenReturn(Optional.of(conta));

        Conta resultado = contaService.obterOuCriarConta(cliente);

        assertEquals(new BigDecimal("10.00"), resultado.getSaldo());
        verify(contaRepo, never()).save(any());
    }

    // ─── Crédito ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Creditar deve aumentar o saldo e registar movimento")
    void creditarSaldo() {
        conta.setSaldo(new BigDecimal("5.00"));
        when(contaRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movimentoRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        MovimentoConta mov = contaService.creditar(conta, new BigDecimal("3.00"),
                TipoMovimentoConta.CARREGAMENTO, "Teste crédito", null);

        assertEquals(new BigDecimal("8.00"), conta.getSaldo());
        assertEquals(new BigDecimal("5.00"), mov.getSaldoAntes());
        assertEquals(new BigDecimal("8.00"), mov.getSaldoDepois());
        assertEquals(new BigDecimal("3.00"), mov.getValor());
        assertEquals(TipoMovimentoConta.CARREGAMENTO, mov.getTipo());
    }

    // ─── Débito ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Debitar com saldo suficiente deve reduzir o saldo e registar movimento")
    void debitarComSaldoSuficiente() {
        conta.setSaldo(new BigDecimal("10.00"));
        when(contaRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movimentoRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        MovimentoConta mov = contaService.debitar(conta, new BigDecimal("4.00"),
                TipoMovimentoConta.COMPRA_BILHETE, "Compra bilhete", null);

        assertEquals(new BigDecimal("6.00"), conta.getSaldo());
        assertEquals(new BigDecimal("10.00"), mov.getSaldoAntes());
        assertEquals(new BigDecimal("6.00"), mov.getSaldoDepois());
        assertEquals(new BigDecimal("4.00"), mov.getValor());
    }

    @Test
    @DisplayName("Debitar com saldo insuficiente deve lançar exceção e não alterar saldo")
    void debitarComSaldoInsuficiente() {
        conta.setSaldo(new BigDecimal("2.00"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> contaService.debitar(conta, new BigDecimal("5.00"),
                        TipoMovimentoConta.COMPRA_BILHETE, "Compra bilhete", null));

        assertTrue(ex.getMessage().contains("Saldo insuficiente"));
        assertEquals(new BigDecimal("2.00"), conta.getSaldo());
        verify(contaRepo, never()).save(any());
        verify(movimentoRepo, never()).save(any());
    }

    // ─── Integridade saldoAntes/saldoDepois ──────────────────────────────────

    @Test
    @DisplayName("saldoAntes e saldoDepois devem reflectir estado antes e depois da operação")
    void movimentoRegistaSaldosCorretos() {
        conta.setSaldo(new BigDecimal("20.00"));
        when(contaRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movimentoRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        MovimentoConta mov = contaService.creditar(conta, new BigDecimal("7.50"),
                TipoMovimentoConta.CARREGAMENTO, "Carregamento teste", null);

        assertEquals(new BigDecimal("20.00"), mov.getSaldoAntes());
        assertEquals(new BigDecimal("27.50"), mov.getSaldoDepois());
        assertEquals(mov.getSaldoDepois(), conta.getSaldo());
    }
}
