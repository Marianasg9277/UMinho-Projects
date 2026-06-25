package com.example.loginapi.service.autenticacao;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.clientes.Utilizador.Role;
import com.example.loginapi.repository.clientes.ClienteRepository;
import com.example.loginapi.repository.clientes.UtilizadorRepository;
import com.example.loginapi.model.titulos.Passe;


@Service
public class AuthService {

    @Autowired
    private UtilizadorRepository utilizadorRepo;

    @Autowired
    private ClienteRepository clienteRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Cliente getClienteByEmail(String email) {
        return utilizadorRepo.findByEmail(email)
                .flatMap(clienteRepo::findByUtilizador)
                .orElse(null);
    }

    public Cliente register(String email, String password, String confirmPassword, String nome, String sobrenome,
        String dataNascimento, String morada, String nif, String telefone,
        String numeroCartaoCidadao) {

    Optional<Utilizador> existingUser = utilizadorRepo.findByEmail(email);
    if (existingUser.isPresent()) {
        throw new IllegalArgumentException("Este email já está a ser utilizado.");
    }

    if (email == null || email.trim().isEmpty() ||
        password == null || password.trim().isEmpty() ||
        confirmPassword == null || confirmPassword.trim().isEmpty() ||
        nome == null || nome.trim().isEmpty() ||
        sobrenome == null || sobrenome.trim().isEmpty() ||
        dataNascimento == null || dataNascimento.trim().isEmpty() ||
        morada == null || morada.trim().isEmpty() ||
        nif == null || nif.trim().isEmpty() ||
        telefone == null || telefone.trim().isEmpty() ||
        numeroCartaoCidadao == null || numeroCartaoCidadao.trim().isEmpty()) {
        throw new IllegalArgumentException("Preenche todos os campos obrigatórios.");
    }

    if (!password.equals(confirmPassword)) {
        throw new IllegalArgumentException("As palavras-passe não coincidem.");
    }

    if (password.length() < 6) {
        throw new IllegalArgumentException("A palavra-passe deve ter pelo menos 6 caracteres.");
    }

    String nifLimpo = nif.replaceAll("\\D", "");
    if (!nifLimpo.matches("\\d{9}")) {
        throw new IllegalArgumentException("O NIF deve ter exatamente 9 dígitos.");
    }

    if (clienteRepo.findByNif(nifLimpo).isPresent()) {
        throw new IllegalArgumentException("Este NIF já está registado.");
    }

    String telefoneLimpo = telefone.replaceAll("\\D", "");
    if (!telefoneLimpo.matches("\\d{9}")) {
        throw new IllegalArgumentException("O número de telefone deve ter exatamente 9 dígitos.");
    }

    String ccLimpo = numeroCartaoCidadao.trim().toUpperCase().replaceAll("\\s+", "");
    if (!ccLimpo.matches("\\d{8}")) {
        throw new IllegalArgumentException("O número do Cartão de Cidadão deve ter exatamente 8 números.");
    }

    if (clienteRepo.findByNumeroCartaoCidadao(ccLimpo).isPresent()) {
        throw new IllegalArgumentException("Este número de Cartão de Cidadão já está registado.");
    }

    Utilizador novoUtilizador = new Utilizador();
    novoUtilizador.setEmail(email.trim().toLowerCase());
    novoUtilizador.setPassword(passwordEncoder.encode(password));
    novoUtilizador.setRole(Role.CLIENTE);

    Utilizador utilizadorGuardado = utilizadorRepo.save(novoUtilizador);

    Cliente novoCliente = new Cliente();
    novoCliente.setPerfil("NORMAL");
    novoCliente.setNome(nome.trim());
    novoCliente.setSobrenome(sobrenome.trim());
    novoCliente.setDataNascimento(LocalDate.parse(dataNascimento));
    novoCliente.setMorada(morada.trim());
    novoCliente.setNif(nifLimpo);
    novoCliente.setTelefone(telefoneLimpo);
    novoCliente.setNumeroCartaoCidadao(ccLimpo);
    novoCliente.setDigitoVerificacaoCartaoCidadao("-");
    novoCliente.setUtilizador(utilizadorGuardado);

    return clienteRepo.save(novoCliente);
}
}