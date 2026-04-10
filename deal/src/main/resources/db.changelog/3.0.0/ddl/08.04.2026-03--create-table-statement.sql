CREATE TABLE IF NOT EXISTS statement
(
    statement_id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    client_id UUID NOT NULL,
    credit_id UUID,
    status VARCHAR
(
    30
) NOT NULL,
    creation_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    applied_offer JSONB,
    sign_date TIMESTAMP,
    ses_code VARCHAR
(
    255
),
    status_history JSONB NOT NULL,
    CONSTRAINT fk_statement_client FOREIGN KEY
(
    client_id
) REFERENCES client
(
    client_id
),
    CONSTRAINT fk_statement_credit FOREIGN KEY
(
    credit_id
) REFERENCES credit
(
    credit_id
),
    CONSTRAINT uk_statement_client UNIQUE
(
    client_id
),
    CONSTRAINT uk_statement_credit UNIQUE
(
    credit_id
)
    );