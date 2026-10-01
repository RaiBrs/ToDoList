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
- [Menus](#menus)
- [Project structure](#project-structure)
- [Current limitations](#current-limitations)
- [Next steps](#next-steps)

</details>

<a id="features"></a>
## ✨ Features

- 🔐 Simple username and password login
- 🆕 Create a personal account
- 👤 Username normalization (`Rai.Dev` becomes `@rai.dev`)
- 🔑 Change the password while signed in
- 🔁 Resume the last account after restarting the application
- ✅ Require at least 8 characters and reject passwords containing the username or matching a short common-password list
- ➕ Add tasks
- ✅ Complete tasks
- ✏️ Edit task titles
- 🗑️ Remove tasks
- 💾 Save accounts and per-user task lists in local JSON files
- 🔒 Store password hashes instead of plain-text passwords
- 👋 Logout and return to the account menu
- 🧪 JUnit 6 tests for domain logic, JSON storage, and console flows

<a id="run-locally"></a>
## 🚀 Run locally

**Requirements:** Java 25 and Maven.

### Maven

```bash
mvn compile exec:java -Dexec.mainClass=com.raibrs.todolist.App
```

<a id="tests"></a>
## 🧪 Tests

Tests use JUnit 6 and mirror the packages they validate.

```bash
mvn test
```

<a id="local-storage"></a>
## 💾 Local storage

Tasks are saved automatically after adding, completing, or removing a task.
Accounts are saved when created or when their password changes. Each task list is keyed by the user's normalized username.

```text
data/tasks.json
data/users.json
data/session.txt
```

These local files are ignored by Git. The session file stores only the username; choosing Logout deletes it.

<a id="commands"></a>
## 🎮 Menus

### Account menu

| Option | Action |
| --- | --- |
| `L` | Sign in |
| `N` | Create an account |
| `Q` | Quit |

### Task menu

| Command | Action |
| --- | --- |
| `A` | Add a task |
| `E` | Edit a task title |
| `C` | Complete a task |
| `R` | Remove a task |
| `P` | Change the password |
| `L` | Logout |
| `Q` | Quit and keep the saved session |

To change a password, enter the current password, then the new password twice. The new password must contain at least 8 characters, must not include the username, and must not exactly match one of the common passwords blocked by the application.

Choose Logout to clear the saved session and return to the account menu. Choose Quit and keep the saved session to close the application and resume this account next time.

<a id="project-structure"></a>
## 📁 Project structure

```text
src/
├── main/
│   ├── java/
│   │   └── com/raibrs/todolist/
│   │       ├── App.java                  # Application composition and startup
│   │       ├── model/
│   │       │   ├── User.java             # User data and owned tasks
│   │       │   └── Task.java             # A task and its completion state
│   │       ├── repository/
│   │       │   ├── UserRepository.java   # User registration and lookup
│   │       │   ├── UserStorage.java      # Account persistence contract
│   │       │   ├── JsonUserStorage.java  # JSON account storage
│   │       │   ├── TaskStorage.java      # Task persistence contract
│   │       │   ├── JsonTaskStorage.java  # JSON file implementation
│   │       │   └── SessionStorage.java   # Remembered local username
│   │       ├── security/
│   │       │   ├── PasswordPolicy.java   # Rules for new passwords
│   │       │   └── PasswordHasher.java   # Salted password hashing and verification
│   │       ├── service/
│   │       │   └── TaskService.java      # Task actions and persistence
│   │       └── ui/
│   │           └── ConsoleUi.java        # Terminal input and output
│   └── resources/                         # Future application resources
└── test/
    └── java/
        └── com/raibrs/todolist/
            ├── model/                     # Domain tests
            ├── repository/                # Persistence tests
            ├── security/                  # Password hashing tests
            ├── service/                   # Task workflow tests
            └── ui/                        # Console interaction tests
```

<a id="current-limitations"></a>
## ⚠️ Current limitations

- Local account and task JSON files are not encrypted.
- The saved session skips the login prompt for this local copy of the app, so use it only on a trusted computer.
- There is no password reset flow.

<a id="next-steps"></a>
## 🧭 Next steps

- Add filtering for completed and pending tasks.
- Consider a database if the application grows beyond local use.

---

Built with ☕ Java and a little momentum.
