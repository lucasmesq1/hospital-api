package br.com.portfolio.hospital;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.portfolio.hospital.shared.validation.CnsValidator;
import br.com.portfolio.hospital.shared.validation.CpfValidator;
import org.junit.jupiter.api.Test;

class DocumentosValidatorTest {

    @Test
    void cpfValidoPassa() {
        assertThat(CpfValidator.isValido("52998224725")).isTrue();
        assertThat(CpfValidator.isValido("11144477735")).isTrue();
    }

    @Test
    void cpfInvalidoFalha() {
        assertThat(CpfValidator.isValido("52998224726")).isFalse();   // dígito errado
        assertThat(CpfValidator.isValido("11111111111")).isFalse();   // todos iguais
        assertThat(CpfValidator.isValido("5299822472")).isFalse();    // 10 dígitos
        assertThat(CpfValidator.isValido("529.982.247-25")).isFalse();// com máscara
        assertThat(CpfValidator.isValido(null)).isFalse();
    }

    @Test
    void cnsDefinitivoValidoPassa() {
        assertThat(CnsValidator.isValido("123456789010000")).isTrue();
        assertThat(CnsValidator.isValido("200000000190007")).isTrue();
    }

    @Test
    void cnsProvisorioValidoPassa() {
        assertThat(CnsValidator.isValido("700000000000005")).isTrue();
    }

    @Test
    void cnsInvalidoFalha() {
        assertThat(CnsValidator.isValido("123456789012345")).isFalse(); // dígito verificador errado
        assertThat(CnsValidator.isValido("700000000000006")).isFalse();
        assertThat(CnsValidator.isValido("300000000000000")).isFalse(); // prefixo inexistente
        assertThat(CnsValidator.isValido("12345")).isFalse();
        assertThat(CnsValidator.isValido(null)).isFalse();
    }
}
