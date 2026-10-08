-- Registro de quem acessou qual prontuário (LGPD: rastreabilidade de acesso).
-- Guarda apenas o ID do recurso, nunca dados pessoais do paciente.
CREATE TABLE auditoria_acesso (
    id         BIGSERIAL PRIMARY KEY,
    usuario    VARCHAR(100) NOT NULL,
    acao       VARCHAR(50)  NOT NULL,
    recurso    VARCHAR(50)  NOT NULL,
    recurso_id BIGINT,
    data_hora  TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_auditoria_usuario ON auditoria_acesso (usuario, data_hora);
