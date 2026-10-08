CREATE TABLE paciente (
    id              BIGSERIAL PRIMARY KEY,
    nome            VARCHAR(150) NOT NULL,
    cpf             VARCHAR(11)  UNIQUE,
    cns             VARCHAR(15)  UNIQUE,
    data_nascimento DATE         NOT NULL,
    criado_em       TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_paciente_nome ON paciente (nome);
