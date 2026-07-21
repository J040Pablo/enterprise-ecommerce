ALTER TABLE products
    ADD COLUMN category_id UUID;

UPDATE products
SET category_id = (
    SELECT id
    FROM categories
             LIMIT 1
    );

ALTER TABLE products
    ALTER COLUMN category_id SET NOT NULL;

ALTER TABLE products
    ADD CONSTRAINT fk_products_category
        FOREIGN KEY (category_id)
            REFERENCES categories(id);