package com.example.loginapi.config;

import com.example.loginapi.service.autenticacao.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.model.titulos.Passe;


@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
            .authenticationProvider(authenticationProvider())
            .authorizeHttpRequests(auth -> auth

                // ── Authentication ──────────────────────────────────────────────
                .requestMatchers(HttpMethod.POST, "/api/login").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/register").permitAll()

                // ── Guest payment ────────────────────────────────────────────────
                .requestMatchers("/api/pagamentos/guest/**").permitAll()

                // ── Guest fatura (consulta por QR s/ autenticação) ────────────────
                .requestMatchers("/api/user/faturas/guest/**").permitAll()

                // ── Public API ──────────────────────────────────────────────────
                .requestMatchers(
                    "/api/linhas",
                    "/api/linhas/*/detalhe",
                    "/api/linhas/*/percurso",
                    "/api/autocarros",
                    "/api/autocarros/posicoes",
                    "/api/simulacao/autocarros/posicao",
                    "/api/avisos",
                    "/api/precario",
                    "/api/tipos-bilhete",
                    "/api/publico/**"
                ).permitAll()

                // ── Mapa – rotas (GET público, POST valida API key no controller) ──
                .requestMatchers(HttpMethod.GET, "/api/mapa/rotas/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/mapa/rotas/importar").permitAll()

                // ── Swagger / OpenAPI (dev tooling) ──────────────────────────────
                .requestMatchers(
                    "/swagger-ui/**",
                    "/swagger-ui.html",
                    "/v3/api-docs/**"
                ).permitAll()

                // ── Static frontend resources ────────────────────────────────────
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/register.html",
                    "/carteira.html",
                    "/perfil.html",
                    "/avisos.html",
                    "/ver_linhas.html",
                    "/ver_precario.html",
                    "/comprar_bilhete.html",
                    "/comprar_passe.html",
                    "/pagamentos.html",
                    "/pedir_estatuto.html",
                    "/notificacoes.html",
                    "/backoffice.html",
                    "/ajuda.html",
                    "/faturas.html",
                    "/historico_viagens.html",
                    "/metodos_pagamento.html",
                    "/carregar_saldo.html",
                    "/mapa_autocarros.html",
                    "/styles.css",
                    "/subpage.css",
                    "/app.js",
                    "/admin.js",
                    "/notificacoes.js",
                    "/i18n.js",
                    "/favicon.png"
                ).permitAll()
                .requestMatchers("/imgs/**").permitAll()

                // ── QR validation — MOTORISTA, FISCALIZADOR e ADMIN ────────────
                // A validação deixou de ser pública: requer autenticação com role
                // operacional. CLIENTE e utilizadores não autenticados recebem 403.
                .requestMatchers("/verificar_qr.html").hasAnyRole("MOTORISTA", "FISCALIZADOR", "ADMIN")
                .requestMatchers("/api/validar/**").hasAnyRole("MOTORISTA", "FISCALIZADOR", "ADMIN")

                // ── Fiscalização — apenas FISCALIZADOR e ADMIN ──────────────
                .requestMatchers("/fiscalizacao.html", "/fiscalizar_qr.html").hasAnyRole("FISCALIZADOR", "ADMIN")
                .requestMatchers("/api/fiscalizacao/**").hasAnyRole("FISCALIZADOR", "ADMIN")

                // ── Bilhetes, passes e área de cliente — apenas CLIENTE e ADMIN ──
                // Nota: .authenticated() foi intencionalmente substituído por hasAnyRole
                // para garantir que futuras roles operacionais (ex: MOTORISTA) não herdem
                // acesso a dados de cliente. Princípio de menor privilégio.
                .requestMatchers("/api/user/**").hasAnyRole("CLIENTE", "ADMIN")
                .requestMatchers("/api/cliente/**").hasAnyRole("CLIENTE", "ADMIN")

                // ── POST /api/comprar — apenas CLIENTE e ADMIN ──────────────────
                // Endpoint legacy de criação de bilhetes; não deve ser acessível
                // a utilizadores operacionais sem perfil de cliente.
                .requestMatchers(HttpMethod.POST, "/api/comprar").hasAnyRole("CLIENTE", "ADMIN")

                // ── Admin — stats partilhados (dashboard) ───────────────────────
                .requestMatchers(HttpMethod.GET, "/api/admin/stats")
                    .hasAnyRole("ADMIN", "GESTOR_SERVICOS", "GESTOR_FROTAS")

                // ── Admin — GESTOR_SERVICOS (serviços) ───────────────────────────
                .requestMatchers("/api/admin/paragens/**")
                    .hasAnyRole("ADMIN", "GESTOR_SERVICOS")
                .requestMatchers("/api/admin/horarios/**")
                    .hasAnyRole("ADMIN", "GESTOR_SERVICOS")
                .requestMatchers("/api/admin/linhas", "/api/admin/linhas/**")
                    .hasAnyRole("ADMIN", "GESTOR_SERVICOS")
                .requestMatchers("/api/admin/colaboradores/**")
                    .hasAnyRole("ADMIN", "GESTOR_SERVICOS")
                .requestMatchers("/api/admin/avisos/**")
                    .hasAnyRole("ADMIN", "GESTOR_SERVICOS")
                .requestMatchers("/api/admin/precario/**")
                    .hasAnyRole("ADMIN", "GESTOR_SERVICOS")

                // ── Admin — endpoints mínimos de alocação (GESTOR_SERVICOS incluído) ──
                // Listagem de autocarros e alocação a linha — necessários para a tab Alocações.
                // Matchers específicos devem vir ANTES da regra geral abaixo (first-match).
                .requestMatchers(HttpMethod.GET, "/api/admin/autocarros")
                    .hasAnyRole("ADMIN", "GESTOR_FROTAS", "GESTOR_SERVICOS")
                .requestMatchers(HttpMethod.PATCH, "/api/admin/autocarros/*/linha")
                    .hasAnyRole("ADMIN", "GESTOR_FROTAS", "GESTOR_SERVICOS")

                // ── Admin — GESTOR_FROTAS (gestão completa de frota) ─────────────
                .requestMatchers("/api/admin/autocarros/**")
                    .hasAnyRole("ADMIN", "GESTOR_FROTAS")

                // ── Admin — GESTOR_SERVICOS pode gerir coroas, tipos de passe e regras de preço ──
                .requestMatchers("/api/admin/coroas/**")
                    .hasAnyRole("ADMIN", "GESTOR_SERVICOS")
                .requestMatchers("/api/admin/tipos-passe/**")
                    .hasAnyRole("ADMIN", "GESTOR_SERVICOS")
                .requestMatchers("/api/admin/pricing/**")
                    .hasAnyRole("ADMIN", "GESTOR_SERVICOS")

                // ── Admin — tudo o resto apenas ADMIN ────────────────────────────
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // ── Everything else requires authentication ───────────────────────
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .logout(logout -> logout
                .logoutUrl("/api/logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\": \"Logout com sucesso\"}");
                })
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Não autenticado\",\"error\":\"UNAUTHORIZED\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\":\"Acesso negado\",\"error\":\"FORBIDDEN\"}");
                })
            );

        return http.build();
    }
}
