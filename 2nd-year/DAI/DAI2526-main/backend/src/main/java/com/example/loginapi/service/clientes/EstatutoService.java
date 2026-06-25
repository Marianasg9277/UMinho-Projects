package com.example.loginapi.service.clientes;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.clientes.EstatutoUtilizador;
import com.example.loginapi.model.clientes.enums.EstadoEstatutoUtilizador;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import com.example.loginapi.repository.clientes.EstatutoUtilizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.clientes.PedidoEstatuto;


/**
 * Serviço responsável pela resolução do estatuto efetivo de um utilizador.
 *
 * Regras:
 * - Cada utilizador tem sempre um estatuto resolvido (nunca null).
 * - Se não existir estatuto ACTIVE, devolve SEM_ESTATUTO.
 * - SENIOR e CRIANCA são derivados automaticamente da idade.
 * - As idades são configuráveis via application.properties.
 */
@Service
public class EstatutoService {

    @Autowired
    private EstatutoUtilizadorRepository estatutoRepo;

    /** Idade mínima para SENIOR — configurável. */
    @Value("${tub.estatuto.senior.idade-minima:65}")
    private int idadeMiniaSenior;

    /** Idade máxima para CRIANCA — configurável. */
    @Value("${tub.estatuto.crianca.idade-maxima:12}")
    private int idadeMaximaCrianca;

    /**
     * Resolve o estatuto efetivo actual de um Cliente.
     * Nunca retorna null — usa SEM_ESTATUTO como fallback.
     */
    public TipoEstatuto resolverEstatutoEfetivo(Cliente cliente) {
        // 1. Verificar estatutos automáticos por idade
        TipoEstatuto automatico = resolverEstatutoAutomatico(cliente);
        if (automatico != null) {
            return automatico;
        }

        // 2. Verificar estatutos ACTIVE na BD
        Optional<EstatutoUtilizador> ativo = estatutoRepo.findByClienteAndEstado(
                cliente, EstadoEstatutoUtilizador.ACTIVE);

        if (ativo.isPresent()) {
            EstatutoUtilizador est = ativo.get();
            // Verificar se não expirou
            if (est.getDataFim() != null && LocalDate.now().isAfter(est.getDataFim())) {
                est.setEstado(EstadoEstatutoUtilizador.EXPIRED);
                estatutoRepo.save(est);
                return TipoEstatuto.SEM_ESTATUTO;
            }
            return est.getTipoEstatuto();
        }

        // 3. Fallback
        return TipoEstatuto.SEM_ESTATUTO;
    }

    /**
     * Verifica se a idade do cliente corresponde a SENIOR ou CRIANCA.
     * Retorna null se não se aplica nenhum estatuto automático.
     */
    private TipoEstatuto resolverEstatutoAutomatico(Cliente cliente) {
        if (cliente.getDataNascimento() == null) return null;

        int idade = Period.between(cliente.getDataNascimento(), LocalDate.now()).getYears();

        if (idade <= idadeMaximaCrianca) {
            return TipoEstatuto.CRIANCA;
        }
        if (idade >= idadeMiniaSenior) {
            return TipoEstatuto.SENIOR;
        }
        return null;
    }

    /**
     * Verifica se o utilizador pode solicitar um determinado tipo de estatuto.
     * Ex: não pode pedir CRIANCA manualmente (é automático), nem pedir algo que já tem ACTIVE.
     */
    public boolean podeSubmeterPedido(Cliente cliente, TipoEstatuto tipo) {
        if (tipo == TipoEstatuto.SEM_ESTATUTO) return false;

        // Verificar se já tem este estatuto ACTIVE
        Optional<EstatutoUtilizador> existente = estatutoRepo.findByClienteAndTipoEstatutoAndEstado(
                cliente, tipo, EstadoEstatutoUtilizador.ACTIVE);
        return existente.isEmpty();
    }

    /**
     * Cria um registo de estatuto ACTIVE para o utilizador.
     * Desactiva qualquer estatuto anterior do mesmo tipo.
     */
    public EstatutoUtilizador ativarEstatuto(Cliente cliente, TipoEstatuto tipo,
                                               com.example.loginapi.model.clientes.PedidoEstatuto pedido,
                                               Integer validadeDias) {
        // Desativar estatutos conflitantes (ACTIVE do mesmo cliente que não sejam automáticos)
        List<EstatutoUtilizador> ativos = estatutoRepo.findByClienteOrderByCriadoEmDesc(cliente);
        for (EstatutoUtilizador e : ativos) {
            if (e.getEstado() == EstadoEstatutoUtilizador.ACTIVE
                    && !e.getTipoEstatuto().isAutomatico()) {
                e.setEstado(EstadoEstatutoUtilizador.INACTIVE);
                estatutoRepo.save(e);
            }
        }

        EstatutoUtilizador novo = new EstatutoUtilizador();
        novo.setCliente(cliente);
        novo.setTipoEstatuto(tipo);
        novo.setEstado(EstadoEstatutoUtilizador.ACTIVE);
        novo.setDataInicio(LocalDate.now());
        if (validadeDias != null) {
            novo.setDataFim(LocalDate.now().plusDays(validadeDias));
        }
        novo.setPedido(pedido);
        return estatutoRepo.save(novo);
    }

    /**
     * Obtém o histórico de estatutos do utilizador.
     */
    public List<EstatutoUtilizador> obterHistorico(Cliente cliente) {
        return estatutoRepo.findByClienteOrderByCriadoEmDesc(cliente);
    }

    // ── Getters para limiares (usados em testes) ─────────────────────────────

    public int getIdadeMinimaSenior() { return idadeMiniaSenior; }
    public int getIdadeMaximaCrianca() { return idadeMaximaCrianca; }
}
