package com.example.loginapi.service.pagamentos;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.Conta;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.pagamentos.enums.TipoMovimentoConta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.loginapi.service.titulos.CompraPasseService;
import com.example.loginapi.service.clientes.ContaService;
import com.example.loginapi.service.titulos.PasseService;


@Service
public class SaldoCompraService {

    private final ContaService contaService;
    private final PagamentoService pagamentoService;
    @Autowired
    @org.springframework.context.annotation.Lazy
    private CompraPasseService compraPasseService;
    private final PasseService passeService;

    @Autowired
    public SaldoCompraService(ContaService contaService, PagamentoService pagamentoService,
                               PasseService passeService) {
        this.contaService = contaService;
        this.pagamentoService = pagamentoService;
        this.passeService = passeService;
    }

    // ─── Bilhete ──────────────────────────────────────────────────────────────

    @Transactional
    public Transacao pagarBilheteComSaldo(Cliente cliente, Transacao transacao) {
        // 1. Obter ou criar conta
        Conta conta = contaService.obterOuCriarConta(cliente);

        // 2. Validar saldo suficiente
        if (conta.getSaldo().compareTo(transacao.getPreco()) < 0) {
            throw new IllegalStateException(
                    "Saldo insuficiente. Carregue a conta antes de comprar o bilhete.");
        }

        // 3. Debitar saldo — se a confirmação a seguir falhar, o rollback reverte este débito
        contaService.debitar(conta, transacao.getPreco(), TipoMovimentoConta.COMPRA_BILHETE,
                "Compra de bilhete #" + transacao.getId(), null);

        // 4. Confirmar pagamento (gera QR definitivo, fatura, envia email)
        return pagamentoService.confirmarPagamentoTransacao(transacao, "SALDO_CONTA");
    }

    // ─── Passe ────────────────────────────────────────────────────────────────

    @Transactional
    public Pagamento pagarPasseComSaldo(Cliente cliente, Long passeId) {
        // 1. Obter passe (para saber o preço)
        Passe passe = passeService.obterPasse(passeId)
                .orElseThrow(() -> new IllegalArgumentException("Passe não encontrado."));

        // 2. Obter ou criar conta
        Conta conta = contaService.obterOuCriarConta(cliente);

        // 3. Validar saldo suficiente
        if (conta.getSaldo().compareTo(passe.getPrecoAplicado()) < 0) {
            throw new IllegalStateException(
                    "Saldo insuficiente. Carregue a conta antes de pagar o passe.");
        }

        // 4. Debitar saldo — se a ativação a seguir falhar, o rollback reverte este débito
        contaService.debitar(conta, passe.getPrecoAplicado(), TipoMovimentoConta.COMPRA_PASSE,
                "Compra de passe #" + passeId, null);

        // 5. Confirmar pagamento e ativar passe
        return compraPasseService.simularPagamento(passeId, "SALDO_CONTA");
    }
}
