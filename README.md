# MSCS 632 – Assignment 6: Data Processing System (Java + Go)

This repository contains the implementation for Assignment 6 of **MSCS 632 – Advanced Programming Languages**.
The project demonstrates multi-threaded and concurrent data processing using **Java threads** and **Go goroutines** with:
- a shared task queue,
- multiple workers,
- poison pill termination,
- synchronized result storage,
- and file output.

---

## Repository Structure

```text
MSCS 632 Assignment 6 Data Processing/
│
├── java/
│   ├── src/
│   │   └── main/java/edu/ucumberlands/dps/
│   │       ├── Main.java
│   │       ├── Worker.java
│   │       ├── Task.java
│   │       └── SharedTaskQueue.java
│   └── java_results.txt
│
├── go/
│   ├── main.go
│   └── go_results.txt
│
└── README.md
