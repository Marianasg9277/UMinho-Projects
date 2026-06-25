package com.example.loginapi.config;

import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.frota.Autocarro;
import com.example.loginapi.model.infraestrutura.Horario;
import com.example.loginapi.model.comunicacao.Aviso;
import com.example.loginapi.model.comunicacao.AvisoTipo;
import com.example.loginapi.model.titulos.TipoBilhete;
import com.example.loginapi.model.titulos.TipoPasse;
import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.infraestrutura.RegraPreco;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import com.example.loginapi.repository.frota.AutocarroRepository;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import com.example.loginapi.repository.infraestrutura.HorarioRepository;
import com.example.loginapi.repository.comunicacao.AvisoRepository;
import com.example.loginapi.repository.titulos.TipoBilheteRepository;
import com.example.loginapi.repository.clientes.UtilizadorRepository;
import com.example.loginapi.repository.clientes.ClienteRepository;
import com.example.loginapi.repository.titulos.TipoPasseRepository;
import com.example.loginapi.repository.infraestrutura.CoroaRepository;
import com.example.loginapi.repository.infraestrutura.RegraPrecoRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.service.frota.GtfsImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Seeds the database with initial demo data on startup.
 * Runs only when the respective tables are empty so it is idempotent.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired private AutocarroRepository autocarroRepo;
    @Autowired private LinhaRepository linhaRepo;
    @Autowired private HorarioRepository horarioRepo;
    @Autowired private AvisoRepository avisoRepo;
    @Autowired private TipoBilheteRepository tipoBilheteRepo;
    @Autowired private UtilizadorRepository utilizadorRepo;
    @Autowired private ClienteRepository clienteRepo;
    @Autowired private TipoPasseRepository tipoPasseRepo;
    @Autowired private CoroaRepository coroaRepo;
    @Autowired private RegraPrecoRepository regraPrecoRepo;
    @Autowired private TransacaoRepository transacaoRepo;
    @Autowired private BCryptPasswordEncoder passwordEncoder;
    @Autowired private GtfsImportService gtfsImportService;

    @Override
    public void run(ApplicationArguments args) {
        seedAdmin();
        seedMotorista();
        seedFiscalizador();
        seedGestorServicos();
        seedGestorFrotas();
        // Dados reais GTFS: routes.txt, stops.txt, fare_attributes.txt, trips.txt e stop_times.txt.
        // Substitui linhas demo, preços avulso hardcoded e preenche percursos/paragens por linha.
        importarGtfsSeNecessario();
        seedCoroas();          // coroas devem existir antes dos tipos de bilhete
        seedTiposBilhete();
        seedAutocarros();
        // seedHorarios(); // desativado: horários simulados não representam stop_times.txt
        seedAvisos();
        seedTiposPasse();
        seedRegrasPreco();
    }

    // ── Admin seed user ───────────────────────────────────────────────────────

    private void seedAdmin() {
        if (utilizadorRepo.findByEmail("admin@tub.pt").isPresent()) return;

        Utilizador admin = new Utilizador();
        admin.setEmail("admin@tub.pt");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setRole(Utilizador.Role.ADMIN);
        Utilizador savedAdmin = utilizadorRepo.save(admin);

        Cliente adminCliente = new Cliente();
        adminCliente.setPerfil("ADMIN");
        adminCliente.setNome("Administrador");
        adminCliente.setSobrenome("TUB");
        adminCliente.setDataNascimento(LocalDate.of(1990, 1, 1));
        adminCliente.setMorada("Braga, Portugal");
        adminCliente.setNif("000000000");
        adminCliente.setTelefone("253000000");
        adminCliente.setNumeroCartaoCidadao("00000000");
        adminCliente.setDigitoVerificacaoCartaoCidadao("-");
        adminCliente.setUtilizador(savedAdmin);
        clienteRepo.save(adminCliente);
    }

    // ── Linhas ────────────────────────────────────────────────────────────────

    private void seedMotorista() {
        if (utilizadorRepo.findByEmail("motorista@tub.pt").isPresent()) return;

        Utilizador motorista = new Utilizador();
        motorista.setEmail("motorista@tub.pt");
        motorista.setPassword(passwordEncoder.encode("motorista123"));
        motorista.setRole(Utilizador.Role.MOTORISTA);
        utilizadorRepo.save(motorista);
    }

    private void seedFiscalizador() {
        if (utilizadorRepo.findByEmail("fiscalizador@tub.pt").isPresent()) return;

        Utilizador fiscalizador = new Utilizador();
        fiscalizador.setEmail("fiscalizador@tub.pt");
        fiscalizador.setPassword(passwordEncoder.encode("fiscal123"));
        fiscalizador.setRole(Utilizador.Role.FISCALIZADOR);
        utilizadorRepo.save(fiscalizador);
    }

    private void seedGestorServicos() {
        if (utilizadorRepo.findByEmail("gestor.servicos@tub.pt").isPresent()) return;

        Utilizador gestorServicos = new Utilizador();
        gestorServicos.setEmail("gestor.servicos@tub.pt");
        gestorServicos.setPassword(passwordEncoder.encode("servicos123"));
        gestorServicos.setRole(Utilizador.Role.GESTOR_SERVICOS);
        utilizadorRepo.save(gestorServicos);
    }

    private void seedGestorFrotas() {
        if (utilizadorRepo.findByEmail("gestor.frotas@tub.pt").isPresent()) return;

        Utilizador gestorFrotas = new Utilizador();
        gestorFrotas.setEmail("gestor.frotas@tub.pt");
        gestorFrotas.setPassword(passwordEncoder.encode("frotas123"));
        gestorFrotas.setRole(Utilizador.Role.GESTOR_FROTAS);
        utilizadorRepo.save(gestorFrotas);
    }

    // ── GTFS import (skipped if lines already exist) ─────────────────────────

    private void importarGtfsSeNecessario() {
        if (linhaRepo.count() > 0) return;
        gtfsImportService.importarDadosBase();
    }

    // ── Autocarros ────────────────────────────────────────────────────────────

    private void seedAutocarros() {
        if (autocarroRepo.count() > 0) return;

        for (Linha linha : linhaRepo.findAll()) {
            String n = linha.getNumero();
            autocarroRepo.save(autocarro("BUS-" + n + "-01", "Autocarro " + n + "-01", linha));
            autocarroRepo.save(autocarro("BUS-" + n + "-02", "Autocarro " + n + "-02", linha));
        }
    }

    private Autocarro autocarro(String codigo, String nome, Linha linha) {
        Autocarro a = new Autocarro();
        a.setCodigo(codigo);
        a.setNome(nome);
        a.setAtivo(true);
        a.setLinha(linha);
        return a;
    }

    private void seedLinhas() {
        if (linhaRepo.count() > 0) return;


        linhaRepo.save(linha("5",  "Estação → Universidade",  "Estação Central", "Universidade do Minho", 22, 18, "#0ea5e9"));
        linhaRepo.save(linha("12", "TUB → Braga Park",        "Terminal TUB",    "Braga Park",            15, 12, "#6366f1"));
        linhaRepo.save(linha("24", "Centro → Gualtar",        "Centro",          "Gualtar",               18, 22, "#7c3aed"));
        linhaRepo.save(linha("31", "Circular Centro",         "Centro",          "Centro",                28, 35, "#f97316"));
        linhaRepo.save(linha("42", "Braga Norte → Hospital",  "Braga Norte",     "Hospital de Braga",     20, 26, "#10b981"));
    }

    private Linha linha(String numero, String nome, String origem, String destino,
                        int paragens, int duracao, String cor) {
        Linha l = new Linha();
        l.setNumero(numero);
        l.setNome(nome);
        l.setOrigem(origem);
        l.setDestino(destino);
        l.setNumParagens(paragens);
        l.setDuracaoMin(duracao);
        l.setCor(cor);
        return l;
    }

    // ── Horarios (próximas chegadas – simulated) ───────────────────────────────

    private void seedHorarios() {
        if (horarioRepo.count() > 0) return;

        var linhas = linhaRepo.findAll();
        // Map by numero for convenience
        var l24 = linhas.stream().filter(l -> l.getNumero().equals("24")).findFirst().orElse(null);
        var l12 = linhas.stream().filter(l -> l.getNumero().equals("12")).findFirst().orElse(null);
        var l5  = linhas.stream().filter(l -> l.getNumero().equals("5")).findFirst().orElse(null);
        var l31 = linhas.stream().filter(l -> l.getNumero().equals("31")).findFirst().orElse(null);
        var l42 = linhas.stream().filter(l -> l.getNumero().equals("42")).findFirst().orElse(null);

        horarioRepo.save(horario(l24, "Largo do Prado", 3));
        horarioRepo.save(horario(l12, "Av. da Liberdade", 7));
        horarioRepo.save(horario(l5,  "Estação Central", 11));
        horarioRepo.save(horario(l31, "Praça da República", 14));
        horarioRepo.save(horario(l42, "Hospital de Braga", 19));
        horarioRepo.save(horario(l24, "Gualtar – Terminal", 25));
    }

    private Horario horario(Linha linha, String paragem, int minutosAte) {
        Horario h = new Horario();
        h.setLinha(linha);
        h.setParagem(paragem);
        h.setMinutosAte(minutosAte);
        return h;
    }

    // ── Avisos ────────────────────────────────────────────────────────────────

    private void seedAvisos() {
        if (avisoRepo.count() > 0) return;

        LocalDateTime base = LocalDateTime.of(2026, 3, 19, 14, 32);

        avisoRepo.save(aviso("Perturbação – Linha 24",
                "Desvio temporário no troço Prado–Gualtar devido a obras na Av. Central. Prevê-se resolução às 19h00.",
                base, AvisoTipo.ALERTA, true));

        avisoRepo.save(aviso("Atraso – Linha 12",
                "Atrasos de aproximadamente 5 min devido a trânsito intenso na EN14 em direção ao Braga Park.",
                base.minusMinutes(87), AvisoTipo.ALERTA, true));

        avisoRepo.save(aviso("Horário Especial – Feriado",
                "No dia 25 de abril os autocarros circulam em horário de domingo.",
                base.minusDays(1).withHour(9).withMinute(0), AvisoTipo.INFO, false));

        avisoRepo.save(aviso("Linha 5 – Serviço restabelecido",
                "A interrupção na Linha 5 foi resolvida. O serviço está a funcionar normalmente.",
                LocalDateTime.of(2026, 3, 18, 17, 45), AvisoTipo.OK, false));
    }

    private Aviso aviso(String titulo, String descricao, LocalDateTime dataHora,
                        AvisoTipo tipo, boolean novo) {
        Aviso a = new Aviso();
        a.setTitulo(titulo);
        a.setDescricao(descricao);
        a.setDataHora(dataHora);
        a.setTipo(tipo);
        a.setNovo(novo);
        return a;
    }

    // ── Tipos de Bilhete ──────────────────────────────────────────────────────

    private void seedTiposBilhete() {
        garantirTipoComercial("Coroa 1", new BigDecimal("0.75"),
                coroaRepo.findByNome("Coroa 1").orElse(null));
        garantirTipoComercial("Coroa 2", new BigDecimal("1.50"),
                coroaRepo.findByNome("Coroa 2").orElse(null));
        desativarTiposGtfs();
    }

    private void garantirTipoComercial(String nome, BigDecimal preco, Coroa coroa) {
        TipoBilhete t = tipoBilheteRepo.findByNome(nome).orElseGet(TipoBilhete::new);
        boolean novo = t.getId() == null;
        t.setNome(nome);
        t.setPreco(preco);
        t.setCategoria(TipoBilhete.Categoria.AVULSO);
        t.setAtivo(true);
        t.setGtfsFareId(null);
        t.setCoroa(coroa);
        tipoBilheteRepo.save(t);
        log.info("Tipo comercial {}: {} — {}€ coroa={}", novo ? "criado" : "atualizado",
                nome, preco, coroa != null ? coroa.getNome() : "null");
    }

    private void desativarTiposGtfs() {
        List<TipoBilhete> todos = tipoBilheteRepo.findAll();
        for (TipoBilhete t : todos) {
            if (t.getGtfsFareId() != null && t.isAtivo()) {
                t.setAtivo(false);
                tipoBilheteRepo.save(t);
                log.info("Tipo GTFS desativado: {} (fare_id={})", t.getNome(), t.getGtfsFareId());
            }
        }
    }

    // ── Tipos de Passe ───────────────────────────────────────────────────────

    private void seedTiposPasse() {
        if (tipoPasseRepo.count() > 0) return;

        TipoPasse mensal = new TipoPasse();
        mensal.setNome("Mensal");
        mensal.setDescricao("Passe válido durante 30 dias");
        mensal.setDuracaoDias(30);
        mensal.setAtivo(true);
        tipoPasseRepo.save(mensal);

        TipoPasse trimestral = new TipoPasse();
        trimestral.setNome("Trimestral");
        trimestral.setDescricao("Passe válido durante 90 dias");
        trimestral.setDuracaoDias(90);
        trimestral.setAtivo(true);
        tipoPasseRepo.save(trimestral);
    }

    // ── Coroas ───────────────────────────────────────────────────────────────

    private void seedCoroas() {
        if (coroaRepo.count() > 0) return;

        coroaRepo.save(coroa("Coroa 1", "Zona urbana central de Braga"));
        coroaRepo.save(coroa("Coroa 2", "Zona periurbana e freguesias limítrofes"));
        coroaRepo.save(coroa("Coroa 3", "Zona interurbana e concelhos adjacentes"));
    }

    private Coroa coroa(String nome, String descricao) {
        Coroa c = new Coroa();
        c.setNome(nome);
        c.setDescricao(descricao);
        c.setAtivo(true);
        return c;
    }

    // ── Regras de Preço ──────────────────────────────────────────────────────
    // Preços realistas inspirados nos TUB (euros):
    //
    //                    Mensal                    Trimestral
    // Estatuto       C1    C2    C3            C1     C2     C3
    // SEM_ESTATUTO   40    50    60           105    135    162
    // ESTUDANTE      20    25    30            54     67     81
    // RESIDENTE      30    38    45            81    102    121
    // SENIOR         30    38    45            81    102    121
    // MILITAR        30    38    45            81    102    121
    // CRIANCA        15    19    23            40     51     62
    // INCAPACITADO   15    19    23            40     51     62

    private void seedRegrasPreco() {
        var tiposPasse = tipoPasseRepo.findAll();
        var coroas = coroaRepo.findAll();
        if (tiposPasse.size() < 2 || coroas.size() < 3) return;

        TipoPasse mensal = tiposPasse.stream().filter(t -> t.getNome().equals("Mensal")).findFirst().orElse(null);
        TipoPasse trimestral = tiposPasse.stream().filter(t -> t.getNome().equals("Trimestral")).findFirst().orElse(null);
        Coroa c1 = coroas.stream().filter(c -> c.getNome().equals("Coroa 1")).findFirst().orElse(null);
        Coroa c2 = coroas.stream().filter(c -> c.getNome().equals("Coroa 2")).findFirst().orElse(null);
        Coroa c3 = coroas.stream().filter(c -> c.getNome().equals("Coroa 3")).findFirst().orElse(null);

        if (mensal == null || trimestral == null || c1 == null || c2 == null || c3 == null) return;

        LocalDate inicio = LocalDate.of(2026, 1, 1);
        List<RegraPreco> regrasExistentes = regraPrecoRepo.findAll();

        // SEM_ESTATUTO (Normal)
        // Mensal: C1 - 12.00, C2 - 24.00, C3 - 36.00
        // Trimestral (3x): C1 - 36.00, C2 - 72.00, C3 - 108.00
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SEM_ESTATUTO, mensal, c1, "12.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SEM_ESTATUTO, mensal, c2, "24.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SEM_ESTATUTO, mensal, c3, "36.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SEM_ESTATUTO, trimestral, c1, "36.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SEM_ESTATUTO, trimestral, c2, "72.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SEM_ESTATUTO, trimestral, c3, "108.00", inicio);

        // ESTUDANTE (Gratuito)
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.ESTUDANTE, mensal, c1, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.ESTUDANTE, mensal, c2, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.ESTUDANTE, mensal, c3, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.ESTUDANTE, trimestral, c1, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.ESTUDANTE, trimestral, c2, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.ESTUDANTE, trimestral, c3, "0.00", inicio);

        // RESIDENTE (desconto 25%)
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.RESIDENTE, mensal, c1, "9.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.RESIDENTE, mensal, c2, "18.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.RESIDENTE, mensal, c3, "27.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.RESIDENTE, trimestral, c1, "27.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.RESIDENTE, trimestral, c2, "54.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.RESIDENTE, trimestral, c3, "81.00", inicio);

        // SENIOR (desconto 75%)
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SENIOR, mensal, c1, "3.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SENIOR, mensal, c2, "6.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SENIOR, mensal, c3, "9.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SENIOR, trimestral, c1, "9.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SENIOR, trimestral, c2, "18.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.SENIOR, trimestral, c3, "27.00", inicio);

        // MILITAR / EX-COMBATENTE (Gratuito)
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.MILITAR, mensal, c1, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.MILITAR, mensal, c2, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.MILITAR, mensal, c3, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.MILITAR, trimestral, c1, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.MILITAR, trimestral, c2, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.MILITAR, trimestral, c3, "0.00", inicio);

        // CRIANCA (Gratuito)
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.CRIANCA, mensal, c1, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.CRIANCA, mensal, c2, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.CRIANCA, mensal, c3, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.CRIANCA, trimestral, c1, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.CRIANCA, trimestral, c2, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.CRIANCA, trimestral, c3, "0.00", inicio);

        // INCAPACITADO (Gratuito)
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.INCAPACITADO, mensal, c1, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.INCAPACITADO, mensal, c2, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.INCAPACITADO, mensal, c3, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.INCAPACITADO, trimestral, c1, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.INCAPACITADO, trimestral, c2, "0.00", inicio);
        criarRegraSeNaoExistir(regrasExistentes, TipoEstatuto.INCAPACITADO, trimestral, c3, "0.00", inicio);
    }

    private void criarRegraSeNaoExistir(List<RegraPreco> regrasExistentes, TipoEstatuto estatuto,
                                        TipoPasse tipoPasse, Coroa coroa, String preco, LocalDate inicio) {
        boolean jaExiste = regrasExistentes.stream().anyMatch(r ->
                r.getTipoEstatuto() == estatuto
                && r.getTipoPasse().getId().equals(tipoPasse.getId())
                && r.getCoroa().getId().equals(coroa.getId())
                && r.isAtivo());

        if (jaExiste) {
            log.debug("Regra de preço já existe — ignorada pelo seeder: {} / {} / {}",
                    estatuto, tipoPasse.getNome(), coroa.getNome());
            return;
        }

        RegraPreco r = new RegraPreco();
        r.setTipoEstatuto(estatuto);
        r.setTipoPasse(tipoPasse);
        r.setCoroa(coroa);
        r.setPreco(new BigDecimal(preco));
        r.setDataInicioVigencia(inicio);
        r.setDataFimVigencia(null);
        r.setAtivo(true);
        regraPrecoRepo.save(r);
        log.info("Regra de preço base criada: {} / {} / {} = {}",
                estatuto, tipoPasse.getNome(), coroa.getNome(), preco);
    }
}
