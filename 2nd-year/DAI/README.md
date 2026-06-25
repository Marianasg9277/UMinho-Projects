# Urban Management Platform (PGU) - Braga Urban Transport 🚌

Academic project developed for the **Desenvolvimento de Aplicações Informáticas (DAI)** (Software Application Development) course at the University of Minho (2025/2026).

### 🔗 Live Demo
**👉 [Access the Live PGU Application Here](https://pgu-projetodai-gqaefedrdje5ggfw.francecentral-01.azurewebsites.net/index.html)**

### 📋 Description
The Urban Management Platform (PGU) is an integrated system designed to support smart cities and territories by promoting operational efficiency, sustainability, and data-driven decision-making. This project was developed as a technological response to modernize the **Transportes Urbanos de Braga (TUB)** (Braga's Urban Transport network), aiming to aggregate and monitor all operational and mobility systems in real-time.

### ✨ Key Verticals & Features
To overcome information fragmentation across isolated systems, the platform operates across multiple urban domains:
* **🎫 Ticketing & Payments:** Management of transport cards and passes, digital validation of Transport Tickets (TT) with *Offline* operation support, and payment gateway integration.
* **🚍 Fleet Management & IoT:** Real-time transport network monitoring, precise GPS tracking (with strict 2-second updates), and automated maintenance alerts.
* **🅿️ On-Street Parking:** Seamless integration with the city's physical parking meters (FlowBird).
* **📊 Dashboards & Analytics:** Comprehensive visualization of operational performance by overlapping interactive data layers on a dynamic map.

### 🛠️ Tech Stack & Architecture
The system was designed exclusively with Open Source technologies to ensure maximum robustness and scalability:
* **Core Language:** Java 17
* **Backend Framework:** Spring Boot (Modular)
* **Frontend:** HTML, Responsive CSS, and JavaScript
* **Database & Cloud:** PostgreSQL hosted on Microsoft Azure
* **Infrastructure & Deployment:** Containerization via Docker
* **Communication:** RESTful APIs (JSON format), fully documented using Swagger / OpenAPI 3.0

The PGU architecture follows the rigorous **4SRS** method and adopts a strict **Layered Architecture**, ensuring low coupling and a clear separation of concerns between the **Interface (Frontend)**, **Application Logic (Backend)**, and **Data Management (Relational Data Lake)**.

### 🛡️ European Compliance & Interoperability
This solution strictly complies with the demands of the National Strategy for Smart Territories (ENTI), ensuring:
* Full compliance with AMA's **Smart Data Models**.
* Native compatibility with the **Orion Context Broker** and the European standard **NGSI-LD** for distributed context sharing.
* System-wide security implemented via **OAuth 2.0 / OpenID Connect** and strong encryption protocols.

### 📂 Repository Structure
* 📁 **`backend/`:** Contains the Spring Boot Java source code, business logic, and REST API controllers.
* 📁 **`frontend/`:** HTML, CSS, and JS files for the interactive user interface.
* 📄 **`docker-compose.yml`:** Configuration file for setting up the containerized environment.
* 📄 **`docs/`:** UML models, 4SRS architecture documentation, and OpenAPI specifications.

---
### 👥 Authors
* Ana Carlota Ferreira Coelho (a108719)
* Ana Lúcia Guerreiro Romão (a111516)
* Diogo Fernandes (a110272)
* Diogo Soucasaux (a107331)
* Gabriel Almeida (a109952)
* Guilherme de Carvalho Monteiro Correia (a105387)
* João Pedro Silva Castro Ribeiro (a111191)
* Luísa Rodrigues Couto (a110905)
* Mariana Silva Gomes (a110943)
* Soraia Sá (a112733)
* Tiago Lima (a108269)
