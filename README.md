# Hospital API

Um sistema de **backend** (sem telas) que guarda pacientes de um hospital e controla quem está internado em qual leito.

> Projeto de portfólio. **Todos os dados são fictícios.**

---

## O que ele faz, em linguagem simples

Imagine a recepção de um hospital:

1. Chega uma paciente. A recepcionista **cadastra** nome, CPF e data de nascimento.
2. Ela precisa ficar internada. A recepcionista **interna** a paciente no leito UTI-01.
3. Dias depois, o médico **dá alta**. O leito fica livre de novo.
4. A qualquer momento, alguém pode ver **quem está internado agora**.

Este projeto é a parte "invisível" que faz tudo isso funcionar. Outro sistema (um site, um app) poderia conversar com ele pela internet.

### Regras que o sistema garante

- Todo paciente precisa ter **CPF ou CNS** (Cartão Nacional de Saúde) e o número precisa ser válido.
- Não existem dois pacientes com o mesmo CPF ou CNS.
- Um paciente **não pode estar internado em dois leitos** ao mesmo tempo.
- Um leito **não pode ter dois pacientes** ao mesmo tempo.
- **Só o médico dá alta.** A recepção não consegue.
- Paciente **nunca é apagado**, porque prontuário precisa ser guardado.
- Toda consulta a um paciente fica **registrada** (quem viu, e quando).

---

## Tecnologias (e para que cada uma serve)

| Tecnologia | Para que serve aqui |
|---|---|
| **Java 21** | A linguagem do programa |
| **Spring Boot** | Monta a API (recebe os pedidos e responde) |
| **PostgreSQL** | O banco de dados onde tudo fica guardado |
| **Flyway** | Cria e atualiza as tabelas do banco automaticamente |
| **Spring Security** | Exige login e separa o que cada perfil pode fazer |
| **Swagger** | Uma página para testar a API clicando, sem programar |
| **JUnit / Mockito** | Testes automáticos que garantem que as regras funcionam |
| **Docker** | Sobe o banco de dados com um comando |
| **GitHub Actions** | Roda os testes sozinho a cada envio para o GitHub |
| **HL7 FHIR** | Formato padrão de troca de dados de saúde, usado pela RNDS (Ministério da Saúde) |

---

## Como rodar no seu computador

### O que instalar antes

| Programa | Para quê | Como conferir |
|---|---|---|
| **Java 21** | Rodar o programa | `java -version` |
| **Maven** | Baixar bibliotecas e compilar | `mvn -version` |
| **Docker Desktop** | Rodar o banco de dados | `docker --version` |

### Passo a passo

**1. Abra o terminal dentro da pasta do projeto.**

**2. Ligue o banco de dados:**

```bash
docker compose up -d db
```

**3. Ligue a API:**

```bash
mvn spring-boot:run
```

Espere aparecer algo como `Started HospitalApiApplication`. Pronto: a API está no ar em `http://localhost:8080`.

**4. Teste pelo navegador.** Abra:

👉 http://localhost:8080/swagger-ui.html

É uma página com todas as rotas. Clique no botão **Authorize**, entre com um dos usuários da tabela abaixo e experimente cada rota com o botão **Try it out**.

Para desligar: aperte `Ctrl + C` no terminal e rode `docker compose down`.

### Usuários para teste

| Usuário | Senha | O que pode fazer |
|---|---|---|
| `recepcao` | `recepcao123` | Cadastrar paciente, internar, consultar |
| `medico` | `medico123` | Consultar e **dar alta** |
| `admin` | `admin123` | Tudo |

São senhas de exemplo, só para desenvolvimento. Num sistema real elas seriam trocadas (veja `application.yml`).

---

## Um exemplo completo, passo a passo

Siga esta ordem no Swagger (ou use o arquivo `requests.http`, que já tem tudo pronto):

| # | Quem | Ação | Rota |
|---|---|---|---|
| 1 | `recepcao` | Cadastra a paciente Maria | `POST /pacientes` |
| 2 | `recepcao` | Interna a Maria no leito UTI-01 | `POST /internacoes` |
| 3 | `medico` | Vê quem está internado agora | `GET /internacoes/em-aberto` |
| 4 | `medico` | Dá alta para a Maria | `PATCH /internacoes/1/alta` |
| 5 | `medico` | Vê o histórico da Maria | `GET /pacientes/1/internacoes` |
| 6 | `medico` | Vê a Maria no formato FHIR | `GET /fhir/Patient/1` |

Corpo do pedido do passo 1 (CPF e CNS de teste, válidos mas fictícios):

```json
{
  "nome": "Maria Fictícia da Silva",
  "cpf": "52998224725",
  "cns": "123456789010000",
  "dataNascimento": "1980-05-10"
}
```

Corpo do passo 2:

```json
{
  "pacienteId": 1,
  "leito": "UTI-01"
}
```

**Experimente também quebrar as regras** para ver as respostas de erro:

- Interne a Maria de novo → erro **409** ("já possui uma internação em aberto").
- Interne outro paciente no mesmo leito → erro **409** ("leito ocupado").
- Tente dar alta logado como `recepcao` → erro **403** (sem permissão).
- Cadastre um CPF inválido → erro **400** dizendo qual campo está errado.

---

## Lista de rotas

| Método | Rota | O que faz | Quem pode |
|---|---|---|---|
| POST | `/pacientes` | Cadastra paciente | recepção, admin |
| GET | `/pacientes/{id}` | Busca um paciente | qualquer usuário logado |
| GET | `/pacientes?nome=` | Lista pacientes (com busca por nome e páginas) | qualquer usuário logado |
| PUT | `/pacientes/{id}` | Atualiza paciente | recepção, admin |
| POST | `/internacoes` | Interna um paciente | recepção, admin |
| PATCH | `/internacoes/{id}/alta` | Dá alta | médico, admin |
| GET | `/internacoes/em-aberto` | Quem está internado agora | qualquer usuário logado |
| GET | `/pacientes/{id}/internacoes` | Histórico de internações | qualquer usuário logado |
| GET | `/fhir/Patient/{id}` | Paciente no formato FHIR | qualquer usuário logado |

### Códigos de resposta que você vai ver

| Código | Significa |
|---|---|
| 200 / 201 | Deu certo (201 = algo foi criado) |
| 400 | Dados inválidos (ex.: CPF errado) |
| 401 | Faltou login ou a senha está errada |
| 403 | Você está logado, mas seu perfil não pode fazer isso |
| 404 | Não encontrado |
| 409 | Conflito (ex.: leito ocupado, CPF já cadastrado) |
| 422 | Falta algo exigido pela regra (ex.: nem CPF nem CNS) |

---

## Como o código está organizado

O código segue um caminho simples: **o pedido entra pelo Controller, o Service aplica as regras, o Repository fala com o banco.**

```
Pedido ──► Controller ──► Service ──► Repository ──► Banco de dados
          (recebe)       (regras)    (grava/lê)
```

```
src/main/java/br/com/portfolio/hospital/
├── paciente/     Cadastro de pacientes
├── internacao/   Internar, dar alta, ver quem está internado
├── fhir/         Converte o paciente para o formato FHIR
├── auditoria/    Registra quem consultou o quê
├── config/       Login/permissões e configurações gerais
└── shared/       Validação de CPF e CNS, tratamento de erros

src/main/resources/db/migration/   Scripts que criam as tabelas
src/test/                          Testes automáticos
```

**Por onde começar a ler o código?** Nesta ordem:

1. `paciente/PacienteController.java`: veja como uma rota é declarada.
2. `paciente/PacienteService.java`: veja as regras de cadastro.
3. `internacao/InternacaoService.java`: o coração do projeto (leito ocupado, alta).
4. `src/test/.../FluxoHospitalarIT.java`: um teste que usa a API do começo ao fim.

---

## Rodando os testes

```bash
mvn verify
```

Não precisa de Docker nem do banco ligado: os testes usam um banco temporário na memória. No final deve aparecer `BUILD SUCCESS`.

O que é testado:

- CPF e CNS válidos e inválidos.
- As regras de internação (leito ocupado, paciente já internado, alta repetida).
- O fluxo completo pela API, incluindo login e permissões.

---

## Perguntas e problemas comuns

**"Connection refused" ao iniciar a API**
O banco não está ligado. Rode `docker compose up -d db` e espere alguns segundos.

**"Port 5432 is already in use"**
Você já tem um PostgreSQL rodando no computador. Desligue-o ou mude a porta no `docker-compose.yml`.

**"Port 8080 is already in use"**
Outro programa usa a porta 8080. Feche-o ou rode com `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8081`.

**Erro 401 no Swagger**
Clique em **Authorize** e informe usuário e senha.

**Quero tudo em Docker, sem instalar Java**
Rode `docker compose up --build`. Isso liga o banco e a API juntos.

---

## Sobre dados e privacidade (LGPD)

- Nunca coloque dados reais de pacientes em projetos de portfólio.
- A tabela `auditoria_acesso` guarda **quem** consultou **qual paciente** e **quando**, só com o número do paciente, sem nome ou CPF.
- Mensagens de erro e logs não mostram dados pessoais.

---

## Ideias para evoluir o projeto

- Trocar o login simples por **JWT** ou **Keycloak**.
- Testar com um PostgreSQL de verdade usando **Testcontainers**.
- Adicionar o recurso FHIR `Encounter` (a internação).
- Incluir prescrições, exames e evolução clínica.
- Criar um painel de métricas com Micrometer e Prometheus.

---

## Observação sobre o FHIR

O endpoint `/fhir/Patient/{id}` mostra como um paciente aparece no padrão usado pela RNDS. Antes de integrar de verdade com a RNDS, confira no guia oficial (BR Core) os perfis e identificadores vigentes, pois podem mudar.
