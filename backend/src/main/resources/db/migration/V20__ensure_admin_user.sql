-- V20__ensure_admin_user.sql
-- Migration para garantir a existência do usuário Administrador e sua permissão ADMIN no formato UUID pós-V19

-- 1. Garante que a role ADMIN existe
INSERT INTO roles (id, name, description, created_at, updated_at)
VALUES (
    gen_random_uuid(),
    'ADMIN',
    'System administrator',
    NOW(),
    NOW()
)
ON CONFLICT (name) DO NOTHING;

-- 2. Garante que o usuário admin@ecommerce.com existe com senha BCrypt 'Admin@123'
INSERT INTO users (
    id,
    first_name,
    last_name,
    email,
    password,
    enabled,
    email_verified,
    provider,
    created_at,
    updated_at
)
VALUES (
    gen_random_uuid(),
    'System',
    'Administrator',
    'admin@ecommerce.com',
    '$2a$10$.Z7lBoYgXupQspYdjXynAep1Wp4kLJg30Ux.rW1Ixj5MaxqewpOvu',
    true,
    true,
    'LOCAL',
    NOW(),
    NOW()
)
ON CONFLICT (email) DO NOTHING;

-- 3. Garante a associação do usuário com a role ADMIN na tabela user_roles
INSERT INTO user_roles (id, user_id, role_id, created_at, updated_at)
SELECT
    gen_random_uuid(),
    u.id,
    r.id,
    NOW(),
    NOW()
FROM users u
JOIN roles r ON r.name = 'ADMIN'
WHERE u.email = 'admin@ecommerce.com'
ON CONFLICT (user_id, role_id) DO NOTHING;
