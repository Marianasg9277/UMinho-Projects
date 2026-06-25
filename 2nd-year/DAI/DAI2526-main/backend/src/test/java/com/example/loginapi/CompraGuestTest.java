package com.example.loginapi;

import com.example.loginapi.dto.CompraGuestRequestDTO;
import com.example.loginapi.model.titulos.TipoBilhete;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import com.example.loginapi.repository.pagamentos.PagamentoRepository;
import com.example.loginapi.repository.titulos.TipoBilheteRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.service.comunicacao.EmailService;
import com.example.loginapi.service.pagamentos.FaturaService;
import com.example.loginapi.service.pagamentos.PagamentoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o fluxo de compra de bilhetes por utilizadores guest.
 *
 * Cobre:
 * - Compra válida com nome, email, NIF → bilhete criado com validoAte
 * - Tipo de bilhete inexistente → IllegalArgumentException
 * - Cálculo de validade com base em validadeHoras
 */
@ExtendWith(MockitoExtension.class)
class CompraGuestTest {

    @Mock private PagamentoRepository pagamentoRepo;
    @Mock private TransacaoRepository transacaoRepo;
    @Mock private TipoBilheteRepository tipoBilheteRepo;
    @Mock private LinhaRepository linhaRepo;
    @Mock private EmailService emailService;
    @Mock private FaturaService faturaService;

    @InjectMocks
    private PagamentoService pagamentoService;

    private TipoBilhete tipoBilheteAvulso;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(pagamentoService, "falhaSimulada", false);

        tipoBilheteAvulso = new TipoBilhete();
        tipoBilheteAvulso.setNome("Simples Coroa 1 (app/cartão)");
        tipoBilheteAvulso.setCategoria(TipoBilhete.Categoria.AVULSO);
        tipoBilheteAvulso.setPreco(new BigDecimal("0.75"));
        tipoBilheteAvulso.setValidadeHoras(2);
    }

    @Test
    @DisplayName("Compra guest válida cria transação com validoAte preenchido")
    void testCompraGuestValida() {
        when(tipoBilheteRepo.findById(1L)).thenReturn(Optional.of(tipoBilheteAvulso));

        // Simular save retornando o objecto com id=10
        when(transacaoRepo.save(any(Transacao.class))).thenAnswer(inv -> {
            Transacao t = inv.getArgument(0);
            // Simular ID gerado pela BD
            return t;
        });
        doNothing().when(emailService).enviarFatura(any(), any(), any(), any(), any());
        doNothing().when(emailService).enviarBilhete(any(Transacao.class), nullable(String.class), nullable(byte[].class));
        when(faturaService.gerarPdfFaturaBilhete(any())).thenReturn(null);
        when(faturaService.gerarHtmlFaturaBilhete(any())).thenReturn("<html>fatura</html>");

        CompraGuestRequestDTO dto = new CompraGuestRequestDTO();
        dto.setTipoBilheteId(1L);
        dto.setGuestEmail("teste@exemplo.com");
        dto.setGuestNome("João Silva");
        dto.setGuestNif("123456789");

        Transacao resultado = pagamentoService.comprarBilheteGuest(dto);

        assertNotNull(resultado);
        assertEquals(EstadoPagamento.PAID, resultado.getEstadoPagamento());
        assertEquals("João Silva", resultado.getGuestNome());
        assertEquals("123456789", resultado.getGuestNif());
        assertEquals("teste@exemplo.com", resultado.getGuestEmail());
        assertNotNull(resultado.getValidoAte(),
                "validoAte deve ser preenchido após compra");
        assertTrue(resultado.getValidoAte().isAfter(LocalDateTime.now().plusHours(1)),
                "validoAte deve ser pelo menos 1 hora no futuro");
        assertNotNull(resultado.getCodigoQr());
        assertTrue(resultado.getCodigoQr().startsWith("GUEST-"));
    }

    @Test
    @DisplayName("Tipo de bilhete inexistente lança IllegalArgumentException")
    void testCompraGuestTipoBilheteInexistente() {
        when(tipoBilheteRepo.findById(99L)).thenReturn(Optional.empty());

        CompraGuestRequestDTO dto = new CompraGuestRequestDTO();
        dto.setTipoBilheteId(99L);
        dto.setGuestEmail("teste@exemplo.com");
        dto.setGuestNome("Maria Santos");
        dto.setGuestNif("987654321");

        assertThrows(IllegalArgumentException.class,
                () -> pagamentoService.comprarBilheteGuest(dto),
                "Deve lançar exceção para tipo de bilhete não encontrado");
    }

    @Test
    @DisplayName("Validade calculada correctamente: 2 horas para bilhete avulso")
    void testValidadeCalculadaCorretamente() {
        when(tipoBilheteRepo.findById(1L)).thenReturn(Optional.of(tipoBilheteAvulso));
        when(transacaoRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).enviarFatura(any(), any(), any(), any(), any());
        doNothing().when(emailService).enviarBilhete(any(Transacao.class), nullable(String.class), nullable(byte[].class));
        when(faturaService.gerarPdfFaturaBilhete(any())).thenReturn(null);
        when(faturaService.gerarHtmlFaturaBilhete(any())).thenReturn("<html/>");

        CompraGuestRequestDTO dto = new CompraGuestRequestDTO();
        dto.setTipoBilheteId(1L);
        dto.setGuestEmail("a@b.com");
        dto.setGuestNome("Ana Costa");
        dto.setGuestNif("111222333");

        LocalDateTime antes = LocalDateTime.now();
        Transacao resultado = pagamentoService.comprarBilheteGuest(dto);
        LocalDateTime depois = LocalDateTime.now();

        // validoAte deve ser entre (antes + 2h) e (depois + 2h)
        assertTrue(resultado.getValidoAte().isAfter(antes.plusHours(2).minusSeconds(5)));
        assertTrue(resultado.getValidoAte().isBefore(depois.plusHours(2).plusSeconds(5)));
    }

    @Test
    @DisplayName("Email do guest é guardado em lowercase sem espaços")
    void testGuestEmailNormalizado() {
        when(tipoBilheteRepo.findById(1L)).thenReturn(Optional.of(tipoBilheteAvulso));
        when(transacaoRepo.save(any())).thenAnswer(i -> i.getArgument(0));
        doNothing().when(emailService).enviarFatura(any(), any(), any(), any(), any());
        doNothing().when(emailService).enviarBilhete(any(Transacao.class), nullable(String.class), nullable(byte[].class));
        when(faturaService.gerarPdfFaturaBilhete(any())).thenReturn(null);
        when(faturaService.gerarHtmlFaturaBilhete(any())).thenReturn("<html/>");

        CompraGuestRequestDTO dto = new CompraGuestRequestDTO();
        dto.setTipoBilheteId(1L);
        dto.setGuestEmail("  TESTE@EXEMPLO.COM  ");
        dto.setGuestNome("Teste");
        dto.setGuestNif("000000001");

        Transacao resultado = pagamentoService.comprarBilheteGuest(dto);

        assertEquals("teste@exemplo.com", resultado.getGuestEmail());
    }
}
