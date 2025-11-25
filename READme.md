# Distributed Research Database System

A JavaFX application demonstrating **horizontal fragmentation** across two MS SQL Server instances using Docker containers.

---

## Table of Contents

1. [Overview](#overview)
2. [Horizontal Fragmentation Explained](#horizontal-fragmentation-explained)
3. [Database Schema](#database-schema)
4. [Prerequisites](#prerequisites)
5. [Quick Start](#quick-start)
6. [Project Structure](#project-structure)
7. [Application Features](#application-features)
8. [Transparency Levels](#transparency-levels)
9. [Troubleshooting](#troubleshooting)

---

## Overview

This project implements a **distributed database system** that manages research groups, employees, projects, and participation records. Data is horizontally fragmented (split) across two SQL Server containers based on department affiliation (P1 or P2).

```
┌─────────────────────────────────────────────────────────────────┐
│                    JavaFX Application                            │
│              (Fragment & Location Transparency)                  │
└─────────────────────┬───────────────────────────┬───────────────┘
                      │                           │
                      ▼                           ▼
         ┌────────────────────┐       ┌────────────────────┐
         │   SQL Server P1    │       │   SQL Server P2    │
         │   (Port 14331)     │       │   (Port 14332)     │
         │                    │       │                    │
         │  Department: P1    │       │  Department: P2    │
         │  Groups: NC01,NC02 │       │  Groups: NC03,NC04 │
         │  Employees: NV01-03│       │  Employees: NV04-06│
         └────────────────────┘       └────────────────────┘
```

---

## Horizontal Fragmentation Explained

### What is Horizontal Fragmentation?

**Horizontal fragmentation** divides a table into subsets of rows based on a condition. Each fragment contains complete rows but only a subset of all rows.

```
Original Table (nhomnc - Research Groups)
┌──────────┬─────────────────────────┬──────────┐
│ manhomnc │ tennhomnc               │ tenphong │
├──────────┼─────────────────────────┼──────────┤
│ NC01     │ Research Group Alpha    │ P1       │
│ NC02     │ AI Research Lab         │ P1       │
│ NC03     │ Software Engineering    │ P2       │
│ NC04     │ Database Research Lab   │ P2       │
└──────────┴─────────────────────────┴──────────┘
                    │
        ┌───────────┴───────────┐
        ▼                       ▼
┌─────────────────┐     ┌─────────────────┐
│  Fragment P1    │     │  Fragment P2    │
│  (tenphong=P1)  │     │  (tenphong=P2)  │
├─────────────────┤     ├─────────────────┤
│ NC01 │ P1       │     │ NC03 │ P2       │
│ NC02 │ P1       │     │ NC04 │ P2       │
└─────────────────┘     └─────────────────┘
```

### Fragmentation Strategy in This Project

| Table      | Fragmentation Type | Rule                                      |
|------------|-------------------|-------------------------------------------|
| `nhomnc`   | Primary           | Based on `tenphong = 'P1'` or `'P2'`      |
| `nhanvien` | Derived           | Follows `nhomnc` via `manhomnc` foreign key |
| `dean`     | Derived           | Follows `nhomnc` via `manhomnc` foreign key |
| `thamgia`  | Derived           | Follows `nhanvien` via `manv` foreign key  |

### Why Use Horizontal Fragmentation?

1. **Performance**: Queries targeting specific departments only access relevant fragments
2. **Scalability**: Each fragment can be on a different server
3. **Data Locality**: Data is stored closer to where it's used
4. **Parallel Processing**: Queries can run in parallel across fragments

---

## Database Schema

### Global Schema (Logical View)

```sql
-- Research Groups (nhóm nghiên cứu)
nhomnc (manhomnc, tennhomnc, tenphong)

-- Employees (nhân viên)
nhanvien (manv, hoten, manhomnc)  -- FK: manhomnc → nhomnc

-- Projects (đề án)
dean (mada, tenda, manhomnc)      -- FK: manhomnc → nhomnc

-- Participation (tham gia)
thamgia (manv, mada)              -- FK: manv → nhanvien, mada → dean
```

### Physical Schema (Fragmented)

**Server P1 (localhost:14331) - ResearchDB_P1:**
- `nhomnc_p1` - Groups where tenphong = 'P1'
- `nhanvien_p1` - Employees in P1 groups
- `dean_p1` - Projects owned by P1 groups
- `thamgia_p1` - Participations by P1 employees

**Server P2 (localhost:14332) - ResearchDB_P2:**
- `nhomnc_p2` - Groups where tenphong = 'P2'
- `nhanvien_p2` - Employees in P2 groups
- `dean_p2` - Projects owned by P2 groups
- `thamgia_p2` - Participations by P2 employees

---

## Prerequisites

### Required Software

```bash
# Arch Linux
sudo pacman -S docker docker-compose jdk17-openjdk maven

# Ubuntu/Debian
sudo apt install docker.io docker-compose openjdk-17-jdk maven

# Fedora
sudo dnf install docker docker-compose java-17-openjdk maven
```

### Docker Setup

```bash
# Start Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Add user to docker group (to run without sudo)
sudo usermod -aG docker $USER
newgrp docker

# Verify installation
docker --version
docker-compose --version
```

---

## Quick Start

### 1. Clone and Navigate to Project

```bash
cd ~/Study/distributed-database
```

### 2. Start SQL Server Containers

```bash
docker-compose up -d
```

Wait ~30 seconds for SQL Servers to fully start.

### 3. Initialize Databases

```bash
./init_database.sh
```

This creates:
- `ResearchDB_P1` on port 14331 with P1 fragment tables
- `ResearchDB_P2` on port 14332 with P2 fragment tables
- Sample data for testing

### 4. Run the JavaFX Application

```bash
mvn javafx:run
```

### 5. (Optional) Verify Database Data

```bash
# Check P1 data
docker exec research_db_p1 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'YourStrong@Pass123' -C \
  -d ResearchDB_P1 -Q "SELECT * FROM nhomnc_p1"

# Check P2 data
docker exec research_db_p2 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'YourStrong@Pass123' -C \
  -d ResearchDB_P2 -Q "SELECT * FROM nhomnc_p2"
```

---

## Project Structure

```
distributed-database/
├── docker-compose.yml          # Docker configuration for 2 SQL Servers
├── init_database.sh            # Shell script to initialize databases
├── pom.xml                     # Maven configuration
│
├── src/main/java/com/research/distributed/
│   ├── MainApplication.java    # JavaFX entry point
│   │
│   ├── config/
│   │   ├── DatabaseConfig.java       # Database connection settings
│   │   └── FragmentationConfig.java  # Fragment mapping rules
│   │
│   ├── connection/
│   │   ├── ConnectionPool.java           # HikariCP connection pool
│   │   ├── FragmentConnectionManager.java # Manages connections to fragments
│   │   └── TransparencyLevel.java        # Enum for transparency levels
│   │
│   ├── model/
│   │   ├── NhomNC.java    # Research group entity
│   │   ├── NhanVien.java  # Employee entity
│   │   ├── DeAn.java      # Project entity
│   │   └── ThamGia.java   # Participation entity
│   │
│   ├── dao/
│   │   ├── BaseDAO.java      # Abstract base DAO
│   │   ├── NhomNCDAO.java    # Research group data access
│   │   ├── NhanVienDAO.java  # Employee data access
│   │   ├── DeAnDAO.java      # Project data access
│   │   └── ThamGiaDAO.java   # Participation data access
│   │
│   ├── service/
│   │   ├── QueryService.java  # Complex queries with transparency
│   │   └── CRUDService.java   # CRUD operations
│   │
│   ├── controller/
│   │   ├── MainController.java  # Main view controller
│   │   └── CRUDController.java  # CRUD view controller
│   │
│   └── exception/
│       ├── DatabaseException.java
│       ├── FragmentException.java
│       └── ValidationException.java
│
└── src/main/resources/
    ├── fxml/
    │   ├── MainView.fxml   # Main application view
    │   └── CRUDView.fxml   # CRUD management view
    ├── css/
    │   └── pastel-theme.css  # Pastel color theme
    ├── application.properties  # App configuration
    └── logback.xml            # Logging configuration
```

---

## Application Features

### 1. Query Operations (3 predefined queries)

| Query | Description |
|-------|-------------|
| **Query 1** | Find projects with external participants (employees from other groups) |
| **Query 2** | Update department for a research group |
| **Query 3** | Find projects without any participants |

### 2. CRUD Operations

Full Create, Read, Update, Delete for:
- Research Groups (nhomnc)
- Employees (nhanvien)
- Projects (dean)
- Participations (thamgia)

### 3. Transparency Level Selection

Switch between Fragment Transparency and Location Transparency via radio buttons.

---

## Transparency Levels

### Level 1: Fragment Transparency

**User knows about fragments but not their locations.**

```java
// User specifies which fragment to query
List<DeAn> results = queryService.getProjectsWithExternalParticipantsLevel1("NC01");
// Internally queries: dean_p1, thamgia_p1, nhanvien_p1 on P1 server
```

The application:
- Determines the correct fragment based on the group ID
- Queries only the relevant fragment
- User doesn't need to know server addresses

### Level 2: Location Transparency

**User doesn't know about fragments at all.**

```java
// User queries without knowing about fragmentation
List<DeAn> results = queryService.getProjectsWithExternalParticipantsLevel2("NC01");
// Internally queries ALL fragments and merges results
```

The application:
- Queries all fragments automatically
- Merges results from all servers
- User sees a unified view of data

### Comparison

| Aspect | Level 1 (Fragment) | Level 2 (Location) |
|--------|-------------------|-------------------|
| User knowledge | Knows fragments exist | No fragment knowledge |
| Query scope | Single fragment | All fragments |
| Performance | Faster (targeted) | Slower (scans all) |
| Use case | Department-specific queries | Global queries |

---

## Sample Data

### P1 Fragment (ResearchDB_P1)

**Groups:**
| manhomnc | tennhomnc | tenphong |
|----------|-----------|----------|
| NC01 | Research Group Alpha | P1 |
| NC02 | AI Research Lab | P1 |

**Employees:**
| manv | hoten | manhomnc |
|------|-------|----------|
| NV01 | Nguyen Van A | NC01 |
| NV02 | Tran Thi B | NC01 |
| NV03 | Le Van C | NC02 |

### P2 Fragment (ResearchDB_P2)

**Groups:**
| manhomnc | tennhomnc | tenphong |
|----------|-----------|----------|
| NC03 | Software Engineering Group | P2 |
| NC04 | Database Research Lab | P2 |

**Employees:**
| manv | hoten | manhomnc |
|------|-------|----------|
| NV04 | Pham Van D | NC03 |
| NV05 | Hoang Thi E | NC03 |
| NV06 | Vu Van F | NC04 |

---

## Troubleshooting

### Container Issues

```bash
# Check container status
docker-compose ps

# View container logs
docker-compose logs sqlserver-p1
docker-compose logs sqlserver-p2

# Restart containers
docker-compose restart

# Full reset (removes data)
docker-compose down -v
docker-compose up -d
./init_database.sh
```

### Database Connection Failed

1. **Check containers are running:**
   ```bash
   docker ps | grep research_db
   ```

2. **Wait for SQL Server to be ready** (takes ~30 seconds after start)

3. **Verify database exists:**
   ```bash
   docker exec research_db_p1 /opt/mssql-tools18/bin/sqlcmd \
     -S localhost -U sa -P 'YourStrong@Pass123' -C \
     -Q "SELECT name FROM sys.databases"
   ```

4. **Re-run initialization:**
   ```bash
   ./init_database.sh
   ```

### Java/Maven Issues

```bash
# Verify Java version (needs 17+)
java -version

# Clean and rebuild
mvn clean compile

# Run with debug output
mvn javafx:run -X
```

### Common Error: "Login failed for user 'sa'"

This usually means the database doesn't exist yet. Run:
```bash
./init_database.sh
```

---

## Useful Commands

```bash
# Start everything
docker-compose up -d && sleep 30 && ./init_database.sh && mvn javafx:run

# Stop containers
docker-compose down

# Stop and remove all data
docker-compose down -v

# Connect to P1 database interactively
docker exec -it research_db_p1 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'YourStrong@Pass123' -C -d ResearchDB_P1

# Connect to P2 database interactively
docker exec -it research_db_p2 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'YourStrong@Pass123' -C -d ResearchDB_P2
```

---

## Configuration

### Database Credentials

File: `src/main/resources/application.properties`

```properties
db.p1.url=jdbc:sqlserver://localhost:14331;databaseName=ResearchDB_P1;...
db.p1.username=sa
db.p1.password=YourStrong@Pass123

db.p2.url=jdbc:sqlserver://localhost:14332;databaseName=ResearchDB_P2;...
db.p2.username=sa
db.p2.password=YourStrong@Pass123
```

### Docker Ports

| Service | Internal Port | External Port |
|---------|---------------|---------------|
| SQL Server P1 | 1433 | 14331 |
| SQL Server P2 | 1433 | 14332 |

---

## License

Educational project for distributed database course.
