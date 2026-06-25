package com.example.loginapi;

import com.example.loginapi.dto.CarregarPasseRequest;
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
import com.example.loginapi.repository.pagamentos.CartaoPagamentoRepository;
import com.example.loginapi.repository.infraestrutura.CoroaRepository;
import com.example.loginapi.repository.titulos.TipoPasseRepository;
import com.example.loginapi.service.titulos.PasseService;
import com.example.loginapi.service.titulos.CompraPasseService;
import com.example.loginapi.service.pagamentos.PagamentoService;
import com.example.loginapi.service.clientes.EstatutoService;
import com.example.loginapi.service.infraestrutura.PricingService;
import com.example.loginapi.service.comunicacao.EmailService;
import com.example.loginapi.service.clientes.AuditLogService;
import com.example.loginapi.service.pagamentos.SaldoCompraService;
import com.example.loginapi.service.clientes.ContaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para o fluxo de passe gratuito (preço = 0.00).
 *
 * Cobre:
 * - Compra inicial (criarPasseComPagamento) com preço zero
 * - Carregamento (carregarPasse) com preço zero
 * - Regressão: fluxo pago continua inalterado em ambos os casos
 */
@ExtendWith(MockitoExtension.class)
class PasseGratuitoTest {

    @Mock private PasseService passeService;
    @Mock private PagamentoService pagamentoService;
    @Mock private EstatutoService estatutoService;
    @Mock private PricingService pricingService;
    @Mock private EmailService emailService;
    @Mock private AuditLogService auditLogService;
    @Mock private TipoPasseRepository tipoPasseRepo;
    @Mock private CoroaRepository coroaRepo;
    @Mock private SaldoCompraService saldoCompraService;
    @Mock private ContaService contaService;
    @Mock private CartaoPagamentoRepository cartaoRepo;

    @InjectMocks
    private CompraPasseService compraPasseService;

    private static final Long TIPO_ID  = 10L;
    private static final Long COROA_ID = 20L;
    private static final Long PASSE_ID = 100L;

    private Cliente   cliente;
    private TipoPasse tipoMensal;
    private Coroa     coroa1;
    private RegraPreco regraGratuita;

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
        // ativo = true por omissão em TipoPasse

        coroa1 = new Coroa();
        coroa1.setNome("Coroa 1");
        // ativo = true por omissão em Coroa

        regraGratuita = new RegraPreco();
        regraGratuita.setPreco(BigDecimal.ZERO);
    }

    // ─── criarPasseComPagamento ─────────────────────────────────────────────────

    @Test
    @DisplayName("Compra gratuita: passe devolvido fica PAID/ACTIVE com datas corretas")
    void testCriarPasseGratuito_ficaPaidActive() {
        stubCriarComum(regraGratuita);
        mockAtivarPasse();

        Passe resultado = compraPasseService.criarPasseComPagamento(cliente, TIPO_ID, COROA_ID);

        assertEquals(EstadoComercialPasse.PAID,   resultado.getEstadoComercial());
        assertEquals(EstadoOperacionalPasse.ACTIVE, resultado.getEstadoOperacional());
        assertEquals(BigDecimal.ZERO,              resultado.getPrecoAplicado());
        assertEquals(LocalDate.now(),              resultado.getDataInicio());
        assertEquals(LocalDate.now().plusDays(30), resultado.getDataFim());
        assertNotNull(resultado.getCodigoQr(),     "codigoQr deve ter sido gerado");
    }

    @Test
    @DisplayName("Compra gratuita: criarPagamento normal nunca é chamado; criarPagamentoGratuito é chamado uma vez")
    void testCriarPasseGratuito_chamaPagamentoGratuitoNaoChamaNormal() {
        stubCriarComum(regraGratuita);
        mockAtivarPasse();

        compraPasseService.criarPasseComPagamento(cliente, TIPO_ID, COROA_ID);

        verify(pagamentoService, never()).criarPagamento(any());
        verify(pagamentoService).criarPagamentoGratuito(any(Passe.class), eq("cliente@exemplo.com"));
        verify(emailService).enviarConfirmacaoPasse(eq("cliente@exemplo.com"), any());
    }

    @Test
    @DisplayName("Compra gratuita: passe não fica PENDING_PAYMENT nem INACTIVE em nenhum momento devolvido")
    void testCriarPasseGratuito_naoFicaPendingPaymentInactive() {
        stubCriarComum(regraGratuita);
        mockAtivarPasse();

        Passe resultado = compraPasseService.criarPasseComPagamento(cliente, TIPO_ID, COROA_ID);

        assertNotEquals(EstadoComercialPasse.PENDING_PAYMENT, resultado.getEstadoComercial());
        assertNotEquals(EstadoOperacionalPasse.INACTIVE,      resultado.getEstadoOperacional());
    }

    @Test
    @DisplayName("Regressão compra paga: passe fica PENDING_PAYMENT/INACTIVE; criarPagamento normal é chamado")
    void testCriarPassePago_ficaPendingPayment() {
        RegraPreco regraPaga = new RegraPreco();
        regraPaga.setPreco(new BigDecimal("25.00"));
        stubCriarComum(regraPaga);

        Passe resultado = compraPasseService.criarPasseComPagamento(cliente, TIPO_ID, COROA_ID);

        assertEquals(EstadoComercialPasse.PENDING_PAYMENT,  resultado.getEstadoComercial());
        assertEquals(EstadoOperacionalPasse.INACTIVE,       resultado.getEstadoOperacional());
        verify(pagamentoService).criarPagamento(any(Passe.class));
        verify(pagamentoService, never()).criarPagamentoGratuito(any(), any());
    }

    // ─── carregarPasse ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("Carregamento gratuito: dataFim estendida a partir da dataFim atual do passe")
    void testCarregarPasseGratuito_dataFimAtualizada() {
        LocalDate dataFimOriginal = LocalDate.now().plusDays(10);
        stubCarregar(regraGratuita, dataFimOriginal);

        Passe atualizado = compraPasseService.carregarPasse(PASSE_ID, cliente, criarReq("CARTAO"));

        // dataFim = dataFimOriginal (10 dias) + duracaoDias do tipo (30) = hoje+40
        assertEquals(dataFimOriginal.plusDays(30), atualizado.getDataFim());
    }

    @Test
    @DisplayName("Carregamento gratuito: saldoCompraService e criarPagamento normal nunca chamados")
    void testCarregarPasseGratuito_semDebitoNemPagamentoPendente() {
        stubCarregar(regraGratuita, LocalDate.now().plusDays(10));

        compraPasseService.carregarPasse(PASSE_ID, cliente, criarReq("SALDO_CONTA"));

        verify(saldoCompraService, never()).pagarPasseComSaldo(any(), any());
        verify(pagamentoService,   never()).criarPagamento(any());
    }

    @Test
    @DisplayName("Carregamento gratuito: criarPagamentoGratuito é chamado uma vez")
    void testCarregarPasseGratuito_criarPagamentoGratuito() {
        stubCarregar(regraGratuita, LocalDate.now().plusDays(10));

        compraPasseService.carregarPasse(PASSE_ID, cliente, criarReq("CARTAO"));

        verify(pagamentoService).criarPagamentoGratuito(any(Passe.class), eq("cliente@exemplo.com"));
    }

    @Test
    @DisplayName("Regressão carregamento pago: criarPagamento normal é chamado; criarPagamentoGratuito nunca")
    void testCarregarPassePago_chamarPagamentoNormal() {
        RegraPreco regraPaga = new RegraPreco();
        regraPaga.setPreco(new BigDecimal("25.00"));
        stubCarregar(regraPaga, LocalDate.now().plusDays(10));
        when(pagamentoService.criarPagamento(any())).thenReturn(new Pagamento());

        compraPasseService.carregarPasse(PASSE_ID, cliente, criarReq("CARTAO"));

        verify(pagamentoService).criarPagamento(any(Passe.class));
        verify(pagamentoService, never()).criarPagamentoGratuito(any(), any());
        verify(saldoCompraService, never()).pagarPasseComSaldo(any(), any());
    }

    // ─── Helpers ────────────────────────────────────────────────────────────────

    /** Stubs comuns para criarPasseComPagamento. */
    private void stubCriarComum(RegraPreco regra) {
        when(passeService.listarPasses(cliente)).thenReturn(Collections.emptyList());
        when(tipoPasseRepo.findById(TIPO_ID)).thenReturn(Optional.of(tipoMensal));
        when(coroaRepo.findById(COROA_ID)).thenReturn(Optional.of(coroa1));
        when(estatutoService.resolverEstatutoEfetivo(cliente)).thenReturn(TipoEstatuto.CRIANCA);
        when(pricingService.resolverRegra(TipoEstatuto.CRIANCA, tipoMensal, coroa1))
                .thenReturn(Optional.of(regra));
        when(passeService.guardar(any(Passe.class))).thenAnswer(i -> i.getArgument(0));
    }

    /** Stubs comuns para carregarPasse. */
    private void stubCarregar(RegraPreco regra, LocalDate dataFimOriginal) {
        Passe passeAtivo = new Passe();
        ReflectionTestUtils.setField(passeAtivo, "id", PASSE_ID);
        passeAtivo.setCliente(cliente);
        passeAtivo.setTipoPasse(tipoMensal);
        passeAtivo.setCoroa(coroa1);
        passeAtivo.setEstadoComercial(EstadoComercialPasse.PAID);
        passeAtivo.setEstadoOperacional(EstadoOperacionalPasse.ACTIVE);
        passeAtivo.setDataInicio(LocalDate.now().minusDays(20));
        passeAtivo.setDataFim(dataFimOriginal);

        when(passeService.obterPasse(PASSE_ID)).thenReturn(Optional.of(passeAtivo));
        when(tipoPasseRepo.findById(TIPO_ID)).thenReturn(Optional.of(tipoMensal));
        when(coroaRepo.findById(COROA_ID)).thenReturn(Optional.of(coroa1));
        when(estatutoService.resolverEstatutoEfetivo(cliente)).thenReturn(TipoEstatuto.CRIANCA);
        when(pricingService.resolverRegra(any(TipoEstatuto.class), eq(tipoMensal), eq(coroa1)))
                .thenReturn(Optional.of(regra));
        when(passeService.guardar(any(Passe.class))).thenAnswer(i -> i.getArgument(0));
    }

    /** Mock de ativarPasse que simula o comportamento real (necessário nos testes de criação). */
    private void mockAtivarPasse() {
        when(passeService.ativarPasse(any(Passe.class))).thenAnswer(i -> {
            Passe p = i.getArgument(0);
            p.setEstadoComercial(EstadoComercialPasse.PAID);
            p.setEstadoOperacional(EstadoOperacionalPasse.ACTIVE);
            p.setDataInicio(LocalDate.now());
            p.setDataFim(LocalDate.now().plusDays(p.getTipoPasse().getDuracaoDias()));
            return p;
        });
    }

    private CarregarPasseRequest criarReq(String metodo) {
        CarregarPasseRequest req = new CarregarPasseRequest();
        req.setTipoPasseId(TIPO_ID);
        req.setCoroaId(COROA_ID);
        req.setMetodoPagamento(metodo);
        return req;
    }
}
