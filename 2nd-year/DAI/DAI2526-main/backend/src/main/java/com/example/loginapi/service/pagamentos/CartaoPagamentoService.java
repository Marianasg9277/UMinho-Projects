package com.example.loginapi.service.pagamentos;

import com.example.loginapi.dto.CartaoPagamentoRequest;
import com.example.loginapi.model.pagamentos.CartaoPagamento;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.repository.pagamentos.CartaoPagamentoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class CartaoPagamentoService {

    @Autowired
    private CartaoPagamentoRepository cartaoRepo;

    // ─── Listar ──────────────────────────────────────────────────────────────

    public List<CartaoPagamento> listarCartoes(Cliente cliente) {
        return cartaoRepo.findByClienteAndAtivoTrueOrderByCriadoEmDesc(cliente);
    }

    // ─── Associar ────────────────────────────────────────────────────────────

    @Transactional
    public CartaoPagamento associarCartao(Cliente cliente, CartaoPagamentoRequest req) {
        validarRequest(req);

        String numero = req.getNumeroCartao().replaceAll("\\s+", "");

        CartaoPagamento cartao = new CartaoPagamento();
        cartao.setCliente(cliente);
        cartao.setNomeTitular(req.getNomeTitular().trim());
        cartao.setUltimos4Digitos(numero.substring(numero.length() - 4));
        cartao.setBandeira(detetarBandeira(numero));
        cartao.setMesValidade(req.getMesValidade());
        cartao.setAnoValidade(req.getAnoValidade());
        cartao.setTokenSimulado("CARD_SIM_" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        cartao.setAtivo(true);

        boolean primeiroCartao = cartaoRepo.countByClienteAndAtivoTrue(cliente) == 0;
        cartao.setPredefinido(primeiroCartao);

        return cartaoRepo.save(cartao);
    }

    // ─── Definir predefinido ──────────────────────────────────────────────────

    @Transactional
    public CartaoPagamento definirPredefinido(Long cartaoId, Cliente cliente) {
        CartaoPagamento cartao = cartaoRepo.findByIdAndCliente(cartaoId, cliente)
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado."));

        if (!cartao.isAtivo()) {
            throw new IllegalArgumentException("Não é possível definir como predefinido um cartão inativo.");
        }

        cartaoRepo.limparPredefinidosDoCliente(cliente);
        cartao.setPredefinido(true);
        return cartaoRepo.save(cartao);
    }

    // ─── Remover (soft delete) ────────────────────────────────────────────────

    @Transactional
    public void removerCartao(Long cartaoId, Cliente cliente) {
        CartaoPagamento cartao = cartaoRepo.findByIdAndCliente(cartaoId, cliente)
                .orElseThrow(() -> new IllegalArgumentException("Cartão não encontrado."));

        cartao.setAtivo(false);
        cartao.setPredefinido(false);
        cartaoRepo.save(cartao);
    }

    // ─── Validação ───────────────────────────────────────────────────────────

    void validarRequest(CartaoPagamentoRequest req) {
        if (req.getNomeTitular() == null || req.getNomeTitular().isBlank()) {
            throw new IllegalArgumentException("O nome do titular é obrigatório.");
        }

        String numero = req.getNumeroCartao() == null ? "" : req.getNumeroCartao().replaceAll("\\s+", "");
        if (!numero.matches("\\d{13,19}")) {
            throw new IllegalArgumentException("O número do cartão deve ter entre 13 e 19 dígitos numéricos.");
        }
        if (!passaLuhn(numero)) {
            throw new IllegalArgumentException("Número de cartão inválido.");
        }

        String cvv = req.getCvv() == null ? "" : req.getCvv().trim();
        if (!cvv.matches("\\d{3,4}")) {
            throw new IllegalArgumentException("O CVV deve ter 3 ou 4 dígitos.");
        }

        int mes = req.getMesValidade();
        int ano = req.getAnoValidade();
        if (mes < 1 || mes > 12) {
            throw new IllegalArgumentException("O mês de validade deve estar entre 1 e 12.");
        }

        LocalDate hoje = LocalDate.now();
        LocalDate expiracao = LocalDate.of(ano, mes, 1).plusMonths(1).minusDays(1);
        if (expiracao.isBefore(hoje)) {
            throw new IllegalArgumentException("O cartão está expirado.");
        }
    }

    // Algoritmo de Luhn
    boolean passaLuhn(String numero) {
        int soma = 0;
        boolean dobrar = false;
        for (int i = numero.length() - 1; i >= 0; i--) {
            int digito = numero.charAt(i) - '0';
            if (dobrar) {
                digito *= 2;
                if (digito > 9) digito -= 9;
            }
            soma += digito;
            dobrar = !dobrar;
        }
        return soma % 10 == 0;
    }

    // Deteção de bandeira pelo prefixo do número
    String detetarBandeira(String numero) {
        if (numero.matches("^4\\d+")) return "VISA";
        if (numero.matches("^5[1-5]\\d+") || numero.matches("^2(2[2-9][1-9]|[3-6]\\d{2}|7[01]\\d|720)\\d+")) return "MASTERCARD";
        if (numero.matches("^3[47]\\d+")) return "AMEX";
        if (numero.matches("^6(011|5\\d{2})\\d+")) return "DISCOVER";
        return "OUTRO";
    }
}
