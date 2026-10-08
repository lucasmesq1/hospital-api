package br.com.portfolio.hospital.internacao;

import java.time.Instant;

public record InternacaoResponse(
        Long id,
        Long pacienteId,
        String pacienteNome,
        String leito,
        Instant dataEntrada,
        Instant dataAlta,
        String status) {

    public static InternacaoResponse de(Internacao i) {
        return new InternacaoResponse(
            i.getId(),
            i.getPaciente().getId(),
            i.getPaciente().getNome(),
            i.getLeito(),
            i.getDataEntrada(),
            i.getDataAlta(),
            i.emAberto() ? "INTERNADO" : "ALTA");
    }
}
