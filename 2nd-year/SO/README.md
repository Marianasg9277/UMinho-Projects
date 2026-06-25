# Client-Server System for Remote Execution 🐧

Academic project developed in **C** for the **Sistemas Operativos (SO)** (Operating Systems) course at the University of Minho (2025/2026).

### 📋 Description
A robust distributed system based on a Producer-Consumer architecture, designed for the remote execution of non-interactive tasks in a Linux environment. The system utilizes a single executable file that dynamically acts as either a Client or a Server, depending on the arguments passed via the terminal.

### ✨ Technical Implementation & Key Features
Developed under strict pedagogical constraints, this project focuses heavily on low-level Linux Kernel interactions, intentionally avoiding high-level standard library functions (like `system()`, `popen()`, `printf()`, or `sprintf()`).

* **Inter-Process Communication (IPC):** Uses *Named Pipes (FIFOs)* for reliable, unidirectional data exchange between independent client and server processes.
* **Low-Level Process Management:** Full control over the process lifecycle (creation, execution, and synchronization) utilizing POSIX system calls such as `fork()`, `execvp()`, and `waitpid()`.
* **Memory Optimization:** Implements "In-Place" parsing. The server manually parses commands by modifying the original string directly in memory (replacing spaces with `\0`), avoiding unnecessary memory allocation.
* **Persistent & Atomic Logging:** Securely records the execution history and *exit status* of every command into a `servidor.log` file using `O_WRONLY | O_CREAT | O_APPEND` flags.
* **Manual Data Conversion:** Features a custom, low-level integer-to-string (ASCII) conversion algorithm using modular arithmetic, strictly adhering to the prohibition of standard formatting functions.

### 🚀 Quick Start
The system requires two separate terminal instances to operate (one for the server and one for the client).

Compile the source code:
`gcc projeto.c -o projeto`

1. **Start the Server:** `./projeto servidor`
2. **Execute a Command (Client):** `./projeto ls -lisa`
3. **Check Logs:** `cat servidor.log`

### 📂 Repository Structure
* 📄 **[`projeto.c`](./projeto.c):** Main source code containing both Client and Server logic, IPC mechanisms, and manual string manipulation functions.

---
### 👥 Authors (Group 24)
* Gabriela Sousa e Santos (A104359)
* Mariana Silva Gomes (A110943)
* Matilde Pires Cunha (A111787)
