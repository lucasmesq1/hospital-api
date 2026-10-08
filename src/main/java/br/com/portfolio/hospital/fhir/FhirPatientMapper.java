package br.com.portfolio.hospital.fhir;

import br.com.portfolio.hospital.paciente.Paciente;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Converte nosso Paciente para o recurso "Patient" do HL7 FHIR R4, padrão usado
 * pela RNDS (Rede Nacional de Dados em Saúde) do Ministério da Saúde.
 *
 * Os endereços (system) dos identificadores seguem o BR Core da RNDS.
 * Confira a versão vigente em https://simplifier.net/redenacionaldedadosemsaude
 * antes de integrar com a RNDS de verdade.
 */
public final class FhirPatientMapper {

    static final String SYSTEM_CPF = "http://rnds.saude.gov.br/fhir/r4/NamingSystem/cpf";
    static final String SYSTEM_CNS = "http://rnds.saude.gov.br/fhir/r4/NamingSystem/cns";

    private FhirPatientMapper() {
    }

    public static Map<String, Object> paraFhir(Paciente p) {
        Map<String, Object> patient = new LinkedHashMap<>();
        patient.put("resourceType", "Patient");
        patient.put("id", String.valueOf(p.getId()));

        List<Map<String, Object>> identifiers = new ArrayList<>();
        if (p.getCpf() != null) {
            identifiers.add(identifier(SYSTEM_CPF, p.getCpf()));
        }
        if (p.getCns() != null) {
            identifiers.add(identifier(SYSTEM_CNS, p.getCns()));
        }
        patient.put("identifier", identifiers);

        patient.put("name", List.of(Map.of("use", "official", "text", p.getNome())));
        patient.put("birthDate", p.getDataNascimento().toString()); // AAAA-MM-DD, como o FHIR exige
        return patient;
    }

    private static Map<String, Object> identifier(String system, String value) {
        Map<String, Object> id = new LinkedHashMap<>();
        id.put("system", system);
        id.put("value", value);
        return id;
    }
}
