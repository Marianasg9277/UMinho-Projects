# Scientific Laboratory & Project Management System 🔬

Academic project developed in **Java** for the **Programação com Objetos (PO)** (Object-Oriented Programming) course at the University of Minho (2025/2026).

### 📋 Description
An integrated system built in **Java** for the administrative and operational management of a scientific laboratory network focusing on Biotechnology, Robotics, and Renewable Energy. The application facilitates the seamless management of researchers, laboratories, coordinators, projects, and activities, enabling continuous tracking of research progress.

### 🛠️ Architecture & Technical Implementation
* **OOP Principles:** Extensive application of Inheritance, Polymorphism, and Encapsulation.
* **Modular Architecture:** Strictly divided into two main packages: `BackEnd` (handling business logic, domain models, and a dedicated `BackEnd.exceptions` sub-package) and `FrontEnd` (managing the interactive Command-Line Interface).
* **Data Persistence:** System state preservation via binary serialization (`sistema.dat`), ensuring complete retention of object associations and data across sessions.

### ✨ Key Features & User Roles
The system features role-based access control with distinct interactive menus and permissions:
* **Administrator:** Global management of laboratories, user accounts, and system-wide statistical monitoring.
* **Coordinator:** Project creation, team allocation (enforcing scientific area constraints), project state tracking, and activity planning.
* **Researcher:** Subdivided into three specializations (Biotechnology, Robotics, and Energy). Researchers can view their assigned projects, log actual activity execution times, and analyze personal performance statistics.

### 🚀 Advanced Features (Extra Implementation)
To bridge the gap between academic requirements and real-world business logic, the project includes several robust advanced features:
* **Security & Authentication:** Login tracking with automatic account lockout after 3 failed attempts, requiring an administrator unlock.
* **Audit & History Logs:** Immutable global logging of all sensitive actions to audit state changes and critical operations.
* **Gamification & Rewards:** Automatic calculation and assignment of productivity bonuses, meal vouchers, team awards, and tech gifts based on overtime and completed activities.
* **Robust Data Validation:** Uniqueness constraints on IDs, emails, and usernames, backed by a custom exception hierarchy (e.g., `EntidadeDuplicadaException`, `RegraNegocioException`).
* **Labor Constraints:** Strict enforcement of a 60-hour weekly work limit per researcher, with salary calculations based exclusively on validated and completed activities.
* **Referential Integrity:** Automatic handling of complex dependencies (e.g., deleting a laboratory dynamically forces the transfer of its staff to another operational unit).

### ⚙️ Quick Start

1. Ensure **Java (JDK)** is installed on your system.
2. Clone this repository:
   ```bash
   git clone [https://github.com/YOUR-USERNAME/YOUR-REPO-NAME.git](https://github.com/YOUR-USERNAME/YOUR-REPO-NAME.git)
   ```
3. Compile the `.java` files from both the `BackEnd` and `FrontEnd` packages.
4. Execute the main application by running the `Main` class located in the `FrontEnd` package.

### 📂 Repository Structure
* 📁 **`BackEnd/`:** Core business logic, domain entities, and custom exceptions.
* 📁 **`FrontEnd/`:** Command-Line Interface (CLI) menus and user interaction logic.
* 📄 **`sistema.dat`:** Binary persistence file (generated automatically after the first run).

---
### 👥 Authors 
* Francisca da Silva Fernandes de Sousa Carvalho (a112190)
* Gabriela Sousa e Santos (a104359)
* Mariana Silva Gomes (a110943)
