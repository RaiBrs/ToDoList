<div align="center">

# 📝 ToDoList

### A minimal terminal ToDoList built in Java

<p>
  <img src="https://img.shields.io/badge/Java-11%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 11 or newer">
  <img src="https://img.shields.io/badge/Interface-Terminal-1F2937?style=for-the-badge&logo=windowsterminal&logoColor=white" alt="Terminal interface">
  <img src="https://img.shields.io/badge/Status-Learning%20Project-7C3AED?style=for-the-badge" alt="Learning project">
</p>

<sub>✨ Plan less. Do more.</sub>

</div>

---

<details>
<summary>🗂️ <b>Table of contents</b></summary>

<br>

- [Features](#features)
- [Run locally](#run-locally)
- [Demo account](#demo-account)
- [Commands](#commands)
- [Project structure](#project-structure)
- [Current limitations](#current-limitations)
- [Next steps](#next-steps)

</details>

## ✨ Features

- 🔐 Simple username and password login
- 👤 Username normalization (`Rai.Dev` becomes `@rai.dev`)
- ➕ Add tasks
- ✅ Complete tasks
- 🗑️ Remove tasks
- 👋 Logout and return to the sign-in screen

## 🚀 Run locally

**Requirement:** Java 11 or newer.

### PowerShell

```powershell
javac -d out (Get-ChildItem -Recurse -Filter *.java | ForEach-Object { $_.FullName })
java -cp out com.todolist.Main
```

### macOS / Linux

```bash
javac -d out $(find src -name "*.java")
java -cp out com.todolist.Main
```

## 🔑 Demo account

| Field | Value |
| --- | --- |
| Username | `rai` |
| Password | `1234` |

The application formats the username internally as `@rai`.

## 🎮 Commands

| Command | Action |
| --- | --- |
| `A` | Add a task |
| `C` | Complete a task |
| `R` | Remove a task |
| `L` | Logout |

## 📁 Project structure

```text
src/
└── com/todolist/
    ├── Main.java                 # Terminal interface and application flow
    ├── User.java                 # User data and owned tasks
    ├── Task.java                 # A task and its completion state
    └── repository/
        └── UserRepository.java   # User registration and lookup
```

## ⚠️ Current limitations

- Data is kept only in memory and is lost when the program closes.
- The demo account is temporary.
- Passwords are not ready for persistent storage yet.

## 🧭 Next steps

- Add file-based persistence.
- Replace the demo account with account creation.
- Hash passwords before persisting them.
- Add automated tests.

---

Built with ☕ Java and a little momentum.
