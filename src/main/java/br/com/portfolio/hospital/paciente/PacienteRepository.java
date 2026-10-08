package br.com.portfolio.hospital.paciente;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    boolean existsByCpf(String cpf);

    boolean existsByCpfAndIdNot(String cpf, Long id);

    boolean existsByCns(String cns);

    boolean existsByCnsAndIdNot(String cns, Long id);

    Page<Paciente> findByNomeContainingIgnoreCase(String nome, Pageable pageable);
}
