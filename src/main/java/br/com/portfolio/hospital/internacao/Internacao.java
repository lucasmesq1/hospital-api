package br.com.portfolio.hospital.internacao;

import br.com.portfolio.hospital.paciente.Paciente;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "internacao")
public class Internacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @Column(nullable = false, length = 20)
    private String leito;

    @Column(name = "data_entrada", nullable = false)
    private Instant dataEntrada;

    @Column(name = "data_alta")
    private Instant dataAlta;

    protected Internacao() {
        // exigido pelo JPA
    }

    public Internacao(Paciente paciente, String leito, Instant dataEntrada) {
        this.paciente = paciente;
        this.leito = leito;
        this.dataEntrada = dataEntrada;
    }

    public boolean emAberto() {
        return dataAlta == null;
    }

    public void registrarAlta(Instant quando) {
        this.dataAlta = quando;
    }

    public Long getId() { return id; }
    public Paciente getPaciente() { return paciente; }
    public String getLeito() { return leito; }
    public Instant getDataEntrada() { return dataEntrada; }
    public Instant getDataAlta() { return dataAlta; }
}
