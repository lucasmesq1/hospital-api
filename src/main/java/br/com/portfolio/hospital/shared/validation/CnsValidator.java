package br.com.portfolio.hospital.shared.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Valida o CNS conforme as regras publicadas pelo DATASUS:
 *  - começa com 1 ou 2: CNS "definitivo", derivado do PIS (11 primeiros dígitos);
 *  - começa com 7, 8 ou 9: CNS "provisório", cuja soma ponderada deve ser múltipla de 11.
 */
public class CnsValidator implements ConstraintValidator<Cns, String> {

    @Override
    public boolean isValid(String valor, ConstraintValidatorContext context) {
        return valor == null || isValido(valor);
    }

    public static boolean isValido(String cns) {
        if (cns == null || !cns.matches("\\d{15}")) {
            return false;
        }
        char primeiro = cns.charAt(0);
        if (primeiro == '1' || primeiro == '2') {
            return cns.equals(gerarDefinitivo(cns.substring(0, 11)));
        }
        if (primeiro == '7' || primeiro == '8' || primeiro == '9') {
            return somaPonderada(cns, 15) % 11 == 0;
        }
        return false;
    }

    private static String gerarDefinitivo(String pis) {
        int soma = somaPonderada(pis, 11);
        int dv = 11 - (soma % 11);
        if (dv == 11) {
            dv = 0;
        }
        if (dv == 10) {
            soma += 2;
            dv = 11 - (soma % 11);
            if (dv == 11) {
                dv = 0;
            }
            return pis + "001" + dv;
        }
        return pis + "000" + dv;
    }

    /** Multiplica cada dígito pelo peso (15, 14, 13...) e soma. */
    private static int somaPonderada(String texto, int quantidade) {
        int soma = 0;
        for (int i = 0; i < quantidade; i++) {
            soma += (texto.charAt(i) - '0') * (15 - i);
        }
        return soma;
    }
}
