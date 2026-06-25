package com.example.loginapi.service;

import com.example.loginapi.service.autenticacao.AuthService;

import com.example.loginapi.service.clientes.ClienteService;

import com.example.loginapi.dto.AtualizarDadosClienteRequest;
import com.example.loginapi.model.clientes.Cliente;
import com.example.loginapi.repository.clientes.ClienteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock private AuthService authService;
    @Mock private ClienteRepository clienteRepo;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    @DisplayName("Atualizar dados altera apenas campos editaveis")
    void atualizarDadosClienteAlteraApenasCamposEditaveis() {
        Cliente cliente = new Cliente();
        cliente.setPerfil("NORMAL");
        cliente.setNome("Joao");
        cliente.setSobrenome("Silva");
        cliente.setDataNascimento(LocalDate.of(1995, 5, 12));
        cliente.setMorada("Morada antiga");
        cliente.setNif("123456789");
        cliente.setTelefone("912345678");
        cliente.setNumeroCartaoCidadao("12345678");
        cliente.setDigitoVerificacaoCartaoCidadao("-");

        AtualizarDadosClienteRequest request = new AtualizarDadosClienteRequest();
        request.setNome(" Maria ");
        request.setSobrenome(" Costa ");
        request.setMorada(" Rua Nova 123 ");
        request.setTelefone("923456789");

        when(authService.getClienteByEmail("cliente@tub.pt")).thenReturn(cliente);
        when(clienteRepo.save(any(Cliente.class))).thenAnswer(inv -> inv.getArgument(0));

        Cliente atualizado = clienteService.atualizarDadosCliente("cliente@tub.pt", request);

        assertEquals("Maria", atualizado.getNome());
        assertEquals("Costa", atualizado.getSobrenome());
        assertEquals("Rua Nova 123", atualizado.getMorada());
        assertEquals("923456789", atualizado.getTelefone());
        assertEquals("NORMAL", atualizado.getPerfil());
        assertEquals(LocalDate.of(1995, 5, 12), atualizado.getDataNascimento());
        assertEquals("123456789", atualizado.getNif());
        assertEquals("12345678", atualizado.getNumeroCartaoCidadao());
        verify(clienteRepo).save(cliente);
    }

    @Test
    @DisplayName("Atualizar dados falha quando cliente autenticado nao existe")
    void atualizarDadosClienteFalhaSemCliente() {
        when(authService.getClienteByEmail("sem-cliente@tub.pt")).thenReturn(null);

        assertThrows(
                NoSuchElementException.class,
                () -> clienteService.atualizarDadosCliente("sem-cliente@tub.pt", new AtualizarDadosClienteRequest())
        );
        verify(clienteRepo, never()).save(any());
    }
}
