package com.example.loginapi.service.autenticacao;

import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.repository.clientes.UtilizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Loads the user for Spring Security and maps Utilizador.Role → Spring authority.
 * ROLE_ADMIN or ROLE_CLIENTE based on Utilizador.role field.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UtilizadorRepository utilizadorRepo;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Utilizador utilizador = utilizadorRepo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Utilizador não encontrado: " + email));

        // Map Utilizador.Role to Spring Security GrantedAuthority
        String springRole = "ROLE_" + utilizador.getRole().name(); // ROLE_ADMIN or ROLE_CLIENTE
        return new User(
            utilizador.getEmail(),
            utilizador.getPassword(),
            List.of(new SimpleGrantedAuthority(springRole))
        );
    }
}
