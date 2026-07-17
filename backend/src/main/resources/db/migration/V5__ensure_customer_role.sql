INSERT INTO roles (name, description)
VALUES ('CUSTOMER', 'Customer role')
ON CONFLICT (name) DO NOTHING;
