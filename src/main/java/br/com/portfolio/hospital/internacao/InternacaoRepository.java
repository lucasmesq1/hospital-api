package br.com.portfolio.hospital.internacao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InternacaoRepository extends JpaRepository<Internacao, Long> {

    boolean existsByPacienteIdAndDataAltaIsNull(Long pacienteId);

    boolean existsByLeitoAndDataAltaIsNull(String leito);

    // "join fetch" traz o paciente junto, evitando uma consulta extra por internação (problema N+1)
    @Query("select i from Internacao i join fetch i.paciente where i.dataAlta is null order by i.dataEntrada")
    List<Internacao> findEmAberto();

    @Query("select i from Internacao i join fetch i.paciente where i.paciente.id = :pacienteId order by i.dataEntrada desc")
    List<Internacao> findByPaciente(@Param("pacienteId") Long pacienteId);
}
