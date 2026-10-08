package br.com.portfolio.hospital.auditoria;

import java.time.Clock;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Registra "quem acessou o quê e quando" (rastreabilidade exigida pela LGPD). */
@Service
public class AuditoriaService {

    private final AuditoriaRepository repository;
    private final Clock clock;

    public AuditoriaService(AuditoriaRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Transactional
    public void registrar(String acao, String recurso, Long recursoId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String usuario = auth != null ? auth.getName() : "anonimo";
        repository.save(new AuditoriaAcesso(usuario, acao, recurso, recursoId, clock.instant()));
    }
}
