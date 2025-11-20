Based on your Arch Linux setup and folder structure, here's how to run both SQL Server containers and execute initialization scripts.[1][2]

## Step 1: Install Docker on Arch Linux

```bash
# Install Docker
sudo pacman -S docker docker-compose

# Start and enable Docker service
sudo systemctl start docker
sudo systemctl enable docker

# Add your user to docker group (to run without sudo)
sudo usermod -aG docker $USER
newgrp docker
```

## Step 2: Create SQL Initialization Scripts

Create two separate init scripts in your project directory:

```bash
cd ~/Study/distributed-database
mkdir -p sql-scripts
```

Create `sql-scripts/init-p1.sql`:[3][2]

```sql
-- Wait for SQL Server to be ready
WAITFOR DELAY '00:00:10';
GO

-- Create database for P1
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'DistributedDB_P1')
BEGIN
    CREATE DATABASE DistributedDB_P1;
END
GO

USE DistributedDB_P1;
GO

-- Create tables
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'nhomnc')
BEGIN
    CREATE TABLE nhomnc (
        manhom VARCHAR(10) PRIMARY KEY,
        tennhom NVARCHAR(100),
        tenphong VARCHAR(10)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'nhanvien')
BEGIN
    CREATE TABLE nhanvien (
        manv VARCHAR(10) PRIMARY KEY,
        hoten NVARCHAR(100),
        manhom VARCHAR(10),
        FOREIGN KEY (manhom) REFERENCES nhomnc(manhom)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'dean')
BEGIN
    CREATE TABLE dean (
        mada VARCHAR(10) PRIMARY KEY,
        tenda NVARCHAR(100),
        manhom VARCHAR(10),
        FOREIGN KEY (manhom) REFERENCES nhomnc(manhom)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'thamgia')
BEGIN
    CREATE TABLE thamgia (
        manv VARCHAR(10),
        mada VARCHAR(10),
        PRIMARY KEY (manv, mada),
        FOREIGN KEY (manv) REFERENCES nhanvien(manv),
        FOREIGN KEY (mada) REFERENCES dean(mada)
    );
END
GO

-- Insert sample data for P1 fragment
INSERT INTO nhomnc (manhom, tennhom, tenphong) VALUES 
    ('NC01', N'Nhóm Nghiên Cứu 1', 'P1'),
    ('NC02', N'Nhóm Nghiên Cứu 2', 'P1');
GO

INSERT INTO nhanvien (manv, hoten, manhom) VALUES
    ('NV001', N'Nguyễn Văn A', 'NC01'),
    ('NV002', N'Trần Thị B', 'NC01'),
    ('NV003', N'Lê Văn C', 'NC02');
GO

INSERT INTO dean (mada, tenda, manhom) VALUES
    ('DA001', N'Đề Án AI', 'NC01'),
    ('DA002', N'Đề Án Blockchain', 'NC02');
GO

INSERT INTO thamgia (manv, mada) VALUES
    ('NV001', 'DA001'),
    ('NV002', 'DA001'),
    ('NV003', 'DA002');
GO

PRINT 'Database P1 initialized successfully';
```

Create `sql-scripts/init-p2.sql`:

```sql
-- Wait for SQL Server to be ready
WAITFOR DELAY '00:00:10';
GO

-- Create database for P2
IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'DistributedDB_P2')
BEGIN
    CREATE DATABASE DistributedDB_P2;
END
GO

USE DistributedDB_P2;
GO

-- Create tables (same structure)
IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'nhomnc')
BEGIN
    CREATE TABLE nhomnc (
        manhom VARCHAR(10) PRIMARY KEY,
        tennhom NVARCHAR(100),
        tenphong VARCHAR(10)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'nhanvien')
BEGIN
    CREATE TABLE nhanvien (
        manv VARCHAR(10) PRIMARY KEY,
        hoten NVARCHAR(100),
        manhom VARCHAR(10),
        FOREIGN KEY (manhom) REFERENCES nhomnc(manhom)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'dean')
BEGIN
    CREATE TABLE dean (
        mada VARCHAR(10) PRIMARY KEY,
        tenda NVARCHAR(100),
        manhom VARCHAR(10),
        FOREIGN KEY (manhom) REFERENCES nhomnc(manhom)
    );
END
GO

IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'thamgia')
BEGIN
    CREATE TABLE thamgia (
        manv VARCHAR(10),
        mada VARCHAR(10),
        PRIMARY KEY (manv, mada),
        FOREIGN KEY (manv) REFERENCES nhanvien(manv),
        FOREIGN KEY (mada) REFERENCES dean(mada)
    );
END
GO

-- Insert sample data for P2 fragment
INSERT INTO nhomnc (manhom, tennhom, tenphong) VALUES 
    ('NC03', N'Nhóm Nghiên Cứu 3', 'P2'),
    ('NC04', N'Nhóm Nghiên Cứu 4', 'P2');
GO

INSERT INTO nhanvien (manv, hoten, manhom) VALUES
    ('NV004', N'Phạm Văn D', 'NC03'),
    ('NV005', N'Hoàng Thị E', 'NC03'),
    ('NV006', N'Vũ Văn F', 'NC04');
GO

INSERT INTO dean (mada, tenda, manhom) VALUES
    ('DA003', N'Đề Án IoT', 'NC03'),
    ('DA004', N'Đề Án Cloud Computing', 'NC04');
GO

INSERT INTO thamgia (manv, mada) VALUES
    ('NV004', 'DA003'),
    ('NV005', 'DA003'),
    ('NV006', 'DA004');
GO

PRINT 'Database P2 initialized successfully';
```

## Step 3: Update docker-compose.yml

Update your `docker-compose.yml` with initialization scripts:[2][1]

```yaml
version: '3.8'

services:
  sqlserver1:
    image: mcr.microsoft.com/mssql/server:2022-latest
    container_name: mssql_p1
    environment:
      - ACCEPT_EULA=Y
      - SA_PASSWORD=YourStrong!Pass123
      - MSSQL_PID=Developer
    ports:
      - "1433:1433"
    volumes:
      - sqlserver1_data:/var/opt/mssql
      - ./sql-scripts:/sql-scripts
    networks:
      - db_network
    healthcheck:
      test: ["CMD-SHELL", "/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P YourStrong!Pass123 -C -Q 'SELECT 1' || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  sqlserver2:
    image: mcr.microsoft.com/mssql/server:2022-latest
    container_name: mssql_p2
    environment:
      - ACCEPT_EULA=Y
      - SA_PASSWORD=YourStrong!Pass123
      - MSSQL_PID=Developer
    ports:
      - "1434:1433"
    volumes:
      - sqlserver2_data:/var/opt/mssql
      - ./sql-scripts:/sql-scripts
    networks:
      - db_network
    healthcheck:
      test: ["CMD-SHELL", "/opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P YourStrong!Pass123 -C -Q 'SELECT 1' || exit 1"]
      interval: 10s
      timeout: 5s
      retries: 5
      start_period: 30s

  # Init container for P1
  init-sqlserver1:
    image: mcr.microsoft.com/mssql/server:2022-latest
    container_name: init_mssql_p1
    depends_on:
      sqlserver1:
        condition: service_healthy
    volumes:
      - ./sql-scripts:/sql-scripts
    networks:
      - db_network
    command: >
      bash -c '
      echo "Waiting for SQL Server P1...";
      sleep 10;
      echo "Executing init script for P1...";
      /opt/mssql-tools18/bin/sqlcmd -S sqlserver1 -U sa -P YourStrong!Pass123 -C -i /sql-scripts/init-p1.sql;
      echo "P1 initialization completed!";
      '
    restart: "no"

  # Init container for P2
  init-sqlserver2:
    image: mcr.microsoft.com/mssql/server:2022-latest
    container_name: init_mssql_p2
    depends_on:
      sqlserver2:
        condition: service_healthy
    volumes:
      - ./sql-scripts:/sql-scripts
    networks:
      - db_network
    command: >
      bash -c '
      echo "Waiting for SQL Server P2...";
      sleep 10;
      echo "Executing init script for P2...";
      /opt/mssql-tools18/bin/sqlcmd -S sqlserver2 -U sa -P YourStrong!Pass123 -C -i /sql-scripts/init-p2.sql;
      echo "P2 initialization completed!";
      '
    restart: "no"

volumes:
  sqlserver1_data:
  sqlserver2_data:

networks:
  db_network:
    driver: bridge
```

## Step 4: Run the Containers

Execute these commands in your project directory:[4][1]

```bash
# Navigate to your project
cd ~/Study/distributed-database

# Start the containers
docker-compose up -d

# Check container status
docker-compose ps

# View logs to see initialization progress
docker-compose logs -f init-sqlserver1
docker-compose logs -f init-sqlserver2

# View main server logs
docker-compose logs sqlserver1
docker-compose logs sqlserver2
```

## Step 5: Verify Database Initialization

Connect to each SQL Server and verify the data:[5][4]

```bash
# Connect to Server 1 (P1)
docker exec -it mssql_p1 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'YourStrong!Pass123' -C

# Once connected, run:
# USE DistributedDB_P1;
# GO
# SELECT * FROM nhomnc;
# GO
# exit

# Connect to Server 2 (P2)
docker exec -it mssql_p2 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'YourStrong!Pass123' -C

# Once connected, run:
# USE DistributedDB_P2;
# GO
# SELECT * FROM nhomnc;
# GO
# exit
```

## Step 6: Alternative - Manual Script Execution

If you prefer to execute scripts manually after containers are running:[2][5]

```bash
# Start containers without init
docker-compose up -d sqlserver1 sqlserver2

# Wait for servers to be ready (about 30 seconds)
sleep 30

# Execute init script on Server 1
docker exec -i mssql_p1 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'YourStrong!Pass123' -C \
  < ./sql-scripts/init-p1.sql

# Execute init script on Server 2
docker exec -i mssql_p2 /opt/mssql-tools18/bin/sqlcmd \
  -S localhost -U sa -P 'YourStrong!Pass123' -C \
  < ./sql-scripts/init-p2.sql
```

## Updated Folder Structure

Your final structure should look like:

```
~/Study/distributed-database
├── docker-compose.yml
├── pom.xml
├── sql-scripts/
│   ├── init-p1.sql
│   └── init-p2.sql
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/
    │   │       └── k1en/
    │   │           ├── controller/
    │   │           │   └── MainController.java
    │   │           ├── database/
    │   │           │   ├── DatabaseManager.java
    │   │           │   └── MainApp.java
    │   │           ├── MainApp.java
    │   │           └── model/
    │   │               └── NhomNC.java
    │   └── resources/
    └── test/
        └── java/
```

## Useful Docker Commands

```bash
# Stop containers
docker-compose down

# Stop and remove volumes (clean slate)
docker-compose down -v

# Restart containers
docker-compose restart

# View all logs
docker-compose logs -f

# Check running containers
docker ps

# Remove specific container
docker rm -f mssql_p1 mssql_p2
```

The initialization containers (`init-sqlserver1` and `init-sqlserver2`) will automatically execute the SQL scripts when the main SQL Server containers are healthy, then exit. This ensures your databases are set up with the proper fragmentation schema before your Java application connects to them.

