# NanoBase
> A lightweight, file-persistent database engine built from scratch using **Core Java only** — no external libraries, no frameworks.

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=java)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen?style=flat-square)
![License](https://img.shields.io/badge/License-MIT-blue?style=flat-square)
![Type](https://img.shields.io/badge/Type-Academic%20Project-purple?style=flat-square)

---

## 📌 What is this?

Most developers **use** databases like MySQL or PostgreSQL.  
This project **builds** one from scratch.

Mini Database Engine is a fully functional SQL-like database engine that:
- Accepts SQL commands from a terminal (REPL interface)
- Parses and executes `CREATE`, `INSERT`, `SELECT`, `UPDATE`, `DELETE` queries
- Indexes data using a custom **B-Tree** implementation for O(log n) lookups
- Persists all data to disk as `.tbl` files using Java Serialization
- Survives program restarts — data is never lost

---

## 🎯 Why I Built This

> *"Using MySQL is driving a car. Building this is understanding how the engine works."*

This project was built to deeply understand what happens **underneath** a database:
- How SQL strings are tokenized and parsed
- How B-Trees provide fast indexed lookups
- How data is stored and retrieved from disk
- How query execution engines route and process operations

---

## ✨ Features

| Feature | Description |
|---|---|
| **SQL Parser** | Tokenizer + Parser converts raw SQL into structured Query objects |
| **B-Tree Index** | Custom B-Tree on primary key for O(log n) search |
| **File Persistence** | Data saved to `.tbl` binary files — survives restarts |
| **WHERE Clauses** | Supports `=` `!=` `>` `<` `>=` `<=` operators |
| **AND / OR** | Chained conditions in WHERE clause |
| **Pretty Output** | Results displayed as formatted ASCII tables |
| **Error Handling** | Custom `DBException` with meaningful error types |
| **REPL Interface** | Interactive terminal — type SQL, see results instantly |

---

## 🚀 Supported SQL Commands

```sql
-- Create a table
CREATE TABLE students (id, name, age, gpa)

-- Insert rows
INSERT INTO students VALUES (1, Alice, 20, 9.1)

-- Select all rows
SELECT * FROM students

-- Select specific columns
SELECT name, gpa FROM students

-- Filter with WHERE
SELECT * FROM students WHERE age > 19

-- Chained conditions
SELECT * FROM students WHERE age > 18 AND gpa > 8.5

-- Update rows
UPDATE students SET gpa = 9.9 WHERE id = 2

-- Delete rows
DELETE FROM students WHERE age < 20

-- Drop a table
DROP TABLE students

-- Show all tables
SHOW TABLES
```

---

## 🏗️ Project Structure

```
MiniDBEngine/
│
├── src/
│   ├── main/
│   │   └── Main.java                  ← Entry point, REPL loop
│   │
│   ├── lexer/
│   │   ├── Token.java                 ← Token types and data holder
│   │   └── Tokenizer.java             ← SQL string → List of Tokens
│   │
│   ├── parser/
│   │   ├── Query.java                 ← Parsed query data holder
│   │   └── QueryParser.java           ← Tokens → Query object
│   │
│   ├── engine/
│   │   ├── DatabaseEngine.java        ← Core controller, query executor
│   │   ├── Table.java                 ← Table schema + row operations
│   │   └── Row.java                   ← Single row of data
│   │
│   ├── index/
│   │   ├── BTree.java                 ← Full B-Tree implementation
│   │   └── BTreeNode.java             ← B-Tree node structure
│   │
│   ├── storage/
│   │   └── StorageManager.java        ← File-based persistence (.tbl files)
│   │
│   └── exception/
│       └── DBException.java           ← Custom exception with error types
│
└── data/                              ← Auto-created, stores .tbl files
```

---

## ⚙️ How It Works — Architecture

```
You type SQL
     │
     ▼
┌─────────────┐
│ Tokenizer   │  "SELECT * FROM students" → [SELECT][*][FROM][students][EOF]
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ QueryParser │  Tokens → Query{type=SELECT, table=students}
└──────┬──────┘
       │
       ▼
┌──────────────────┐
│ DatabaseEngine   │  Routes to correct handler method
└──────┬───────────┘
       │
       ▼
┌─────────────┐     ┌─────────────┐
│  Table      │────▶│   BTree     │  O(log n) primary key lookup
└──────┬──────┘     └─────────────┘
       │
       ▼
┌─────────────────┐
│ StorageManager  │  Reads/writes .tbl files on disk
└─────────────────┘
```

---

## 🧠 Core Java Concepts Used

| Concept | Where Used |
|---|---|
| **B-Tree (Self-balancing tree)** | `BTree.java` — Primary key indexing |
| **File I/O & Serialization** | `StorageManager.java` — Disk persistence |
| **Collections (HashMap, LinkedHashMap, ArrayList)** | Throughout all layers |
| **String Parsing & Tokenization** | `Tokenizer.java` — Lexical analysis |
| **Recursion** | `BTree.java` search/insert, `QueryParser.java` condition parsing |
| **OOP & Layered Architecture** | Clean separation across 5 layers |
| **Custom Exceptions** | `DBException.java` — Typed error handling |
| **Generics** | B-Tree and Collections usage |
| **Enums** | `TokenType`, `QueryType`, `ErrorType` |
| **Switch Expressions (Java 14+)** | Query routing, condition evaluation |

---

## 🔧 How to Run

### Prerequisites
- Java 14 or higher installed
- IntelliJ IDEA (recommended) or any Java IDE

### Option 1 — IntelliJ IDEA
```
1. Open project in IntelliJ
2. Right-click src/ → Mark Directory As → Sources Root
3. Create a data/ folder at project root
4. Run → Edit Configurations → Add Application → Main class: main.Main
5. Set Working Directory to project root
6. Press Shift + F10
```

### Option 2 — Command Line
```bash
# Navigate to project root
cd path/to/MiniDBEngine

# Create output directory
mkdir out

# Compile all files
javac -d out src/exception/DBException.java src/lexer/Token.java src/lexer/Tokenizer.java src/parser/Query.java src/parser/QueryParser.java src/engine/Row.java src/index/BTreeNode.java src/index/BTree.java src/engine/Table.java src/storage/StorageManager.java src/engine/DatabaseEngine.java src/main/Main.java

# Run
java -cp out main.Main
```

---

## 💡 Sample Session

```
╔══════════════════════════════════════════╗
║       Mini Database Engine v1.0          ║
║       Built with Core Java (MCA)         ║
╚══════════════════════════════════════════╝

MiniDB> CREATE TABLE students (id, name, age, gpa)
✓ Table 'students' created with columns: [id, name, age, gpa]

MiniDB> INSERT INTO students VALUES (1, Alice, 20, 9.1)
✓ 1 row inserted into 'students'.

MiniDB> INSERT INTO students VALUES (2, Bob, 22, 8.5)
✓ 1 row inserted into 'students'.

MiniDB> INSERT INTO students VALUES (3, Charlie, 19, 9.7)
✓ 1 row inserted into 'students'.

MiniDB> SELECT * FROM students
+----+---------+-----+-----+
| id | name    | age | gpa |
+----+---------+-----+-----+
| 1  | Alice   | 20  | 9.1 |
| 2  | Bob     | 22  | 8.5 |
| 3  | Charlie | 19  | 9.7 |
+----+---------+-----+-----+
3 row(s) returned.

MiniDB> SELECT * FROM students WHERE gpa > 9.0
+----+---------+-----+-----+
| id | name    | age | gpa |
+----+---------+-----+-----+
| 1  | Alice   | 20  | 9.1 |
| 3  | Charlie | 19  | 9.7 |
+----+---------+-----+-----+
2 row(s) returned.

MiniDB> UPDATE students SET gpa = 9.9 WHERE id = 2
✓ 1 row(s) updated in 'students'.

MiniDB> DELETE FROM students WHERE age < 20
✓ 1 row(s) deleted from 'students'.
```

---

## 📊 Complexity Analysis

| Operation | Time Complexity | How |
|---|---|---|
| INSERT | O(log n) | B-Tree insert |
| SELECT by primary key | O(log n) | B-Tree search |
| SELECT with WHERE | O(n) | Full table scan |
| UPDATE | O(n) | Full table scan + B-Tree update |
| DELETE | O(n) | Full table scan + B-Tree rebuild |

---

## 🔮 Future Improvements

- [ ] JOIN support (INNER JOIN between two tables)
- [ ] ORDER BY and LIMIT clauses  
- [ ] Secondary indexes (index on non-primary key columns)
- [ ] Transaction support (COMMIT / ROLLBACK)
- [ ] Multi-threaded query execution
- [ ] Query optimization (cost-based planner)

---

## 👨‍💻 Author

**Chirag Gupta**  
MCA Student  

---

## 📄 License

This project is open source and available under the [MIT License](LICENSE).
