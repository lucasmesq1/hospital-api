package br.com.portfolio.hospital.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CpfValidator implements ConstraintValidator<Cpf, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        return valor == null || isValido(valor); // nulo é tratado por @NotNull, se necessário
    }

    public static boolean isValido(String cpf) {
        if (cpf == null || !cpf.matches("\\d{11}")) {
            return false;
        }
        // "11111111111", "22222222222"... passam na conta, mas não são CPFs válidos
        if (cpf.chars().distinct().count() == 1) {
            return false;
        }
        return digitoVerificador(cpf, 9) == digito(cpf, 9)
            && digitoVerificador(cpf, 10) == digito(cpf, 10);
    }

    /** Calcula o dígito verificador usando os primeiros "tamanho" dígitos. */
    private static int digitoVerificador(String cpf, int tamanho) {
        int soma = 0;
        for (int i = 0; i < tamanho; i++) {
            soma += digito(cpf, i) * (tamanho + 1 - i);
        }
        int resto = (soma * 10) % 11;
        return resto == 10 ? 0 : resto;
    }

    private static int digito(String texto, int posicao) {
        return texto.charAt(posicao) - '0';
    }
}
