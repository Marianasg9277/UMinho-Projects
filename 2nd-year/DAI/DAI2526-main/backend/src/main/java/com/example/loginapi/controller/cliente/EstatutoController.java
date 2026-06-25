package com.example.loginapi.controller.cliente;

import com.example.loginapi.dto.*;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.clientes.DocumentoEstatuto;
import com.example.loginapi.model.clientes.PedidoEstatuto;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import com.example.loginapi.service.autenticacao.AuthService;
import com.example.loginapi.service.clientes.EstatutoService;
import com.example.loginapi.service.clientes.PedidoEstatutoService;
import com.example.loginapi.service.clientes.DocumentoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.repository.clientes.DocumentoEstatutoRepository;


@RestController
@RequestMapping("/api/user/estatuto")
@Tag(name = "Estatuto", description = "Gestão do estatuto do utilizador e pedidos de estatuto")
public class EstatutoController {

    @Autowired private EstatutoService estatutoService;
    @Autowired private PedidoEstatutoService pedidoService;
    @Autowired private DocumentoService documentoService;
    @Autowired private AuthService authService;

    @GetMapping("/tipos")
    @Operation(summary = "Listar tipos de estatuto disponíveis para pedido manual")
    public ResponseEntity<List<Map<String, Object>>> listarTipos() {
        List<Map<String, Object>> tipos = Arrays.stream(TipoEstatuto.values())
                .filter(t -> t != TipoEstatuto.SEM_ESTATUTO)
                .map(t -> Map.<String, Object>of(
                        "tipo", t.name(),
                        "exigeDocumentos", t.exigeDocumentos()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/atual")
    @Operation(summary = "Obter estatuto efetivo atual do utilizador")
    public ResponseEntity<EstatutoResponse> obterEstatutoAtual(Authentication auth) {
        Cliente cliente = getCliente(auth);
        TipoEstatuto efetivo = estatutoService.resolverEstatutoEfetivo(cliente);
        return ResponseEntity.ok(new EstatutoResponse(efetivo.name(), efetivo.isAutomatico()));
    }

    @PostMapping("/pedido")
    @Operation(summary = "Criar pedido de estatuto")
    public ResponseEntity<?> criarPedido(@RequestBody PedidoEstatutoRequest req, Authentication auth) {
        if (req.getTipoEstatuto() == null || req.getTipoEstatuto().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "tipoEstatuto é obrigatório."));
        }
        try {
            Cliente cliente = getCliente(auth);
            TipoEstatuto tipo = TipoEstatuto.valueOf(req.getTipoEstatuto().trim().toUpperCase());
            PedidoEstatuto pedido = pedidoService.criarPedido(cliente, tipo, req.getObservacoes());
            return ResponseEntity.ok(toPedidoResponse(pedido));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/pedidos")
    @Operation(summary = "Listar pedidos de estatuto do utilizador")
    public ResponseEntity<List<PedidoEstatutoResponse>> listarPedidos(Authentication auth) {
        Cliente cliente = getCliente(auth);
        List<PedidoEstatutoResponse> pedidos = pedidoService.listarPedidosDoCliente(cliente)
                .stream().map(this::toPedidoResponse).collect(Collectors.toList());
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/pedido/{id}")
    @Operation(summary = "Detalhes de um pedido de estatuto")
    public ResponseEntity<?> obterPedido(@PathVariable Long id, Authentication auth) {
        Cliente cliente = getCliente(auth);
        return pedidoService.obterPedidoPorId(id)
                .filter(p -> p.getCliente().getId().equals(cliente.getId()))
                .map(p -> ResponseEntity.ok((Object) toPedidoResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping(value = "/pedido/{id}/documentos", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload de documento para pedido de estatuto")
    public ResponseEntity<?> uploadDocumento(@PathVariable Long id,
                                              @io.swagger.v3.oas.annotations.Parameter(
                                                  description = "Ficheiro a enviar (PDF, JPEG ou PNG, máx. 5MB)",
                                                  content = @io.swagger.v3.oas.annotations.media.Content(
                                                      mediaType = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
                                                  )
                                              )
                                              @RequestParam("ficheiro") MultipartFile file,
                                              Authentication auth) {
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Nenhum ficheiro enviado."));
        }
        try {
            Cliente cliente = getCliente(auth);
            PedidoEstatuto pedido = pedidoService.obterPedidoPorId(id)
                    .filter(p -> p.getCliente().getId().equals(cliente.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

            DocumentoEstatuto doc = documentoService.upload(pedido, file);
            return ResponseEntity.ok(Map.of(
                    "id", doc.getId(),
                    "nomeFicheiro", doc.getNomeFicheiro(),
                    "tipoConteudo", doc.getTipoConteudo(),
                    "tamanhoBytes", doc.getTamanhoBytes(),
                    "message", "Documento carregado com sucesso."
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Erro ao processar o ficheiro."));
        }
    }

    @GetMapping("/pedido/{id}/documentos")
    @Operation(summary = "Listar documentos de um pedido")
    public ResponseEntity<?> listarDocumentos(@PathVariable Long id, Authentication auth) {
        Cliente cliente = getCliente(auth);
        return pedidoService.obterPedidoPorId(id)
                .filter(p -> p.getCliente().getId().equals(cliente.getId()))
                .map(p -> {
                    List<PedidoEstatutoResponse.DocumentoInfo> docs = documentoService.listarDocumentos(p)
                            .stream().map(d -> new PedidoEstatutoResponse.DocumentoInfo(
                                    d.getId(), d.getNomeFicheiro(), d.getTipoConteudo(),
                                    d.getTamanhoBytes(), d.getCriadoEm().toString()
                            )).collect(Collectors.toList());
                    return ResponseEntity.ok((Object) docs);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/pedido/{id}/documento/{docId}")
    @Operation(summary = "Download de um documento do pedido")
    public ResponseEntity<org.springframework.core.io.Resource> downloadDocumento(@PathVariable Long id, @PathVariable Long docId, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            
            // Validate that the request belongs to the user
            pedidoService.obterPedidoPorId(id)
                    .filter(p -> p.getCliente().getId().equals(cliente.getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado ou não pertence ao utilizador."));

            com.example.loginapi.repository.clientes.DocumentoEstatutoRepository documentoRepo = 
                org.springframework.web.context.support.WebApplicationContextUtils.getWebApplicationContext(
                    ((org.springframework.web.context.request.ServletRequestAttributes) org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest().getServletContext()
                ).getBean(com.example.loginapi.repository.clientes.DocumentoEstatutoRepository.class);

            com.example.loginapi.model.clientes.DocumentoEstatuto doc = documentoRepo.findById(docId)
                    .filter(d -> d.getPedido().getId().equals(id))
                    .orElseThrow(() -> new IllegalArgumentException("Documento não encontrado no pedido."));
            
            java.nio.file.Path file = documentoService.obterCaminhoFisico(doc);
            org.springframework.core.io.Resource resource = new org.springframework.core.io.UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + doc.getNomeFicheiro() + "\"")
                        .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, doc.getTipoConteudo())
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/pedido/{id}/submeter")
    @Operation(summary = "Submeter pedido para revisão — estado passa a PENDING_APPROVAL")
    public ResponseEntity<?> submeterPedido(@PathVariable Long id, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            PedidoEstatuto pedido = pedidoService.submeterPedido(id, cliente);
            return ResponseEntity.ok(toPedidoResponse(pedido));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/pedido/{id}/cancelar")
    @Operation(summary = "Cancelar pedido de estatuto")
    public ResponseEntity<?> cancelarPedido(@PathVariable Long id, Authentication auth) {
        try {
            Cliente cliente = getCliente(auth);
            PedidoEstatuto pedido = pedidoService.cancelarPedido(id, cliente);
            return ResponseEntity.ok(toPedidoResponse(pedido));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // ── Helpers ────────────────────────────────────────────────────────────────

    private Cliente getCliente(Authentication auth) {
        Cliente cliente = authService.getClienteByEmail(auth.getName());
        if (cliente == null) throw new IllegalStateException("Perfil de cliente não encontrado.");
        return cliente;
    }

    private PedidoEstatutoResponse toPedidoResponse(PedidoEstatuto p) {
        PedidoEstatutoResponse resp = new PedidoEstatutoResponse();
        resp.setId(p.getId());
        resp.setTipoEstatuto(p.getTipoEstatuto().name());
        resp.setEstado(p.getEstado().name());
        resp.setObservacoesCliente(p.getObservacoesCliente());
        resp.setObservacoesRevisor(p.getObservacoesRevisor());
        resp.setCriadoEm(p.getCriadoEm().toString());
        resp.setAtualizadoEm(p.getAtualizadoEm().toString());
        resp.setDocumentos(p.getDocumentos().stream().map(d ->
                new PedidoEstatutoResponse.DocumentoInfo(
                        d.getId(), d.getNomeFicheiro(), d.getTipoConteudo(),
                        d.getTamanhoBytes(), d.getCriadoEm().toString()
                )).collect(Collectors.toList()));
        return resp;
    }
}
