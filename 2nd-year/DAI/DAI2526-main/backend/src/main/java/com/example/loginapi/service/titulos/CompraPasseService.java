package com.example.loginapi.service.titulos;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.TipoPasse;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.infraestrutura.RegraPreco;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.pagamentos.CartaoPagamento;
import com.example.loginapi.model.pagamentos.Conta;
import com.example.loginapi.model.titulos.enums.EstadoComercialPasse;
import com.example.loginapi.model.titulos.enums.EstadoOperacionalPasse;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import com.example.loginapi.model.pagamentos.enums.TipoMovimentoConta;
import com.example.loginapi.dto.CarregarPasseRequest;
import com.example.loginapi.repository.pagamentos.CartaoPagamentoRepository;
import com.example.loginapi.repository.infraestrutura.CoroaRepository;
import com.example.loginapi.repository.titulos.TipoPasseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.service.clientes.EstatutoService;
import com.example.loginapi.service.clientes.AuditLogService;
import com.example.loginapi.service.comunicacao.EmailService;
import com.example.loginapi.service.clientes.ContaService;
import com.example.loginapi.service.pagamentos.SaldoCompraService;
import com.example.loginapi.service.infraestrutura.PricingService;
import com.example.loginapi.service.pagamentos.PagamentoService;


@Service
public class CompraPasseService {

    private static final Logger log = LoggerFactory.getLogger(CompraPasseService.class);

    @Autowired private PasseService passeService;
    @Autowired private PagamentoService pagamentoService;
    @Autowired private EstatutoService estatutoService;
    @Autowired private PricingService pricingService;
    @Autowired private EmailService emailService;
    @Autowired private AuditLogService auditLogService;
    @Autowired private TipoPasseRepository tipoPasseRepo;
    @Autowired private CoroaRepository coroaRepo;
    @Autowired private SaldoCompraService saldoCompraService;
    @Autowired private ContaService contaService;
    @Autowired private CartaoPagamentoRepository cartaoRepo;

    // ─────────────────────────────────────────────────────────────────────────
    // Criar passe (fluxo completo)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Passe criarPasseComPagamento(Cliente cliente, Long tipoPasseId, Long coroaId) {
        // Bloquear nova compra se já existe passe ativo, pendente ou pago não iniciado
        List<Passe> existentes = passeService.listarPasses(cliente);
        boolean temPasseBloqueante = existentes.stream().anyMatch(p -> {
            EstadoOperacionalPasse op = p.getEstadoOperacional();
            EstadoComercialPasse com = p.getEstadoComercial();
            return op == EstadoOperacionalPasse.ACTIVE
                    || op == EstadoOperacionalPasse.FALTA_RENOVAR
                    || com == EstadoComercialPasse.PENDING_PAYMENT
                    || (com == EstadoComercialPasse.PAID && op == EstadoOperacionalPasse.INACTIVE);
        });
        if (temPasseBloqueante) {
            throw new IllegalStateException(
                "Já tens um passe ativo ou pendente. Cancela ou aguarda o fim do passe atual antes de comprar outro.");
        }

        TipoPasse tipoPasse = tipoPasseRepo.findById(tipoPasseId)
                .orElseThrow(() -> new IllegalArgumentException("Tipo de passe não encontrado."));
        if (!tipoPasse.isAtivo()) throw new IllegalArgumentException("Este tipo de passe não está disponível.");

        Coroa coroa = coroaRepo.findById(coroaId)
                .orElseThrow(() -> new IllegalArgumentException("Coroa/zona não encontrada."));
        if (!coroa.isAtivo()) throw new IllegalArgumentException("Esta coroa/zona não está disponível.");

        TipoEstatuto estatuto = estatutoService.resolverEstatutoEfetivo(cliente);
        RegraPreco regra = pricingService.resolverRegra(estatuto, tipoPasse, coroa)
                .orElseThrow(() -> new IllegalStateException(
                        "Não existe regra de preço válida para: " + estatuto + " / " + tipoPasse.getNome() + " / " + coroa.getNome()));

        Passe passe = new Passe();
        passe.setCliente(cliente);
        passe.setTipoPasse(tipoPasse);
        passe.setCoroa(coroa);
        passe.setTipoEstatutoAplicado(estatuto);
        passe.setRegraPreco(regra);
        passe.setPrecoAplicado(regra.getPreco());

        // Passe gratuito: ativar imediatamente sem pagamento pendente
        if (regra.getPreco().compareTo(java.math.BigDecimal.ZERO) == 0) {
            passe.setEstadoComercial(EstadoComercialPasse.PAID);
            passe.setEstadoOperacional(EstadoOperacionalPasse.ACTIVE);
            passe.setCodigoQr("PASSE-" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
            Passe passeSalvo = passeService.guardar(passe);
            Passe ativo = passeService.ativarPasse(passeSalvo);
            String emailGratuito = cliente.getUtilizador().getEmail();
            pagamentoService.criarPagamentoGratuito(ativo, emailGratuito);
            emailService.enviarConfirmacaoPasse(emailGratuito, ativo);
            log.info("[PASSE] Passe gratuito id={} ativado | cliente={} | tipo={} | coroa={} | estatuto={}",
                    ativo.getId(), cliente.getId(), tipoPasse.getNome(), coroa.getNome(), estatuto);
            return ativo;
        }

        passe.setEstadoComercial(EstadoComercialPasse.PENDING_PAYMENT);
        passe.setEstadoOperacional(EstadoOperacionalPasse.INACTIVE);
        passe.setCodigoQr("PASSE-" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        Passe passeSalvo = passeService.guardar(passe);

        pagamentoService.criarPagamento(passeSalvo);

        log.info("[PASSE] Criado passe id={} | cliente={} | tipo={} | coroa={} | estatuto={} | preço={}",
                passeSalvo.getId(), cliente.getId(), tipoPasse.getNome(),
                coroa.getNome(), estatuto, regra.getPreco());

        return passeSalvo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Simular/Confirmar pagamento
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Pagamento simularPagamento(Long passeId, String metodo) {
        return simularPagamento(passeId, metodo, null);
    }

    @Transactional
    public Pagamento simularPagamento(Long passeId, String metodo, Long cartaoId) {
        Passe passe = passeService.obterPasse(passeId)
                .orElseThrow(() -> new IllegalArgumentException("Passe não encontrado."));

        Pagamento pagamento = pagamentoService.obterPagamentoPorPasse(passe)
                .orElseThrow(() -> new IllegalStateException("Pagamento não encontrado para este passe."));

        String emailCliente = passe.getCliente().getUtilizador().getEmail();
        String cartaoToken = resolverTokenCartao(cartaoId, passe.getCliente());
        Pagamento confirmado = pagamentoService.confirmarPagamento(pagamento, metodo, emailCliente, cartaoToken);
        Passe ativado = passeService.ativarPasse(passe);

        emailService.enviarConfirmacaoPasse(emailCliente, ativado);

        auditLogService.registar(emailCliente, "USER", "PASSE_PAGO",
                "passe", "Passe id=" + passeId + " pago via " + metodo, true);

        return confirmado;
    }

    /** Compatibilidade retroactiva sem metodo. */
    @Transactional
    public Pagamento simularPagamento(Long passeId) {
        return simularPagamento(passeId, "CARTAO");
    }


    @Transactional
    public Passe carregarPasse(Long passeId, Cliente cliente, CarregarPasseRequest req) {
        // Verify pass exists and belongs to client
        Passe passe = passeService.obterPasse(passeId)
                .orElseThrow(() -> new IllegalArgumentException("Passe não encontrado."));
        if (!passe.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Este passe não pertence ao utilizador.");
        }

        // Resolve new TipoPasse and Coroa based on request
        TipoPasse tipoPasse = tipoPasseRepo.findById(req.getTipoPasseId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de passe não encontrado."));
        if (!tipoPasse.isAtivo()) throw new IllegalArgumentException("Este tipo de passe não está disponível.");
        Coroa coroa = coroaRepo.findById(req.getCoroaId())
                .orElseThrow(() -> new IllegalArgumentException("Coroa/zona não encontrada."));
        if (!coroa.isAtivo()) throw new IllegalArgumentException("Esta coroa/zona não está disponível.");

        // Resolve price rule based on current estatuto
        TipoEstatuto estatuto = estatutoService.resolverEstatutoEfetivo(cliente);
        RegraPreco regra = pricingService.resolverRegra(estatuto, tipoPasse, coroa)
                .orElseThrow(() -> new IllegalStateException("Não existe regra de preço válida para o carregamento."));

        // Update validity – extend from current end date if present, otherwise from today
        java.time.LocalDate novaDataFim = (passe.getDataFim() != null ? passe.getDataFim() : java.time.LocalDate.now())
                .plusDays(tipoPasse.getDuracaoDias());
        passe.setTipoPasse(tipoPasse);
        passe.setCoroa(coroa);
        passe.setTipoEstatutoAplicado(estatuto);
        passe.setRegraPreco(regra);
        passe.setPrecoAplicado(regra.getPreco());
        passe.setDataFim(novaDataFim);
        // keep existing dataInicio if already set
        if (passe.getDataInicio() == null) {
            passe.setDataInicio(java.time.LocalDate.now());
        }

        // Carregamento gratuito: estender validade sem debitar nem exigir método
        if (regra.getPreco().compareTo(java.math.BigDecimal.ZERO) == 0) {
            Passe atualizado = passeService.guardar(passe);
            pagamentoService.criarPagamentoGratuito(atualizado, cliente.getUtilizador().getEmail());
            log.info("[CARREGAR] Passe id={} carregado GRATUITAMENTE | cliente={} | tipo={} | coroa={} | estatuto={}",
                    atualizado.getId(), cliente.getId(), tipoPasse.getNome(), coroa.getNome(), estatuto);
            return atualizado;
        }

        // Process payment
        String metodo = req.getMetodoPagamento();
        if ("SALDO_CONTA".equalsIgnoreCase(metodo)) {
            saldoCompraService.pagarPasseComSaldo(cliente, passeId);
        } else {
            String cartaoToken = resolverTokenCartao(req.getCartaoId(), cliente);
            Pagamento pagamento = pagamentoService.criarPagamento(passe);
            String email = cliente.getUtilizador().getEmail();
            pagamentoService.confirmarPagamento(pagamento, metodo, email, cartaoToken);
        }

        // Save and return updated pass
        Passe atualizado = passeService.guardar(passe);
        log.info("[CARREGAR] Passe id={} carregado | cliente={} | tipo={} | coroa={} | estatuto={} | preço={}",
                atualizado.getId(), cliente.getId(), tipoPasse.getNome(), coroa.getNome(), estatuto, regra.getPreco());
        return atualizado;
    }


    /**
     * Renova um passe expirado ou prestes a expirar (FALTA_RENOVAR).
     *
     * Regras:
     * - Só é possível renovar passes com estadoComercial=PAID
     * - Recalcula o preço com o estatuto actual do cliente
     * - Actualiza dataFim com base no duracaoDias do TipoPasse
     * - Cria novo Pagamento e confirma imediatamente
     * - Envia email de confirmação
     *
     * @param passeId id do passe a renovar
     * @param cliente cliente autenticado (para verificar ownership)
     * @param metodo    método de pagamento (CARTAO | MBWAY | SALDO_CONTA)
     * @param cartaoId  id do cartão a usar; null mantém comportamento simulado sem cartão específico
     * @return passe renovado
     */
    @Transactional
    public Passe renovarPasse(Long passeId, Cliente cliente, String metodo) {
        return renovarPasse(passeId, cliente, metodo, null);
    }

    @Transactional
    public Passe renovarPasse(Long passeId, Cliente cliente, String metodo, Long cartaoId) {
        Passe passe = passeService.obterPasse(passeId)
                .orElseThrow(() -> new IllegalArgumentException("Passe não encontrado."));

        if (!passe.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Este passe não pertence ao utilizador.");
        }

        // Só renova passes já pagos (expirados ou prestes a expirar)
        if (passe.getEstadoComercial() != EstadoComercialPasse.PAID) {
            throw new IllegalStateException("Só é possível renovar passes pagos.");
        }

        EstadoOperacionalPasse op = passe.getEstadoOperacional();
        if (op != EstadoOperacionalPasse.FALTA_RENOVAR && op != EstadoOperacionalPasse.ACTIVE) {
            throw new IllegalStateException(
                "O passe não se encontra num estado que permita renovação (estado: " + op + ").");
        }

        // Recalcular preço com estatuto actual
        TipoEstatuto novoEstatuto = estatutoService.resolverEstatutoEfetivo(cliente);
        RegraPreco novaRegra = pricingService.resolverRegra(novoEstatuto, passe.getTipoPasse(), passe.getCoroa())
                .orElseThrow(() -> new IllegalStateException(
                        "Não existe regra de preço para a renovação: " + novoEstatuto));

        // Actualizar validade do passe in-place
        java.time.LocalDate novaDataInicio = java.time.LocalDate.now();
        java.time.LocalDate novaDataFim = novaDataInicio.plusDays(passe.getTipoPasse().getDuracaoDias());

        passe.setDataInicio(novaDataInicio);
        passe.setDataFim(novaDataFim);
        passe.setTipoEstatutoAplicado(novoEstatuto);
        passe.setRegraPreco(novaRegra);
        passe.setPrecoAplicado(novaRegra.getPreco());
        passe.setEstadoComercial(EstadoComercialPasse.PENDING_PAYMENT);
        passe.setEstadoOperacional(EstadoOperacionalPasse.INACTIVE);
        Passe passeAtualizado = passeService.guardar(passe);

        String emailCliente = cliente.getUtilizador().getEmail();

        // Renovação gratuita: ativar diretamente sem pagamento pendente
        if (novaRegra.getPreco().compareTo(java.math.BigDecimal.ZERO) == 0) {
            passeAtualizado.setEstadoComercial(EstadoComercialPasse.PAID);
            passeAtualizado.setEstadoOperacional(EstadoOperacionalPasse.ACTIVE);
            Passe passeGratuito = passeService.guardar(passeAtualizado);
            pagamentoService.criarPagamentoGratuito(passeGratuito, emailCliente);
            emailService.enviarConfirmacaoPasse(emailCliente, passeGratuito);
            auditLogService.registar(emailCliente, "USER", "PASSE_RENOVADO",
                    "passe", "Passe id=" + passeId + " renovado GRATUITAMENTE"
                            + " | novoEstatuto=" + novoEstatuto
                            + " | novaDataFim=" + novaDataFim, true);
            log.info("[RENOVAÇÃO] Passe id={} renovado GRATUITAMENTE | cliente={} | estatuto={} | dataFim={}",
                    passeId, emailCliente, novoEstatuto, novaDataFim);
            return passeGratuito;
        }

        // Criar pagamento e confirmar conforme método
        if ("SALDO_CONTA".equalsIgnoreCase(metodo)) {
            Conta conta = contaService.obterOuCriarConta(cliente);
            if (conta.getSaldo().compareTo(novaRegra.getPreco()) < 0) {
                throw new IllegalStateException("Saldo insuficiente para renovar o passe.");
            }
            contaService.debitar(conta, novaRegra.getPreco(), TipoMovimentoConta.COMPRA_PASSE,
                    "Renovação de passe #" + passeId, null);
            Pagamento novoPagamento = pagamentoService.criarPagamento(passeAtualizado);
            pagamentoService.confirmarPagamento(novoPagamento, metodo, emailCliente);
        } else {
            String cartaoToken = resolverTokenCartao(cartaoId, cliente);
            Pagamento novoPagamento = pagamentoService.criarPagamento(passeAtualizado);
            pagamentoService.confirmarPagamento(novoPagamento, metodo, emailCliente, cartaoToken);
        }

        // Activar passe renovado
        Passe passeRenovado = passeService.ativarPasse(passeAtualizado);
        passeRenovado.setDataInicio(novaDataInicio);
        passeRenovado.setDataFim(novaDataFim);
        Passe passeRenovadoSalvo = passeService.guardar(passeRenovado);

        // Email de confirmação
        emailService.enviarConfirmacaoPasse(emailCliente, passeRenovadoSalvo);

        auditLogService.registar(emailCliente, "USER", "PASSE_RENOVADO",
                "passe", "Passe id=" + passeId + " renovado via " + metodo
                        + " | novoEstatuto=" + novoEstatuto
                        + " | novoPreço=" + novaRegra.getPreco()
                        + " | novaDataFim=" + novaDataFim, true);

        log.info("[RENOVAÇÃO] Passe id={} renovado | cliente={} | estatuto={} | preço={} | dataFim={}",
                passeId, emailCliente, novoEstatuto, novaRegra.getPreco(), novaDataFim);

        return passeRenovadoSalvo;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Valida que o cartão existe, pertence ao cliente e está ativo.
     * Devolve o tokenSimulado para usar como referência no pagamento.
     * Se cartaoId for null, devolve null (compatibilidade com fluxos sem cartão).
     */
    private String resolverTokenCartao(Long cartaoId, Cliente cliente) {
        if (cartaoId == null) return null;
        CartaoPagamento cartao = cartaoRepo.findByIdAndCliente(cartaoId, cliente)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Cartão não encontrado ou não pertence ao utilizador."));
        if (!cartao.isAtivo()) {
            throw new IllegalArgumentException("O cartão selecionado está inativo.");
        }
        return cartao.getTokenSimulado();
    }
}
