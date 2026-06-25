# Gestão de Parque de Estacionamento (GPE) 🚗

Academic project developed in **C** for the **Algoritmos e Estruturas de Dados 1 (AED1)** course at the University of Minho (2024/2025). 

### 📋 Description
A command-line interface (CLI) application built to manage a multi-story parking lot. The system handles vehicle entries and exits, customer registrations, and dynamic fee calculations while supporting different vehicle types (2-wheelers, light, and large light vehicles).

### ✨ Key Features
* **Configuration & Pricing:** Dynamic setup of floors, parking spots, and time-based parking fees.
* **Access Control:** Vehicle entry and exit registration with automatic or manual spot allocation.
* **Customer Management:** System to add, edit, and remove clients, linking up to 5 license plates per user.
* **Real-time Monitoring:** Visual matrix of the parking lot showing available/occupied spots and tracking parked time per vehicle.

### 🛠️ Technical Implementation
* **Language:** C
* **Data Persistence:** Uses `.csv` files (`config_parque.csv` and `clientes.csv`) to save the system's state between executions.
* **Data Structures:** Implementation of custom `structs` (`Lugar`, `Parque`, `Cliente`) to encapsulate and manage data efficiently in memory.

---
### 👥 Authors
* Gabriela Sousa e Santos (a104359)
* Maria Nair da Cunha e Silva Lacerda Ângelo (a111488)
* Mariana Silva Gomes (a110943)
