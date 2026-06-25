package com.example.loginapi.service;

import com.example.loginapi.service.colaboradores.ColaboradorService;

import com.example.loginapi.dto.ColaboradorRequest;
import com.example.loginapi.dto.ColaboradorResponse;
import com.example.loginapi.model.colaboradores.Colaborador;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.colaboradores.enums.TipoColaborador;
import com.example.loginapi.repository.colaboradores.ColaboradorRepository;
import com.example.loginapi.repository.clientes.UtilizadorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ColaboradorService.
 *
 * Cobre:
 * - Criação de Utilizador associado ao colaborador
 * - Mapeamento de role por tipo
 * - Duplicate checks (colaboradores e utilizadores)
 * - Gestão de password (fornecida vs. temporária)
 * - Regressão: desativar colaborador
 */
@ExtendWith(MockitoExtension.class)
class ColaboradorServiceTest {

    @Mock private ColaboradorRepository colaboradorRepo;
    @Mock private UtilizadorRepository utilizadorRepo;
    @Mock private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private ColaboradorService colaboradorService;

    private static final String FAKE_HASH = "$2a$10$fakeHashForTests";

    // ─── Helpers ────────────────────────────────────────────────────────────────

    /** Stubs para o caminho sem erros (sem NIF para evitar stub desnecessário). */
    private void stubHappyPath() {
        when(colaboradorRepo.existsByEmail(anyString())).thenReturn(false);
        when(utilizadorRepo.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(FAKE_HASH);
        when(utilizadorRepo.save(any(Utilizador.class))).thenAnswer(i -> i.getArgument(0));
        when(colaboradorRepo.save(any(Colaborador.class))).thenAnswer(i -> {
            Colaborador c = i.getArgument(0);
            ReflectionTestUtils.setField(c, "id", 1L);
            return c;
        });
    }

    private ColaboradorRequest reqMotorista(String password) {
        ColaboradorRequest req = new ColaboradorRequest();
        req.setNome("Ana Silva");
        req.setEmail("ana@tub.pt");
        req.setTipoColaborador("MOTORISTA");
        req.setNumeroCarta("M123456");
        req.setPassword(password);
        return req;
    }

    private ColaboradorRequest reqFiscalizador() {
        ColaboradorRequest req = new ColaboradorRequest();
        req.setNome("Carlos Mota");
        req.setEmail("carlos@tub.pt");
        req.setTipoColaborador("FISCALIZADOR");
        return req;
    }

    private ColaboradorRequest reqGestorServicos() {
        ColaboradorRequest req = new ColaboradorRequest();
        req.setNome("Maria Faria");
        req.setEmail("maria@tub.pt");
        req.setTipoColaborador("GESTOR_SERVICOS");
        return req;
    }

    // ─── 1. Criar colaborador cria Utilizador ───────────────────────────────────

    @Test
    @DisplayName("Criar colaborador deve criar também um Utilizador com o mesmo email")
    void testCriarColaboradorCriaUtilizador() {
        stubHappyPath();

        colaboradorService.criar(reqMotorista(null));

        ArgumentCaptor<Utilizador> captor = ArgumentCaptor.forClass(Utilizador.class);
        verify(utilizadorRepo).save(captor.capture());
        assertEquals("ana@tub.pt", captor.getValue().getEmail());
        assertNotNull(captor.getValue().getPassword());
        assertNotNull(captor.getValue().getRole());
    }

    // ─── 2-4. Mapeamento de role por tipo ──────────────────────────────────────

    @Test
    @DisplayName("MOTORISTA cria Utilizador com role MOTORISTA")
    void testMotoristaRoleMotorista() {
        stubHappyPath();

        colaboradorService.criar(reqMotorista(null));

        ArgumentCaptor<Utilizador> captor = ArgumentCaptor.forClass(Utilizador.class);
        verify(utilizadorRepo).save(captor.capture());
        assertEquals(Utilizador.Role.MOTORISTA, captor.getValue().getRole());
    }

    @Test
    @DisplayName("FISCALIZADOR cria Utilizador com role FISCALIZADOR")
    void testFiscalizadorRoleFiscalizador() {
        stubHappyPath();

        colaboradorService.criar(reqFiscalizador());

        ArgumentCaptor<Utilizador> captor = ArgumentCaptor.forClass(Utilizador.class);
        verify(utilizadorRepo).save(captor.capture());
        assertEquals(Utilizador.Role.FISCALIZADOR, captor.getValue().getRole());
    }

    @Test
    @DisplayName("GESTOR_SERVICOS cria Utilizador com role GESTOR_SERVICOS")
    void testGestorServicosRoleGestorServicos() {
        stubHappyPath();

        colaboradorService.criar(reqGestorServicos());

        ArgumentCaptor<Utilizador> captor = ArgumentCaptor.forClass(Utilizador.class);
        verify(utilizadorRepo).save(captor.capture());
        assertEquals(Utilizador.Role.GESTOR_SERVICOS, captor.getValue().getRole());
    }

    // ─── 5. Email duplicado em colaboradores ───────────────────────────────────

    @Test
    @DisplayName("Email já existente em colaboradores deve ser rejeitado com 409")
    void testEmailDuplicadoEmColaboradoresRejeitado() {
        when(colaboradorRepo.existsByEmail(anyString())).thenReturn(true);

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> colaboradorService.criar(reqMotorista(null)));

        assertTrue(ex.getMessage().contains("colaborador"));
        verify(utilizadorRepo, never()).save(any());
        verify(colaboradorRepo, never()).save(any());
    }

    // ─── 6. Email duplicado em utilizadores ────────────────────────────────────

    @Test
    @DisplayName("Email já existente em utilizadores deve ser rejeitado com 409")
    void testEmailDuplicadoEmUtilizadoresRejeitado() {
        when(colaboradorRepo.existsByEmail(anyString())).thenReturn(false);
        when(utilizadorRepo.findByEmail(anyString())).thenReturn(Optional.of(new Utilizador()));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> colaboradorService.criar(reqMotorista(null)));

        assertTrue(ex.getMessage().contains("utilizador"));
        verify(utilizadorRepo, never()).save(any());
        verify(colaboradorRepo, never()).save(any());
    }

    // ─── 7. Password fornecida é encriptada ────────────────────────────────────

    @Test
    @DisplayName("Password fornecida pelo admin é encriptada com BCrypt e não é devolvida na resposta")
    void testPasswordFornecidaEncriptada() {
        stubHappyPath();

        ColaboradorResponse response = colaboradorService.criar(reqMotorista("MinhaPassword123"));

        verify(passwordEncoder).encode("MinhaPassword123");

        ArgumentCaptor<Utilizador> captor = ArgumentCaptor.forClass(Utilizador.class);
        verify(utilizadorRepo).save(captor.capture());
        assertEquals(FAKE_HASH, captor.getValue().getPassword());

        assertNull(response.getPasswordTemporaria(), "Password fornecida não deve ser devolvida");
    }

    // ─── 8. Password temporária gerada quando não vem password ─────────────────

    @Test
    @DisplayName("Sem password no request: gera password temporária não nula e não vazia")
    void testSemPasswordGeraTemporaria() {
        stubHappyPath();

        ColaboradorResponse response = colaboradorService.criar(reqMotorista(null));

        assertNotNull(response.getPasswordTemporaria());
        assertFalse(response.getPasswordTemporaria().isBlank());
        verify(passwordEncoder).encode(argThat((String p) -> p != null && !p.isBlank()));
    }

    // ─── 9. Password temporária não é igual ao hash guardado ───────────────────

    @Test
    @DisplayName("Password temporária devolvida é diferente do hash guardado na BD")
    void testPasswordTemporariaNaoIgualAoHash() {
        stubHappyPath(); // encode() devolve FAKE_HASH

        ColaboradorResponse response = colaboradorService.criar(reqMotorista(null));

        assertNotNull(response.getPasswordTemporaria());
        assertNotEquals(FAKE_HASH, response.getPasswordTemporaria(),
                "Plaintext não pode ser igual ao hash BCrypt");
    }

    // ─── 10. Regressão: desativar colaborador ──────────────────────────────────

    @Test
    @DisplayName("Regressão: desativar colaborador ativo continua a funcionar sem erro")
    void testDesativarColaboradorSemErro() {
        Colaborador ativo = new Colaborador();
        ativo.setAtivo(true);
        when(colaboradorRepo.findById(1L)).thenReturn(Optional.of(ativo));
        when(colaboradorRepo.save(any(Colaborador.class))).thenAnswer(i -> i.getArgument(0));

        assertDoesNotThrow(() -> colaboradorService.desativar(1L));

        ArgumentCaptor<Colaborador> captor = ArgumentCaptor.forClass(Colaborador.class);
        verify(colaboradorRepo).save(captor.capture());
        assertFalse(captor.getValue().getAtivo(), "Colaborador deve ficar inativo");
    }

    @Test
    @DisplayName("Regressão: desativar colaborador já inativo é idempotente (sem erro, sem save)")
    void testDesativarColaboradorJaInativoIdempotente() {
        Colaborador inativo = new Colaborador();
        inativo.setAtivo(false);
        when(colaboradorRepo.findById(2L)).thenReturn(Optional.of(inativo));

        assertDoesNotThrow(() -> colaboradorService.desativar(2L));

        verify(colaboradorRepo, never()).save(any());
    }

    @Test
    @DisplayName("Regressão: desativar id inexistente lança NoSuchElementException")
    void testDesativarIdInexistente() {
        when(colaboradorRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> colaboradorService.desativar(99L));
    }
}
