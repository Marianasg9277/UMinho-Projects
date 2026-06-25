package com.example.loginapi.controller.backoffice;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.comunicacao.Aviso;
import com.example.loginapi.model.backoffice.AuditLog;
import com.example.loginapi.model.titulos.Transacao;
import com.example.loginapi.repository.clientes.ClienteRepository;
import com.example.loginapi.repository.comunicacao.AvisoRepository;
import com.example.loginapi.repository.backoffice.AuditLogRepository;
import com.example.loginapi.repository.titulos.TransacaoRepository;
import com.example.loginapi.service.clientes.AuditLogService;
import com.example.loginapi.util.CsvExportUtil;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.titulos.TipoBilhete;


/**
 * Export endpoints (CSV + JSON) – admin only.
 * All under /api/admin/export/** protected by SecurityConfig.
 */
@RestController
@RequestMapping("/api/admin/export")
@Tag(name = "Exportação", description = "Exportação de dados em CSV e JSON (admin only)")
public class ExportController {

    @Autowired private ClienteRepository clienteRepo;
    @Autowired private AvisoRepository avisoRepo;
    @Autowired private AuditLogRepository auditLogRepo;
    @Autowired private TransacaoRepository transacaoRepo;
    @Autowired private AuditLogService auditLogService;

    // ─────────────────────────────────────────────────────────────────────────
    // Utilizadores
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/utilizadores/csv")
    @Operation(summary = "Exportar utilizadores em CSV")
    public ResponseEntity<byte[]> exportUtilizadoresCsv(Authentication auth, HttpServletRequest req) {
        List<Cliente> clientes = clienteRepo.findAll();
        String[] headers = {"id", "nome", "sobrenome", "nif", "telefone", "morada", "dataNascimento"};
        List<String[]> rows = new ArrayList<>();
        for (Cliente c : clientes) {
            rows.add(new String[]{
                str(c.getId()), c.getNome(), c.getSobrenome(),
                c.getNif(), c.getTelefone(), c.getMorada(),
                str(c.getDataNascimento())
            });
        }
        String csv = CsvExportUtil.toCsv(headers, rows);
        auditLogService.registar(auth.getName(), "ADMIN", "EXPORT_CSV", "utilizadores",
            "Exportados " + clientes.size() + " utilizadores", true, req);
        return csvResponse(csv, "utilizadores.csv");
    }

    @GetMapping("/utilizadores/json")
    @Operation(summary = "Exportar utilizadores em JSON")
    public ResponseEntity<List<Cliente>> exportUtilizadoresJson(Authentication auth, HttpServletRequest req) {
        List<Cliente> clientes = clienteRepo.findAll();
        auditLogService.registar(auth.getName(), "ADMIN", "EXPORT_JSON", "utilizadores",
            "Exportados " + clientes.size() + " utilizadores", true, req);
        return jsonResponse(clientes, "utilizadores.json");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Avisos
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/avisos/csv")
    @Operation(summary = "Exportar avisos em CSV")
    public ResponseEntity<byte[]> exportAvisosCsv(Authentication auth, HttpServletRequest req) {
        List<Aviso> avisos = avisoRepo.findAll();
        String[] headers = {"id", "titulo", "descricao", "tipo", "dataHora", "novo"};
        List<String[]> rows = new ArrayList<>();
        for (Aviso a : avisos) {
            rows.add(new String[]{
                str(a.getId()), a.getTitulo(), a.getDescricao(),
                str(a.getTipo()), str(a.getDataHora()), str(a.isNovo())
            });
        }
        String csv = CsvExportUtil.toCsv(headers, rows);
        auditLogService.registar(auth.getName(), "ADMIN", "EXPORT_CSV", "avisos",
            "Exportados " + avisos.size() + " avisos", true, req);
        return csvResponse(csv, "avisos.csv");
    }

    @GetMapping("/avisos/json")
    @Operation(summary = "Exportar avisos em JSON")
    public ResponseEntity<List<Aviso>> exportAvisosJson(Authentication auth, HttpServletRequest req) {
        List<Aviso> avisos = avisoRepo.findAll();
        auditLogService.registar(auth.getName(), "ADMIN", "EXPORT_JSON", "avisos",
            "Exportados " + avisos.size() + " avisos", true, req);
        return jsonResponse(avisos, "avisos.json");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Logs
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/logs/csv")
    @Operation(summary = "Exportar logs de auditoria em CSV")
    public ResponseEntity<byte[]> exportLogsCsv(Authentication auth, HttpServletRequest req) {
        List<AuditLog> logs = auditLogRepo.findAllByOrderByTimestampDesc();
        String[] headers = {"id", "timestamp", "username", "role", "acao", "recurso", "detalhes", "sucesso", "ip"};
        List<String[]> rows = new ArrayList<>();
        for (AuditLog l : logs) {
            rows.add(new String[]{
                str(l.getId()), str(l.getTimestamp()), l.getUsername(),
                l.getRole(), l.getAcao(), l.getRecurso(), l.getDetalhes(),
                str(l.isSucesso()), l.getIp()
            });
        }
        String csv = CsvExportUtil.toCsv(headers, rows);
        auditLogService.registar(auth.getName(), "ADMIN", "EXPORT_CSV", "logs",
            "Exportados " + logs.size() + " logs", true, req);
        return csvResponse(csv, "audit_logs.csv");
    }

    @GetMapping("/logs/json")
    @Operation(summary = "Exportar logs de auditoria em JSON")
    public ResponseEntity<List<AuditLog>> exportLogsJson(Authentication auth, HttpServletRequest req) {
        List<AuditLog> logs = auditLogRepo.findAllByOrderByTimestampDesc();
        auditLogService.registar(auth.getName(), "ADMIN", "EXPORT_JSON", "logs",
            "Exportados " + logs.size() + " logs", true, req);
        return jsonResponse(logs, "audit_logs.json");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Transações
    // ─────────────────────────────────────────────────────────────────────────

    @GetMapping("/transacoes/csv")
    @Operation(summary = "Exportar transações em CSV")
    public ResponseEntity<byte[]> exportTransacoesCsv(Authentication auth, HttpServletRequest req) {
        List<Transacao> list = transacaoRepo.findAll();
        String[] headers = {"id", "clienteEmail", "guestEmail", "tipoBilhete", "linha", "dataCompra", "codigoQr"};
        List<String[]> rows = new ArrayList<>();
        for (Transacao t : list) {
            String clienteEmail = t.getCliente() != null
                ? t.getCliente().getUtilizador().getEmail() : "-";
            String linhaNome = t.getLinha() != null ? t.getLinha().getNome() : "-";
            rows.add(new String[]{
                str(t.getId()), clienteEmail, t.getGuestEmail(),
                t.getTipoBilhete().getNome(), linhaNome,
                str(t.getDataCompra()), t.getCodigoQr()
            });
        }
        String csv = CsvExportUtil.toCsv(headers, rows);
        auditLogService.registar(auth.getName(), "ADMIN", "EXPORT_CSV", "transacoes",
            "Exportadas " + list.size() + " transações", true, req);
        return csvResponse(csv, "transacoes.csv");
    }

    @GetMapping("/transacoes/json")
    @Operation(summary = "Exportar transações em JSON")
    public ResponseEntity<List<Transacao>> exportTransacoesJson(Authentication auth, HttpServletRequest req) {
        List<Transacao> list = transacaoRepo.findAll();
        auditLogService.registar(auth.getName(), "ADMIN", "EXPORT_JSON", "transacoes",
            "Exportadas " + list.size() + " transações", true, req);
        return jsonResponse(list, "transacoes.json");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    private ResponseEntity<byte[]> csvResponse(String csv, String filename) {
        byte[] bytes = csv.getBytes(StandardCharsets.UTF_8);
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .header(HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
            .body(bytes);
    }

    private <T> ResponseEntity<T> jsonResponse(T body, String filename) {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body);
    }

    private String str(Object o) {
        return o == null ? "" : o.toString();
    }
}
