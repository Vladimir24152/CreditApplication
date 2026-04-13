CREATE TABLE IF NOT EXISTS client
(
    client_id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    last_name VARCHAR
(
    255
) NOT NULL,
    first_name VARCHAR
(
    255
) NOT NULL,
    middle_name VARCHAR
(
    255
),
    birth_date DATE NOT NULL,
    email VARCHAR
(
    255
) NOT NULL,
    gender VARCHAR
(
    20
),
    marital_status VARCHAR
(
    20
),
    dependent_amount INTEGER,
    passport JSONB NOT NULL,
    employment JSONB,
    account_number VARCHAR
(
    255
)
    );