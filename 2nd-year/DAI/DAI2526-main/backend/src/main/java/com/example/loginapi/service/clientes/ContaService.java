package com.example.loginapi.service.clientes;

import com.example.loginapi.dto.CarregamentoSaldoRequest;
import com.example.loginapi.dto.CarregamentoSaldoResponse;
import com.example.loginapi.dto.ResultadoPagamentoSimulado;
import com.example.loginapi.model.pagamentos.CartaoPagamento;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.Conta;
import com.example.loginapi.model.pagamentos.MovimentoConta;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.pagamentos.enums.TipoMovimentoConta;
import com.example.loginapi.repository.pagamentos.CartaoPagamentoRepository;
import com.example.loginapi.repository.pagamentos.ContaRepository;
import com.example.loginapi.repository.pagamentos.MovimentoContaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.example.loginapi.service.comunicacao.EmailService;
import com.example.loginapi.service.pagamentos.FaturaService;
import com.example.loginapi.service.pagamentos.PagamentoService;


@Service
public class ContaService {

    private final ContaRepository contaRepo;
    private final MovimentoContaRepository movimentoRepo;
    private final PagamentoService pagamentoService;
    private final CartaoPagamentoRepository cartaoRepo;
    private final FaturaService faturaService;
    private final EmailService emailService;

    private static final BigDecimal VALOR_MIN = new BigDecimal("1.00");
    private static final BigDecimal VALOR_MAX = new BigDecimal("250.00");

    @Autowired
    public ContaService(ContaRepository contaRepo, MovimentoContaRepository movimentoRepo,
                        PagamentoService pagamentoService, CartaoPagamentoRepository cartaoRepo,
                        FaturaService faturaService, EmailService emailService) {
        this.contaRepo = contaRepo;
        this.movimentoRepo = movimentoRepo;
        this.pagamentoService = pagamentoService;
        this.cartaoRepo = cartaoRepo;
        this.faturaService = faturaService;
        this.emailService = emailService;
    }

    // ─── Obter ou criar conta ─────────────────────────────────────────────────

    @Transactional
    public Conta obterOuCriarConta(Cliente cliente) {
        return contaRepo.findByCliente(cliente).orElseGet(() -> {
            Conta nova = new Conta();
            nova.setCliente(cliente);
            nova.setSaldo(BigDecimal.ZERO);
            return contaRepo.save(nova);
        });
    }

    // ─── Creditar ─────────────────────────────────────────────────────────────

    @Transactional
    public MovimentoConta creditar(Conta conta, BigDecimal valor,
                                   TipoMovimentoConta tipo, String descricao,
                                   Pagamento pagamento) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor a creditar deve ser positivo.");
        }

        BigDecimal saldoAntes = conta.getSaldo();
        BigDecimal saldoDepois = saldoAntes.add(valor);

        conta.setSaldo(saldoDepois);
        contaRepo.save(conta);

        return registarMovimento(conta, tipo, valor, saldoAntes, saldoDepois, descricao, pagamento);
    }

    // ─── Debitar ──────────────────────────────────────────────────────────────

    @Transactional
    public MovimentoConta debitar(Conta conta, BigDecimal valor,
                                  TipoMovimentoConta tipo, String descricao,
                                  Pagamento pagamento) {
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor a debitar deve ser positivo.");
        }
        if (conta.getSaldo().compareTo(valor) < 0) {
            throw new IllegalStateException("Saldo insuficiente.");
        }

        BigDecimal saldoAntes = conta.getSaldo();
        BigDecimal saldoDepois = saldoAntes.subtract(valor);

        conta.setSaldo(saldoDepois);
        contaRepo.save(conta);

        return registarMovimento(conta, tipo, valor, saldoAntes, saldoDepois, descricao, pagamento);
    }

    // ─── Listar movimentos ────────────────────────────────────────────────────

    public List<MovimentoConta> listarMovimentos(Conta conta) {
        return movimentoRepo.findByContaOrderByCriadoEmDesc(conta);
    }

    // ─── Carregar saldo ───────────────────────────────────────────────────────

    @Transactional
    public CarregamentoSaldoResponse processarCarregamento(Cliente cliente, CarregamentoSaldoRequest req) {
        // 1. Validar valor
        if (req.getValor() == null || req.getValor().compareTo(VALOR_MIN) < 0) {
            throw new IllegalArgumentException("O valor mínimo de carregamento é 1,00 €.");
        }
        if (req.getValor().compareTo(VALOR_MAX) > 0) {
            throw new IllegalArgumentException("O valor máximo de carregamento é 250,00 €.");
        }

        // 2. Validar método de pagamento e parâmetros específicos
        String metodo = req.getMetodoPagamento();
        if (metodo == null || metodo.isBlank()) {
            throw new IllegalArgumentException("O método de pagamento é obrigatório.");
        }

        String metodoStr;
        if ("CARTAO_PREDEFINIDO".equals(metodo)) {
            cartaoRepo.findByClienteAndAtivoTrueOrderByCriadoEmDesc(cliente).stream()
                    .filter(CartaoPagamento::isPredefinido)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Não tem cartão predefinido ativo."));
            metodoStr = "CARTAO";
        } else if ("CARTAO_ESPECIFICO".equals(metodo)) {
            if (req.getCartaoId() == null) {
                throw new IllegalArgumentException("O ID do cartão é obrigatório para pagamento por cartão específico.");
            }
            CartaoPagamento cartao = cartaoRepo.findByIdAndCliente(req.getCartaoId(), cliente)
                    .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado ou não pertence ao cliente."));
            if (!cartao.isAtivo()) {
                throw new IllegalArgumentException("O cartão selecionado está inativo.");
            }
            metodoStr = "CARTAO";
        } else if ("MBWAY".equals(metodo)) {
            String tel = req.getTelefone() == null ? "" : req.getTelefone().replaceAll("\\s+", "");
            if (!tel.matches("9\\d{8}")) {
                throw new IllegalArgumentException("Telefone MB Way inválido. Deve ter 9 dígitos e começar por 9.");
            }
            metodoStr = "MBWAY";
        } else {
            throw new IllegalArgumentException("Método de pagamento não suportado: " + metodo);
        }

        // 3. Validar NIF quando solicitada fatura com NIF
        String nifFatura = null;
        if (req.isEmitirFaturaComNif()) {
            String nifRaw = req.getNif() == null ? "" : req.getNif().replaceAll("\\s+", "");
            if (!nifRaw.matches("\\d{9}")) {
                throw new IllegalArgumentException("NIF inválido. Deve ter exatamente 9 dígitos.");
            }
            nifFatura = nifRaw;
        }

        // 4. Obter ou criar conta — não altera saldo
        Conta conta = obterOuCriarConta(cliente);

        // 5. Simular pagamento externo; se rejeitado, lança exceção sem alterar saldo
        ResultadoPagamentoSimulado resultado = pagamentoService.simularPagamento(metodoStr);
        if (!resultado.aprovado()) {
            throw new IllegalStateException(resultado.mensagem());
        }

        // 6. Pagamento aprovado — creditar saldo e registar movimento
        String descricao = "Carregamento via " + metodo.replace("_", " ").toLowerCase();
        MovimentoConta mov = creditar(conta, req.getValor(), TipoMovimentoConta.CARREGAMENTO, descricao, null);

        // 7. Gerar fatura e enviar por email
        LocalDateTime agora = LocalDateTime.now();
        String faturaNumero = pagamentoService.gerarNumeroFatura("CG", mov.getId());
        String nifParaFatura = nifFatura != null ? nifFatura : "—";
        String emailDestino = (req.getEmailFatura() != null && !req.getEmailFatura().isBlank())
                ? req.getEmailFatura()
                : cliente.getUtilizador().getEmail();

        String htmlFatura = faturaService.gerarHtmlFaturaCarregamento(
                faturaNumero, cliente.getNomeCompleto(), nifParaFatura, req.getValor(), agora);
        byte[] pdfFatura = faturaService.gerarPdfFaturaCarregamento(
                faturaNumero, cliente.getNomeCompleto(), nifParaFatura, req.getValor(), agora);
        emailService.enviarFatura(emailDestino,
                "TUB – Fatura " + faturaNumero, htmlFatura, pdfFatura, faturaNumero);

        // 8. Construir resposta
        CarregamentoSaldoResponse resp = new CarregamentoSaldoResponse();
        resp.setNovoSaldo(conta.getSaldo());
        resp.setReferenciaExterna(resultado.referenciaExterna());
        resp.setFaturaNumero(faturaNumero);
        resp.setMensagem("Carregamento efetuado com sucesso.");
        return resp;
    }

    // ─── Interno ──────────────────────────────────────────────────────────────

    private MovimentoConta registarMovimento(Conta conta, TipoMovimentoConta tipo,
                                              BigDecimal valor, BigDecimal saldoAntes,
                                              BigDecimal saldoDepois, String descricao,
                                              Pagamento pagamento) {
        MovimentoConta mov = new MovimentoConta();
        mov.setConta(conta);
        mov.setTipo(tipo);
        mov.setValor(valor);
        mov.setSaldoAntes(saldoAntes);
        mov.setSaldoDepois(saldoDepois);
        mov.setDescricao(descricao);
        mov.setPagamento(pagamento);
        return movimentoRepo.save(mov);
    }
}
