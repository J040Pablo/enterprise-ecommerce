CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    active BOOLEAN NOT NULL DEFAULT TRUE
);