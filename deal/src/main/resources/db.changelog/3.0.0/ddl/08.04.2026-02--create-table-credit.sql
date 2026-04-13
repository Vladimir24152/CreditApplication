CREATE TABLE IF NOT EXISTS credit
(
    credit_id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    amount DECIMAL
(
    19,
    2
) NOT NULL,
    term INTEGER NOT NULL,
    monthly_payment DECIMAL
(
    19,
    2
) NOT NULL,
    rate DECIMAL
(
    5,
    2
) NOT NULL,
    psk DECIMAL
(
    19,
    2
) NOT NULL,
    payment_schedule JSONB NOT NULL,
    insurance_enabled BOOLEAN NOT NULL,
    salary_client BOOLEAN NOT NULL,
    credit_status VARCHAR
(
    20
) NOT NULL
    );