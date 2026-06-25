package com.example.loginapi.repository.colaboradores;

import com.example.loginapi.model.colaboradores.Colaborador;
import com.example.loginapi.model.colaboradores.enums.TipoColaborador;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ColaboradorRepository extends JpaRepository<Colaborador, Long> {

    List<Colaborador> findAllByAtivo(Boolean ativo);

    List<Colaborador> findAllByTipoColaborador(TipoColaborador tipo);

    List<Colaborador> findAllByTipoColaboradorAndAtivo(TipoColaborador tipo, Boolean ativo);

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    Optional<Colaborador> findByNif(String nif);

    boolean existsByNifAndIdNot(String nif, Long id);

    Optional<Colaborador> findByEmailAndAtivoTrue(String email);
}
