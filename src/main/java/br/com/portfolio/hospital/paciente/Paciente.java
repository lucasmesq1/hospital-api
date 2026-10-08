package br.com.portfolio.hospital.paciente;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "paciente")
public class Paciente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nome;

    @Column(length = 11)
    private String cpf;

    @Column(length = 15)
    private String cns;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm;

    protected Paciente() {
        // exigido pelo JPA
    }

    public Paciente(String nome, String cpf, String cns, LocalDate dataNascimento, Instant criadoEm) {
        this.nome = nome;
        this.cpf = cpf;
        this.cns = cns;
        this.dataNascimento = dataNascimento;
        this.criadoEm = criadoEm;
    }

    public void atualizarDados(String nome, String cpf, String cns, LocalDate dataNascimento) {
        this.nome = nome;
        this.cpf = cpf;
        this.cns = cns;
        this.dataNascimento = dataNascimento;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getCns() { return cns; }
    public LocalDate getDataNascimento() { return dataNascimento; }
    public Instant getCriadoEm() { return criadoEm; }
}
