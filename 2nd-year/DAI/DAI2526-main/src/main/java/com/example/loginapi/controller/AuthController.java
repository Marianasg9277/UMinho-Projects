package com.example.loginapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.example.loginapi.dto.LoginRequest;
import com.example.loginapi.dto.LoginResponse;
import com.example.loginapi.dto.RegisterRequest;
import com.example.loginapi.model.Cliente;
import com.example.loginapi.service.AuthService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {

        Cliente cliente = authService.login(request.getEmail(), request.getPassword());

        if (cliente == null) {
            return new LoginResponse(false, null, null, null, "Credenciais erradas");
        }

        return new LoginResponse(
                true,
                cliente.getPerfil(),
                cliente.getNomeCompleto(),
                cliente.getDataNascimento().toString(),
                "Login com sucesso"
        );
    }

    @PostMapping("/register")
    public LoginResponse register(@RequestBody RegisterRequest request) {

        Cliente cliente = authService.register(
                request.getEmail(),
                request.getPassword(),
                request.getNomeCompleto(),
                request.getDataNascimento(),
                request.getPerfil()
        );

        if (cliente == null) {
            return new LoginResponse(false, null, null, null, "Erro ao criar conta ou email já existe");
        }

        return new LoginResponse(
                true,
                cliente.getPerfil(),
                cliente.getNomeCompleto(),
                cliente.getDataNascimento().toString(),
                "Conta criada com sucesso"
        );
    }
}
