package com.example.loginapi.service.clientes;

import com.example.loginapi.dto.AtualizarDadosClienteRequest;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.repository.clientes.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import com.example.loginapi.service.autenticacao.AuthService;


@Service
public class ClienteService {

    @Autowired private AuthService authService;
    @Autowired private ClienteRepository clienteRepo;

    public Cliente getClienteByEmail(String email) {
        return authService.getClienteByEmail(email);
    }

    @Transactional
    public Cliente atualizarDadosCliente(String email, AtualizarDadosClienteRequest dados) {
        Cliente cliente = authService.getClienteByEmail(email);
        if (cliente == null) {
            throw new NoSuchElementException("Cliente nao encontrado.");
        }

        cliente.setNome(dados.getNome().trim());
        cliente.setSobrenome(dados.getSobrenome().trim());
        cliente.setMorada(dados.getMorada().trim());
        cliente.setTelefone(dados.getTelefone().trim());

        return clienteRepo.save(cliente);
    }
}
