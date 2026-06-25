package com.example.loginapi.service.clientes;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.clientes.PedidoEstatuto;
import com.example.loginapi.model.clientes.enums.EstadoPedidoEstatuto;
import com.example.loginapi.model.clientes.enums.TipoEstatuto;
import com.example.loginapi.repository.clientes.PedidoEstatutoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.example.loginapi.model.clientes.Utilizador;


/**
 * Serviço que gere o workflow de pedidos de estatuto:
 * DRAFT → PENDING_APPROVAL → UNDER_REVIEW → APPROVED / REJECTED /
 * CORRECTION_REQUESTED
 *
 * Todos os pedidos, incluindo SENIOR e CRIANCA, requerem revisão manual.
 * Não existe aprovação automática.
 */
@Service
public class PedidoEstatutoService {

    @Autowired
    private PedidoEstatutoRepository pedidoRepo;
    @Autowired
    private EstatutoService estatutoService;

    private static final int VALIDADE_ESTUDANTE = 365;
    private static final int VALIDADE_RESIDENTE = 365;
    private static final int VALIDADE_MILITAR = 365;

    /**
     * Cria um novo pedido de estatuto em estado DRAFT.
     * O utilizador ainda pode anexar documentos antes de submeter.
     */
    public PedidoEstatuto criarPedido(Cliente cliente, TipoEstatuto tipo, String observacoes) {
        if (tipo == null || tipo == TipoEstatuto.SEM_ESTATUTO) {
            throw new IllegalArgumentException("Tipo de estatuto inválido.");
        }
        if (!estatutoService.podeSubmeterPedido(cliente, tipo)) {
            throw new IllegalArgumentException("Não é possível criar um pedido para este tipo de estatuto.");
        }

        PedidoEstatuto pedido = new PedidoEstatuto();
        pedido.setCliente(cliente);
        pedido.setTipoEstatuto(tipo);
        pedido.setEstado(EstadoPedidoEstatuto.DRAFT);
        pedido.setObservacoesCliente(observacoes);
        return pedidoRepo.save(pedido);
    }

    /**
     * Submete o pedido para revisão manual.
     * Transição: DRAFT / CORRECTION_REQUESTED → PENDING_APPROVAL.
     * Não existe aprovação automática — todos os pedidos aguardam decisão de admin.
     */
    @Transactional
    public PedidoEstatuto submeterPedido(Long pedidoId, Cliente cliente) {
        PedidoEstatuto pedido = obterPedidoDoCliente(pedidoId, cliente);

        if (pedido.getEstado() != EstadoPedidoEstatuto.DRAFT
                && pedido.getEstado() != EstadoPedidoEstatuto.CORRECTION_REQUESTED) {
            throw new IllegalStateException("O pedido não está num estado que permita submissão.");
        }

        if (pedido.getTipoEstatuto().exigeDocumentos() && pedido.getDocumentos().isEmpty()) {
            throw new IllegalStateException("É necessário anexar documentos antes de submeter o pedido.");
        }

        pedido.setEstado(EstadoPedidoEstatuto.PENDING_APPROVAL);
        return pedidoRepo.save(pedido);
    }

    /**
     * Admin: inicia revisão do pedido.
     * Transição: PENDING_APPROVAL → UNDER_REVIEW.
     */
    public PedidoEstatuto iniciarRevisao(Long pedidoId, String emailRevisor) {
        PedidoEstatuto pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

        if (pedido.getEstado() != EstadoPedidoEstatuto.PENDING_APPROVAL) {
            throw new IllegalStateException("Pedido não está em estado PENDING_APPROVAL.");
        }

        pedido.setEstado(EstadoPedidoEstatuto.UNDER_REVIEW);
        pedido.setRevisorEmail(emailRevisor);
        return pedidoRepo.save(pedido);
    }

    /**
     * Admin: aprovar pedido → cria estatuto ACTIVE para o utilizador.
     */
    @Transactional
    public PedidoEstatuto aprovarPedido(Long pedidoId, String emailRevisor, String observacoes) {
        PedidoEstatuto pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

        if (pedido.getEstado() != EstadoPedidoEstatuto.PENDING_APPROVAL
                && pedido.getEstado() != EstadoPedidoEstatuto.UNDER_REVIEW) {
            throw new IllegalStateException("Pedido não está em estado de revisão.");
        }

        pedido.setEstado(EstadoPedidoEstatuto.APPROVED);
        pedido.setRevisorEmail(emailRevisor);
        pedido.setObservacoesRevisor(observacoes);
        pedidoRepo.save(pedido);

        Integer validade = resolverValidadeDias(pedido.getTipoEstatuto());
        estatutoService.ativarEstatuto(pedido.getCliente(), pedido.getTipoEstatuto(), pedido, validade);

        return pedido;
    }

    /**
     * Admin: rejeitar pedido.
     */
    public PedidoEstatuto rejeitarPedido(Long pedidoId, String emailRevisor, String observacoes) {
        PedidoEstatuto pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

        if (pedido.getEstado() != EstadoPedidoEstatuto.PENDING_APPROVAL
                && pedido.getEstado() != EstadoPedidoEstatuto.UNDER_REVIEW) {
            throw new IllegalStateException("Pedido não está em estado de revisão.");
        }

        pedido.setEstado(EstadoPedidoEstatuto.REJECTED);
        pedido.setRevisorEmail(emailRevisor);
        pedido.setObservacoesRevisor(observacoes);
        return pedidoRepo.save(pedido);
    }

    /**
     * Admin: pedir correção ao utilizador.
     */
    public PedidoEstatuto pedirCorrecao(Long pedidoId, String emailRevisor, String observacoes) {
        PedidoEstatuto pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

        if (pedido.getEstado() != EstadoPedidoEstatuto.PENDING_APPROVAL
                && pedido.getEstado() != EstadoPedidoEstatuto.UNDER_REVIEW) {
            throw new IllegalStateException("Pedido não está em estado de revisão.");
        }

        pedido.setEstado(EstadoPedidoEstatuto.CORRECTION_REQUESTED);
        pedido.setRevisorEmail(emailRevisor);
        pedido.setObservacoesRevisor(observacoes);
        return pedidoRepo.save(pedido);
    }

    /**
     * Utilizador: cancelar pedido.
     */
    public PedidoEstatuto cancelarPedido(Long pedidoId, Cliente cliente) {
        PedidoEstatuto pedido = obterPedidoDoCliente(pedidoId, cliente);

        if (pedido.getEstado() == EstadoPedidoEstatuto.APPROVED
                || pedido.getEstado() == EstadoPedidoEstatuto.CANCELLED) {
            throw new IllegalStateException("Não é possível cancelar este pedido.");
        }

        pedido.setEstado(EstadoPedidoEstatuto.CANCELLED);
        return pedidoRepo.save(pedido);
    }

    /**
     * Lista pedidos do cliente.
     */
    public List<PedidoEstatuto> listarPedidosDoCliente(Cliente cliente) {
        return pedidoRepo.findByClienteOrderByCriadoEmDesc(cliente);
    }

    /**
     * Lista pedidos pendentes de revisão (para admin).
     */
    public List<PedidoEstatuto> listarPedidosPendentes() {
        return pedidoRepo.findByEstadoInOrderByCriadoEmAsc(
                List.of(EstadoPedidoEstatuto.PENDING_APPROVAL, EstadoPedidoEstatuto.UNDER_REVIEW));
    }

    /**
     * Obtém pedido por ID.
     */
    public Optional<PedidoEstatuto> obterPedidoPorId(Long id) {
        return pedidoRepo.findById(id);
    }

    // ── Helpers privados ─────────────────────────────────────────────────────

    private PedidoEstatuto obterPedidoDoCliente(Long pedidoId, Cliente cliente) {
        PedidoEstatuto pedido = pedidoRepo.findById(pedidoId)
                .orElseThrow(() -> new IllegalArgumentException("Pedido não encontrado."));

        if (!pedido.getCliente().getId().equals(cliente.getId())) {
            throw new IllegalArgumentException("Este pedido não pertence ao utilizador.");
        }
        return pedido;
    }

    private Integer resolverValidadeDias(TipoEstatuto tipo) {
        return switch (tipo) {
            case ESTUDANTE -> VALIDADE_ESTUDANTE;
            case RESIDENTE -> VALIDADE_RESIDENTE;
            case MILITAR -> VALIDADE_MILITAR;
            default -> null;
        };
    }
}
