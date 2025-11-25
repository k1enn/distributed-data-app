#!/bin/bash

# Database initialization script using sqlcmd inside Docker containers
# Usage: ./init_database.sh

set -e

echo "=== Distributed Database Initialization Script ==="
echo ""

# Wait for SQL Server to be ready
wait_for_sqlserver() {
    local container=$1
    local max_attempts=30
    local attempt=1

    echo "Waiting for $container to be ready..."
    while [ $attempt -le $max_attempts ]; do
        if docker exec $container /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrong@Pass123' -C -Q "SELECT 1" &> /dev/null; then
            echo "$container is ready!"
            return 0
        fi
        echo "  Attempt $attempt/$max_attempts - waiting..."
        sleep 2
        attempt=$((attempt + 1))
    done
    echo "ERROR: $container did not become ready in time"
    return 1
}

# Initialize P1 database
init_p1() {
    local container="research_db_p1"
    echo ""
    echo "=== Initializing P1 Database ==="

    wait_for_sqlserver $container

    echo "Creating database ResearchDB_P1..."
    docker exec $container /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrong@Pass123' -C -Q "
    IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'ResearchDB_P1')
    CREATE DATABASE ResearchDB_P1
    "

    sleep 2

    echo "Creating tables for P1 fragment..."
    docker exec $container /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrong@Pass123' -C -d ResearchDB_P1 -Q "
    -- Create nhomnc_p1 table
    IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'nhomnc_p1')
    CREATE TABLE nhomnc_p1 (
        manhomnc NVARCHAR(10) PRIMARY KEY,
        tennhomnc NVARCHAR(100) NOT NULL,
        tenphong NVARCHAR(50) NOT NULL CHECK (tenphong = 'P1'),
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE()
    );

    -- Create nhanvien_p1 table
    IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'nhanvien_p1')
    CREATE TABLE nhanvien_p1 (
        manv NVARCHAR(10) PRIMARY KEY,
        hoten NVARCHAR(100) NOT NULL,
        manhomnc NVARCHAR(10) NOT NULL,
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (manhomnc) REFERENCES nhomnc_p1(manhomnc)
    );

    -- Create dean_p1 table
    IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'dean_p1')
    CREATE TABLE dean_p1 (
        mada NVARCHAR(10) PRIMARY KEY,
        tenda NVARCHAR(200) NOT NULL,
        manhomnc NVARCHAR(10) NOT NULL,
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (manhomnc) REFERENCES nhomnc_p1(manhomnc)
    );

    -- Create thamgia_p1 table
    IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'thamgia_p1')
    CREATE TABLE thamgia_p1 (
        manv NVARCHAR(10),
        mada NVARCHAR(10),
        ngaythamgia DATE DEFAULT GETDATE(),
        created_at DATETIME DEFAULT GETDATE(),
        PRIMARY KEY (manv, mada),
        FOREIGN KEY (manv) REFERENCES nhanvien_p1(manv),
        FOREIGN KEY (mada) REFERENCES dean_p1(mada)
    );
    "

    echo "Inserting sample data for P1..."
    docker exec $container /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrong@Pass123' -C -d ResearchDB_P1 -Q "
    -- Insert research groups
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC01')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC01', 'Research Group Alpha', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC02')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC02', 'AI Research Lab', 'P1');

    -- Insert employees
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV01')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV01', 'Nguyen Van A', 'NC01');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV02')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV02', 'Tran Thi B', 'NC01');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV03')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV03', 'Le Van C', 'NC02');

    -- Insert projects
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA01')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA01', 'Machine Learning Platform', 'NC01');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA02')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA02', 'Data Analytics System', 'NC02');

    -- Insert participations
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV01' AND mada = 'DA01')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV01', 'DA01');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV02' AND mada = 'DA01')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV02', 'DA01');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV03' AND mada = 'DA02')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV03', 'DA02');
    "

    echo "P1 initialization complete!"
}

# Initialize P2 database
init_p2() {
    local container="research_db_p2"
    echo ""
    echo "=== Initializing P2 Database ==="

    wait_for_sqlserver $container

    echo "Creating database ResearchDB_P2..."
    docker exec $container /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrong@Pass123' -C -Q "
    IF NOT EXISTS (SELECT * FROM sys.databases WHERE name = 'ResearchDB_P2')
    CREATE DATABASE ResearchDB_P2
    "

    sleep 2

    echo "Creating tables for P2 fragment..."
    docker exec $container /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrong@Pass123' -C -d ResearchDB_P2 -Q "
    -- Create nhomnc_p2 table
    IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'nhomnc_p2')
    CREATE TABLE nhomnc_p2 (
        manhomnc NVARCHAR(10) PRIMARY KEY,
        tennhomnc NVARCHAR(100) NOT NULL,
        tenphong NVARCHAR(50) NOT NULL CHECK (tenphong = 'P2'),
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE()
    );

    -- Create nhanvien_p2 table
    IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'nhanvien_p2')
    CREATE TABLE nhanvien_p2 (
        manv NVARCHAR(10) PRIMARY KEY,
        hoten NVARCHAR(100) NOT NULL,
        manhomnc NVARCHAR(10) NOT NULL,
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (manhomnc) REFERENCES nhomnc_p2(manhomnc)
    );

    -- Create dean_p2 table
    IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'dean_p2')
    CREATE TABLE dean_p2 (
        mada NVARCHAR(10) PRIMARY KEY,
        tenda NVARCHAR(200) NOT NULL,
        manhomnc NVARCHAR(10) NOT NULL,
        created_at DATETIME DEFAULT GETDATE(),
        updated_at DATETIME DEFAULT GETDATE(),
        FOREIGN KEY (manhomnc) REFERENCES nhomnc_p2(manhomnc)
    );

    -- Create thamgia_p2 table
    IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'thamgia_p2')
    CREATE TABLE thamgia_p2 (
        manv NVARCHAR(10),
        mada NVARCHAR(10),
        ngaythamgia DATE DEFAULT GETDATE(),
        created_at DATETIME DEFAULT GETDATE(),
        PRIMARY KEY (manv, mada),
        FOREIGN KEY (manv) REFERENCES nhanvien_p2(manv),
        FOREIGN KEY (mada) REFERENCES dean_p2(mada)
    );
    "

    echo "Inserting sample data for P2..."
    docker exec $container /opt/mssql-tools18/bin/sqlcmd -S localhost -U sa -P 'YourStrong@Pass123' -C -d ResearchDB_P2 -Q "
    -- Insert research groups
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC03')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC03', 'Software Engineering Group', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC04')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC04', 'Database Research Lab', 'P2');

    -- Insert employees
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV04')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV04', 'Pham Van D', 'NC03');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV05')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV05', 'Hoang Thi E', 'NC03');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV06')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV06', 'Vu Van F', 'NC04');

    -- Insert projects
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA03')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA03', 'Distributed Database System', 'NC03');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA04')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA04', 'Cloud Computing Platform', 'NC04');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA05')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA05', 'Web Application Framework', 'NC03');

    -- Insert participations
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV04' AND mada = 'DA03')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV04', 'DA03');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV05' AND mada = 'DA03')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV05', 'DA03');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV06' AND mada = 'DA04')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV06', 'DA04');
    "

    echo "P2 initialization complete!"
}

# Main execution
echo "Starting database initialization..."
echo ""

init_p1
init_p2

echo ""
echo "=== All databases initialized successfully! ==="
echo ""
echo "Database Summary:"
echo "  - ResearchDB_P1 on localhost:14331 (Department P1)"
echo "  - ResearchDB_P2 on localhost:14332 (Department P2)"
echo ""
echo "You can now run the JavaFX application with: mvn javafx:run"
