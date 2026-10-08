package br.com.portfolio.hospital.auditoria;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "auditoria_acesso")
public class AuditoriaAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String usuario;

    @Column(nullable = false)
    private String acao;

    @Column(nullable = false)
    private String recurso;

    @Column(name = "recurso_id")
    private Long recursoId;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    protected AuditoriaAcesso() {
        // exigido pelo JPA
    }

    public AuditoriaAcesso(String usuario, String acao, String recurso, Long recursoId, Instant dataHora) {
        this.usuario = usuario;
        this.acao = acao;
        this.recurso = recurso;
        this.recursoId = recursoId;
        this.dataHora = dataHora;
    }

    public Long getId() { return id; }
    public String getUsuario() { return usuario; }
    public String getAcao() { return acao; }
    public String getRecurso() { return recurso; }
    public Long getRecursoId() { return recursoId; }
    public Instant getDataHora() { return dataHora; }
}
