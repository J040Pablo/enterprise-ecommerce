CREATE TABLE shippings (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL UNIQUE,
    tracking_code VARCHAR(50) NOT NULL UNIQUE,
    carrier VARCHAR(100) NOT NULL,
    status VARCHAR(30) NOT NULL,
    estimated_delivery DATE NOT NULL,
    shipped_at TIMESTAMP,
    delivered_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
