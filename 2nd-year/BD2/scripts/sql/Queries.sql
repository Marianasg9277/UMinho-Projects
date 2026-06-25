-- 1.
CREATE VIEW Receita_por_categoria AS
SELECT P.product_category_name,
       SUM(OI.price + OI.freight_value) AS receita_total
FROM Order_item OI
JOIN Product P ON OI.product_id = P.product_id
GROUP BY P.product_category_name;

SELECT * 
FROM Receita_por_categoria 
ORDER BY receita_total DESC 
LIMIT 10;


-- 2.
SELECT 
    c.customer_state AS Estado,
    p.product_id AS ID_Produto,
    p.product_category_name AS Categoria,
    COUNT(oi.product_id) AS Quantidade_Vendida
FROM Customer c
JOIN Order o ON c.customer_id = o.customer_id
JOIN Order_item oi ON o.order_id = oi.order_id
JOIN Product p ON oi.product_id = p.product_id
GROUP BY c.customer_state, p.product_id, p.product_category_name
HAVING (c.customer_state, COUNT(oi.product_id)) IN (
    -- esta subquery encontra a quantidade máxima vendida em cada estado
    SELECT estado_sub, MAX(total_vendas)
    FROM (
        SELECT 
            c2.customer_state AS estado_sub, 
            COUNT(oi2.product_id) AS total_vendas
        FROM Customer c2
        JOIN Order o2 ON c2.customer_id = o2.customer_id
        JOIN Order_item oi2 ON o2.order_id = oi2.order_id
        GROUP BY c2.customer_state, oi2.product_id
    ) AS sub_tabela
    GROUP BY estado_sub
)
ORDER BY Quantidade_Vendida DESC;


-- 3.
SELECT c.customer_state, s.seller_state,  COUNT(distinct o.order_id) AS total_fluxo
FROM Customer c
JOIN `Order` o ON c.customer_id=o.customer_id
JOIN Order_item oi ON o.order_id=oi.order_id
JOIN Seller s ON oi.seller_id=s.seller_id
WHERE o.order_purchase_timestamp BETWEEN '2016-01-01 00:00:00' AND '2016-12-31 23:59:59'
GROUP BY s.seller_state, c.customer_state
ORDER BY total_fluxo DESC;


-- 4.
SELECT
    p.product_category_name AS Categoria,
    SUM(IF(o.order_status = 'delivered', 1, 0)) AS Total_Entregue,
    SUM(IF(o.order_status = 'canceled', 1, 0)) AS Total_Cancelado,
    -- Cálculo da Taxa de Sucesso: (Entregues / (Entregues + Cancelados))
    ROUND(
        SUM(IF(o.order_status = 'delivered', 1, 0)) /
            NULLIF(SUM(IF(o.order_status = 'delivered' OR o.order_status = 'canceled', 1, 0)), 0) * 100, 2) AS Taxa_Sucesso_Percent,
    -- Soma do preço + taxa de envio apenas para os cancelados (Faturação Perdida)
    SUM(IF(o.order_status = 'canceled', oi.price + oi.freight_value, 0)) AS Faturacao_Perdida
FROM
    Order_item oi
JOIN
    `Order` o ON oi.order_id = o.order_id
JOIN
    Product p ON oi.product_id = p.product_id
WHERE
    YEAR(o.order_purchase_timestamp) IN (2017, 2018)
GROUP BY
    p.product_category_name
HAVING
    Total_Entregue > 0 OR Total_Cancelado > 0
ORDER BY
    Faturacao_Perdida DESC;


-- 5.
SELECT c.customer_unique_id,
    SUM(oi.price + oi.freight_value) AS receita_total
FROM `Order` o
JOIN customer c
    ON c.customer_id = o.customer_id
JOIN order_item oi
    ON oi.order_id = o.order_id
WHERE o.order_purchase_timestamp
      BETWEEN '2016-01-01 00:00:00'
          AND '2018-12-31 23:59:59'
  AND o.order_status = 'delivered'
GROUP BY c.customer_unique_id
ORDER BY receita_total DESC
LIMIT 10;


-- 6.
CREATE OR REPLACE VIEW vw_pares_produtos_exatos AS
SELECT 
    oi1.product_id AS id_produto_A, 
    oi2.product_id AS id_produto_B,
    COUNT(*) AS vezes_comprados_juntos
FROM 
    Order_item oi1
JOIN 
    Order_item oi2 ON oi1.order_id = oi2.order_id
WHERE 
    oi1.product_id < oi2.product_id
GROUP BY 
    oi1.product_id, oi2.product_id
ORDER BY 
    vezes_comprados_juntos DESC;

SELECT 
    oi1.product_id, 
    p1.product_category_name,
    oi2.product_id, 
    p2.product_category_name,
    COUNT(*) AS frequencia
FROM Order_item oi1
JOIN Order_item oi2 ON oi1.order_id = oi2.order_id
JOIN Product p1 ON oi1.product_id = p1.product_id
JOIN Product p2 ON oi2.product_id = p2.product_id
WHERE oi1.product_id < oi2.product_id
GROUP BY oi1.product_id, oi2.product_id, p1.product_category_name, p2.product_category_name
ORDER BY frequencia DESC
LIMIT 10;
