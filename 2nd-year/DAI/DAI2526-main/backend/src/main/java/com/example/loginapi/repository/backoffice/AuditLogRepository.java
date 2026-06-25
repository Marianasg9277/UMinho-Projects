package com.example.loginapi.repository.backoffice;

import com.example.loginapi.model.backoffice.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findAllByOrderByTimestampDesc();
    Page<AuditLog> findAllByOrderByTimestampDesc(Pageable pageable);
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);
    List<AuditLog> findByAcaoOrderByTimestampDesc(String acao);
    List<AuditLog> findBySucessoOrderByTimestampDesc(boolean sucesso);
}
