-- Inserindo categorias
INSERT INTO category (id, name, description) VALUES ('018d2f1a-b123-7a45-8c67-d1e2f3a4b5c6', 'Eletrônicos', 'Dispositivos eletrônicos em geral');
INSERT INTO category (id, name, description) VALUES ('018d2f1a-b124-7b56-9d78-e2f3a4b5c6d7', 'Livros', 'Livros de diversos gêneros');
INSERT INTO category (id, name, description) VALUES ('018d2f1a-b125-7c67-af89-f3a4b5c6d7e8', 'Casa e Cozinha', 'Utensílios para casa e cozinha');
INSERT INTO category (id, name, description) VALUES ('018d2f1a-b126-7d78-b09a-04b5c6d7e8f9', 'Moda', 'Vestuário e acessórios');
INSERT INTO category (id, name, description) VALUES ('018d2f1a-b127-7e89-c1ab-15c6d7e8f90a', 'Esportes', 'Equipamentos esportivos');

-- Inserindo produtos
INSERT INTO product (id, name, description, available_quantity, price, category_id) VALUES ('018d2f1a-c101-7123-8234-a1b2c3d4e5f6', 'Smartphone XYZ', 'Celular de última geração', 50.0, 2500.00, '018d2f1a-b123-7a45-8c67-d1e2f3a4b5c6');
INSERT INTO product (id, name, description, available_quantity, price, category_id) VALUES ('018d2f1a-c102-7234-9345-b2c3d4e5f607', 'Notebook Ultra', 'Notebook potente para trabalho', 20.0, 4500.00, '018d2f1a-b123-7a45-8c67-d1e2f3a4b5c6');
INSERT INTO product (id, name, description, available_quantity, price, category_id) VALUES ('018d2f1a-c103-7345-a456-c3d4e5f60718', 'Java para Iniciantes', 'Livro técnico de Java', 100.0, 89.90, '018d2f1a-b124-7b56-9d78-e2f3a4b5c6d7');
INSERT INTO product (id, name, description, available_quantity, price, category_id) VALUES ('018d2f1a-c104-7456-b567-d4e5f6071829', 'Fritadeira Elétrica', 'Fritadeira sem óleo', 15.0, 350.00, '018d2f1a-b125-7c67-af89-f3a4b5c6d7e8');
INSERT INTO product (id, name, description, available_quantity, price, category_id) VALUES ('018d2f1a-c105-7567-c678-e5f60718293a', 'Camiseta Esportiva', 'Camiseta dry fit', 200.0, 49.99, '018d2f1a-b126-7d78-b09a-04b5c6d7e8f9');
INSERT INTO product (id, name, description, available_quantity, price, category_id) VALUES ('018d2f1a-c106-7678-d789-f60718293a4b', 'Tênis de Corrida', 'Tênis amortecido', 30.0, 299.90, '018d2f1a-b127-7e89-c1ab-15c6d7e8f90a');
INSERT INTO product (id, name, description, available_quantity, price, category_id) VALUES ('018d2f1a-c107-7789-e89a-0718293a4b5c', 'Fone Bluetooth', 'Cancelamento de ruído', 75.0, 599.00, '018d2f1a-b123-7a45-8c67-d1e2f3a4b5c6');
INSERT INTO product (id, name, description, available_quantity, price, category_id) VALUES ('018d2f1a-c108-789a-f90b-18293a4b5c6d', 'Monitor 4K', 'Tela de 27 polegadas', 12.0, 1800.00, '018d2f1a-b123-7a45-8c67-d1e2f3a4b5c6');
INSERT INTO product (id, name, description, available_quantity, price, category_id) VALUES ('018d2f1a-c109-79ab-0a1c-293a4b5c6d7e', 'Livro de Receitas', 'Receitas do mundo todo', 40.0, 55.00, '018d2f1a-b124-7b56-9d78-e2f3a4b5c6d7');
INSERT INTO product (id, name, description, available_quantity, price, category_id) VALUES ('018d2f1a-c10a-7abc-1b2d-3a4b5c6d7e8f', 'Cafeteira Expresso', 'Máquina de café em cápsulas', 25.0, 420.00, '018d2f1a-b125-7c67-af89-f3a4b5c6d7e8');
