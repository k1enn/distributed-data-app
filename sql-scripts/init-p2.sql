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

