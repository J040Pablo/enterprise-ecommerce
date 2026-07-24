INSERT INTO roles (
    name,
    description
)
VALUES (
    'ADMIN',
    'System administrator'
)
ON CONFLICT (name) DO NOTHING;