package com.example.loginapi.controller.backoffice;

import com.example.loginapi.dto.*;
import com.example.loginapi.model.clientes.PedidoEstatuto;
import com.example.loginapi.model.clientes.enums.EstadoPedidoEstatuto;
import com.example.loginapi.service.clientes.EstatutoService;
import com.example.loginapi.service.clientes.PedidoEstatutoService;
import com.example.loginapi.repository.clientes.PedidoEstatutoRepository;
import com.example.loginapi.repository.clientes.DocumentoEstatutoRepository;
import com.example.loginapi.service.clientes.DocumentoService;
import com.example.loginapi.service.clientes.AuditLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.clientes.DocumentoEstatuto;


@RestController
@RequestMapping("/api/admin/estatutos")
@Tag(name = "Admin \u2013 Estatutos", description = "Revis\u00e3o de pedidos de estatuto (admin only)")
public class AdminEstatutoController {

    @Autowired private PedidoEstatutoService pedidoService;
    @Autowired private PedidoEstatutoRepository pedidoRepo;
    @Autowired private EstatutoService estatutoService;
    @Autowired private DocumentoEstatutoRepository documentoRepo;
    @Autowired private DocumentoService documentoService;
    @Autowired private AuditLogService auditLogService;

    /**
     * Lista pedidos pendentes de revisão (PENDING_APPROVAL + UNDER_REVIEW).
     * Mantido para compatibilidade com código anterior.
     */
    @GetMapping("/pedidos")
    @Transactional(readOnly = true)
    @Operation(summary = "Listar pedidos pendentes de revis\u00e3o")
    public ResponseEntity<List<PedidoEstatutoResponse>> listarPedidosPendentes() {
        List<PedidoEstatutoResponse> pedidos = pedidoRepo.findAllComClienteOrderByDesc()
                .stream()
                .filter(p -> p.getEstado() == EstadoPedidoEstatuto.PENDING_APPROVAL
                          || p.getEstado() == EstadoPedidoEstatuto.UNDER_REVIEW)
                .map(this::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(pedidos);
    }

    /**
     * Lista todos os pedidos com filtro opcional de estado.
     * ?estado=TODOS | PENDING_APPROVAL | UNDER_REVIEW | APPROVED | REJECTED | ...
     */
    @GetMapping("/pedidos/todos")
    @Transactional(readOnly = true)
    @Operation(summary = "Listar todos os pedidos de estatuto (com filtro opcional por estado)")
    public ResponseEntity<List<PedidoEstatutoResponse>> listarTodosPedidos(
            @RequestParam(required = false) String estado) {
        List<PedidoEstatuto> pedidos;
        if (estado != null && !estado.isBlank() && !estado.equalsIgnoreCase("TODOS")) {
            try {
                EstadoPedidoEstatuto est = EstadoPedidoEstatuto.valueOf(estado.toUpperCase());
                pedidos = pedidoRepo.findComClienteByEstado(est);
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        } else {
            pedidos = pedidoRepo.findAllComClienteOrderByDesc();
        }
        return ResponseEntity.ok(pedidos.stream().map(this::toResponse).collect(Collectors.toList()));
    }

    /**
     * Contagem de pedidos pendentes — badge do backoffice.
     */
    @GetMapping("/pedidos/count-pendentes")
    @Operation(summary = "N\u00famero de pedidos aguardando revis\u00e3o")
    public ResponseEntity<Map<String, Long>> countPendentes() {
        long count = pedidoRepo.countPedidosPendentes();
        return ResponseEntity.ok(Map.of("pendentes", count));
    }

    @GetMapping("/pedido/{id}")
    @Transactional(readOnly = true)
    @Operation(summary = "Detalhes de um pedido de estatuto")
    public ResponseEntity<?> obterPedido(@PathVariable Long id) {
        return pedidoRepo.findByIdComCliente(id)
                .map(p -> ResponseEntity.ok((Object) toResponse(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/pedido/{id}/aprovar")
    @Transactional
    @Operation(summary = "Aprovar pedido de estatuto")
    public ResponseEntity<?> aprovar(@PathVariable Long id, @RequestBody(required = false) RevisaoRequest req,
                                      Authentication auth) {
        try {
            String obs = req != null ? req.getObservacoes() : null;
            PedidoEstatuto pedido = pedidoService.aprovarPedido(id, auth.getName(), obs);
            auditLogService.registar(auth.getName(), "ADMIN", "APROVAR_ESTATUTO", "Estatuto", "Aprovou o pedido de estatuto #" + id, true);
            return ResponseEntity.ok(Map.of("message", "Pedido aprovado com sucesso.", "pedido", toResponse(pedido)));
        } catch (Exception e) {
            auditLogService.registar(auth.getName(), "ADMIN", "APROVAR_ESTATUTO", "Estatuto", "Erro ao aprovar pedido #" + id + ": " + e.getMessage(), false);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/pedido/{id}/rejeitar")
    @Transactional
    @Operation(summary = "Rejeitar pedido de estatuto")
    public ResponseEntity<?> rejeitar(@PathVariable Long id, @RequestBody(required = false) RevisaoRequest req,
                                       Authentication auth) {
        try {
            String obs = req != null ? req.getObservacoes() : null;
            PedidoEstatuto pedido = pedidoService.rejeitarPedido(id, auth.getName(), obs);
            auditLogService.registar(auth.getName(), "ADMIN", "REJEITAR_ESTATUTO", "Estatuto", "Rejeitou o pedido de estatuto #" + id, true);
            return ResponseEntity.ok(Map.of("message", "Pedido rejeitado.", "pedido", toResponse(pedido)));
        } catch (Exception e) {
            auditLogService.registar(auth.getName(), "ADMIN", "REJEITAR_ESTATUTO", "Estatuto", "Erro ao rejeitar pedido #" + id + ": " + e.getMessage(), false);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/pedido/{id}/correcao")
    @Transactional
    @Operation(summary = "Pedir corre\u00e7\u00e3o ao utilizador")
    public ResponseEntity<?> pedirCorrecao(@PathVariable Long id, @RequestBody(required = false) RevisaoRequest req,
                                            Authentication auth) {
        try {
            String obs = req != null ? req.getObservacoes() : null;
            PedidoEstatuto pedido = pedidoService.pedirCorrecao(id, auth.getName(), obs);
            auditLogService.registar(auth.getName(), "ADMIN", "PEDIR_CORRECAO_ESTATUTO", "Estatuto", "Pediu correção p/ o pedido de estatuto #" + id, true);
            return ResponseEntity.ok(Map.of("message", "Corre\u00e7\u00e3o solicitada.", "pedido", toResponse(pedido)));
        } catch (Exception e) {
            auditLogService.registar(auth.getName(), "ADMIN", "PEDIR_CORRECAO_ESTATUTO", "Estatuto", "Erro ao pedir correção para pedido #" + id + ": " + e.getMessage(), false);
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/pedido/{id}/documento/{docId}")
    @Transactional(readOnly = true)
    @Operation(summary = "Download de um documento do pedido")
    public ResponseEntity<org.springframework.core.io.Resource> downloadDocumento(@PathVariable Long id, @PathVariable Long docId) {
        try {
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


    // ── Helper — executado sempre dentro de sessão Hibernate activa ─────────────

    private PedidoEstatutoResponse toResponse(PedidoEstatuto p) {
        PedidoEstatutoResponse resp = new PedidoEstatutoResponse();
        resp.setId(p.getId());
        resp.setTipoEstatuto(p.getTipoEstatuto().name());
        resp.setEstado(p.getEstado().name());
        resp.setObservacoesCliente(p.getObservacoesCliente());
        resp.setObservacoesRevisor(p.getObservacoesRevisor());
        resp.setCriadoEm(p.getCriadoEm().toString());
        resp.setAtualizadoEm(p.getAtualizadoEm().toString());
        resp.setRevisadoPor(p.getRevisorEmail());

        // Documentos (lazy — carregados dentro da transação)
        try {
            resp.setDocumentos(p.getDocumentos().stream().map(d ->
                    new PedidoEstatutoResponse.DocumentoInfo(
                            d.getId(), d.getNomeFicheiro(), d.getTipoConteudo(),
                            d.getTamanhoBytes(), d.getCriadoEm().toString()
                    )).collect(Collectors.toList()));
        } catch (Exception e) {
            resp.setDocumentos(java.util.Collections.emptyList());
        }

        // Dados do cliente (carregados pelo JOIN FETCH na query)
        if (p.getCliente() != null) {
            resp.setNomeCliente(p.getCliente().getNomeCompleto());
            try {
                if (p.getCliente().getUtilizador() != null) {
                    resp.setEmailCliente(p.getCliente().getUtilizador().getEmail());
                }
            } catch (Exception e) { /* lazy fallback */ }
            // Estatuto efetivo
            try {
                resp.setEstatutoAtual(estatutoService.resolverEstatutoEfetivo(p.getCliente()).name());
            } catch (Exception e) {
                resp.setEstatutoAtual("SEM_ESTATUTO");
            }
        }

        return resp;
    }
}
