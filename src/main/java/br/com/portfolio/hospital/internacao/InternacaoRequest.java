package br.com.portfolio.hospital.internacao;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record InternacaoRequest(
        @NotNull Long pacienteId,
        @NotBlank @Pattern(regexp = "[A-Za-z0-9\\-]{1,20}", message = "Leito deve ter até 20 letras, números ou hífen")
        String leito) {
}
