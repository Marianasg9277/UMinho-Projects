package com.example.loginapi.controller.titulos;

import java.util.UUID;

import com.example.loginapi.dto.BilheteResponse;
import com.example.loginapi.dto.CompraRequest;
import com.example.loginapi.dto.CompraResponse;
import com.example.loginapi.dto.CodigoQrResponse;
import com.example.loginapi.dto.LinhaDetalheResponse;
import com.example.loginapi.dto.ParagemPercursoResponse;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.infraestrutura.Horario;
import com.example.loginapi.model.comunicacao.Aviso;
import com.example.loginapi.model.titulos.TipoBilhete;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.model.infraestrutura.LinhaParagem;
import com.example.loginapi.repository.infraestrutura.LinhaRepository;
import com.example.loginapi.repository.infraestrutura.HorarioRepository;
import com.example.loginapi.repository.comunicacao.AvisoRepository;
import com.example.loginapi.repository.titulos.TipoBilheteRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.repository.infraestrutura.LinhaParagemRepository;
import com.example.loginapi.service.autenticacao.AuthService;
import com.example.loginapi.service.clientes.AuditLogService;
import com.example.loginapi.service.comunicacao.NotificacaoService;
import com.example.loginapi.service.titulos.QrCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.example.loginapi.model.pagamentos.Pagamento;


@RestController
@RequestMapping("/api")
@Tag(name = "Transportes", description = "Linhas, horários, avisos, preçário e compra de bilhetes")
public class TransporteController {

    @Autowired
    private LinhaRepository linhaRepo;
    @Autowired
    private HorarioRepository horarioRepo;
    @Autowired
    private AvisoRepository avisoRepo;
    @Autowired
    private TipoBilheteRepository tipoBilheteRepo;
    @Autowired
    private TransacaoRepository transacaoRepo;
    @Autowired
    private AuthService authService;
    @Autowired
    private AuditLogService auditLogService;
    @Autowired
    private NotificacaoService notificacaoService;
    @Autowired
    private QrCodeService qrCodeService;
    @Autowired
    private LinhaParagemRepository linhaParagemRepo;

    @GetMapping("/linhas")
    public List<Linha> getLinhas() {
        return linhaRepo.findByAtivoTrueOrderByNumeroAsc();
    }

    @GetMapping("/autocarros")
    public List<Horario> getAutocarros() {
        return horarioRepo.findAllByOrderByMinutosAteAsc();
    }

    @GetMapping("/avisos")
    public List<Aviso> getAvisos() {
        return avisoRepo.findAllByOrderByDataHoraDesc();
    }

    @GetMapping("/precario")
    public List<TipoBilhete> getPrecario() {
        return tipoBilheteRepo.findByAtivoTrueOrderByCategoriaAscNomeAsc();
    }

    @GetMapping("/tipos-bilhete")
    public List<TipoBilhete> getTiposBilhete() {
        return tipoBilheteRepo.findByAtivoTrueOrderByCategoriaAscNomeAsc();
    }

    @PostMapping("/comprar")
    @Operation(summary = "Criar bilhete pendente de pagamento")
    public ResponseEntity<CompraResponse> comprar(@RequestBody CompraRequest req, Authentication authentication,
            HttpServletRequest httpRequest) {
        TipoBilhete tipo = tipoBilheteRepo.findById(req.getTipoBilheteId()).orElse(null);
        if (tipo == null) {
            return ResponseEntity.badRequest().body(new CompraResponse(false, "Tipo de bilhete inválido.", null, null));
        }

        Linha linha = null;
        if (req.getLinhaId() != null)
            linha = linhaRepo.findById(req.getLinhaId()).orElse(null);

        Transacao t = new Transacao();
        t.setTipoBilhete(tipo);
        t.setLinha(linha);
        t.setDataCompra(LocalDateTime.now());
        t.setPreco(tipo.getPreco());
        t.setEstadoPagamento(EstadoPagamento.NOT_STARTED);

        String comprador = "anonymous";
        String role = null;
        if (authentication != null && authentication.isAuthenticated()) {
            Cliente cliente = authService.getClienteByEmail(authentication.getName());
            if (cliente != null) {
                t.setCliente(cliente);
                comprador = authentication.getName();
                role = cliente.getUtilizador().getRole().name();
            }
        }
        if (t.getCliente() == null) {
            String email = req.getGuestEmail();
            t.setGuestEmail(email != null && !email.isBlank() ? email.trim().toLowerCase() : null);
        }

        Transacao saved = transacaoRepo.save(t);
        auditLogService.registar(comprador, role, "BILHETE_CRIADO_PENDENTE", "comprar", "Bilhete: " + tipo.getNome(),
                true, httpRequest);
        return ResponseEntity
                .ok(new CompraResponse(true, "Bilhete criado. Falta efetuar o pagamento.", null, saved.getId()));
    }

    // ── Novo modelo: percurso e detalhe de linha ───────────────────────────────

    @GetMapping("/linhas/{id}/percurso")
    @Operation(summary = "Devolve o percurso ordenado de uma linha")
    public ResponseEntity<List<ParagemPercursoResponse>> getPercurso(
            @PathVariable Long id,
            @RequestParam(defaultValue = "IDA") String sentido
    ) {
        if (!linhaRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        List<LinhaParagem> registos = linhaParagemRepo.findByLinhaIdAndSentidoOrderByOrdemAsc(id, normalizarSentido(sentido));
        if (registos.isEmpty()) {
            // Compatibilidade com dados antigos sem campo sentido.
            registos = linhaParagemRepo.findByLinhaIdOrderByOrdemAsc(id);
        }
        return ResponseEntity.ok(toPercursoResponse(registos));
    }

    @GetMapping("/linhas/{id}/detalhe")
    @Operation(summary = "Devolve os dados da linha com percurso completo")
    public ResponseEntity<LinhaDetalheResponse> getLinhaDetalhe(
            @PathVariable Long id,
            @RequestParam(defaultValue = "IDA") String sentido
    ) {
        return linhaRepo.findById(id).map(linha -> {
            List<LinhaParagem> registos = linhaParagemRepo.findByLinhaIdAndSentidoOrderByOrdemAsc(id, normalizarSentido(sentido));
            if (registos.isEmpty()) {
                // Compatibilidade com dados antigos sem campo sentido.
                registos = linhaParagemRepo.findByLinhaIdOrderByOrdemAsc(id);
            }
            List<ParagemPercursoResponse> percurso = toPercursoResponse(registos);
            LinhaDetalheResponse resp = new LinhaDetalheResponse();
            resp.setId(linha.getId());
            resp.setNumero(linha.getNumero());
            resp.setNome(linha.getNome());
            resp.setOrigem(linha.getOrigem());
            resp.setDestino(linha.getDestino());
            resp.setDuracaoMin(linha.getDuracaoMin());
            resp.setNumParagens(linha.getNumParagens());
            resp.setCor(linha.getCor());
            resp.setPercurso(percurso);
            return ResponseEntity.ok(resp);
        }).orElse(ResponseEntity.notFound().build());
    }

    private String normalizarSentido(String sentido) {
        if (sentido == null) return "IDA";
        String value = sentido.trim().toUpperCase();
        if ("1".equals(value) || "VOLTA".equals(value)) return "VOLTA";
        return "IDA";
    }

    private List<ParagemPercursoResponse> toPercursoResponse(List<LinhaParagem> registos) {
        List<ParagemPercursoResponse> percurso = new ArrayList<>();
        for (LinhaParagem lp : registos) {
            percurso.add(new ParagemPercursoResponse(
                    lp.getOrdem(),
                    lp.getParagem().getNome(),
                    lp.getMinutosDesdeInicio(),
                    lp.getParagem().getLatitude(),
                    lp.getParagem().getLongitude()
            ));
        }
        return percurso;
    }

}
