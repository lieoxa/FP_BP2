# Student CRUD Database - Java Console App

## Overview
Console-based CRUD application for managing students (ID, name, age, grade) using SQLite database.

## Setup
1. Download SQLite JDBC: [sqlite-jdbc-3.46.1.jar](https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1/sqlite-jdbc-3.46.1.jar) → `lib/`
2. Open in VS Code (Java extension auto-detects).

## Run
In terminal (from project root):
```
javac -cp "lib/*;src" src/*.java
java -cp "bin;lib/*" App
```

Or use VS Code Run/Debug (F5).

## Features
- 1: Create student
- 2: List all
- 3: Update by ID
- 4: Delete by ID
- 5: Exit

Data persisted in `students.db`.

## Structure
- `src/App.java`: Main menu/UI
- `src/DatabaseManager.java`: JDBC CRUD ops
- `lib/sqlite-jdbc-*.jar`: DB driver
- `students.db`: SQLite DB (auto-created)
