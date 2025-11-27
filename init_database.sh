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
    -- Insert research groups (20 groups for P1)
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC01')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC01', 'Research Group Alpha', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC02')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC02', 'AI Research Lab', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC05')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC05', 'Machine Learning Division', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC06')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC06', 'Computer Vision Lab', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC07')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC07', 'Natural Language Processing', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC08')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC08', 'Robotics Research Center', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC09')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC09', 'Quantum Computing Lab', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC10')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC10', 'Cybersecurity Research', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC11')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC11', 'Bioinformatics Group', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC12')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC12', 'Data Science Team', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC13')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC13', 'IoT Research Lab', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC14')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC14', 'Blockchain Research', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC15')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC15', 'Cloud Architecture Team', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC16')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC16', 'Edge Computing Lab', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC17')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC17', 'AR/VR Development', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC18')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC18', 'High Performance Computing', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC19')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC19', 'Network Security Group', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC20')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC20', 'Software Testing Lab', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC21')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC21', 'DevOps Research', 'P1');
    IF NOT EXISTS (SELECT * FROM nhomnc_p1 WHERE manhomnc = 'NC22')
        INSERT INTO nhomnc_p1 (manhomnc, tennhomnc, tenphong) VALUES ('NC22', 'Mobile Development Lab', 'P1');

    -- Insert employees (20 employees for P1)
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV01')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV01', 'Nguyen Van A', 'NC01');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV02')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV02', 'Tran Thi B', 'NC01');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV03')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV03', 'Le Van C', 'NC02');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV07')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV07', 'Pham Minh Duc', 'NC05');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV08')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV08', 'Hoang Thi Mai', 'NC05');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV09')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV09', 'Vu Van Nam', 'NC06');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV10')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV10', 'Dang Thi Lan', 'NC06');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV11')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV11', 'Bui Van Hung', 'NC07');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV12')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV12', 'Ngo Thi Hoa', 'NC07');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV13')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV13', 'Do Van Tuan', 'NC08');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV14')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV14', 'Ly Thi Ngoc', 'NC08');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV15')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV15', 'Truong Van Binh', 'NC09');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV16')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV16', 'Mai Thi Thu', 'NC09');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV17')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV17', 'Dinh Van Long', 'NC10');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV18')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV18', 'Cao Thi Huong', 'NC10');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV19')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV19', 'Duong Van Hai', 'NC11');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV20')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV20', 'Trinh Thi Yen', 'NC11');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV21')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV21', 'Vo Van Khanh', 'NC12');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV22')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV22', 'Luong Thi Phuong', 'NC12');
    IF NOT EXISTS (SELECT * FROM nhanvien_p1 WHERE manv = 'NV23')
        INSERT INTO nhanvien_p1 (manv, hoten, manhomnc) VALUES ('NV23', 'Ta Van Son', 'NC13');

    -- Insert projects (20 projects for P1)
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA01')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA01', 'Machine Learning Platform', 'NC01');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA02')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA02', 'Data Analytics System', 'NC02');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA06')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA06', 'Deep Learning Framework', 'NC05');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA07')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA07', 'Image Recognition System', 'NC06');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA08')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA08', 'Chatbot Development', 'NC07');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA09')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA09', 'Autonomous Robot Control', 'NC08');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA10')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA10', 'Quantum Algorithm Research', 'NC09');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA11')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA11', 'Intrusion Detection System', 'NC10');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA12')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA12', 'Genome Analysis Pipeline', 'NC11');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA13')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA13', 'Predictive Analytics Tool', 'NC12');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA14')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA14', 'Smart Home IoT Platform', 'NC13');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA15')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA15', 'Cryptocurrency Exchange', 'NC14');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA16')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA16', 'Serverless Architecture', 'NC15');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA17')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA17', 'Edge AI Deployment', 'NC16');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA18')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA18', 'Virtual Reality Training', 'NC17');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA19')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA19', 'Parallel Processing Engine', 'NC18');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA20')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA20', 'Zero Trust Security Model', 'NC19');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA21')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA21', 'Automated Testing Framework', 'NC20');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA22')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA22', 'CI/CD Pipeline Optimizer', 'NC21');
    IF NOT EXISTS (SELECT * FROM dean_p1 WHERE mada = 'DA23')
        INSERT INTO dean_p1 (mada, tenda, manhomnc) VALUES ('DA23', 'Cross-Platform Mobile App', 'NC22');

    -- Insert participations (20 participations for P1)
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV01' AND mada = 'DA01')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV01', 'DA01');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV02' AND mada = 'DA01')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV02', 'DA01');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV03' AND mada = 'DA02')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV03', 'DA02');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV07' AND mada = 'DA06')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV07', 'DA06');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV08' AND mada = 'DA06')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV08', 'DA06');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV09' AND mada = 'DA07')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV09', 'DA07');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV10' AND mada = 'DA07')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV10', 'DA07');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV11' AND mada = 'DA08')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV11', 'DA08');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV12' AND mada = 'DA08')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV12', 'DA08');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV13' AND mada = 'DA09')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV13', 'DA09');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV14' AND mada = 'DA09')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV14', 'DA09');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV15' AND mada = 'DA10')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV15', 'DA10');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV16' AND mada = 'DA10')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV16', 'DA10');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV17' AND mada = 'DA11')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV17', 'DA11');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV18' AND mada = 'DA11')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV18', 'DA11');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV19' AND mada = 'DA12')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV19', 'DA12');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV20' AND mada = 'DA12')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV20', 'DA12');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV21' AND mada = 'DA13')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV21', 'DA13');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV22' AND mada = 'DA13')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV22', 'DA13');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV23' AND mada = 'DA14')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV23', 'DA14');
    IF NOT EXISTS (SELECT * FROM thamgia_p1 WHERE manv = 'NV01' AND mada = 'DA06')
        INSERT INTO thamgia_p1 (manv, mada) VALUES ('NV01', 'DA06');
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
    -- Insert research groups (20 groups for P2)
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC03')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC03', 'Software Engineering Group', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC04')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC04', 'Database Research Lab', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC23')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC23', 'Web Development Team', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC24')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC24', 'API Design Group', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC25')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC25', 'Microservices Architecture', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC26')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC26', 'Data Engineering Lab', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC27')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC27', 'Backend Development', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC28')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC28', 'Frontend Development', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC29')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC29', 'Full Stack Team', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC30')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC30', 'System Integration Lab', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC31')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC31', 'Performance Engineering', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC32')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC32', 'Quality Assurance Team', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC33')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC33', 'Release Engineering', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC34')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC34', 'Site Reliability Team', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC35')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC35', 'Platform Engineering', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC36')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC36', 'Infrastructure Team', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC37')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC37', 'Monitoring Systems', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC38')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC38', 'Log Analytics Group', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC39')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC39', 'Container Orchestration', 'P2');
    IF NOT EXISTS (SELECT * FROM nhomnc_p2 WHERE manhomnc = 'NC40')
        INSERT INTO nhomnc_p2 (manhomnc, tennhomnc, tenphong) VALUES ('NC40', 'Service Mesh Research', 'P2');

    -- Insert employees (20 employees for P2)
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV04')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV04', 'Pham Van D', 'NC03');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV05')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV05', 'Hoang Thi E', 'NC03');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV06')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV06', 'Vu Van F', 'NC04');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV24')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV24', 'Nguyen Thi Hong', 'NC23');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV25')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV25', 'Tran Van Minh', 'NC23');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV26')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV26', 'Le Thi Nga', 'NC24');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV27')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV27', 'Pham Van Thanh', 'NC24');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV28')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV28', 'Hoang Van Cuong', 'NC25');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV29')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV29', 'Vu Thi Thao', 'NC25');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV30')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV30', 'Dang Van Hieu', 'NC26');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV31')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV31', 'Bui Thi Linh', 'NC26');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV32')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV32', 'Ngo Van Phong', 'NC27');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV33')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV33', 'Do Thi Loan', 'NC27');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV34')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV34', 'Ly Van Dat', 'NC28');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV35')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV35', 'Truong Thi My', 'NC28');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV36')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV36', 'Mai Van Quang', 'NC29');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV37')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV37', 'Dinh Thi Thuy', 'NC29');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV38')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV38', 'Cao Van Tien', 'NC30');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV39')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV39', 'Duong Thi Kim', 'NC30');
    IF NOT EXISTS (SELECT * FROM nhanvien_p2 WHERE manv = 'NV40')
        INSERT INTO nhanvien_p2 (manv, hoten, manhomnc) VALUES ('NV40', 'Trinh Van Tai', 'NC31');

    -- Insert projects (20 projects for P2)
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA03')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA03', 'Distributed Database System', 'NC03');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA04')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA04', 'Cloud Computing Platform', 'NC04');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA05')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA05', 'Web Application Framework', 'NC03');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA24')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA24', 'E-commerce Platform', 'NC23');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA25')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA25', 'RESTful API Gateway', 'NC24');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA26')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA26', 'Microservices Platform', 'NC25');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA27')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA27', 'ETL Pipeline System', 'NC26');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA28')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA28', 'Authentication Service', 'NC27');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA29')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA29', 'React Component Library', 'NC28');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA30')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA30', 'Full Stack Dashboard', 'NC29');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA31')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA31', 'Enterprise Integration Hub', 'NC30');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA32')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA32', 'Load Testing Framework', 'NC31');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA33')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA33', 'Test Automation Suite', 'NC32');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA34')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA34', 'Deployment Automation', 'NC33');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA35')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA35', 'Incident Management System', 'NC34');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA36')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA36', 'Platform as a Service', 'NC35');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA37')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA37', 'Infrastructure as Code', 'NC36');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA38')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA38', 'Real-time Monitoring Dashboard', 'NC37');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA39')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA39', 'Centralized Logging System', 'NC38');
    IF NOT EXISTS (SELECT * FROM dean_p2 WHERE mada = 'DA40')
        INSERT INTO dean_p2 (mada, tenda, manhomnc) VALUES ('DA40', 'Kubernetes Operator', 'NC39');

    -- Insert participations (20 participations for P2)
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV04' AND mada = 'DA03')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV04', 'DA03');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV05' AND mada = 'DA03')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV05', 'DA03');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV06' AND mada = 'DA04')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV06', 'DA04');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV24' AND mada = 'DA24')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV24', 'DA24');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV25' AND mada = 'DA24')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV25', 'DA24');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV26' AND mada = 'DA25')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV26', 'DA25');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV27' AND mada = 'DA25')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV27', 'DA25');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV28' AND mada = 'DA26')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV28', 'DA26');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV29' AND mada = 'DA26')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV29', 'DA26');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV30' AND mada = 'DA27')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV30', 'DA27');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV31' AND mada = 'DA27')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV31', 'DA27');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV32' AND mada = 'DA28')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV32', 'DA28');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV33' AND mada = 'DA28')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV33', 'DA28');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV34' AND mada = 'DA29')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV34', 'DA29');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV35' AND mada = 'DA29')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV35', 'DA29');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV36' AND mada = 'DA30')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV36', 'DA30');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV37' AND mada = 'DA30')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV37', 'DA30');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV38' AND mada = 'DA31')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV38', 'DA31');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV39' AND mada = 'DA31')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV39', 'DA31');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV40' AND mada = 'DA32')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV40', 'DA32');
    IF NOT EXISTS (SELECT * FROM thamgia_p2 WHERE manv = 'NV04' AND mada = 'DA05')
        INSERT INTO thamgia_p2 (manv, mada) VALUES ('NV04', 'DA05');
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
