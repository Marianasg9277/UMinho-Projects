package com.example.loginapi.service.comunicacao;

import com.example.loginapi.model.comunicacao.Notificacao;
import com.example.loginapi.repository.comunicacao.NotificacaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for in-app notifications.
 * Use criarParaUtilizador() to target a specific user,
 * or criarBroadcast() for admin-visible global notifications.
 */
@Service
public class NotificacaoService {

    @Autowired
    private NotificacaoRepository repo;

    public void criarParaUtilizador(String email, String titulo, String mensagem, Notificacao.Tipo tipo) {
        Notificacao n = new Notificacao();
        n.setUtilizadorDestino(email);
        n.setTitulo(titulo);
        n.setMensagem(mensagem);
        n.setTipo(tipo);
        n.setDataCriacao(LocalDateTime.now());
        repo.save(n);
    }

    /** Visible to admin users (null destination = broadcast). */
    public void criarBroadcast(String titulo, String mensagem, Notificacao.Tipo tipo) {
        Notificacao n = new Notificacao();
        n.setUtilizadorDestino(null);
        n.setTitulo(titulo);
        n.setMensagem(mensagem);
        n.setTipo(tipo);
        n.setDataCriacao(LocalDateTime.now());
        repo.save(n);
    }

    public List<Notificacao> listarParaUtilizador(String email) {
        return repo.findByUtilizadorDestinoOrUtilizadorDestinoIsNullOrderByDataCriacaoDesc(email);
    }

    public long contarNaoLidasParaUtilizador(String email) {
        return repo.countByUtilizadorDestinoAndLidaFalse(email)
             + repo.countByUtilizadorDestinoIsNullAndLidaFalse();
    }

    public boolean marcarComoLida(Long id, String email) {
        Optional<Notificacao> opt = repo.findById(id);
        if (opt.isEmpty()) return false;
        Notificacao n = opt.get();
        // Allow if destino matches OR it is broadcast AND the requester sees it
        if (n.getUtilizadorDestino() != null && !n.getUtilizadorDestino().equals(email)) return false;
        n.setLida(true);
        repo.save(n);
        return true;
    }

    public boolean marcarTodasComoLidas(String email) {
        List<Notificacao> lista = listarParaUtilizador(email);
        lista.forEach(n -> n.setLida(true));
        repo.saveAll(lista);
        return true;
    }
}
