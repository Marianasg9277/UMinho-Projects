package com.example.loginapi.controller.titulos;



import com.example.loginapi.dto.*;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.pagamentos.Pagamento;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.PasseQrToken;
import com.example.loginapi.model.infraestrutura.RegraPreco;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import com.example.loginapi.service.autenticacao.AuthService;
import com.example.loginapi.service.titulos.PasseService;
import com.example.loginapi.service.titulos.CompraPasseService;
import com.example.loginapi.service.titulos.QrCodeService;
import com.example.loginapi.service.infraestrutura.PricingService;
import com.example.loginapi.service.clientes.EstatutoService;
import com.example.loginapi.service.pagamentos.SaldoCompraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.loginapi.model.clientes.Utilizador;


@RestController
@RequestMapping("/api/user/passes")
@Tag(name = "Passes", description = "Compra e gestão de passes de transporte")
public class PasseController {

    private static final Logger log = LoggerFactory.getLogger(PasseController.class);

    @Autowired private PasseService passeService;
    @Autowired private CompraPasseService compraPasseService;
    @Autowired private PricingService pricingService;
    @Autowired private EstatutoService estatutoService;
    @Autowired private AuthService authService;
    @Autowired private QrCodeService qrCodeService;
    @Autowired private SaldoCompraService saldoCompraService;

    @GetMapping("/opcoes")
    @Operation(summary = "Listar opções de passe e preços com base no estatuto atual")
    public ResponseEntity<List<OpcaoPasseResponse>> listarOpcoes(Authentication auth) {
        Cliente cliente = getCliente(auth);
        TipoEstatuto estatuto = estatutoService.resolverEstatutoEfetivo(cliente);
        List<RegraPreco> regras = pricingService.listarOpcoesPorEstatuto(estatuto);
        List<OpcaoPasseResponse> opcoes = regras.stream().map(r -> {
            OpcaoPasseResponse o = new OpcaoPasseResponse();
            o.setTipoPasseId(r.getTipoPasse().getId());
            o.setTipoPasseNome(r.getTipoPasse().getNome());
            o.setDuracaoDias(r.getTipoPasse().getDuracaoDias());
            o.setCoroaId(r.getCoroa().getId());
            o.setCoroaNome(r.getCoroa().getNome());
            o.setTipoEstatuto(estatuto.name());
            o.setPreco(r.getPreco());
            o.setRegraPrecoId(r.getId());
            return o;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(opcoes);
    }

    @PostMapping
    @Operation(summary = "Criar passe pendente de pagamento")
    public ResponseEntity<?> criarPasse(@RequestBody CriarPasseRequest req, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            if (req.getIdCliente() == null) throw new IllegalArgumentException("O campo idCliente é obrigatório.");
            if (!req.getIdCliente().equals(cliente.getId())) throw new IllegalArgumentException("O idCliente enviado não corresponde ao utilizador autenticado.");
            Passe passe = compraPasseService.criarPasseComPagamento(cliente, req.getTipoPasseId(), req.getCoroaId());
            return ResponseEntity.ok(toPasseResponse(passe, cliente));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<PasseResponse>> listarPasses(Authentication auth) {
        Cliente cliente = getCliente(auth);
        List<Passe> passes = passeService.listarPasses(cliente);
        // Verificar e notificar passes prestes a expirar
        passeService.notificarExpiracoesProximas(cliente);
        return ResponseEntity.ok(passes.stream().map(p -> toPasseResponse(p, cliente)).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> obterPasse(@PathVariable Long id, Authentication auth) {
        Cliente cliente = getCliente(auth);
        return passeService.obterPasse(id)
                .filter(p -> p.getCliente().getId().equals(cliente.getId()))
                .map(p -> ResponseEntity.ok((Object) toPasseResponse(p, cliente)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/qr-atual")
    @Operation(summary = "Obter QR atual do passe — token e imagem numa só resposta")
    public ResponseEntity<?> qrAtual(@PathVariable Long id, Authentication auth) {
        Cliente cliente = getCliente(auth);
        Passe passe = passeService.obterPasse(id)
                .filter(p -> p.getCliente().getId().equals(cliente.getId()))
                .orElse(null);
        if (passe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Passe não encontrado."));
        }
        try {
            PasseQrToken qrToken = passeService.obterOuGerarQrToken(passe);
            String payload = passeService.construirPayloadQr(passe, qrToken);
            byte[] png = qrCodeService.gerarPng(payload, 260, 260);
            String imagemBase64 = "data:image/png;base64," + Base64.getEncoder().encodeToString(png);

            PasseQrAtualResponse resp = new PasseQrAtualResponse();
            resp.setToken(qrToken.getToken());
            resp.setExpiraEm(qrToken.getExpiraEm().toString());
            resp.setCriadoEm(qrToken.getGeradoEm().toString());
            resp.setImagemBase64(imagemBase64);

            return ResponseEntity.ok()
                    .cacheControl(CacheControl.noStore())
                    .body(resp);
        } catch (IllegalStateException e) {
            // Violação de regra de negócio (ex: passe não ativo)
            return ResponseEntity.status(422)
                    .body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            // Erro interno inesperado
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Erro interno ao gerar QR."));
        }
    }

    @GetMapping(value = "/{id}/codigo-qr/imagem", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<?> imagemCodigoQr(@PathVariable Long id, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            Passe passe = passeService.obterPasse(id)
                    .filter(p -> p.getCliente().getId().equals(cliente.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Passe não encontrado."));
            PasseQrToken qrToken = passeService.obterOuGerarQrToken(passe);
            byte[] png = qrCodeService.gerarPng(passeService.construirPayloadQr(passe, qrToken), 260, 260);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_PNG)
                    .cacheControl(CacheControl.noStore())
                    .body(png);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage().getBytes());
        }
    }

    @GetMapping("/{id}/codigo-qr")
    public ResponseEntity<?> gerarCodigoQr(@PathVariable Long id, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            Passe passe = passeService.obterPasse(id)
                    .filter(p -> p.getCliente().getId().equals(cliente.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Passe não encontrado."));
            PasseQrToken qrToken = passeService.obterOuGerarQrToken(passe);
            CodigoQrResponse resp = new CodigoQrResponse();
            resp.setId(passe.getId());
            resp.setTipo("PASSE");
            resp.setToken(qrToken.getToken());
            resp.setPayload(passeService.construirPayloadQr(passe, qrToken));
            resp.setGeradoEm(qrToken.getGeradoEm().toString());
            resp.setExpiraEm(qrToken.getExpiraEm().toString());
            resp.setSegundosRestantes(passeService.segundosRestantesQrToken(qrToken));
            resp.setMensagem("QR temporário gerado com sucesso.");
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<?> pagarPasse(@PathVariable Long id,
                                         @RequestParam(defaultValue = "CARTAO") String metodo,
                                         @RequestParam(required = false) Long cartaoId,
                                         Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            passeService.obterPasse(id)
                    .filter(p -> p.getCliente().getId().equals(cliente.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Passe não encontrado."));
            Pagamento pagamento;
            if ("SALDO_CONTA".equals(metodo)) {
                pagamento = saldoCompraService.pagarPasseComSaldo(cliente, id);
            } else {
                pagamento = compraPasseService.simularPagamento(id, metodo, cartaoId);
            }
            Passe atualizado = passeService.obterPasse(id).orElseThrow();
            return ResponseEntity.ok(Map.of(
                    "message", "Pagamento confirmado com sucesso!",
                    "metodo", metodo,
                    "faturaNumero", pagamento.getFaturaNumero(),
                    "passe", toPasseResponse(atualizado, cliente)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/carregar")
    @Operation(summary = "Carregar/Atualizar passe")
    public ResponseEntity<?> carregarPasse(@PathVariable Long id, @RequestBody CarregarPasseRequest req, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            Passe atualizado = compraPasseService.carregarPasse(id, cliente, req);
            return ResponseEntity.ok(Map.of(
                    "message", "Passe carregado com sucesso!",
                    "passe", toPasseResponse(atualizado, cliente)
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/anular")
    @Operation(summary = "Anular passe pendente de pagamento")
    public ResponseEntity<?> anularPasse(@PathVariable Long id, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            Passe cancelado = passeService.cancelarPasse(id, cliente);
            return ResponseEntity.ok(toPasseResponse(cancelado, cliente));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{id}/renovar")
    @Operation(summary = "Renovar passe expirado ou prestes a expirar")
    public ResponseEntity<?> renovarPasse(@PathVariable Long id,
                                           @RequestBody(required = false) RenovarPasseRequest req,
                                           Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            String metodo = (req != null && req.getMetodo() != null) ? req.getMetodo() : "CARTAO";
            Long cartaoId = (req != null) ? req.getCartaoId() : null;
            Passe renovado = compraPasseService.renovarPasse(id, cliente, metodo, cartaoId);
            return ResponseEntity.ok(Map.of(
                    "message", "Passe renovado com sucesso!",
                    "metodo", metodo,
                    "passe", toPasseResponse(renovado, cliente)
            ));
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            log.error("Erro inesperado ao renovar passe id={}: {}", id, e.getMessage(), e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("message", "Ocorreu um erro ao renovar o passe. Tente novamente."));
        }
    }

    @GetMapping("/expirar-em-breve")
    @Operation(summary = "Listar passes prestes a expirar nos próximos 7 dias")
    public ResponseEntity<List<PasseResponse>> passesPrestoExpirar(Authentication auth) {
        Cliente cliente = getCliente(auth);
        List<Passe> prestoExpirar = passeService.verificarPassesPrestoExpirar(cliente);
        return ResponseEntity.ok(prestoExpirar.stream().map(p -> toPasseResponse(p, cliente)).collect(Collectors.toList()));
    }

    private Cliente getCliente(Authentication auth) {
        Cliente cliente = authService.getClienteByEmail(auth.getName());
        if (cliente == null) throw new IllegalStateException("Perfil de cliente não encontrado.");
        return cliente;
    }

    private PasseResponse toPasseResponse(Passe p, Cliente cliente) {
        PasseResponse r = new PasseResponse();
        r.setId(p.getId());
        r.setIdCliente(p.getCliente().getId());
        r.setTipoPasseNome(p.getTipoPasse().getNome());
        r.setCoroaNome(p.getCoroa().getNome());
        r.setTipoEstatutoAplicado(p.getTipoEstatutoAplicado().name());
        r.setPrecoAplicado(p.getPrecoAplicado());
        r.setEstadoComercial(p.getEstadoComercial().name());
        r.setEstadoOperacional(p.getEstadoOperacional().name());
        r.setCodigoQr(p.getCodigoQr());
        r.setDataInicio(p.getDataInicio() != null ? p.getDataInicio().toString() : null);
        r.setDataFim(p.getDataFim() != null ? p.getDataFim().toString() : null);
        r.setCriadoEm(p.getCriadoEm().toString());
        r.setFaturaNumero(passeService.obterFaturaNumero(p));
        boolean temFoto = cliente != null && cliente.getFotoPassePath() != null;
        r.setTemFotoPasse(temFoto);
        r.setFotoPasseUrl(temFoto ? "/api/cliente/foto-passe" : null);
        r.setNomeTitular(cliente != null ? cliente.getNomeCompleto() : null);
        return r;
    }
}
