CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    price NUMERIC(19,2) NOT NULL,
    stock INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);
