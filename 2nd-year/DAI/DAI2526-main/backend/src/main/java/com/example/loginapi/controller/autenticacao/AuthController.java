package com.example.loginapi.controller.autenticacao;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;
import com.example.loginapi.dto.LoginRequest;
import com.example.loginapi.dto.LoginResponse;
import com.example.loginapi.dto.RegisterRequest;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.comunicacao.Notificacao;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.repository.clientes.UtilizadorRepository;
import com.example.loginapi.service.autenticacao.AuthService;
import com.example.loginapi.service.clientes.AuditLogService;
import com.example.loginapi.service.comunicacao.NotificacaoService;
import com.example.loginapi.model.pagamentos.Conta;


@RestController
@RequestMapping("/api")
@Tag(name = "Autenticação", description = "Endpoints de login, registo e perfil do utilizador autenticado")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private AuthenticationManager authenticationManager;
    @Autowired private AuditLogService auditLogService;
    @Autowired private NotificacaoService notificacaoService;
    @Autowired private UtilizadorRepository utilizadorRepo;

    @PostMapping("/login")
    @Operation(summary = "Iniciar sessão", description = "Autentica o utilizador e cria uma sessão HTTP.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login bem-sucedido"),
        @ApiResponse(responseCode = "401", description = "Credenciais inválidas")
    })
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);

            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

            // Utilizador autenticado com sucesso pelo Spring Security.
            // Verificar se tem perfil Cliente (CLIENTE/ADMIN) ou se é um utilizador
            // operacional sem entidade Cliente (ex: futura role MOTORISTA).
            Cliente cliente = authService.getClienteByEmail(request.getEmail());

            if (cliente == null) {
                // Utilizador autenticado mas sem Cliente associado.
                // Retornar resposta mínima coerente — sem dados pessoais de cliente.
                // Situação esperada para roles operacionais futuras (ex: MOTORISTA).
                Utilizador utilizador = utilizadorRepo.findByEmail(request.getEmail()).orElseThrow();
                String role = utilizador.getRole().name();
                auditLogService.registar(request.getEmail(), role, "LOGIN_SUCCESS", "auth", "Login bem-sucedido (sem perfil cliente)", true, httpRequest);
                return ResponseEntity.ok(new LoginResponse(
                    true, null, null, role, request.getEmail(), "Login com sucesso"
                ));
            }

            String role = cliente.getUtilizador().getRole().name();
            auditLogService.registar(request.getEmail(), role, "LOGIN_SUCCESS", "auth", "Login bem-sucedido", true, httpRequest);

            return ResponseEntity.ok(new LoginResponse(
                true,
                cliente.getId(),
                cliente.getPerfil(),
                role,
                cliente.getNomeCompleto(),
                cliente.getDataNascimento().toString(),
                cliente.getMorada(),
                cliente.getNif(),
                cliente.getTelefone(),
                cliente.getNumeroCartaoCidadao(),
                "Login com sucesso"
            ));
        } catch (BadCredentialsException e) {
            auditLogService.registar(request.getEmail(), null, "LOGIN_FAILED", "auth", "Credenciais inválidas", false, httpRequest);
            return ResponseEntity.status(401).body(new LoginResponse(false, null, null, null, null, "Credenciais erradas"));
        }
    }

    @PostMapping("/register")
    @Operation(summary = "Criar conta de utilizador")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Conta criada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos ou email já existente")
    })
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request, HttpServletRequest httpRequest) {
        try {
            Cliente cliente = authService.register(
                request.getEmail(), request.getPassword(), request.getConfirmPassword(),
                request.getNome(), request.getSobrenome(), request.getDataNascimento(),
                request.getMorada(), request.getNif(), request.getTelefone(), request.getNumeroCartaoCidadao()
            );

            auditLogService.registar(request.getEmail(), "CLIENTE", "REGISTO", "auth", "Nova conta criada", true, httpRequest);
            notificacaoService.criarParaUtilizador(request.getEmail(), "Bem-vindo ao TUB!", "A sua conta foi criada com sucesso. Boas viagens!", Notificacao.Tipo.SUCESSO);

            String role = cliente.getUtilizador().getRole().name();
            return ResponseEntity.ok(new LoginResponse(
                true,
                cliente.getId(),
                cliente.getPerfil(),
                role,
                cliente.getNomeCompleto(),
                cliente.getDataNascimento().toString(),
                cliente.getMorada(),
                cliente.getNif(),
                cliente.getTelefone(),
                cliente.getNumeroCartaoCidadao(),
                "Conta criada com sucesso"
            ));
        } catch (IllegalArgumentException e) {
            auditLogService.registar(request.getEmail(), null, "REGISTO_FALHADO", "auth", e.getMessage(), false, httpRequest);
            return ResponseEntity.badRequest().body(new LoginResponse(false, null, null, null, null, e.getMessage()));
        }
    }

    @GetMapping("/me")
    @Operation(summary = "Dados do utilizador autenticado")
    @ApiResponse(responseCode = "200", description = "Dados do perfil")
    @ApiResponse(responseCode = "401", description = "Não autenticado")
    public ResponseEntity<LoginResponse> me(Authentication authentication) {
        Cliente cliente = authService.getClienteByEmail(authentication.getName());

        if (cliente == null) {
            // Utilizador autenticado mas sem entidade Cliente (ex: role operacional futura).
            // Retornar resposta mínima coerente em vez de 404.
            Utilizador utilizador = utilizadorRepo.findByEmail(authentication.getName()).orElse(null);
            if (utilizador == null) return ResponseEntity.status(401).build();
            return ResponseEntity.ok(new LoginResponse(
                true, null, null, utilizador.getRole().name(), authentication.getName(), "Utilizador autenticado"
            ));
        }

        String role = cliente.getUtilizador().getRole().name();
        return ResponseEntity.ok(new LoginResponse(
            true,
            cliente.getId(),
            cliente.getPerfil(),
            role,
            cliente.getNomeCompleto(),
            cliente.getDataNascimento().toString(),
            cliente.getMorada(),
            cliente.getNif(),
            cliente.getTelefone(),
            cliente.getNumeroCartaoCidadao(),
            "Utilizador autenticado"
        ));
    }
}
