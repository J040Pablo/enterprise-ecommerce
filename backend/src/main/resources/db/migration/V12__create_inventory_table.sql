CREATE TABLE inventories (
     id UUID PRIMARY KEY,
     product_id UUID NOT NULL UNIQUE,
     quantity INTEGER NOT NULL,

     CONSTRAINT fk_inventory_product
         FOREIGN KEY (product_id)
             REFERENCES products(id)
             ON DELETE CASCADE,

     CONSTRAINT chk_inventory_quantity
         CHECK (quantity >= 0)
);

ALTER TABLE products
DROP COLUMN stock;