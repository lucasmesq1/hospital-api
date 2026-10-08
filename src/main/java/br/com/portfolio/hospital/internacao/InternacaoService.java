package br.com.portfolio.hospital.internacao;

import br.com.portfolio.hospital.auditoria.AuditoriaService;
import br.com.portfolio.hospital.paciente.Paciente;
import br.com.portfolio.hospital.paciente.PacienteService;
import br.com.portfolio.hospital.shared.exception.ConflitoException;
import br.com.portfolio.hospital.shared.exception.RecursoNaoEncontradoException;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InternacaoService {

    private final InternacaoRepository repository;
    private final PacienteService pacienteService;
    private final AuditoriaService auditoria;
    private final Clock clock;

    public InternacaoService(InternacaoRepository repository, PacienteService pacienteService,
                             AuditoriaService auditoria, Clock clock) {
        this.repository = repository;
        this.pacienteService = pacienteService;
        this.auditoria = auditoria;
        this.clock = clock;
    }

    /**
     * Regras: o paciente precisa existir, não pode ter outra internação em aberto
     * e o leito precisa estar livre.
     */
    @Transactional
    public InternacaoResponse internar(InternacaoRequest request) {
        Paciente paciente = pacienteService.obter(request.pacienteId());
        String leito = request.leito().trim().toUpperCase(Locale.ROOT);

        if (repository.existsByPacienteIdAndDataAltaIsNull(paciente.getId())) {
            throw new ConflitoException("Paciente já possui uma internação em aberto");
        }
        if (repository.existsByLeitoAndDataAltaIsNull(leito)) {
            throw new ConflitoException("Leito " + leito + " já está ocupado");
        }

        try {
            Internacao salva = repository.saveAndFlush(new Internacao(paciente, leito, clock.instant()));
            auditoria.registrar("INTERNACAO", "internacao", salva.getId());
            return InternacaoResponse.de(salva);
        } catch (DataIntegrityViolationException e) {
            // Duas requisições simultâneas passaram pelas checagens acima;
            // os índices únicos parciais do PostgreSQL barram a segunda.
            throw new ConflitoException("Paciente já internado ou leito ocupado");
        }
    }

    @Transactional
    public InternacaoResponse darAlta(Long id) {
        Internacao internacao = repository.findById(id)
            .orElseThrow(() -> new RecursoNaoEncontradoException("Internação", id));
        if (!internacao.emAberto()) {
            throw new ConflitoException("Esta internação já recebeu alta");
        }
        internacao.registrarAlta(clock.instant());
        auditoria.registrar("ALTA", "internacao", id);
        return InternacaoResponse.de(internacao);
    }

    @Transactional(readOnly = true)
    public List<InternacaoResponse> listarEmAberto() {
        return repository.findEmAberto().stream().map(InternacaoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public List<InternacaoResponse> listarPorPaciente(Long pacienteId) {
        pacienteService.obter(pacienteId); // garante 404 se o paciente não existir
        return repository.findByPaciente(pacienteId).stream().map(InternacaoResponse::de).toList();
    }
}
