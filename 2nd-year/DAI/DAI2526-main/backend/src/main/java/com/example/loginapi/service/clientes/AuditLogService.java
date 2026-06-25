package com.example.loginapi.service.clientes;

import com.example.loginapi.model.backoffice.AuditLog;
import com.example.loginapi.repository.backoffice.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import com.example.loginapi.model.comunicacao.Aviso;


/**
 * Central service for recording audit trail entries.
 * Call this from controllers/services after each significant action.
 */
@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository repo;

    /**
     * Records an audit event.
     *
     * @param username  email or "anonymous"
     * @param role      role string from authentication, or null
     * @param acao      short action key (e.g. LOGIN_SUCCESS)
     * @param recurso   affected resource (e.g. "auth", "aviso", "export")
     * @param detalhes  free-text detail
     * @param sucesso   whether the operation succeeded
     * @param request   HTTP request for IP extraction (may be null)
     */
    public void registar(String username, String role, String acao,
                         String recurso, String detalhes, boolean sucesso,
                         HttpServletRequest request) {
        AuditLog log = new AuditLog();
        log.setTimestamp(LocalDateTime.now());
        log.setUsername(username != null ? username : "anonymous");
        log.setRole(role);
        log.setAcao(acao);
        log.setRecurso(recurso);
        log.setDetalhes(detalhes);
        log.setSucesso(sucesso);
        if (request != null) {
            String ip = request.getHeader("X-Forwarded-For");
            if (ip == null || ip.isBlank()) ip = request.getRemoteAddr();
            log.setIp(ip);
        }
        repo.save(log);
    }

    /** Convenience – no HTTP request. */
    public void registar(String username, String role, String acao,
                         String recurso, String detalhes, boolean sucesso) {
        registar(username, role, acao, recurso, detalhes, sucesso, null);
    }

    public List<AuditLog> listarTodos() {
        return repo.findAllByOrderByTimestampDesc();
    }

    public List<AuditLog> filtrarPorUtilizador(String username) {
        return repo.findByUsernameOrderByTimestampDesc(username);
    }

    public List<AuditLog> filtrarPorAcao(String acao) {
        return repo.findByAcaoOrderByTimestampDesc(acao);
    }

    public List<AuditLog> filtrarPorSucesso(boolean sucesso) {
        return repo.findBySucessoOrderByTimestampDesc(sucesso);
    }
}
