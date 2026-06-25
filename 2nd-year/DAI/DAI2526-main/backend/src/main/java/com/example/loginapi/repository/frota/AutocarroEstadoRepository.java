package com.example.loginapi.repository.frota;

import com.example.loginapi.model.frota.AutocarroEstado;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AutocarroEstadoRepository extends JpaRepository<AutocarroEstado, Long> {
    Optional<AutocarroEstado> findByAutocarroId(Long autocarroId);
}
