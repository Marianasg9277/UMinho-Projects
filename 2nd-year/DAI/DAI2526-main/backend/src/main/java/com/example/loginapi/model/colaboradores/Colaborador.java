package com.example.loginapi.model.colaboradores;

import com.example.loginapi.model.colaboradores.enums.TipoColaborador;
import jakarta.persistence.*;
import java.time.LocalDate;
import com.example.loginapi.model.infraestrutura.Linha;
import com.example.loginapi.model.frota.Autocarro;


@Entity
@Table(name = "colaboradores")
public class Colaborador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nome;

    @Column(nullable = false, unique = true)
    private String email;

    private String morada;

    @Column(name = "data_nascimento")
    private LocalDate dataNascimento;

    @Column(length = 9)
    private String nif;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_colaborador", nullable = false, length = 20)
    private TipoColaborador tipoColaborador;

    @Column(name = "numero_carta")
    private String numeroCarta;

    @Column(nullable = false)
    private Boolean ativo = true;

    /** Autocarro alocado — preenchido para MOTORISTA, null para outros tipos. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "autocarro_id")
    private Autocarro autocarro;

    /** Linha alocada — preenchida para FISCALIZADOR, null para outros tipos. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "linha_atual_id")
    private Linha linhaAtual;

    public Colaborador() {}

    public Long getId() { return id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMorada() { return morada; }
    public void setMorada(String morada) { this.morada = morada; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getNif() { return nif; }
    public void setNif(String nif) { this.nif = nif; }

    public TipoColaborador getTipoColaborador() { return tipoColaborador; }
    public void setTipoColaborador(TipoColaborador tipoColaborador) { this.tipoColaborador = tipoColaborador; }

    public String getNumeroCarta() { return numeroCarta; }
    public void setNumeroCarta(String numeroCarta) { this.numeroCarta = numeroCarta; }

    public Boolean getAtivo() { return ativo; }
    public void setAtivo(Boolean ativo) { this.ativo = ativo; }

    public Autocarro getAutocarro() { return autocarro; }
    public void setAutocarro(Autocarro autocarro) { this.autocarro = autocarro; }

    public Linha getLinhaAtual() { return linhaAtual; }
    public void setLinhaAtual(Linha linhaAtual) { this.linhaAtual = linhaAtual; }
}
