INSERT INTO users (
    first_name,
    last_name,
    email,
    password,
    enabled,
    email_verified,
    created_at,
    updated_at
)
VALUES (
           'System',
           'Administrator',
           'admin@ecommerce.com',
           '$2a$10$.Z7lBoYgXupQspYdjXynAep1Wp4kLJg30Ux.rW1Ixj5MaxqewpOvu',
           true,
           true,
           NOW(),
           NOW()
       )
    ON CONFLICT (email) DO NOTHING;

INSERT INTO user_roles (user_id, role_id, created_at, updated_at)
SELECT
    u.id,
    r.id,
    NOW(),
    NOW()
FROM users u
         JOIN roles r ON r.name = 'ADMIN'
WHERE u.email = 'admin@ecommerce.com'
    ON CONFLICT DO NOTHING;