package com.example.loginapi.service;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.loginapi.model.Cliente;
import com.example.loginapi.model.Utilizador;
import com.example.loginapi.repository.ClienteRepository;
import com.example.loginapi.repository.UtilizadorRepository;

@Service
public class AuthService {

    @Autowired
    private UtilizadorRepository utilizadorRepo;

    @Autowired
    private ClienteRepository clienteRepo;

    public Cliente login(String email, String password) {

        Optional<Utilizador> userOpt = utilizadorRepo.findByEmail(email);

        if (userOpt.isEmpty()) {
            return null;
        }

        Utilizador user = userOpt.get();

        if (!user.getPassword().equals(password)) {
            return null;
        }

        return clienteRepo.findByUtilizador(user).orElse(null);
    }

    public Cliente register(String email, String password, String nomeCompleto, String dataNascimento, String perfil) {

        Optional<Utilizador> existingUser = utilizadorRepo.findByEmail(email);

        if (existingUser.isPresent()) {
            return null;
        }

        if (!perfil.equals("ESTUDANTE") && !perfil.equals("MILITAR") && !perfil.equals("NORMAL")) {
            return null;
        }

        Utilizador novoUtilizador = new Utilizador();
        novoUtilizador.setEmail(email);
        novoUtilizador.setPassword(password);

        Utilizador utilizadorGuardado = utilizadorRepo.save(novoUtilizador);

        Cliente novoCliente = new Cliente();
        novoCliente.setPerfil(perfil);
        novoCliente.setNomeCompleto(nomeCompleto);
        novoCliente.setDataNascimento(LocalDate.parse(dataNascimento));
        novoCliente.setUtilizador(utilizadorGuardado);

        return clienteRepo.save(novoCliente);
    }
}