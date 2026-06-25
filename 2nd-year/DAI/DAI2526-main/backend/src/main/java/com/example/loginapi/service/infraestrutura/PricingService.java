package com.example.loginapi.service.infraestrutura;

import com.example.loginapi.model.infraestrutura.Coroa;
import com.example.loginapi.model.infraestrutura.RegraPreco;
import com.example.loginapi.model.titulos.TipoPasse;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import com.example.loginapi.repository.infraestrutura.RegraPrecoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.titulos.Passe;


/**
 * Serviço de resolução de preços.
 *
 * Regras:
 * - O preço depende de (estatuto + tipo de passe + coroa).
 * - A regra ativa é a mais recente cuja vigência contenha a data atual.
 * - Se não existir regra, o passe não pode ser criado.
 */
@Service
public class PricingService {

    @Autowired
    private RegraPrecoRepository regraPrecoRepo;

    /**
     * Resolve a regra de preço aplicável para a combinação dada na data atual.
     *
     * @return a regra encontrada ou Optional.empty() se não existir regra válida
     */
    public Optional<RegraPreco> resolverRegra(TipoEstatuto estatuto, TipoPasse tipoPasse, Coroa coroa) {
        return resolverRegra(estatuto, tipoPasse, coroa, LocalDate.now());
    }

    /**
     * Resolve a regra de preço aplicável para uma data específica.
     */
    public Optional<RegraPreco> resolverRegra(TipoEstatuto estatuto, TipoPasse tipoPasse,
                                                Coroa coroa, LocalDate data) {
        List<RegraPreco> regras = regraPrecoRepo.findVigentes(estatuto, tipoPasse, coroa, data);
        // A query já ordena por dataInicioVigencia DESC, então a primeira é a mais recente
        return regras.isEmpty() ? Optional.empty() : Optional.of(regras.get(0));
    }

    /**
     * Lista todas as opções de preço vigentes para um dado estatuto.
     * Usado para apresentar opções ao utilizador.
     */
    public List<RegraPreco> listarOpcoesPorEstatuto(TipoEstatuto estatuto) {
        return regraPrecoRepo.findVigentesPorEstatuto(estatuto, LocalDate.now());
    }

    /**
     * Lista todas as regras de preço (para admin).
     */
    public List<RegraPreco> listarTodasRegras() {
        return regraPrecoRepo.findAllByOrderByCriadoEmDesc();
    }

    /**
     * Cria nova regra de preço (admin).
     */
    public RegraPreco criarRegra(RegraPreco regra) {
        return regraPrecoRepo.save(regra);
    }

    /**
     * Desativa uma regra de preço (admin).
     */
    public RegraPreco desativarRegra(Long id) {
        RegraPreco regra = regraPrecoRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Regra de preço não encontrada."));
        regra.setAtivo(false);
        return regraPrecoRepo.save(regra);
    }
}
