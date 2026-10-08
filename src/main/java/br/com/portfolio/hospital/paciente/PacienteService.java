package br.com.portfolio.hospital.paciente;

import br.com.portfolio.hospital.auditoria.AuditoriaService;
import br.com.portfolio.hospital.shared.PaginaResponse;
import br.com.portfolio.hospital.shared.exception.ConflitoException;
import br.com.portfolio.hospital.shared.exception.RecursoNaoEncontradoException;
import br.com.portfolio.hospital.shared.exception.RegraDeNegocioException;
import java.time.Clock;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PacienteService {

    private final PacienteRepository repository;
    private final AuditoriaService auditoria;
    private final Clock clock;

    public PacienteService(PacienteRepository repository, AuditoriaService auditoria, Clock clock) {
        this.repository = repository;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    @Transactional
    public PacienteResponse criar(PacienteRequest r) {
        exigirAlgumDocumento(r);
        if (r.cpf() != null && repository.existsByCpf(r.cpf())) {
            throw new ConflitoException("Já existe paciente cadastrado com este CPF");
        }
        if (r.cns() != null && repository.existsByCns(r.cns())) {
            throw new ConflitoException("Já existe paciente cadastrado com este CNS");
        }
        Paciente paciente = new Paciente(r.nome().trim(), r.cpf(), r.cns(), r.dataNascimento(), clock.instant());
        return PacienteResponse.de(repository.save(paciente));
    }

    @Transactional
    public PacienteResponse buscar(Long id) {
        Paciente paciente = obter(id);
        auditoria.registrar("CONSULTA", "paciente", id);
        return PacienteResponse.de(paciente);
    }

    @Transactional(readOnly = true)
    public PaginaResponse<PacienteResponse> listar(String nome, Pageable pageable) {
        String filtro = nome == null ? "" : nome.trim();
        return PaginaResponse.de(repository.findByNomeContainingIgnoreCase(filtro, pageable), PacienteResponse::de);
    }

    @Transactional
    public PacienteResponse atualizar(Long id, PacienteRequest r) {
        exigirAlgumDocumento(r);
        Paciente paciente = obter(id);
        if (r.cpf() != null && repository.existsByCpfAndIdNot(r.cpf(), id)) {
            throw new ConflitoException("Já existe outro paciente com este CPF");
        }
        if (r.cns() != null && repository.existsByCnsAndIdNot(r.cns(), id)) {
            throw new ConflitoException("Já existe outro paciente com este CNS");
        }
        paciente.atualizarDados(r.nome().trim(), r.cpf(), r.cns(), r.dataNascimento());
        auditoria.registrar("ATUALIZACAO", "paciente", id);
        return PacienteResponse.de(paciente);
    }

    /** Usado por outros serviços (internação, FHIR) para buscar a entidade. */
    @Transactional(readOnly = true)
    public Paciente obter(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Paciente", id));
    }

    private void exigirAlgumDocumento(PacienteRequest r) {
        if (r.cpf() == null && r.cns() == null) {
            throw new RegraDeNegocioException("Informe ao menos o CPF ou o CNS do paciente");
        }
    }
}
