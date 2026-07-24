CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    amount NUMERIC(10,2) NOT NULL,
    status VARCHAR(30) NOT NULL,
    payment_method VARCHAR(50),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
