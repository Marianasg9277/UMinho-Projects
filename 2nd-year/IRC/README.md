# Computer Networks Introduction (IRC) 🌐

Academic repository containing the practical assignments, reports, and network simulation files developed for the **Introdução às Redes de Computadores (IRC)** course at the University of Minho (2025/2026).

### 📋 Description
This project explores the fundamental concepts of computer networking, following the OSI and TCP/IP models. Through practical simulations using the CORE Network Emulator and traffic analysis with Wireshark, the assignments cover everything from basic physical topologies to application-layer protocols.

### 📊 Course Modules & Assignments
* **Module 1: Tools & Fundamentals:** Introduction to the CORE emulator and Wireshark. Creation of virtual topologies, protocol encapsulation analysis, bandwidth measurement (`iperf3`), and RTT analysis (ICMP/ping).
* **Module 2: Data Link Layer & LANs:** Exploration of Ethernet, physical addressing (MAC), collision/broadcast domains, ARP protocol mapping, and traffic forwarding differences between Hubs and Switches.
* **Module 3: Network Layer:** IPv4 addressing, VLSM (Variable Length Subnet Masking), static routing configuration, route aggregation (supernets), and packet analysis of the 4-step DHCP process.
* **Module 4: Transport & Application Layers:** Comparative analysis of TCP and UDP. Observation of the TCP three-way handshake, DNS resolution, and the impact of network anomalies (latency/packet loss) on application protocols like SFTP, FTP, TFTP, and HTTP.

### 🛠️ Tools & Technologies
* **Emulation:** CORE Network Emulator (v9.0.3) via Docker.
* **Traffic Analysis:** Wireshark and `tcpdump`.
* **Environment:** Linux (Ubuntu) and Bash shell for network service administration.

### 📂 Repository Structure
* 📄 **Assignment PDFs:** Step-by-step guides and theoretical formulations for each module.
* 📄 **Reports:** Detailed documents containing answers, traffic analysis, and conclusions for each assignment.
* 📁 **`shared/`:** Directory synchronized with the Linux container housing the simulation artifacts:
  * 📄 **`.xml` files:** Virtual network topologies created in the CORE emulator.
  * 📄 **`.pcap` / `.pcapng` files:** Raw network traffic captures extracted via Wireshark/tcpdump for offline packet inspection.
  * 📄 **`.log` files:** Console outputs from emulated nodes (e.g., `iperf3` results, routing tables, ARP caches).

---
### 👥 Authors 
* Ana Íris Machado Torres (a110489)
* Maria Nair da Cunha e Silva Lacerda Ângelo (a111488)
* Mariana Silva Gomes (a110943)
