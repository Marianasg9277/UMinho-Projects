# Sistema de Gestão de Encomendas 📦

Academic project developed in **C** for the **Algoritmos e Estruturas de Dados 2 (AED2)** course at the University of Minho (2024/2025).

### 📋 Description
A command-line interface (CLI) application built to manage a parcel delivery system for an online store. The system simulates a real delivery workflow, handling the creation of pending parcels, moving them to a dispatch queue, and finally sending them to a history log.

### ✨ Key Features
* **Order Management:** Add new pending parcels with automatic unique ID generation and rigorous input validation (including leap year checks for YYYYMMDD format).
* **Dispatch Process:** Move pending orders into a dispatch queue maintaining arrival order, and process them for final delivery.
* **History & Advanced Search:** Store all dispatched parcels in a history log with search and filter capabilities by ID range, date range, or client name.
* **Interactive CLI:** An intuitive, menu-driven interface with clear options and contextual messages.

### 🛠️ Technical Implementation
* **Language:** C
* **Data Persistence:** Uses `.txt` files (`EncomendasPendentes.txt`, `EncomendasExpedidas.txt`, `Historico.txt`) to automatically save and update the system's state after every operation.
* **Dynamic Data Structures:** Implements Linked Lists (pending parcels), Queues/FIFO (parcels waiting for dispatch), and Stacks/LIFO (history of dispatched parcels).
* **Custom Structs:** Uses `Encomenda`, `EncomendaPendente`, `EncomendaExpedida`, and `Historico` to encapsulate and manage data efficiently in memory.

### 📂 Repository Structure
* 📁 **`src/`:** Main folder containing the project's source code and text databases.
  * 📄 **`main.c`:** Core logic for the delivery workflow, including the implementation of the dynamic data structures and CLI menus.
  * 📄 **`EncomendasPendentes.txt`:** Automatically saves the current state of all pending parcels.
  * 📄 **`EncomendasExpedidas.txt`:** Saves the state of parcels that have been moved to the dispatch queue.
  * 📄 **`Historico.txt`:** Persistent log containing the history of all successfully delivered orders.
* 📄 **`Algoritmos_e_Estruturas_de_Dados...`:** Detailed project assignment and documentation. *(Nota: verifica e ajusta aqui o nome completo do ficheiro que está cortado na imagem)*

---
### 👥 Authors
* Ana Íris Machado Torres (a110489)
* Francisca da Silva Fernandes de Sousa Carvalho (a112190)
* Mariana Silva Gomes (a110943)
* Matilde Pires Cunha (a111787)
