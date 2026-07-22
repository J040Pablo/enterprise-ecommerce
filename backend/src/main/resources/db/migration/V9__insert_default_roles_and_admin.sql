INSERT INTO roles (name, description, created_at, updated_at)
VALUES
    ('ADMIN', 'Administrator', NOW(), NOW()),
    ('CUSTOMER', 'Customer', NOW(), NOW())
    ON CONFLICT (name) DO NOTHING;