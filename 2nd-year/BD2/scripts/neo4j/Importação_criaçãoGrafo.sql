CREATE CONSTRAINT FOR (c:Customer) REQUIRE c.customer_id IS UNIQUE;
CREATE CONSTRAINT FOR (o:Order) REQUIRE o.order_id IS UNIQUE;
CREATE CONSTRAINT FOR (s:Seller) REQUIRE s.seller_id IS UNIQUE;
CREATE CONSTRAINT FOR (p:Product) REQUIRE p.product_id IS UNIQUE;

LOAD CSV WITH HEADERS
FROM 'file:///olist_customers_dataset.csv' AS csvLineCustomer

CREATE (customer:Customer {
    customer_id: csvLineCustomer.customer_id,
    customer_unique_id: csvLineCustomer.customer_unique_id,
    customer_zip_code_prefix: csvLineCustomer.customer_zip_code_prefix,
    customer_city: csvLineCustomer.customer_city,
    customer_state: csvLineCustomer.customer_state
});


LOAD CSV WITH HEADERS
FROM 'file:///olist_orders_dataset.csv' AS csvLineOrder

CREATE (order:Order {
    order_id: csvLineOrder.order_id,
    order_status: csvLineOrder.order_status,
    order_purchase_timestamp: datetime(replace(csvLineOrder.order_purchase_timestamp,' ','T')),
    order_delivered_customer_date: datetime(replace(csvLineOrder.order_delivered_customer_date,' ','T'))
})

WITH csvLineOrder, order

MATCH (customer:Customer {customer_id: csvLineOrder.customer_id})

CREATE (customer)-[:FAZ]->(order);


LOAD CSV WITH HEADERS
FROM 'file:///olist_sellers_dataset.csv' AS csvLineSeller

CREATE (seller:Seller {
    seller_id: csvLineSeller.seller_id,
    seller_zip_code_prefix: csvLineSeller.seller_zip_code_prefix,
    seller_city: csvLineSeller.seller_city,
    seller_state: csvLineSeller.seller_state
});


LOAD CSV WITH HEADERS FROM 'file:///olist_products_dataset.csv' AS csvLineProduct
CREATE(product:Product { 
    product_id: csvLineProduct.product_id, 
    product_category_name: csvLineProduct.product_category_name,
    product_name_length: toInteger(csvLineProduct.product_name_lenght),
    product_description_length: toInteger(csvLineProduct.product_description_lenght),
    product_photos_qty: toInteger(csvLineProduct.product_photos_qty),
    product_weight_g: toInteger(csvLineProduct.product_weight_g),
    product_length_cm: toInteger(csvLineProduct.product_length_cm),
    product_height_cm: toInteger(csvLineProduct.product_height_cm),
    product_width_cm: toInteger(csvLineProduct.product_width_cm)
});

LOAD CSV WITH HEADERS
FROM 'file:///olist_order_items_dataset.csv' AS csvLineOrderItem

MATCH (order:Order {order_id: csvLineOrderItem.order_id})

MATCH (product:Product {product_id: csvLineOrderItem.product_id})

MATCH (seller:Seller {seller_id: csvLineOrderItem.seller_id})

CREATE (seller)-[:EMITIU]->(order)

CREATE (order)-[:CONTEM {
    order_item_id: toInteger(csvLineOrderItem.order_item_id),
    shipping_limit_date:datetime(replace(csvLineOrderItem.shipping_limit_date,' ','T')),
    price: toFloat(csvLineOrderItem.price),
    freight_value: toFloat(csvLineOrderItem.freight_value)
}]->(product);
