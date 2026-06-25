package com.example.loginapi;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.TipoPasse;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.infraestrutura.RegraPreco;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.enums.EstadoComercialPasse;
import com.example.loginapi.model.titulos.enums.EstadoOperacionalPasse;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import com.example.loginapi.service.titulos.PasseService;
import com.example.loginapi.service.titulos.CompraPasseService;
import com.example.loginapi.service.pagamentos.PagamentoService;
import com.example.loginapi.service.clientes.EstatutoService;
import com.example.loginapi.service.infraestrutura.PricingService;
import com.example.loginapi.service.comunicacao.EmailService;
import com.example.loginapi.service.clientes.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para renovação de passe calculando estatutos de desconto.
 */
@ExtendWith(MockitoExtension.class)
class RenovacaoPasseTest {

    @Mock private PasseService passeService;
    @Mock private PagamentoService pagamentoService;
    @Mock private EstatutoService estatutoService;
    @Mock private PricingService pricingService;
    @Mock private EmailService emailService;
    @Mock private AuditLogService auditLogService;

    @InjectMocks
    private CompraPasseService compraPasseService;

    private Cliente cliente;
    private Passe passeExpirado;
    private TipoPasse tipoMensal;
    private Coroa coroa1;

    @BeforeEach
    void setUp() {
        Utilizador u = new Utilizador();
        u.setEmail("cliente@exemplo.com");

        cliente = new Cliente();
        ReflectionTestUtils.setField(cliente, "id", 1L);
        cliente.setUtilizador(u);

        tipoMensal = new TipoPasse();
        tipoMensal.setNome("Mensal");
        tipoMensal.setDuracaoDias(30);

        coroa1 = new Coroa();
        coroa1.setNome("Coroa 1");

        passeExpirado = new Passe();
        ReflectionTestUtils.setField(passeExpirado, "id", 100L);
        passeExpirado.setCliente(cliente);
        passeExpirado.setTipoPasse(tipoMensal);
        passeExpirado.setCoroa(coroa1);
        passeExpirado.setEstadoComercial(EstadoComercialPasse.PAID);
        passeExpirado.setEstadoOperacional(EstadoOperacionalPasse.FALTA_RENOVAR);
        passeExpirado.setDataInicio(LocalDate.now().minusDays(35));
        passeExpirado.setDataFim(LocalDate.now().minusDays(5));
    }

    @Test
    @DisplayName("Renovação de passe FALTA_RENOVAR calcula preço base (40€) e estende validade para SEM_ESTATUTO")
    void testRenovacaoPasseEstatutoBase() {
        RegraPreco regraNormal = new RegraPreco();
        regraNormal.setPreco(new BigDecimal("40.00"));

        when(passeService.obterPasse(100L)).thenReturn(Optional.of(passeExpirado));
        when(estatutoService.resolverEstatutoEfetivo(cliente)).thenReturn(TipoEstatuto.SEM_ESTATUTO);
        when(pricingService.resolverRegra(TipoEstatuto.SEM_ESTATUTO, tipoMensal, coroa1)).thenReturn(Optional.of(regraNormal));
        when(passeService.guardar(any(Passe.class))).thenAnswer(i -> i.getArgument(0));
        when(pagamentoService.criarPagamento(any())).thenReturn(new Pagamento());
        when(passeService.ativarPasse(any())).thenAnswer(i -> i.getArgument(0));

        Passe renovado = compraPasseService.renovarPasse(100L, cliente, "MBWAY");

        assertNotNull(renovado);
        assertEquals(TipoEstatuto.SEM_ESTATUTO, renovado.getTipoEstatutoAplicado());
        assertEquals(new BigDecimal("40.00"), renovado.getPrecoAplicado());
        // validade
        assertEquals(LocalDate.now(), renovado.getDataInicio());
        assertEquals(LocalDate.now().plusDays(30), renovado.getDataFim());

        verify(pagamentoService).confirmarPagamento(any(), eq("MBWAY"), eq("cliente@exemplo.com"), isNull());
        verify(emailService).enviarConfirmacaoPasse(eq("cliente@exemplo.com"), any());
        verify(auditLogService).registar(eq("cliente@exemplo.com"), eq("USER"), eq("PASSE_RENOVADO"), any(), any(), eq(true));
    }

    @Test
    @DisplayName("Renovação de passe para Estudante calcula preço com 50% desconto (20€)")
    void testDescontoEstudante() {
        RegraPreco regraEstudante = new RegraPreco();
        regraEstudante.setPreco(new BigDecimal("20.00"));

        when(passeService.obterPasse(100L)).thenReturn(Optional.of(passeExpirado));
        when(estatutoService.resolverEstatutoEfetivo(cliente)).thenReturn(TipoEstatuto.ESTUDANTE);
        when(pricingService.resolverRegra(TipoEstatuto.ESTUDANTE, tipoMensal, coroa1))
                .thenReturn(Optional.of(regraEstudante));
        when(passeService.guardar(any(Passe.class))).thenAnswer(i -> i.getArgument(0));
        when(pagamentoService.criarPagamento(any())).thenReturn(new Pagamento());
        when(passeService.ativarPasse(any())).thenAnswer(i -> i.getArgument(0));

        Passe renovado = compraPasseService.renovarPasse(100L, cliente, "CARTAO");

        assertEquals(TipoEstatuto.ESTUDANTE, renovado.getTipoEstatutoAplicado());
        assertEquals(new BigDecimal("20.00"), renovado.getPrecoAplicado());
    }

    @Test
    @DisplayName("Renovação gratuita ativa passe sem chamar pagamento quando preço é 0.00")
    void testRenovacaoGratuita() {
        RegraPreco regraGratuita = new RegraPreco();
        regraGratuita.setPreco(BigDecimal.ZERO);

        when(passeService.obterPasse(100L)).thenReturn(Optional.of(passeExpirado));
        when(estatutoService.resolverEstatutoEfetivo(cliente)).thenReturn(TipoEstatuto.CRIANCA);
        when(pricingService.resolverRegra(TipoEstatuto.CRIANCA, tipoMensal, coroa1))
                .thenReturn(Optional.of(regraGratuita));
        when(passeService.guardar(any(Passe.class))).thenAnswer(i -> i.getArgument(0));

        Passe renovado = compraPasseService.renovarPasse(100L, cliente, "CARTAO");

        assertEquals(BigDecimal.ZERO, renovado.getPrecoAplicado());
        assertEquals(EstadoComercialPasse.PAID, renovado.getEstadoComercial());
        assertEquals(EstadoOperacionalPasse.ACTIVE, renovado.getEstadoOperacional());
        assertEquals(LocalDate.now(), renovado.getDataInicio());
        assertEquals(LocalDate.now().plusDays(30), renovado.getDataFim());

        verify(pagamentoService, never()).criarPagamento(any());
        verify(pagamentoService, never()).confirmarPagamento(any(), any(), any(), any());
        verify(emailService).enviarConfirmacaoPasse(eq("cliente@exemplo.com"), any());
        verify(auditLogService).registar(eq("cliente@exemplo.com"), eq("USER"), eq("PASSE_RENOVADO"), any(), any(), eq(true));
    }

    @Test
    @DisplayName("Renovação falha se passe não estiver pago")
    void testRenovacaoPasseApenasParaPago() {
        Passe naoPago = new Passe();
        ReflectionTestUtils.setField(naoPago, "id", 200L);
        naoPago.setCliente(cliente);
        naoPago.setEstadoComercial(EstadoComercialPasse.PENDING_PAYMENT);
        naoPago.setEstadoOperacional(EstadoOperacionalPasse.INACTIVE);

        when(passeService.obterPasse(200L)).thenReturn(Optional.of(naoPago));

        IllegalStateException ex = assertThrows(IllegalStateException.class, () -> {
            compraPasseService.renovarPasse(200L, cliente, "CARTAO");
        });

        assertEquals("Só é possível renovar passes pagos.", ex.getMessage());
    }
}
