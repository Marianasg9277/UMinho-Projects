package com.example.loginapi.service.colaboradores;

import com.example.loginapi.dto.ColaboradorRequest;
import com.example.loginapi.dto.ColaboradorResponse;
import com.example.loginapi.model.colaboradores.Colaborador;
import com.example.loginapi.model.clientes.Utilizador;
import com.example.loginapi.model.colaboradores.enums.TipoColaborador;
import com.example.loginapi.repository.colaboradores.ColaboradorRepository;
import com.example.loginapi.repository.clientes.UtilizadorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import com.example.loginapi.model.clientes.Cliente;


@Service
public class ColaboradorService {

    @Autowired
    private ColaboradorRepository colaboradorRepo;

    @Autowired
    private UtilizadorRepository utilizadorRepo;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    // ── UC4.3.1 Criar ─────────────────────────────────────────────────────────

    @Transactional
    public ColaboradorResponse criar(ColaboradorRequest dto) {
        String email = normalizeEmail(dto.getEmail());

        // Verificar email duplicado na tabela de colaboradores
        if (colaboradorRepo.existsByEmail(email)) {
            throw new IllegalStateException("Já existe um colaborador com o email: " + email);
        }

        // Verificar email duplicado na tabela de utilizadores (colisão com CLIENTE ou outro colaborador)
        if (utilizadorRepo.findByEmail(email).isPresent()) {
            throw new IllegalStateException("Já existe um utilizador registado com o email: " + email);
        }

        TipoColaborador tipo = parseTipo(dto.getTipoColaborador());

        String nif = normalizeNif(dto.getNif());
        if (nif != null) {
            validarNif(nif);
            colaboradorRepo.findByNif(nif).ifPresent(c -> {
                throw new IllegalStateException("Já existe um colaborador com o NIF: " + nif);
            });
        }

        String numeroCarta = null;
        if (tipo == TipoColaborador.MOTORISTA) {
            if (dto.getNumeroCarta() == null || dto.getNumeroCarta().isBlank()) {
                throw new IllegalArgumentException("numeroCarta é obrigatório para colaboradores do tipo MOTORISTA.");
            }
            numeroCarta = dto.getNumeroCarta().trim();
        }

        // Determinar password: usar a fornecida ou gerar temporária
        boolean passwordFornecida = dto.getPassword() != null && !dto.getPassword().isBlank();
        String passwordPlain = passwordFornecida ? dto.getPassword() : gerarPasswordTemporaria();
        String passwordHash = passwordEncoder.encode(passwordPlain);

        // Criar Utilizador com role derivada do tipo de colaborador
        Utilizador utilizador = new Utilizador();
        utilizador.setEmail(email);
        utilizador.setPassword(passwordHash);
        utilizador.setRole(resolveRole(tipo));
        utilizadorRepo.save(utilizador);

        // Criar Colaborador (se este save falhar, o @Transactional faz rollback do Utilizador)
        Colaborador c = new Colaborador();
        c.setNome(dto.getNome().trim());
        c.setEmail(email);
        c.setMorada(dto.getMorada() != null && !dto.getMorada().isBlank() ? dto.getMorada().trim() : null);
        c.setDataNascimento(parseData(dto.getDataNascimento()));
        c.setNif(nif);
        c.setTipoColaborador(tipo);
        c.setNumeroCarta(numeroCarta);
        c.setAtivo(true);

        ColaboradorResponse response = ColaboradorResponse.from(colaboradorRepo.save(c));

        // Devolver password temporária apenas quando foi gerada automaticamente
        if (!passwordFornecida) {
            response.setPasswordTemporaria(passwordPlain);
        }

        return response;
    }

    // ── UC4.3.2 Consultar ─────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<ColaboradorResponse> listar(String tipoParam, Boolean ativo) {
        List<Colaborador> resultados;

        if (tipoParam != null && !tipoParam.isBlank()) {
            TipoColaborador tipo = parseTipo(tipoParam);
            resultados = (ativo != null)
                    ? colaboradorRepo.findAllByTipoColaboradorAndAtivo(tipo, ativo)
                    : colaboradorRepo.findAllByTipoColaborador(tipo);
        } else {
            resultados = (ativo != null)
                    ? colaboradorRepo.findAllByAtivo(ativo)
                    : colaboradorRepo.findAll();
        }

        return resultados.stream().map(ColaboradorResponse::from).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ColaboradorResponse consultar(Long id) {
        Colaborador c = colaboradorRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Colaborador não encontrado: id=" + id));
        return ColaboradorResponse.from(c);
    }

    // ── UC4.3.5 Remover (lógico) ──────────────────────────────────────────────

    public void desativar(Long id) {
        Colaborador c = colaboradorRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Colaborador não encontrado: id=" + id));
        if (Boolean.TRUE.equals(c.getAtivo())) {
            c.setAtivo(false);
            colaboradorRepo.save(c);
        }
        // já inativo → sucesso idempotente, sem erro
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private String normalizeEmail(String raw) {
        return raw == null ? null : raw.trim().toLowerCase();
    }

    private String normalizeNif(String raw) {
        if (raw == null || raw.isBlank()) return null;
        return raw.trim();
    }

    private void validarNif(String nif) {
        if (!nif.matches("\\d{9}")) {
            throw new IllegalArgumentException("NIF inválido: deve ter exatamente 9 dígitos numéricos.");
        }
    }

    private TipoColaborador parseTipo(String raw) {
        try {
            return TipoColaborador.valueOf(raw.trim().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException(
                    "tipoColaborador inválido: '" + raw + "'. Use MOTORISTA, FISCALIZADOR ou GESTOR_SERVICOS.");
        }
    }

    private LocalDate parseData(String raw) {
        if (raw == null || raw.isBlank()) return null;
        try {
            return LocalDate.parse(raw.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("dataNascimento inválida: use o formato yyyy-MM-dd.");
        }
    }

    private Utilizador.Role resolveRole(TipoColaborador tipo) {
        switch (tipo) {
            case MOTORISTA:       return Utilizador.Role.MOTORISTA;
            case FISCALIZADOR:    return Utilizador.Role.FISCALIZADOR;
            case GESTOR_SERVICOS: return Utilizador.Role.GESTOR_SERVICOS;
            default: throw new IllegalArgumentException("Sem role mapeada para o tipo: " + tipo);
        }
    }

    private String gerarPasswordTemporaria() {
        // Exclui caracteres ambíguos (0/O, 1/l/I) para facilitar leitura pelo admin
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
