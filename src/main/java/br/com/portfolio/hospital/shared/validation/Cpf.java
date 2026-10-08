package br.com.portfolio.hospital.shared.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;

/** CPF com 11 dígitos (somente números) e dígitos verificadores corretos. Aceita nulo. */
@Documented
@Constraint(validatedBy = CpfValidator.class)
@Target({FIELD, PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cpf {
    String message() default "CPF inválido (informe 11 dígitos, sem pontos ou traço)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
