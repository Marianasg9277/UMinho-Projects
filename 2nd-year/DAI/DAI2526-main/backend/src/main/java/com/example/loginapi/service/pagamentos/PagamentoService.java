package com.example.loginapi.service.pagamentos;

import com.example.loginapi.dto.CompraGuestRequestDTO;
import com.example.loginapi.dto.ResultadoPagamentoSimulado;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import com.example.loginapi.repository.pagamentos.PagamentoRepository;
import com.example.loginapi.repository.titulos.TipoBilheteRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.service.comunicacao.EmailService;
import com.example.loginapi.service.titulos.QrCodeService;
import com.example.loginapi.model.titulos.TipoBilhete;


@Service
public class PagamentoService {

    private static final Logger log = LoggerFactory.getLogger(PagamentoService.class);

    @Autowired private PagamentoRepository pagamentoRepo;
    @Autowired private TransacaoRepository transacaoRepo;
    @Autowired private TipoBilheteRepository tipoBilheteRepo;
    @Autowired private LinhaRepository linhaRepo;
    @Autowired private EmailService emailService;
    @Autowired private FaturaService faturaService;
    @Autowired private QrCodeService qrCodeService;

    @Value("${tub.pagamento.falha-simulada:false}")
    private boolean falhaSimulada;

    // ─────────────────────────────────────────────────────────────────────────
    // Pagamentos de Passes
    // ─────────────────────────────────────────────────────────────────────────

    public Pagamento criarPagamento(Passe passe) {
        Pagamento pagamento = new Pagamento();
        pagamento.setPasse(passe);
        pagamento.setValor(passe.getPrecoAplicado());
        pagamento.setEstado(EstadoPagamento.NOT_STARTED);
        pagamento.setMetodo("SIMULADO");
        return pagamentoRepo.save(pagamento);
    }

    /**
     * Confirma pagamento de passe, gera fatura e envia email de confirmação.
     *
     * @param pagamento   pagamento a confirmar
     * @param metodo      CARTAO | MBWAY | SIMULADO
     * @param emailCliente email para envio da confirmação
     */
    @Transactional
    public Pagamento confirmarPagamento(Pagamento pagamento, String metodo, String emailCliente) {
        return confirmarPagamento(pagamento, metodo, emailCliente, null);
    }

    /**
     * Confirma pagamento com referência de cartão opcional.
     * Se cartaoToken não for null, é incluído na referência externa para auditoria.
     */
    @Transactional
    public Pagamento confirmarPagamento(Pagamento pagamento, String metodo, String emailCliente, String cartaoToken) {
        if (pagamento.getEstado() != EstadoPagamento.NOT_STARTED
                && pagamento.getEstado() != EstadoPagamento.PENDING) {
            throw new IllegalStateException("Pagamento não está num estado que permita processamento.");
        }

        // Simular falha de pagamento (opcional — activado via config)
        if (falhaSimulada && new Random().nextInt(5) == 0) {
            pagamento.setEstado(EstadoPagamento.FAILED);
            pagamentoRepo.save(pagamento);
            log.warn("[PAGAMENTO] Falha simulada para passe id={}", pagamento.getPasse().getId());
            throw new IllegalStateException("Pagamento recusado (falha simulada). Tente novamente.");
        }

        pagamento.setEstado(EstadoPagamento.PAID);
        pagamento.setMetodo(metodo != null ? metodo : "CARTAO");
        pagamento.setReferenciaExterna(gerarReferenciaExterna(metodo, cartaoToken));

        if (pagamento.getFaturaNumero() == null || pagamento.getFaturaNumero().isBlank()) {
            pagamento.setFaturaNumero(gerarNumeroFatura("PS", pagamento.getPasse().getId()));
            pagamento.setFaturaEmitidaEm(LocalDateTime.now());
        }
        Pagamento saved = pagamentoRepo.save(pagamento);

        log.info("[PAGAMENTO] Passe id={} pago via {} | fatura={} | cliente={}",
                pagamento.getPasse().getId(), metodo, saved.getFaturaNumero(), emailCliente);

        // Enviar email com fatura PDF em background (não bloqueia resposta)
        Passe passe = pagamento.getPasse();
        byte[] pdf = faturaService.gerarPdfFaturaPasse(passe, saved);
        String htmlFatura = faturaService.gerarHtmlFaturaPasse(passe, saved);
        emailService.enviarFatura(
                emailCliente,
                "TUB – Fatura " + saved.getFaturaNumero(),
                htmlFatura,
                pdf,
                saved.getFaturaNumero()
        );

        return saved;
    }

    /** Compatibilidade retroactiva — usa "CARTAO" como método padrão. */
    public Pagamento confirmarPagamento(Pagamento pagamento) {
        return confirmarPagamento(pagamento, "CARTAO", null);
    }

    /**
     * Regista um pagamento gratuito (valor=0.00) para passes com preço zero.
     * Não passa pela lógica de falha simulada nem exige saldo/cartão.
     */
    @Transactional
    public Pagamento criarPagamentoGratuito(Passe passe, String emailCliente) {
        Pagamento pagamento = new Pagamento();
        pagamento.setPasse(passe);
        pagamento.setValor(java.math.BigDecimal.ZERO);
        pagamento.setEstado(EstadoPagamento.PAID);
        pagamento.setMetodo("GRATUITO");
        pagamento.setFaturaNumero(gerarNumeroFatura("PS", passe.getId()));
        pagamento.setFaturaEmitidaEm(LocalDateTime.now());
        Pagamento saved = pagamentoRepo.save(pagamento);
        log.info("[PAGAMENTO] Passe id={} gratuito | fatura={} | cliente={}",
                passe.getId(), saved.getFaturaNumero(), emailCliente);
        return saved;
    }

    public Optional<Pagamento> obterPagamentoPorPasse(Passe passe) {
        return pagamentoRepo.findFirstByPasseOrderByCriadoEmDesc(passe);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Pagamentos de Bilhetes (utilizador autenticado)
    // ─────────────────────────────────────────────────────────────────────────

    @Transactional
    public Transacao confirmarPagamentoTransacao(Transacao transacao, String metodo) {
        if (transacao.getEstadoPagamento() == EstadoPagamento.PAID) {
            throw new IllegalStateException("Este bilhete já está pago.");
        }

        if (falhaSimulada && new Random().nextInt(5) == 0) {
            transacao.setEstadoPagamento(EstadoPagamento.FAILED);
            transacaoRepo.save(transacao);
            log.warn("[PAGAMENTO] Falha simulada para transação id={}", transacao.getId());
            throw new IllegalStateException("Pagamento recusado (falha simulada). Tente novamente.");
        }

        transacao.setEstadoPagamento(EstadoPagamento.PAID);
        transacao.setCodigoQr("BILHETE-" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        // Calcular validade com base no TipoBilhete
        if (transacao.getValidoAte() == null) {
            int validadeHoras = transacao.getTipoBilhete().getValidadeHoras();
            transacao.setValidoAte(LocalDateTime.now().plusHours(validadeHoras));
        }
        transacao.setFaturaNumero(gerarNumeroFatura("BL", transacao.getId()));
        transacao.setFaturaEmitidaEm(LocalDateTime.now());
        Transacao saved = transacaoRepo.save(transacao);

        log.info("[PAGAMENTO] Bilhete id={} pago via {} | fatura={} | válido até={}",
                saved.getId(), metodo, saved.getFaturaNumero(), saved.getValidoAte());

        // Enviar email com fatura
        String emailDestino = saved.getCliente() != null
                ? saved.getCliente().getUtilizador().getEmail()
                : saved.getGuestEmail();
        if (emailDestino != null) {
            byte[] pdf = faturaService.gerarPdfFaturaBilhete(saved);
            String htmlFatura = faturaService.gerarHtmlFaturaBilhete(saved);
            emailService.enviarFatura(emailDestino,
                    "TUB – Fatura " + saved.getFaturaNumero(), htmlFatura, pdf, saved.getFaturaNumero());

            byte[] qrPng = null;
            String qrBase64 = null;
            try {
                qrPng = qrCodeService.gerarPng(saved.getCodigoQr(), 200, 200);
                qrBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(qrPng);
            } catch (Exception e) {
                log.warn("[PAGAMENTO] Não foi possível gerar imagem QR para email: {}", e.getMessage());
            }
            byte[] pdfBilhete = faturaService.gerarPdfBilhete(saved, qrPng);
            emailService.enviarBilhete(saved, qrBase64, pdfBilhete);
        }

        return saved;
    }

    /** Compatibilidade retroactiva */
    @Transactional
    public Transacao confirmarPagamentoTransacao(Transacao transacao) {
        return confirmarPagamentoTransacao(transacao, "CARTAO");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Compra Guest
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Cria e confirma imediatamente a compra de um bilhete por utilizador guest.
     * Inclui: nome, email, NIF, validade calculada, fatura e envio de email.
     */
    @Transactional
    public Transacao comprarBilheteGuest(CompraGuestRequestDTO dto) {
        com.example.loginapi.model.titulos.TipoBilhete tipoBilhete = tipoBilheteRepo.findById(dto.getTipoBilheteId())
                .orElseThrow(() -> new IllegalArgumentException("Tipo de bilhete não encontrado: " + dto.getTipoBilheteId()));

        Linha linha = null;
        if (dto.getLinhaId() != null) {
            linha = linhaRepo.findById(dto.getLinhaId())
                    .orElseThrow(() -> new IllegalArgumentException("Linha não encontrada: " + dto.getLinhaId()));
        }

        Transacao transacao = new Transacao();
        transacao.setTipoBilhete(tipoBilhete);
        transacao.setLinha(linha);
        transacao.setGuestEmail(dto.getGuestEmail().trim().toLowerCase());
        transacao.setGuestNome(dto.getGuestNome().trim());
        transacao.setGuestNif(dto.getGuestNif() != null ? dto.getGuestNif().trim() : null);
        // Prioridade: coroaId explícito do request; fallback: coroa do tipo de bilhete
        if (dto.getCoroaId() != null) {
            transacao.setCoroaId(dto.getCoroaId());
        } else if (tipoBilhete.getCoroa() != null) {
            transacao.setCoroaId(tipoBilhete.getCoroa().getId());
        }
        transacao.setPreco(tipoBilhete.getPreco());
        transacao.setDataCompra(LocalDateTime.now());

        // Definir todos os campos obrigatórios em memória ANTES do primeiro save().
        // Nunca persistir com codigo_qr = null — a coluna é NOT NULL na BD.
        // O fluxo guest é síncrono: não existe gateway externo nem callback,
        // por isso não há motivo para guardar um rascunho NOT_STARTED.
        transacao.setEstadoPagamento(EstadoPagamento.PAID);
        transacao.setCodigoQr("GUEST-" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        int validadeHoras = tipoBilhete.getValidadeHoras();
        transacao.setValidoAte(LocalDateTime.now().plusHours(validadeHoras));
        transacao.setFaturaEmitidaEm(LocalDateTime.now());
        // faturaNumero gerada após save para usar o id da BD
        Transacao saved = transacaoRepo.save(transacao);
        saved.setFaturaNumero(gerarNumeroFatura("BG", saved.getId()));
        saved = transacaoRepo.save(saved);

        log.info("[COMPRA-GUEST] Bilhete id={} | guest={} | NIF={} | válido até={}",
                saved.getId(), saved.getGuestEmail(), saved.getGuestNif(), saved.getValidoAte());

        // 3. Gerar QR PNG (partilhado entre PDF e email)
        byte[] qrPng = null;
        String qrBase64 = null;
        try {
            qrPng = qrCodeService.gerarPng(saved.getCodigoQr(), 200, 200);
            qrBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(qrPng);
        } catch (Exception e) {
            log.warn("[COMPRA-GUEST] Não foi possível gerar imagem QR: {}", e.getMessage());
        }

        // 4. Gerar PDF do bilhete (com QR incorporado) — distinto da fatura
        byte[] pdfBilhete = faturaService.gerarPdfBilhete(saved, qrPng);

        // 5. Enviar fatura com PDF da fatura em anexo
        byte[] pdfFatura = faturaService.gerarPdfFaturaBilhete(saved);
        String htmlFatura = faturaService.gerarHtmlFaturaBilhete(saved);
        emailService.enviarFatura(saved.getGuestEmail(),
                "TUB – Fatura " + saved.getFaturaNumero(), htmlFatura, pdfFatura, saved.getFaturaNumero());

        // 6. Enviar bilhete com QR inline e PDF do bilhete em anexo
        emailService.enviarBilhete(saved, qrBase64, pdfBilhete);

        return saved;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Simulação de pagamento genérico (carregamentos e futuros usos)
    // ─────────────────────────────────────────────────────────────────────────

    public ResultadoPagamentoSimulado simularPagamento(String metodo) {
        if (falhaSimulada && new Random().nextInt(5) == 0) {
            log.warn("[PAGAMENTO-SIM] Pagamento rejeitado | metodo={}", metodo);
            return new ResultadoPagamentoSimulado(false, null,
                    "Pagamento recusado (falha simulada). Tente novamente.");
        }
        String ref = gerarReferenciaExterna(metodo);
        log.info("[PAGAMENTO-SIM] Pagamento aprovado | metodo={} | ref={}", metodo, ref);
        return new ResultadoPagamentoSimulado(true, ref, "Pagamento aprovado.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    public String gerarNumeroFatura(String prefixo, Long idObjeto) {
        return String.format("FT-%s-%d-%d", prefixo, idObjeto, System.currentTimeMillis());
    }

    private String gerarReferenciaExterna(String metodo) {
        return gerarReferenciaExterna(metodo, null);
    }

    private String gerarReferenciaExterna(String metodo, String cartaoToken) {
        if ("MBWAY".equalsIgnoreCase(metodo)) return "MBWAY-" + System.currentTimeMillis();
        if (cartaoToken != null) {
            String prefixo = cartaoToken.substring(0, Math.min(8, cartaoToken.length()));
            return "CARTAO-" + prefixo + "-" + System.currentTimeMillis();
        }
        return "CARTAO-" + System.currentTimeMillis();
    }
}
