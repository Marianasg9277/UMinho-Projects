# Olist E-Commerce Data Analysis 🛒

Academic project developed for the **Bases de Dados 2 (BD2)** course at the University of Minho (2024/2025).

### 📋 Description
Modeling, implementation, and analytical exploration of an information system using real-world data (the Brazilian E-Commerce Public Dataset by Olist, containing ~100k orders). The project explores and compares three distinct database paradigms: **Relational (MySQL)**, **Column-Family (Apache Cassandra)**, and **Graph (Neo4j)**, entirely containerized in Docker.

### ✨ Key Analytics & Business Intelligence
The system processes complex queries to extract business value, answering critical questions such as:
* **Revenue Analysis:** Identifying the top 10 product categories and the top 10 customers generating the highest revenue.
* **Geospatial Insights:** Determining the best-selling products per Brazilian state and mapping sales flows (Seller vs. Customer) to identify frequent commercial routes.
* **Performance Metrics:** Calculating sales success rates (delivered vs. canceled orders) and lost revenue per category.
* **Market Basket Analysis:** Identifying cross-selling opportunities by finding products frequently bought together within the same category.

### 🛠️ Technical Implementation
* **Relational Paradigm (MySQL):** Conceptual to relational model transformation, data normalization, and complex analytical queries using JOINs and aggregations.
* **Column-Family Paradigm (Apache Cassandra):** *Query-First Design* approach, data denormalization, and strategic definition of Partition and Clustering Keys for optimized read performance.
* **Graph Paradigm (Neo4j):** Implementation using the Property Graph Model and Cypher language, converting associative tables and foreign keys into explicit directed relationships (`:FAZ`, `:EMITIU`, `:CONTEM`).
* **Infrastructure:** Docker environments for consistent deployment across all three paradigms.

### 📂 Repository Structure
* 📄 **`PL2G1_etapa3_bd2.pdf`:** Complete final report detailing the architecture, decisions, and queries for all three stages.
* 📁 **`scripts/`:** Main folder containing all database interactions.
  * 📁 **`sql/`:** DDL scripts for table creation, CSV processing, and the 6 analytical queries in SQL.
  * 📁 **`cassandra/`:** Keyspace and Column Families creation, `COPY` commands for CSV data population, and the 6 analytical queries in CQL.
  * 📁 **`neo4j/`:** Graph creation scripts (Constraints, `LOAD CSV` for nodes/relationships), and the 6 analytical queries in Cypher.

---
### 👥 Authors
* Ana Íris Machado Torres (a110489)
* Daniela Gomes Paulino (a109519)
* Maria Leonor Gomes Sampaio (a111427)
* Maria Nair da Cunha e Silva Lacerda  (a111488)Ângelo
* Mariana Silva Gomes (a110943)
