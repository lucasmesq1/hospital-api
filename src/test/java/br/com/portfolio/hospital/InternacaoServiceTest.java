package br.com.portfolio.hospital;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.portfolio.hospital.auditoria.AuditoriaService;
import br.com.portfolio.hospital.internacao.Internacao;
import br.com.portfolio.hospital.internacao.InternacaoRepository;
import br.com.portfolio.hospital.internacao.InternacaoRequest;
import br.com.portfolio.hospital.internacao.InternacaoResponse;
import br.com.portfolio.hospital.internacao.InternacaoService;
import br.com.portfolio.hospital.paciente.Paciente;
import br.com.portfolio.hospital.paciente.PacienteService;
import br.com.portfolio.hospital.shared.exception.ConflitoException;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

/** Teste unitário das regras de internação, sem banco e sem Spring (rápido). */
class InternacaoServiceTest {

    private static final Instant AGORA = Instant.parse("2026-10-08T12:00:00Z");

    private InternacaoRepository repository;
    private InternacaoService service;
    private Paciente paciente;

    @BeforeEach
    void preparar() {
        repository = mock(InternacaoRepository.class);
        PacienteService pacienteService = mock(PacienteService.class);
        AuditoriaService auditoria = mock(AuditoriaService.class);
        Clock clock = Clock.fixed(AGORA, ZoneOffset.UTC);
        service = new InternacaoService(repository, pacienteService, auditoria, clock);

        paciente = new Paciente("Maria Fictícia", "52998224725", null, LocalDate.of(1980, 5, 10), AGORA);
        ReflectionTestUtils.setField(paciente, "id", 1L);
        when(pacienteService.obter(1L)).thenReturn(paciente);
        when(repository.saveAndFlush(any(Internacao.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void internaPacienteEmLeitoLivre() {
        InternacaoResponse resposta = service.internar(new InternacaoRequest(1L, "uti-01"));

        assertThat(resposta.leito()).isEqualTo("UTI-01"); // normaliza para maiúsculas
        assertThat(resposta.status()).isEqualTo("INTERNADO");
        assertThat(resposta.dataEntrada()).isEqualTo(AGORA);
    }

    @Test
    void naoInternaPacienteJaInternado() {
        when(repository.existsByPacienteIdAndDataAltaIsNull(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.internar(new InternacaoRequest(1L, "UTI-01")))
            .isInstanceOf(ConflitoException.class)
            .hasMessageContaining("internação em aberto");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void naoInternaEmLeitoOcupado() {
        when(repository.existsByLeitoAndDataAltaIsNull("UTI-01")).thenReturn(true);

        assertThatThrownBy(() -> service.internar(new InternacaoRequest(1L, "uti-01")))
            .isInstanceOf(ConflitoException.class)
            .hasMessageContaining("ocupado");
        verify(repository, never()).saveAndFlush(any());
    }

    @Test
    void darAltaRegistraDataEMudaStatus() {
        Internacao internacao = new Internacao(paciente, "UTI-01", AGORA.minusSeconds(3600));
        when(repository.findById(10L)).thenReturn(Optional.of(internacao));

        InternacaoResponse resposta = service.darAlta(10L);

        assertThat(resposta.status()).isEqualTo("ALTA");
        assertThat(resposta.dataAlta()).isEqualTo(AGORA);
    }

    @Test
    void naoDaAltaDuasVezes() {
        Internacao internacao = new Internacao(paciente, "UTI-01", AGORA.minusSeconds(3600));
        internacao.registrarAlta(AGORA.minusSeconds(60));
        when(repository.findById(10L)).thenReturn(Optional.of(internacao));

        assertThatThrownBy(() -> service.darAlta(10L))
            .isInstanceOf(ConflitoException.class)
            .hasMessageContaining("já recebeu alta");
    }
}
