package br.com.portfolio.hospital;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;

/**
 * Teste de integração: sobe a aplicação inteira (Spring + segurança + JPA + Flyway)
 * contra um banco H2 em memória e chama a API como um cliente real.
 * Cada teste roda em uma transação desfeita ao final, então um não afeta o outro.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FluxoHospitalarIT {

    private static final RequestPostProcessor RECEPCAO = user("recepcao").roles("RECEPCAO");
    private static final RequestPostProcessor MEDICO = user("medico").roles("MEDICO");

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper json;

    private static String paciente(String nome, String cpf) {
        return """
            {"nome":"%s","cpf":"%s","dataNascimento":"1980-05-10"}
            """.formatted(nome, cpf);
    }

    private long cadastrarPaciente(String nome, String cpf) throws Exception {
        String corpo = mvc.perform(post("/pacientes").with(RECEPCAO)
                .contentType(MediaType.APPLICATION_JSON).content(paciente(nome, cpf)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return json.readTree(corpo).get("id").asLong();
    }

    private long internar(long pacienteId, String leito) throws Exception {
        String corpo = mvc.perform(post("/internacoes").with(RECEPCAO)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"pacienteId\":%d,\"leito\":\"%s\"}".formatted(pacienteId, leito)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        JsonNode no = json.readTree(corpo);
        return no.get("id").asLong();
    }

    @Test
    void semLoginRetorna401() throws Exception {
        mvc.perform(get("/pacientes/1")).andExpect(status().isUnauthorized());
    }

    @Test
    void loginHttpBasicFunciona() throws Exception {
        mvc.perform(get("/pacientes").with(httpBasic("medico", "medico123")))
            .andExpect(status().isOk());
        mvc.perform(get("/pacientes").with(httpBasic("medico", "senha-errada")))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void cadastraEBuscaPaciente() throws Exception {
        long id = cadastrarPaciente("Maria Fictícia", "52998224725");

        mvc.perform(get("/pacientes/" + id).with(MEDICO))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nome").value("Maria Fictícia"))
            .andExpect(jsonPath("$.cpf").value("52998224725"));
    }

    @Test
    void rejeitaCpfInvalidoComMensagemPorCampo() throws Exception {
        mvc.perform(post("/pacientes").with(RECEPCAO)
                .contentType(MediaType.APPLICATION_JSON).content(paciente("Fulano", "52998224726")))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.erros.cpf").exists());
    }

    @Test
    void exigeCpfOuCns() throws Exception {
        mvc.perform(post("/pacientes").with(RECEPCAO)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nome\":\"Sem Documento\",\"dataNascimento\":\"1990-01-01\"}"))
            .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cpfDuplicadoRetorna409() throws Exception {
        cadastrarPaciente("Primeiro", "11144477735");
        mvc.perform(post("/pacientes").with(RECEPCAO)
                .contentType(MediaType.APPLICATION_JSON).content(paciente("Segundo", "11144477735")))
            .andExpect(status().isConflict());
    }

    @Test
    void medicoNaoCadastraPaciente() throws Exception {
        mvc.perform(post("/pacientes").with(MEDICO)
                .contentType(MediaType.APPLICATION_JSON).content(paciente("Fulano", "52998224725")))
            .andExpect(status().isForbidden());
    }

    @Test
    void pacienteInexistenteRetorna404() throws Exception {
        mvc.perform(get("/pacientes/999999").with(MEDICO))
            .andExpect(status().isNotFound());
    }

    @Test
    void fluxoCompletoDeInternacaoEAlta() throws Exception {
        long pacienteId = cadastrarPaciente("Maria Fictícia", "52998224725");
        long internacaoId = internar(pacienteId, "uti-01");

        // aparece no censo
        mvc.perform(get("/internacoes/em-aberto").with(MEDICO))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].leito").value("UTI-01"))
            .andExpect(jsonPath("$[0].status").value("INTERNADO"));

        // recepção não dá alta; médico dá
        mvc.perform(patch("/internacoes/" + internacaoId + "/alta").with(RECEPCAO))
            .andExpect(status().isForbidden());
        mvc.perform(patch("/internacoes/" + internacaoId + "/alta").with(MEDICO))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ALTA"));

        // não dá alta duas vezes
        mvc.perform(patch("/internacoes/" + internacaoId + "/alta").with(MEDICO))
            .andExpect(status().isConflict());

        // censo esvaziou, histórico mantém o registro
        mvc.perform(get("/internacoes/em-aberto").with(MEDICO))
            .andExpect(jsonPath("$", hasSize(0)));
        mvc.perform(get("/pacientes/" + pacienteId + "/internacoes").with(MEDICO))
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void naoInternaPacienteDuasVezesNemUsaLeitoOcupado() throws Exception {
        long maria = cadastrarPaciente("Maria", "52998224725");
        long joao = cadastrarPaciente("João", "11144477735");
        internar(maria, "ENF-12");

        // mesmo paciente, outro leito
        mvc.perform(post("/internacoes").with(RECEPCAO).contentType(MediaType.APPLICATION_JSON)
                .content("{\"pacienteId\":%d,\"leito\":\"ENF-13\"}".formatted(maria)))
            .andExpect(status().isConflict());

        // outro paciente, mesmo leito
        mvc.perform(post("/internacoes").with(RECEPCAO).contentType(MediaType.APPLICATION_JSON)
                .content("{\"pacienteId\":%d,\"leito\":\"enf-12\"}".formatted(joao)))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").value(containsString("ocupado")));
    }

    @Test
    void exportaPacienteEmFormatoFhir() throws Exception {
        long id = cadastrarPaciente("Maria Fictícia", "52998224725");

        mvc.perform(get("/fhir/Patient/" + id).with(MEDICO))
            .andExpect(status().isOk())
            .andExpect(content().contentTypeCompatibleWith("application/fhir+json"))
            .andExpect(jsonPath("$.resourceType").value("Patient"))
            .andExpect(jsonPath("$.id").value(String.valueOf(id)))
            .andExpect(jsonPath("$.identifier[0].value").value("52998224725"))
            .andExpect(jsonPath("$.name[0].text").value("Maria Fictícia"))
            .andExpect(jsonPath("$.birthDate").value("1980-05-10"));
    }
}
