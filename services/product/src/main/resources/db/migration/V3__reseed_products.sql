CREATE TABLE IF NOT EXISTS category
(
  id UUID NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS product
(
  id UUID NOT NULL PRIMARY KEY,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(255),
  available_quantity DOUBLE PRECISION NOT NULL,
  price NUMERIC(38, 2) NOT NULL,
  category_id UUID,
  CONSTRAINT fk_category FOREIGN KEY (category_id) REFERENCES category(id)
);

INSERT INTO category (id, name, description)
VALUES
  ('018d2f1a-b123-7a45-8c67-d1e2f3a4b5c6', 'Electronics', 'Electronic devices and gadgets'),
  ('018d2f1a-b124-7b56-9d78-e2f3a4b5c6d7', 'Books', 'Books from multiple genres'),
  ('018d2f1a-b125-7c67-af89-f3a4b5c6d7e8', 'Home and Kitchen', 'Home and kitchen utilities'),
  ('018d2f1a-b126-7d78-b09a-04b5c6d7e8f9', 'Fashion', 'Clothing and accessories'),
  ('018d2f1a-b127-7e89-c1ab-15c6d7e8f90a', 'Sports', 'Sports equipment')
ON CONFLICT (id) DO NOTHING;

INSERT INTO product (id, name, description, available_quantity, price, category_id)
VALUES
  ('018d2f1a-c101-7123-8234-a1b2c3d4e5f6', 'Smartphone XYZ', 'Latest generation smartphone', 50.0, 2500.00, '018d2f1a-b123-7a45-8c67-d1e2f3a4b5c6'),
  ('018d2f1a-c102-7234-9345-b2c3d4e5f607', 'Notebook Ultra', 'Powerful notebook for work', 20.0, 4500.00, '018d2f1a-b123-7a45-8c67-d1e2f3a4b5c6'),
  ('018d2f1a-c103-7345-a456-c3d4e5f60718', 'Java for Beginners', 'Technical Java book', 100.0, 89.90, '018d2f1a-b124-7b56-9d78-e2f3a4b5c6d7'),
  ('018d2f1a-c104-7456-b567-d4e5f6071829', 'Air Fryer', 'Oil free fryer', 15.0, 350.00, '018d2f1a-b125-7c67-af89-f3a4b5c6d7e8'),
  ('018d2f1a-c105-7567-c678-e5f60718293a', 'Sports T-Shirt', 'Dry fit sports t-shirt', 200.0, 49.99, '018d2f1a-b126-7d78-b09a-04b5c6d7e8f9'),
  ('018d2f1a-c106-7678-d789-f60718293a4b', 'Running Shoes', 'Cushioned running shoes', 30.0, 299.90, '018d2f1a-b127-7e89-c1ab-15c6d7e8f90a'),
  ('018d2f1a-c107-7789-e89a-0718293a4b5c', 'Bluetooth Headphones', 'Noise canceling headphones', 75.0, 599.00, '018d2f1a-b123-7a45-8c67-d1e2f3a4b5c6'),
  ('018d2f1a-c108-789a-f90b-18293a4b5c6d', '4K Monitor', '27-inch monitor', 12.0, 1800.00, '018d2f1a-b123-7a45-8c67-d1e2f3a4b5c6'),
  ('018d2f1a-c109-79ab-0a1c-293a4b5c6d7e', 'Cookbook', 'Recipes from around the world', 40.0, 55.00, '018d2f1a-b124-7b56-9d78-e2f3a4b5c6d7'),
  ('018d2f1a-c10a-7abc-1b2d-3a4b5c6d7e8f', 'Espresso Machine', 'Capsule coffee machine', 25.0, 420.00, '018d2f1a-b125-7c67-af89-f3a4b5c6d7e8')
ON CONFLICT (id) DO NOTHING;
