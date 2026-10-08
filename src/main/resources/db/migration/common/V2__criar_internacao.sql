CREATE TABLE internacao (
    id           BIGSERIAL PRIMARY KEY,
    paciente_id  BIGINT      NOT NULL REFERENCES paciente (id),
    leito        VARCHAR(20) NOT NULL,
    data_entrada TIMESTAMP WITH TIME ZONE NOT NULL,
    data_alta    TIMESTAMP WITH TIME ZONE,
    CONSTRAINT ck_internacao_alta_apos_entrada
        CHECK (data_alta IS NULL OR data_alta >= data_entrada)
);

CREATE INDEX idx_internacao_paciente ON internacao (paciente_id);
CREATE INDEX idx_internacao_leito    ON internacao (leito);
