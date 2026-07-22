INSERT INTO user_roles (user_id, role_id, created_at, updated_at)
SELECT
    u.id,
    r.id,
    NOW(),
    NOW()
FROM users u
         JOIN roles r
              ON r.name = 'ADMIN'
WHERE u.email = 'admin@ecommerce.com';