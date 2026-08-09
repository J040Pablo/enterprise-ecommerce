-- V22__revoke_default_admin_password.sql
-- Revokes the well-known Admin@123 credential seeded by V11/V20.
-- The password hash below is NOT a usable/documented credential.
--
-- Local development (SPRING_PROFILE=dev):
--   DevelopmentDataSeeder re-enables admin@ecommerce.com with the local default password.
--
-- Production / docker / any non-dev profile:
--   Provision an admin via ADMIN_EMAIL + ADMIN_PASSWORD (see ProductionAdminBootstrap).
--   Do not rely on admin@ecommerce.com / Admin@123.

UPDATE users
SET
    password = '$2a$10$85C6v/80G7OeXK0/byRWIOxmZ5wYtVjNfwZRjUBquSWA4wBnkuhhu',
    enabled = false,
    updated_at = NOW()
WHERE email = 'admin@ecommerce.com';
