package com.example.loginapi.repository.comunicacao;

import com.example.loginapi.model.comunicacao.Aviso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AvisoRepository extends JpaRepository<Aviso, Long> {
    List<Aviso> findAllByOrderByDataHoraDesc();
    long countByNovoTrue();
}
