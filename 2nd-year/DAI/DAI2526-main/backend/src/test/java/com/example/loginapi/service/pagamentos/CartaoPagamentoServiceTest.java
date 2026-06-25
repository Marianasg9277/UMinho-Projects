package com.example.loginapi.service.pagamentos;

import com.example.loginapi.dto.CartaoPagamentoRequest;
import com.example.loginapi.model.pagamentos.CartaoPagamento;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.repository.pagamentos.CartaoPagamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartaoPagamentoServiceTest {

    @Mock private CartaoPagamentoRepository cartaoRepo;
    @InjectMocks private CartaoPagamentoService service;

    private Cliente cliente;

    @BeforeEach
    void setUp() {
        cliente = new Cliente();
    }

    // ─── Luhn ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("Número com dígito de verificação Luhn correto deve passar")
    void luhn_numeroValido() {
        assertTrue(service.passaLuhn("4532015112830366"));
    }

    @Test
    @DisplayName("Número com dígito de verificação Luhn errado deve falhar")
    void luhn_numeroInvalido() {
        assertFalse(service.passaLuhn("4532015112830367"));
    }

    // ─── Validações básicas ───────────────────────────────────────────────────

    @Test
    @DisplayName("Cartão expirado deve lançar exceção")
    void cartaoExpirado_lancaExcecao() {
        CartaoPagamentoRequest req = reqBase();
        req.setAnoValidade(LocalDate.now().getYear() - 1);
        req.setMesValidade(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarRequest(req));
        assertTrue(ex.getMessage().contains("expirado"));
    }

    @Test
    @DisplayName("Número com menos de 13 dígitos deve lançar exceção")
    void numeroCartao_curtoDemais_lancaExcecao() {
        CartaoPagamentoRequest req = reqBase();
        req.setNumeroCartao("123456789012"); // 12 dígitos

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarRequest(req));
        assertTrue(ex.getMessage().contains("13 e 19"));
    }

    @Test
    @DisplayName("Número que não passa Luhn deve lançar exceção com mensagem clara")
    void numeroCartao_luhnFalha_lancaExcecao() {
        CartaoPagamentoRequest req = reqBase();
        req.setNumeroCartao("4532015112830367"); // falha Luhn

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarRequest(req));
        assertTrue(ex.getMessage().contains("inválido"));
    }

    @Test
    @DisplayName("CVV com 2 dígitos deve lançar exceção")
    void cvv_curtoDemais_lancaExcecao() {
        CartaoPagamentoRequest req = reqBase();
        req.setCvv("12");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarRequest(req));
        assertTrue(ex.getMessage().contains("CVV"));
    }

    @Test
    @DisplayName("Nome do titular em branco deve lançar exceção")
    void nomeTitular_vazio_lancaExcecao() {
        CartaoPagamentoRequest req = reqBase();
        req.setNomeTitular("  ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> service.validarRequest(req));
        assertTrue(ex.getMessage().contains("titular"));
    }

    // ─── Regras de negócio ────────────────────────────────────────────────────

    @Test
    @DisplayName("Primeiro cartão do cliente deve ficar automaticamente como predefinido")
    void primeiroCartao_ficaPredefinido() {
        when(cartaoRepo.countByClienteAndAtivoTrue(cliente)).thenReturn(0L);
        when(cartaoRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        CartaoPagamento resultado = service.associarCartao(cliente, reqBase());

        assertTrue(resultado.isPredefinido());
    }

    @Test
    @DisplayName("Segundo cartão do cliente não deve ficar predefinido automaticamente")
    void segundoCartao_naoFicaPredefinido() {
        when(cartaoRepo.countByClienteAndAtivoTrue(cliente)).thenReturn(1L);
        when(cartaoRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        CartaoPagamento resultado = service.associarCartao(cliente, reqBase());

        assertFalse(resultado.isPredefinido());
    }

    @Test
    @DisplayName("Definir predefinido deve limpar outros e marcar apenas o selecionado")
    void definirPredefinido_apenasUmAtivo() {
        CartaoPagamento cartao = new CartaoPagamento();
        cartao.setAtivo(true);
        cartao.setPredefinido(false);

        when(cartaoRepo.findByIdAndCliente(1L, cliente)).thenReturn(Optional.of(cartao));
        when(cartaoRepo.save(any())).thenAnswer(i -> i.getArgument(0));

        CartaoPagamento resultado = service.definirPredefinido(1L, cliente);

        verify(cartaoRepo).limparPredefinidosDoCliente(cliente);
        assertTrue(resultado.isPredefinido());
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private CartaoPagamentoRequest reqBase() {
        CartaoPagamentoRequest req = new CartaoPagamentoRequest();
        req.setNomeTitular("João Silva");
        req.setNumeroCartao("4532015112830366"); // Visa de teste, passa Luhn
        req.setCvv("123");
        req.setMesValidade(12);
        req.setAnoValidade(LocalDate.now().getYear() + 2);
        return req;
    }
}
