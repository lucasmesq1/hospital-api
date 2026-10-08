package br.com.portfolio.hospital.auditoria;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditoriaRepository extends JpaRepository<AuditoriaAcesso, Long> {
}
