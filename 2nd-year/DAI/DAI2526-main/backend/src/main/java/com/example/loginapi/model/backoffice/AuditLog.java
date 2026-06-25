package com.example.loginapi.model.backoffice;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    /** Email or "anonymous" */
    @Column(nullable = false, length = 200)
    private String username;

    /** Role at time of action */
    @Column(length = 20)
    private String role;

    /** Short action key, e.g. LOGIN_SUCCESS, TICKET_PURCHASED */
    @Column(nullable = false, length = 60)
    private String acao;

    /** Resource or module involved */
    @Column(length = 100)
    private String recurso;

    /** Free-text detail */
    @Column(length = 500)
    private String detalhes;

    @Column(nullable = false)
    private boolean sucesso;

    /** Best-effort IP from request */
    @Column(length = 50)
    private String ip;

    public AuditLog() {}

    // ── Getters & Setters ───────────────────────────────────────────────────

    public Long getId() { return id; }

    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAcao() { return acao; }
    public void setAcao(String acao) { this.acao = acao; }

    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }

    public String getDetalhes() { return detalhes; }
    public void setDetalhes(String detalhes) { this.detalhes = detalhes; }

    public boolean isSucesso() { return sucesso; }
    public void setSucesso(boolean sucesso) { this.sucesso = sucesso; }

    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
}
