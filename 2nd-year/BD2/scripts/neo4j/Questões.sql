--1.
MATCH (o:Order)-[c:CONTEM]->(p:Product)
RETURN 
    CASE 
        WHEN p.product_category_name IS NULL OR p.product_category_name = '' THEN 'Sem_Categoria' 
        ELSE p.product_category_name 
    END AS categoria, 
    sum(c.price + c.freight_value) AS receita_total ORDER BY receita_total DESC
LIMIT 10;


--2.
MATCH (c:Customer)-[:FAZ]->(o:Order)-[:CONTEM]->(p:Product)
WITH c.customer_state AS estado, 
     p.product_id AS id_produto, 
     p.product_category_name AS categoria, 
     count(p) AS quantidade_vendida

ORDER BY quantidade_vendida DESC

WITH estado, collect({
    id: id_produto, 
    categoria: categoria, 
    quantidade: quantidade_vendida
})[0] AS best_seller

RETURN estado AS Estado, 
       best_seller.id AS ID_Produto, 
       best_seller.categoria AS Categoria, 
       best_seller.quantidade AS Quantidade_Vendida
ORDER BY Quantidade_Vendida DESC;


--3.
MATCH (c:Customer)-[:FAZ]->(o:Order)<-[:EMITIU]-(s:Seller)
WHERE date(o.order_purchase_timestamp).year = 2016

RETURN c.customer_state AS customer_state, s.seller_state AS seller_state, count(DISTINCT o.order_id) AS total_fluxo

ORDER BY total_fluxo DESC;


--4.
MATCH (o:Order)-[c:CONTEM]->(p:Product)
WHERE o.order_status IN ['delivered', 'canceled']
  
  AND o.order_purchase_timestamp.year IN [2017, 2018]

WITH 
  CASE 
    WHEN p.product_category_name IS NULL OR p.product_category_name = '' THEN 'Sem Categoria'
    ELSE p.product_category_name 
  END AS categoria,
  o.order_status AS status,
  c.price + c.freight_value AS valor_item

WITH categoria,
     sum(CASE WHEN status = 'delivered' THEN 1 ELSE 0 END) AS total_entregue,
     sum(CASE WHEN status = 'canceled' THEN 1 ELSE 0 END) AS total_cancelado,
     sum(CASE WHEN status = 'canceled' THEN valor_item ELSE 0 END) AS faturacao_perdida

RETURN categoria,
       total_entregue,
       total_cancelado,
       CASE 
         WHEN (total_entregue + total_cancelado) = 0 THEN 0.0
         ELSE (toFloat(total_entregue) / (total_entregue + total_cancelado)) * 100
       END AS taxa_sucesso,
       faturacao_perdida

ORDER BY faturacao_perdida DESC;


--5.
MATCH (c:Customer)-[:FAZ]->(o:Order)-[ct:CONTEM]->(p:Product)

WITH 
    c.customer_id AS cliente,
    c.customer_city AS cidade,
    c.customer_state AS estado,
    sum(ct.price + ct.freight_value) AS receita_total

RETURN 
    cliente,
    cidade,
    estado,
    receita_total

ORDER BY receita_total DESC
LIMIT 10;


--6.
MATCH (o:Order)-[:CONTEM]->(p1:Product), (o)-[:CONTEM]->(p2:Product)

WHERE p1.product_id < p2.product_id

RETURN 
    p1.product_id AS produto_A,
    p1.product_category_name AS categoria_A,
    p2.product_id AS produto_B,
    p2.product_category_name AS categoria_B,
    COUNT(*) AS frequencia

ORDER BY frequencia DESC
LIMIT 10;
