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

