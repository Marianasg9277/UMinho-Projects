USE bd2;

LOAD DATA INFILE '/var/lib/mysql/csv/olist_customers_dataset.csv' 
INTO TABLE Customer 
FIELDS TERMINATED BY ',' ENCLOSED BY '"' 
LINES TERMINATED BY '\n' 
IGNORE 1 ROWS;

LOAD DATA INFILE '/var/lib/mysql/csv/olist_sellers_dataset.csv' 
INTO TABLE Seller 
FIELDS TERMINATED BY ',' ENCLOSED BY '"' 
LINES TERMINATED BY '\n' 
IGNORE 1 ROWS;

LOAD DATA INFILE '/var/lib/mysql/csv/olist_products_dataset.csv'
INTO TABLE Product
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(product_id, product_category_name, @vname_len, @vdesc_len, @vphotos_qty, @vweight, @vlength, @vheight, @vwidth)
SET 

product_name_length = IF(@vname_len = '', NULL, @vname_len),
product_description_length = IF(@vdesc_len = '', NULL, @vdesc_len),
product_photos_qty = IF(@vphotos_qty = '', NULL, @vphotos_qty),
product_weight_g = IF(@vweight = '', NULL, @vweight),
product_length_cm = IF(@vlength = '', NULL, @vlength),
product_height_cm = IF(@vheight = '', NULL, @vheight),
product_width_cm = IF(@vwidth = '', NULL, @vwidth);

LOAD DATA INFILE '/var/lib/mysql/csv/olist_orders_dataset.csv'
INTO TABLE `Order`
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(order_id, customer_id, order_status, @v_purchase, @v_approved, @v_carrier, @v_customer, @v_estimated)
SET 

order_purchase_timestamp = IF(@v_purchase = '', NULL, @v_purchase),
order_approved_at = IF(@v_approved = '', NULL, @v_approved),
order_delivered_carrier_date = IF(@v_carrier = '', NULL, @v_carrier),
order_delivered_customer_date = IF(@v_customer = '', NULL, @v_customer),
order_estimated_delivery_date = IF(@v_estimated = '', NULL, @v_estimated);

LOAD DATA INFILE '/var/lib/mysql/csv/olist_order_items_dataset.csv' 
INTO TABLE Order_item 
FIELDS TERMINATED BY ',' ENCLOSED BY '"' 
LINES TERMINATED BY '\n' 
IGNORE 1 ROWS;
