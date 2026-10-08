package br.com.portfolio.hospital.paciente;

import br.com.portfolio.hospital.shared.validation.Cns;
import br.com.portfolio.hospital.shared.validation.Cpf;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/** Dados que o cliente envia para cadastrar/atualizar um paciente. Pelo menos CPF ou CNS é obrigatório. */
public record PacienteRequest(
        @NotBlank @Size(max = 150) String nome,
        @Cpf String cpf,
        @Cns String cns,
        @NotNull @Past LocalDate dataNascimento) {
}
