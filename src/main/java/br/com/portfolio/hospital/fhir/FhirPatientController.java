package br.com.portfolio.hospital.fhir;

import br.com.portfolio.hospital.auditoria.AuditoriaService;
import br.com.portfolio.hospital.paciente.PacienteService;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Exporta o paciente em formato FHIR, para interoperabilidade com outros sistemas de saúde. */
@RestController
@RequestMapping(value = "/fhir", produces = "application/fhir+json")
public class FhirPatientController {

    private final PacienteService pacienteService;
    private final AuditoriaService auditoria;

    public FhirPatientController(PacienteService pacienteService, AuditoriaService auditoria) {
        this.pacienteService = pacienteService;
        this.auditoria = auditoria;
    }

    @GetMapping("/Patient/{id}")
    public Map<String, Object> patient(@PathVariable Long id) {
        var paciente = pacienteService.obter(id);
        auditoria.registrar("CONSULTA_FHIR", "paciente", id);
        return FhirPatientMapper.paraFhir(paciente);
    }
}
