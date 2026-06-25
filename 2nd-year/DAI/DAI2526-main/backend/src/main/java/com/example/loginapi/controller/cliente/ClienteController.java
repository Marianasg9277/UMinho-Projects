package com.example.loginapi.controller.cliente;

import com.example.loginapi.dto.AtualizarDadosClienteRequest;
import com.example.loginapi.dto.ClienteDadosResponse;
import com.example.loginapi.dto.HistoricoValidacaoDTO;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.titulos.HistoricoValidacao;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.comunicacao.Notificacao;
import com.example.loginapi.model.titulos.Passe;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.model.titulos.enums.EstadoComercialPasse;
import com.example.loginapi.model.pagamentos.enums.EstadoPagamento;
import com.example.loginapi.repository.titulos.HistoricoValidacaoRepository;
import com.example.loginapi.repository.titulos.PasseRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.service.clientes.AuditLogService;
import com.example.loginapi.service.clientes.ClienteService;
import com.example.loginapi.service.titulos.FotoPasseService;
import com.example.loginapi.service.comunicacao.NotificacaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.infraestrutura.Coroa;


@RestController
@RequestMapping("/api/cliente")
@Tag(name = "Cliente", description = "Dados pessoais e historico do cliente autenticado")
public class ClienteController {

    @Autowired private ClienteService clienteService;
    @Autowired private FotoPasseService fotoPasseService;
    @Autowired private AuditLogService auditLogService;
    @Autowired private NotificacaoService notificacaoService;
    @Autowired private HistoricoValidacaoRepository historicoValidacaoRepo;
    @Autowired private TransacaoRepository transacaoRepo;
    @Autowired private PasseRepository passeRepo;

    @GetMapping("/dados")
    @Operation(summary = "Consultar dados pessoais do cliente autenticado")
    public ResponseEntity<?> consultarDadosCliente(Authentication authentication) {
        Cliente cliente = clienteService.getClienteByEmail(authentication.getName());
        if (cliente == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Cliente nao encontrado."));
        }

        return ResponseEntity.ok(new ClienteDadosResponse(cliente, "Utilizador autenticado"));
    }

    @PutMapping("/atualizar-dados")
    @Operation(summary = "Atualizar dados pessoais do cliente autenticado")
    public ResponseEntity<?> atualizarDadosCliente(
            @Valid @RequestBody AtualizarDadosClienteRequest request,
            Authentication authentication,
            HttpServletRequest httpRequest
    ) {
        try {
            Cliente atualizado = clienteService.atualizarDadosCliente(authentication.getName(), request);
            String role = atualizado.getUtilizador() != null
                    ? atualizado.getUtilizador().getRole().name()
                    : null;

            auditLogService.registar(
                    authentication.getName(),
                    role,
                    "DADOS_ATUALIZADOS",
                    "cliente",
                    "Dados pessoais atualizados",
                    true,
                    httpRequest
            );
            notificacaoService.criarParaUtilizador(
                    authentication.getName(),
                    "Dados atualizados",
                    "Os seus dados pessoais foram atualizados com sucesso.",
                    Notificacao.Tipo.SUCESSO
            );

            return ResponseEntity.ok(new ClienteDadosResponse(atualizado, "Dados atualizados com sucesso."));
        } catch (NoSuchElementException e) {
            auditLogService.registar(
                    authentication != null ? authentication.getName() : null,
                    null,
                    "DADOS_ATUALIZADOS",
                    "cliente",
                    e.getMessage(),
                    false,
                    httpRequest
            );
            return ResponseEntity.status(404).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping(value = "/foto-passe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Carregar fotografia para o passe digital do cliente autenticado")
    public ResponseEntity<?> carregarFotoPasse(
            @RequestParam("foto") MultipartFile foto,
            Authentication authentication) {
        Cliente cliente = clienteService.getClienteByEmail(authentication.getName());
        if (cliente == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Cliente não encontrado."));
        }
        try {
            fotoPasseService.guardarFoto(cliente, foto);
            return ResponseEntity.ok(Map.of(
                    "message", "Fotografia carregada com sucesso.",
                    "fotoPasseUrl", "/api/cliente/foto-passe"
            ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erro ao guardar fotografia."));
        }
    }

    @DeleteMapping("/foto-passe")
    @Operation(summary = "Remover fotografia do passe digital do cliente autenticado")
    public ResponseEntity<?> removerFotoPasse(Authentication authentication) {
        Cliente cliente = clienteService.getClienteByEmail(authentication.getName());
        if (cliente == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Cliente não encontrado."));
        }
        if (cliente.getFotoPassePath() == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Não existe fotografia registada."));
        }
        try {
            fotoPasseService.removerFoto(cliente);
            return ResponseEntity.ok(Map.of("message", "Fotografia removida com sucesso."));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("message", "Erro ao remover fotografia."));
        }
    }

    @GetMapping("/foto-passe")
    @Operation(summary = "Obter fotografia do passe digital do cliente autenticado")
    public ResponseEntity<byte[]> obterFotoPasse(Authentication authentication) {
        Cliente cliente = clienteService.getClienteByEmail(authentication.getName());
        if (cliente == null || cliente.getFotoPassePath() == null) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] imagem = fotoPasseService.lerFoto(cliente);
            String contentType = fotoPasseService.inferirContentType(cliente.getFotoPassePath());
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("private, no-store");
            return ResponseEntity.ok().headers(headers).body(imagem);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/historico-viagens")
    @Operation(summary = "Consultar historico de validacoes do cliente autenticado")
    public ResponseEntity<?> consultarHistoricoViagens(
            Authentication authentication,
            HttpServletRequest httpRequest,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String tipo
    ) {
        Cliente cliente = clienteService.getClienteByEmail(authentication.getName());
        if (cliente == null) {
            return ResponseEntity.status(404).body(Map.of("message", "Cliente nao encontrado."));
        }

        HistoricoValidacao.TipoTitulo tipoTitulo;
        try {
            tipoTitulo = parseTipoTitulo(tipo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }

        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        List<HistoricoValidacaoDTO> historicoCompleto = construirHistoricoCompleto(cliente, tipoTitulo);

        int totalElementos = historicoCompleto.size();
        int totalPaginas = totalElementos == 0 ? 0 : (int) Math.ceil((double) totalElementos / safeSize);
        int inicioPagina = safePage * safeSize;
        int fimPagina = Math.min(inicioPagina + safeSize, totalElementos);
        List<HistoricoValidacaoDTO> dtos = inicioPagina >= totalElementos
                ? List.of()
                : historicoCompleto.subList(inicioPagina, fimPagina);

        String role = cliente.getUtilizador() != null ? cliente.getUtilizador().getRole().name() : null;
        auditLogService.registar(
                authentication.getName(),
                role,
                "HISTORICO_VIAGENS_CONSULTADO",
                "historico_validacoes",
                "Historico de viagens consultado",
                true,
                httpRequest
        );

        Map<String, Object> response = new HashMap<>();
        response.put("historico", dtos);
        response.put("totalElementos", totalElementos);
        response.put("totalPaginas", totalPaginas);
        response.put("paginaAtual", safePage);
        response.put("tamanhoPagina", safeSize);

        return ResponseEntity.ok(response);
    }

    private List<HistoricoValidacaoDTO> construirHistoricoCompleto(
            Cliente cliente,
            HistoricoValidacao.TipoTitulo tipoTitulo
    ) {
        List<HistoricoValidacaoDTO> historico = new ArrayList<>();

        historicoValidacaoRepo.findAllByClienteIdOrderByDataValidacaoDesc(cliente.getId()).stream()
                .filter(h -> tipoTitulo == null || h.getTipoTitulo() == tipoTitulo)
                .map(this::toHistoricoDto)
                .forEach(historico::add);

        Set<Long> transacoesComHistorico = new HashSet<>(
                historicoValidacaoRepo.findTransacaoIdsByClienteId(cliente.getId())
        );
        Set<Long> passesComHistorico = new HashSet<>(
                historicoValidacaoRepo.findPasseIdsByClienteId(cliente.getId())
        );

        if (tipoTitulo == null || tipoTitulo == HistoricoValidacao.TipoTitulo.BILHETE) {
            transacaoRepo.findByClienteAndEstadoPagamentoOrderByDataCompraDesc(cliente, EstadoPagamento.USED)
                    .stream()
                    .filter(t -> t.getId() != null && !transacoesComHistorico.contains(t.getId()))
                    .map(this::toHistoricoBilheteLegadoDto)
                    .forEach(historico::add);
        }

        if (tipoTitulo == null || tipoTitulo == HistoricoValidacao.TipoTitulo.PASSE) {
            passeRepo.findByClienteOrderByCriadoEmDesc(cliente)
                    .stream()
                    .filter(p -> p.getId() != null && !passesComHistorico.contains(p.getId()))
                    .filter(this::deveIncluirPasseLegado)
                    .map(this::toHistoricoPasseLegadoDto)
                    .forEach(historico::add);
        }

        historico.sort((a, b) -> {
            LocalDateTime dataA = a.getDataValidacao();
            LocalDateTime dataB = b.getDataValidacao();
            if (dataA == null && dataB == null) return 0;
            if (dataA == null) return 1;
            if (dataB == null) return -1;
            return dataB.compareTo(dataA);
        });
        return historico;
    }

    private HistoricoValidacaoDTO toHistoricoDto(HistoricoValidacao h) {
        return new HistoricoValidacaoDTO(
                h.getId(),
                h.getDataValidacao(),
                h.getTipoTitulo().name(),
                h.getTipoDescricao(),
                formatarLinha(h.getLinha()),
                h.getDetalhes()
        );
    }

    private HistoricoValidacaoDTO toHistoricoBilheteLegadoDto(Transacao transacao) {
        return new HistoricoValidacaoDTO(
                null,
                dataHistoricoBilheteLegado(transacao),
                HistoricoValidacao.TipoTitulo.BILHETE.name(),
                transacao.getTipoBilhete() != null ? transacao.getTipoBilhete().getNome() : "Bilhete",
                formatarLinha(transacao.getLinha()),
                "Registo anterior"
        );
    }

    private HistoricoValidacaoDTO toHistoricoPasseLegadoDto(Passe passe) {
        List<String> detalhes = new ArrayList<>();
        if (passe.getCoroa() != null) {
            detalhes.add("Coroa: " + passe.getCoroa().getNome());
        }
        if (passe.getDataFim() != null) {
            detalhes.add("Valido ate " + passe.getDataFim());
        }
        detalhes.add("Registo anterior");

        return new HistoricoValidacaoDTO(
                null,
                dataHistoricoPasseLegado(passe),
                HistoricoValidacao.TipoTitulo.PASSE.name(),
                passe.getTipoPasse() != null ? passe.getTipoPasse().getNome() : "Passe",
                "Linha nao especificada",
                String.join(" | ", detalhes)
        );
    }

    private boolean deveIncluirPasseLegado(Passe passe) {
        if (passe == null || passe.getEstadoComercial() != EstadoComercialPasse.PAID) {
            return false;
        }
        return passe.getDataInicio() == null || !passe.getDataInicio().isAfter(LocalDate.now());
    }

    private LocalDateTime dataHistoricoBilheteLegado(Transacao transacao) {
        if (transacao.getDataCompra() != null) {
            return transacao.getDataCompra();
        }
        return transacao.getValidoAte();
    }

    private LocalDateTime dataHistoricoPasseLegado(Passe passe) {
        if (passe.getDataInicio() != null) {
            return passe.getDataInicio().atStartOfDay();
        }
        if (passe.getCriadoEm() != null) {
            return passe.getCriadoEm();
        }
        return null;
    }

    private String formatarLinha(Linha linha) {
        if (linha == null) {
            return "Linha nao especificada";
        }
        String numero = linha.getNumero() != null && !linha.getNumero().isBlank()
                ? "Linha " + linha.getNumero()
                : linha.getNome();
        return numero + ": " + linha.getOrigem() + " - " + linha.getDestino();
    }

    private HistoricoValidacao.TipoTitulo parseTipoTitulo(String tipo) {
        if (tipo == null || tipo.isBlank() || "TODOS".equalsIgnoreCase(tipo)) {
            return null;
        }
        try {
            return HistoricoValidacao.TipoTitulo.valueOf(tipo.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Tipo de titulo invalido. Use PASSE ou BILHETE.");
        }
    }

}
