package com.example.loginapi.service;

import com.example.loginapi.service.comunicacao.EmailService;

import com.example.loginapi.service.pagamentos.FaturaService;

import com.example.loginapi.service.pagamentos.PagamentoService;

import com.example.loginapi.service.clientes.ContaService;

import com.example.loginapi.dto.CarregamentoSaldoRequest;
import com.example.loginapi.dto.CarregamentoSaldoResponse;
import com.example.loginapi.dto.ResultadoPagamentoSimulado;
import com.example.loginapi.model.pagamentos.CartaoPagamento;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.Conta;
import com.example.loginapi.model.pagamentos.MovimentoConta;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.repository.pagamentos.CartaoPagamentoRepository;
import com.example.loginapi.repository.pagamentos.ContaRepository;
import com.example.loginapi.repository.pagamentos.MovimentoContaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContaServiceCarregamentoTest {

    @Mock private ContaRepository contaRepo;
    @Mock private MovimentoContaRepository movimentoRepo;
    @Mock private PagamentoService pagamentoService;
    @Mock private CartaoPagamentoRepository cartaoRepo;
    @Mock private FaturaService faturaService;
    @Mock private EmailService emailService;

    @InjectMocks private ContaService contaService;

    // ─── Sucesso ──────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Carregamento aprovado deve creditar saldo e devolver novo saldo")
    void carregamentoAprovado_atualizaSaldo() {
        Cliente cliente = clienteComEmail("test@tub.pt");
        Conta conta = contaComSaldo("10.00");

        CarregamentoSaldoRequest req = new CarregamentoSaldoRequest();
        req.setValor(new BigDecimal("20.00"));
        req.setMetodoPagamento("MBWAY");
        req.setTelefone("912345678");
        req.setEmitirFaturaComNif(false);

        when(contaRepo.findByCliente(cliente)).thenReturn(Optional.of(conta));
        when(pagamentoService.simularPagamento("MBWAY"))
                .thenReturn(new ResultadoPagamentoSimulado(true, "MBWAY-999", "Aprovado."));
        when(contaRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(movimentoRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        when(pagamentoService.gerarNumeroFatura(any(), any())).thenReturn("FT-CG-TEST");

        CarregamentoSaldoResponse resp = contaService.processarCarregamento(cliente, req);

        assertEquals(new BigDecimal("30.00"), resp.getNovoSaldo());
        assertEquals("MBWAY-999", resp.getReferenciaExterna());
        assertEquals("FT-CG-TEST", resp.getFaturaNumero());
        assertEquals("Carregamento efetuado com sucesso.", resp.getMensagem());
        verify(contaRepo).save(any());
        verify(movimentoRepo).save(any());
    }

    // ─── Pagamento rejeitado ──────────────────────────────────────────────────

    @Test
    @DisplayName("Pagamento rejeitado não deve alterar saldo, criar movimento nem gerar fatura")
    void pagamentoRejeitado_naoAlteraSaldo() {
        Cliente cliente = clienteComEmail("test@tub.pt");
        Conta conta = contaComSaldo("10.00");

        CarregamentoSaldoRequest req = new CarregamentoSaldoRequest();
        req.setValor(new BigDecimal("20.00"));
        req.setMetodoPagamento("MBWAY");
        req.setTelefone("912345678");

        when(contaRepo.findByCliente(cliente)).thenReturn(Optional.of(conta));
        when(pagamentoService.simularPagamento("MBWAY"))
                .thenReturn(new ResultadoPagamentoSimulado(false, null,
                        "Pagamento recusado (falha simulada). Tente novamente."));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> contaService.processarCarregamento(cliente, req));

        assertTrue(ex.getMessage().contains("recusado"));
        assertEquals(new BigDecimal("10.00"), conta.getSaldo());
        verify(contaRepo, never()).save(any());
        verify(movimentoRepo, never()).save(any());
        verify(faturaService, never()).gerarHtmlFaturaCarregamento(any(), any(), any(), any(), any());
    }

    // ─── Validação NIF ────────────────────────────────────────────────────────

    @Test
    @DisplayName("Emitir fatura com NIF sem fornecer NIF deve lançar exceção antes do pagamento")
    void emitirFaturaComNif_semNif_lancaExcecao() {
        CarregamentoSaldoRequest req = new CarregamentoSaldoRequest();
        req.setValor(new BigDecimal("10.00"));
        req.setMetodoPagamento("MBWAY");
        req.setTelefone("912345678");
        req.setEmitirFaturaComNif(true);
        req.setNif(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> contaService.processarCarregamento(new Cliente(), req));

        assertTrue(ex.getMessage().contains("NIF"));
        verify(pagamentoService, never()).simularPagamento(any());
    }

    // ─── Validação MB Way ─────────────────────────────────────────────────────

    @Test
    @DisplayName("MB Way sem telefone deve lançar exceção antes do pagamento")
    void mbway_semTelefone_lancaExcecao() {
        CarregamentoSaldoRequest req = new CarregamentoSaldoRequest();
        req.setValor(new BigDecimal("10.00"));
        req.setMetodoPagamento("MBWAY");
        req.setTelefone(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> contaService.processarCarregamento(new Cliente(), req));

        assertTrue(ex.getMessage().toLowerCase().contains("mb way")
                || ex.getMessage().toLowerCase().contains("telefone"));
        verify(pagamentoService, never()).simularPagamento(any());
    }

    // ─── Validação cartão predefinido ─────────────────────────────────────────

    @Test
    @DisplayName("Cartão predefinido inexistente deve lançar exceção antes do pagamento")
    void cartaoPredefinido_inexistente_lancaExcecao() {
        Cliente cliente = new Cliente();
        CarregamentoSaldoRequest req = new CarregamentoSaldoRequest();
        req.setValor(new BigDecimal("10.00"));
        req.setMetodoPagamento("CARTAO_PREDEFINIDO");

        when(cartaoRepo.findByClienteAndAtivoTrueOrderByCriadoEmDesc(cliente)).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> contaService.processarCarregamento(cliente, req));

        assertTrue(ex.getMessage().contains("predefinido"));
        verify(pagamentoService, never()).simularPagamento(any());
    }

    // ─── Validação cartão específico ──────────────────────────────────────────

    @Test
    @DisplayName("Cartão específico que não pertence ao cliente deve lançar exceção antes do pagamento")
    void cartaoEspecifico_naoDoCliente_lancaExcecao() {
        Cliente cliente = new Cliente();
        CarregamentoSaldoRequest req = new CarregamentoSaldoRequest();
        req.setValor(new BigDecimal("10.00"));
        req.setMetodoPagamento("CARTAO_ESPECIFICO");
        req.setCartaoId(99L);

        when(cartaoRepo.findByIdAndCliente(99L, cliente)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> contaService.processarCarregamento(cliente, req));

        assertTrue(ex.getMessage().contains("não pertence") || ex.getMessage().contains("não encontrado"));
        verify(pagamentoService, never()).simularPagamento(any());
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Cliente clienteComEmail(String email) {
        Utilizador u = new Utilizador();
        u.setEmail(email);
        Cliente c = new Cliente();
        c.setNome("Test");
        c.setSobrenome("Cliente");
        c.setUtilizador(u);
        return c;
    }

    private Conta contaComSaldo(String saldoStr) {
        Conta c = new Conta();
        c.setSaldo(new BigDecimal(saldoStr));
        return c;
    }
}
