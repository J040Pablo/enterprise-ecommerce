CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- Step 1: Add UUID columns to tables users, roles, user_roles, carts, cart_items
ALTER TABLE users ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE roles ADD COLUMN new_id UUID DEFAULT gen_random_uuid();

ALTER TABLE user_roles ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE user_roles ADD COLUMN new_user_id UUID;
ALTER TABLE user_roles ADD COLUMN new_role_id UUID;

ALTER TABLE carts ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE carts ADD COLUMN new_user_id UUID;

ALTER TABLE cart_items ADD COLUMN new_id UUID DEFAULT gen_random_uuid();
ALTER TABLE cart_items ADD COLUMN new_cart_id UUID;

-- Step 2: Populate FK relationships using existing BIGINT IDs
UPDATE user_roles ur
SET new_user_id = u.new_id
FROM users u
WHERE ur.user_id = u.id;

UPDATE user_roles ur
SET new_role_id = r.new_id
FROM roles r
WHERE ur.role_id = r.id;

UPDATE carts c
SET new_user_id = u.new_id
FROM users u
WHERE c.user_id = u.id;

UPDATE cart_items ci
SET new_cart_id = c.new_id
FROM carts c
WHERE ci.cart_id = c.id;

-- Step 3: Drop FK constraints & Indexes
ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS fk_user_roles_user;
ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS fk_user_roles_role;
ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS uk_user_role;

ALTER TABLE carts DROP CONSTRAINT IF EXISTS fk_cart_user;
DROP INDEX IF EXISTS idx_cart_user_id;

ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS fk_cart_item_cart;
ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS uq_cart_product;
DROP INDEX IF EXISTS idx_cart_item_cart_id;

-- Step 4: Drop old primary key constraints and columns
ALTER TABLE user_roles DROP CONSTRAINT IF EXISTS user_roles_pkey;
ALTER TABLE user_roles DROP COLUMN id;
ALTER TABLE user_roles DROP COLUMN user_id;
ALTER TABLE user_roles DROP COLUMN role_id;

ALTER TABLE cart_items DROP CONSTRAINT IF EXISTS cart_items_pkey;
ALTER TABLE cart_items DROP COLUMN id;
ALTER TABLE cart_items DROP COLUMN cart_id;

ALTER TABLE carts DROP CONSTRAINT IF EXISTS carts_pkey;
ALTER TABLE carts DROP COLUMN id;
ALTER TABLE carts DROP COLUMN user_id;

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_pkey;
ALTER TABLE users DROP COLUMN id;

ALTER TABLE roles DROP CONSTRAINT IF EXISTS roles_pkey;
ALTER TABLE roles DROP COLUMN id;

-- Step 5: Rename new columns to original names
ALTER TABLE users RENAME COLUMN new_id TO id;
ALTER TABLE roles RENAME COLUMN new_id TO id;

ALTER TABLE user_roles RENAME COLUMN new_id TO id;
ALTER TABLE user_roles RENAME COLUMN new_user_id TO user_id;
ALTER TABLE user_roles RENAME COLUMN new_role_id TO role_id;

ALTER TABLE carts RENAME COLUMN new_id TO id;
ALTER TABLE carts RENAME COLUMN new_user_id TO user_id;

ALTER TABLE cart_items RENAME COLUMN new_id TO id;
ALTER TABLE cart_items RENAME COLUMN new_cart_id TO cart_id;

-- Step 6: Set NOT NULL on ID and FK columns
ALTER TABLE users ALTER COLUMN id SET NOT NULL;
ALTER TABLE roles ALTER COLUMN id SET NOT NULL;

ALTER TABLE user_roles ALTER COLUMN id SET NOT NULL;
ALTER TABLE user_roles ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE user_roles ALTER COLUMN role_id SET NOT NULL;

ALTER TABLE carts ALTER COLUMN id SET NOT NULL;
ALTER TABLE carts ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE cart_items ALTER COLUMN id SET NOT NULL;
ALTER TABLE cart_items ALTER COLUMN cart_id SET NOT NULL;

-- Step 7: Re-add primary key constraints
ALTER TABLE users ADD CONSTRAINT users_pkey PRIMARY KEY (id);
ALTER TABLE roles ADD CONSTRAINT roles_pkey PRIMARY KEY (id);
ALTER TABLE user_roles ADD CONSTRAINT user_roles_pkey PRIMARY KEY (id);
ALTER TABLE carts ADD CONSTRAINT carts_pkey PRIMARY KEY (id);
ALTER TABLE cart_items ADD CONSTRAINT cart_items_pkey PRIMARY KEY (id);

-- Step 8: Re-add unique and FK constraints & indexes
ALTER TABLE user_roles ADD CONSTRAINT uk_user_role UNIQUE (user_id, role_id);
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE user_roles ADD CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE;

ALTER TABLE carts ADD CONSTRAINT fk_cart_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE carts ADD CONSTRAINT uq_cart_user_id UNIQUE (user_id);
CREATE INDEX idx_cart_user_id ON carts(user_id);

ALTER TABLE cart_items ADD CONSTRAINT fk_cart_item_cart FOREIGN KEY (cart_id) REFERENCES carts(id) ON DELETE CASCADE;
ALTER TABLE cart_items ADD CONSTRAINT uq_cart_product UNIQUE (cart_id, product_id);
CREATE INDEX idx_cart_item_cart_id ON cart_items(cart_id);
