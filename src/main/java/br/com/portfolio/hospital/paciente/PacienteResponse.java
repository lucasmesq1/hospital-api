package br.com.portfolio.hospital.paciente;

import java.time.LocalDate;

public record PacienteResponse(
        Long id,
        String nome,
        String cpf,
        String cns,
        LocalDate dataNascimento) {

    public static PacienteResponse de(Paciente p) {
        return new PacienteResponse(p.getId(), p.getNome(), p.getCpf(), p.getCns(), p.getDataNascimento());
    }
}
