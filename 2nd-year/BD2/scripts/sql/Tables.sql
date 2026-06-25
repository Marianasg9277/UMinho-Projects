
USE bd2;

DROP TABLE IF EXISTS Order_item;
DROP TABLE IF EXISTS `Order`;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS Seller;
DROP TABLE IF EXISTS Customer;

-- 1. Tabela de clientes (não dependem de ninguém)
CREATE TABLE Customer (
    customer_id VARCHAR(32) NOT NULL,
    customer_unique_id VARCHAR(32),
    customer_zip_code_prefix INT,
    customer_city VARCHAR(100),
    customer_state CHAR(2),
    PRIMARY KEY (customer_id)
);

-- 2. Tabela vendedores (não depende de ninguém)
CREATE TABLE Seller (
    seller_id VARCHAR(32) NOT NULL,
    seller_zip_code_prefix INT,
    seller_city VARCHAR(100),
    seller_state CHAR(2),
    PRIMARY KEY (seller_id)
);

-- 3. Tabela de produtos (não depende de ninguém)
CREATE TABLE Product (
    product_id VARCHAR(32) NOT NULL,
    product_category_name VARCHAR(100),
    product_name_length INT,
    product_description_length INT,
    product_photos_qty INT,
    product_weight_g INT,
    product_length_cm INT,
    product_height_cm INT,
    product_width_cm INT,
    PRIMARY KEY (product_id)
);

-- 4. Tabela de Encomendas (depende da Customer)
CREATE TABLE `Order` (
    order_id VARCHAR(32) NOT NULL,
    customer_id VARCHAR(32) NOT NULL,
    order_status VARCHAR(20),
    order_purchase_timestamp DATETIME,
    order_approved_at DATETIME,
    order_delivered_carrier_date DATETIME,
    order_delivered_customer_date DATETIME,
    order_estimated_delivery_date DATETIME,
    PRIMARY KEY (order_id),
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id)
);

-- 5. Tabela de Itens (depende de Order, Product e Seller)
CREATE TABLE Order_item (
    order_id VARCHAR(32) NOT NULL,
    order_item_id INT NOT NULL,
    product_id VARCHAR(32) NOT NULL,
    seller_id VARCHAR(32) NOT NULL,
    shipping_limit_date DATETIME,
    price DECIMAL(10,2),
    freight_value DECIMAL(10,2),
    PRIMARY KEY (order_id, order_item_id),
    FOREIGN KEY (order_id) REFERENCES `Order`(order_id),
    FOREIGN KEY (product_id) REFERENCES Product(product_id),
    FOREIGN KEY (seller_id) REFERENCES Seller(seller_id)
);

