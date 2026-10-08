-- Garantia no BANCO (vale mesmo com duas requisições simultâneas):
-- um paciente só pode ter uma internação em aberto e um leito só pode
-- ter um paciente por vez. Índices parciais só existem no PostgreSQL,
-- por isso este script fica na pasta "postgresql".
CREATE UNIQUE INDEX uq_internacao_aberta_paciente
    ON internacao (paciente_id) WHERE data_alta IS NULL;

CREATE UNIQUE INDEX uq_internacao_aberta_leito
    ON internacao (leito) WHERE data_alta IS NULL;
