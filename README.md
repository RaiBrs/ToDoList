<div align="center">

# 📝 ToDoList

### A minimal terminal ToDoList built in Java

<p>
  <img src="https://img.shields.io/badge/Java-25-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25">
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
- [Tests](#tests)
- [Local storage](#local-storage)
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
- 💾 Automatically save tasks in a local JSON file
- 👋 Logout and return to the sign-in screen
- 🧪 JUnit 6 tests for the domain and JSON storage

## 🚀 Run locally

**Requirements:** Java 25 and Maven.

### Maven

```bash
mvn compile exec:java -Dexec.mainClass=com.todolist.Main
```

## 🧪 Tests

Tests use JUnit 6 and are organized by class in `com.todolist.tests`.

```bash
mvn test
```

## 💾 Local storage

Tasks are saved automatically after adding, completing, or removing a task.

```text
data/tasks.json
```

The JSON file is ignored by Git, so each user keeps local task data outside the repository.

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
├── main/
│   └── java/
│       └── com/todolist/
│           ├── Main.java                 # Terminal interface and application flow
│           ├── User.java                 # User data and owned tasks
│           ├── Task.java                 # A task and its completion state
│           └── repository/
│               ├── UserRepository.java   # User registration and lookup
│               ├── TaskStorage.java      # Task persistence contract
│               └── JsonTaskStorage.java  # JSON file implementation
└── test/
    └── java/
        └── com/todolist/tests/            # Domain and storage tests
```

## ⚠️ Current limitations

- Only tasks are persisted; accounts remain in memory.
- The demo account is temporary.
- Passwords are stored in memory as plain text and must not be persisted yet.

## 🧭 Next steps

- Replace the demo account with account creation.
- Hash passwords before persisting them.
- Handle malformed JSON files gracefully.
- Expand test coverage.

---

Built with ☕ Java and a little momentum.
