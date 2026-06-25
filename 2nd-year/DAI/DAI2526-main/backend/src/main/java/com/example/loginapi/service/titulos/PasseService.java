package com.example.loginapi.service.titulos;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.PasseQrToken;
import com.example.loginapi.model.titulos.enums.EstadoComercialPasse;
import com.example.loginapi.model.titulos.enums.EstadoOperacionalPasse;
import com.example.loginapi.repository.clientes.ClienteRepository;
import com.example.loginapi.repository.pagamentos.PagamentoRepository;
import com.example.loginapi.repository.titulos.PasseQrTokenRepository;
import com.example.loginapi.repository.titulos.PasseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import com.example.loginapi.service.comunicacao.NotificacaoService;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.comunicacao.Aviso;
import com.example.loginapi.model.pagamentos.Conta;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.comunicacao.Notificacao;


@Service
public class PasseService {

    private static final Logger log = LoggerFactory.getLogger(PasseService.class);
    private static final long QR_VALIDADE_MINUTOS = 10;

    @Autowired private PasseRepository passeRepo;
    @Autowired private PagamentoRepository pagamentoRepo;
    @Autowired private PasseQrTokenRepository qrTokenRepo;
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private NotificacaoService notificacaoService;

    @Value("${tub.passe.aviso-expiracao-dias:7}")
    private int diasAvisoExpiracao;

    public Passe guardar(Passe passe) { return passeRepo.save(passe); }

    @Transactional
    public Passe ativarPasse(Passe passe) {
        passe.setEstadoComercial(EstadoComercialPasse.PAID);
        passe.setEstadoOperacional(EstadoOperacionalPasse.ACTIVE);
        passe.setDataInicio(LocalDate.now());
        passe.setDataFim(LocalDate.now().plusDays(passe.getTipoPasse().getDuracaoDias()));
        return passeRepo.save(passe);
    }

    @Transactional
    public Passe cancelarPasse(Long passeId, Cliente cliente) {
        Passe passe = passeRepo.findById(passeId)
                .orElseThrow(() -> new IllegalArgumentException("Passe não encontrado."));
        if (!passe.getCliente().getId().equals(cliente.getId()))
            throw new IllegalArgumentException("Este passe não pertence ao utilizador.");
        // UC 1.1.4 - Anular Passe pode ocorrer em qualquer estado
        passe.setEstadoComercial(EstadoComercialPasse.CANCELLED);
        passe.setEstadoOperacional(EstadoOperacionalPasse.INVALIDO);
        qrTokenRepo.revogarTodosDoPass(passe, Instant.now());
        return passeRepo.save(passe);
    }

    public List<Passe> listarPasses(Cliente cliente) {
        List<Passe> passes = passeRepo.findByClienteOrderByCriadoEmDesc(cliente);
        passes.forEach(this::atualizarEstadoOperacionalSeNecessario);
        return passes;
    }

    public Optional<Passe> obterPasse(Long id) {
        Optional<Passe> opt = passeRepo.findById(id);
        opt.ifPresent(this::atualizarEstadoOperacionalSeNecessario);
        return opt;
    }

    @Transactional
    public Passe atualizarEstadoOperacionalSeNecessario(Passe passe) {
        // ── [DEBUG] Entrada no método ──────────────────────────────────────────
        System.out.println("[AEONS] atualizarEstadoOperacionalSeNecessario INÍCIO");
        if (passe == null) {
            // ⚠️ CAUSA DIRECTA DO 500: passe é null — getNPE nas linhas seguintes
            System.out.println("[AEONS] ATENÇÃO: passe == null! A lançar NPE a seguir.");
        } else {
            System.out.println("[AEONS] passe.id=" + passe.getId()
                    + " | estadoOperacional=" + passe.getEstadoOperacional()
                    + " | estadoComercial=" + passe.getEstadoComercial()
                    + " | dataInicio=" + passe.getDataInicio()
                    + " | dataFim=" + passe.getDataFim()
                    + " | qrTokenAtual=" + passe.getQrTokenAtual()
                    + " | qrGeradoEm=" + passe.getQrGeradoEm()
                    + " | qrExpiraEm=" + passe.getQrExpiraEm()
                    + " | codigoQr=" + passe.getCodigoQr());
        }

        EstadoOperacionalPasse esperado;

        // ── [DEBUG] Antes de getEstadoComercial() (1ª utilização) ─────────────
        System.out.println("[AEONS] Antes de getEstadoComercial() [ramo CANCELLED] | valor=" + passe.getEstadoComercial());
        if (passe.getEstadoComercial() == EstadoComercialPasse.CANCELLED) {
            esperado = EstadoOperacionalPasse.INVALIDO;
        // ── [DEBUG] Antes de getEstadoComercial() (2ª utilização) ─────────────
        } else if (passe.getEstadoComercial() != EstadoComercialPasse.PAID) {
            System.out.println("[AEONS] estadoComercial != PAID → INACTIVE | valor=" + passe.getEstadoComercial());
            esperado = EstadoOperacionalPasse.INACTIVE;
        // ── [DEBUG] Antes de getDataFim() ─────────────────────────────────────
        } else if (passe.getDataFim() != null && LocalDate.now().isAfter(passe.getDataFim())) {
            System.out.println("[AEONS] dataFim ultrapassada → FALTA_RENOVAR | dataFim=" + passe.getDataFim());
            esperado = EstadoOperacionalPasse.FALTA_RENOVAR;
        // ── [DEBUG] Antes de getDataInicio() ──────────────────────────────────
        } else if (passe.getDataInicio() == null || LocalDate.now().isBefore(passe.getDataInicio())) {
            System.out.println("[AEONS] dataInicio null ou futura → INACTIVE | dataInicio=" + passe.getDataInicio());
            esperado = EstadoOperacionalPasse.INACTIVE;
        } else {
            System.out.println("[AEONS] Todas as condições OK → ACTIVE");
            esperado = EstadoOperacionalPasse.ACTIVE;
        }

        // ── [DEBUG] Antes de getEstadoOperacional() (comparação final) ─────────
        System.out.println("[AEONS] estadoOperacional actual=" + passe.getEstadoOperacional() + " | esperado=" + esperado);
        if (passe.getEstadoOperacional() != esperado) {
            System.out.println("[AEONS] Estado alterado de " + passe.getEstadoOperacional() + " para " + esperado + " | A guardar...");
            passe.setEstadoOperacional(esperado);
            return passeRepo.save(passe);
        }
        System.out.println("[AEONS] Estado já correcto, sem alteração.");
        return passe;
    }

    /**
     * Returns an existing valid QR token for the pass, or generates a new one.
     * The token is valid for QR_VALIDADE_MINUTOS minutes from generation.
     * Only active passes can obtain QR tokens.
     *
     * Business rule (temporary): only one pass per account may have an active QR at a time.
     * Active tokens for all other passes of the same account are explicitly revoked here.
     * To allow multiple passes per account to have simultaneous QR tokens in the future,
     * remove the call to revogarTokensDeOutrosPassesDaConta().
     */
    @Transactional
    public PasseQrToken obterOuGerarQrToken(Passe passe) {
        passe = atualizarEstadoOperacionalSeNecessario(passe);
        if (passe.getEstadoOperacional() != EstadoOperacionalPasse.ACTIVE) {
            throw new IllegalStateException("Só é possível gerar QR para passes ativos.");
        }

        Instant agora = Instant.now();

        // ── Serialização por conta (anti-race-condition) ──────────────────────────
        // Acquires SELECT ... FOR UPDATE on the clientes row before any read/write on
        // passe_qr_tokens. A concurrent request for another pass of the same account
        // will block here until this transaction commits, ensuring it sees our INSERT
        // when it runs its own revocation query.
        // To remove this guard (e.g. if the rule changes to allow QR per pass),
        // delete this line together with the revogarTokensDeOutrosPassesDaConta() call.
        clienteRepo.findByIdForUpdate(passe.getCliente().getId());

        // ── Regra de negócio: só um passe por conta pode ter QR activo de cada vez. ──
        // Revoga explicitamente os tokens activos de todos os outros passes da mesma conta.
        // Para permitir QR simultâneos por conta no futuro, basta remover esta linha.
        qrTokenRepo.revogarTokensDeOutrosPassesDaConta(
                passe.getCliente().getId(), passe.getId(), agora);

        List<PasseQrToken> activos = qrTokenRepo.findActivosByPasse(passe, agora);
        if (!activos.isEmpty()) {
            return activos.get(0);
        }
        PasseQrToken novo = new PasseQrToken();
        novo.setPasse(passe);
        novo.setToken("PASS-" + UUID.randomUUID().toString().replace("-", "").toUpperCase());
        novo.setGeradoEm(agora);
        novo.setExpiraEm(agora.plusSeconds(QR_VALIDADE_MINUTOS * 60));
        return qrTokenRepo.save(novo);
    }

    public long segundosRestantesQrToken(PasseQrToken qrToken) {
        return Math.max(0, Duration.between(Instant.now(), qrToken.getExpiraEm()).getSeconds());
    }

    public String construirPayloadQr(Passe passe, PasseQrToken qrToken) {
        return String.format(
                "{\"tipo\":\"PASSE\",\"passeId\":%d,\"clienteId\":%d,\"token\":\"%s\",\"expiraEm\":\"%s\"}",
                passe.getId(),
                passe.getCliente().getId(),
                qrToken.getToken(),
                qrToken.getExpiraEm().toString()
        );
    }

    public String obterFaturaNumero(Passe passe) {
        Optional<Pagamento> pagamento = pagamentoRepo.findFirstByPasseOrderByCriadoEmDesc(passe);
        return pagamento.map(Pagamento::getFaturaNumero).orElse(null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Validade e aviso de expiração
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Verifica se um passe está válido neste momento.
     * Um passe é válido se: estadoOperacional=ACTIVE e data actual entre dataInicio e dataFim.
     */
    public boolean estaValido(Passe passe) {
        if (passe.getEstadoOperacional() != EstadoOperacionalPasse.ACTIVE) return false;
        LocalDate hoje = LocalDate.now();
        if (passe.getDataInicio() != null && hoje.isBefore(passe.getDataInicio())) return false;
        if (passe.getDataFim() != null && hoje.isAfter(passe.getDataFim())) return false;
        return true;
    }

    /**
     * Devolve os passes do cliente que expiram nos próximos {@code diasAvisoExpiracao} dias.
     */
    public List<Passe> verificarPassesPrestoExpirar(Cliente cliente) {
        LocalDate limite = LocalDate.now().plusDays(diasAvisoExpiracao);
        return listarPasses(cliente).stream()
                .filter(p -> p.getEstadoComercial() == EstadoComercialPasse.PAID
                        && p.getDataFim() != null
                        && !LocalDate.now().isAfter(p.getDataFim())   // ainda não expirou
                        && !p.getDataFim().isAfter(limite))            // mas expira dentro do limite
                .collect(Collectors.toList());
    }

    /**
     * Verifica e envia notificações in-app para passes prestes a expirar.
     * Deve ser chamado após listarPasses() para garantir que os estados estão actualizados.
     */
    public void notificarExpiracoesProximas(Cliente cliente) {
        List<Passe> prestoExpirar = verificarPassesPrestoExpirar(cliente);
        for (Passe p : prestoExpirar) {
            long diasRestantes = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), p.getDataFim());
            String email = cliente.getUtilizador().getEmail();
            notificacaoService.criarParaUtilizador(
                    email,
                    "Passe prestes a expirar",
                    "O seu passe " + p.getTipoPasse().getNome() + " (" + p.getCoroa().getNome()
                    + ") expira em " + diasRestantes + " dia(s) (" + p.getDataFim() + ")."
                    + " Renove antes que expire.",
                    com.example.loginapi.model.comunicacao.Notificacao.Tipo.AVISO
            );
            log.info("[PASSE] Aviso de expiração in-app criado | passe={} | cliente={} | diasRestantes={}",
                    p.getId(), email, diasRestantes);
        }
    }
}
