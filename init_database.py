import pyodbc
import time
import logging

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

def wait_for_sql_server(connection_string, max_retries=30):
    """Wait for SQL Server to be ready with exponential backoff"""
    for attempt in range(max_retries):
        try:
            conn = pyodbc.connect(connection_string, timeout=5)
            conn.close()
            logger.info("SQL Server is ready")
            return True
        except Exception as e:
            wait_time = min(2 ** attempt, 30)
            logger.warning(f"Attempt {attempt + 1}/{max_retries}: {str(e)}")
            time.sleep(wait_time)
    return False

def create_fragment_schema(cursor, fragment_name, department_filter):
    """Create fragmented tables with proper constraints"""
    try:
        # Create nhomnc fragment
        cursor.execute(f"""
        IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'nhomnc_{fragment_name}')
        CREATE TABLE nhomnc_{fragment_name} (
            manhomnc NVARCHAR(10) PRIMARY KEY,
            tennhomnc NVARCHAR(100) NOT NULL,
            tenphong NVARCHAR(50) NOT NULL CHECK (tenphong = '{department_filter}'),
            created_at DATETIME DEFAULT GETDATE(),
            updated_at DATETIME DEFAULT GETDATE()
        )
        """)

        # Create nhanvien fragment
        cursor.execute(f"""
        IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'nhanvien_{fragment_name}')
        CREATE TABLE nhanvien_{fragment_name} (
            manv NVARCHAR(10) PRIMARY KEY,
            hoten NVARCHAR(100) NOT NULL,
            manhomnc NVARCHAR(10) NOT NULL,
            created_at DATETIME DEFAULT GETDATE(),
            updated_at DATETIME DEFAULT GETDATE(),
            FOREIGN KEY (manhomnc) REFERENCES nhomnc_{fragment_name}(manhomnc)
        )
        """)

        # Create dean fragment
        cursor.execute(f"""
        IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'dean_{fragment_name}')
        CREATE TABLE dean_{fragment_name} (
            mada NVARCHAR(10) PRIMARY KEY,
            tenda NVARCHAR(200) NOT NULL,
            manhomnc NVARCHAR(10) NOT NULL,
            created_at DATETIME DEFAULT GETDATE(),
            updated_at DATETIME DEFAULT GETDATE(),
            FOREIGN KEY (manhomnc) REFERENCES nhomnc_{fragment_name}(manhomnc)
        )
        """)

        # Create thamgia fragment
        cursor.execute(f"""
        IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'thamgia_{fragment_name}')
        CREATE TABLE thamgia_{fragment_name} (
            manv NVARCHAR(10),
            mada NVARCHAR(10),
            ngaythamgia DATE DEFAULT GETDATE(),
            created_at DATETIME DEFAULT GETDATE(),
            PRIMARY KEY (manv, mada),
            FOREIGN KEY (manv) REFERENCES nhanvien_{fragment_name}(manv),
            FOREIGN KEY (mada) REFERENCES dean_{fragment_name}(mada)
        )
        """)

        logger.info(f"Created schema for fragment: {fragment_name}")
        return True
    except Exception as e:
        logger.error(f"Error creating schema for {fragment_name}: {str(e)}")
        raise

def insert_sample_data(cursor, fragment_name, department):
    """Insert sample data into fragments"""
    try:
        # Sample groups
        if department == 'P1':
            groups = [
                ('NC01', 'Research Group Alpha', 'P1'),
                ('NC02', 'AI Research Lab', 'P1')
            ]
            employees = [
                ('NV01', 'Nguyen Van A', 'NC01'),
                ('NV02', 'Tran Thi B', 'NC01'),
                ('NV03', 'Le Van C', 'NC02')
            ]
            projects = [
                ('DA01', 'Machine Learning Platform', 'NC01'),
                ('DA02', 'Data Analytics System', 'NC02')
            ]
            participations = [
                ('NV01', 'DA01'),
                ('NV02', 'DA01'),
                ('NV03', 'DA02')
            ]
        else:
            groups = [
                ('NC03', 'Software Engineering Group', 'P2'),
                ('NC04', 'Database Research Lab', 'P2')
            ]
            employees = [
                ('NV04', 'Pham Van D', 'NC03'),
                ('NV05', 'Hoang Thi E', 'NC03'),
                ('NV06', 'Vu Van F', 'NC04')
            ]
            projects = [
                ('DA03', 'Distributed Database System', 'NC03'),
                ('DA04', 'Cloud Computing Platform', 'NC04'),
                ('DA05', 'Web Application Framework', 'NC03')
            ]
            participations = [
                ('NV04', 'DA03'),
                ('NV05', 'DA03'),
                ('NV06', 'DA04')
            ]

        # Insert groups
        for group in groups:
            cursor.execute(f"""
            IF NOT EXISTS (SELECT * FROM nhomnc_{fragment_name} WHERE manhomnc = ?)
            INSERT INTO nhomnc_{fragment_name} (manhomnc, tennhomnc, tenphong)
            VALUES (?, ?, ?)
            """, group[0], group[0], group[1], group[2])

        # Insert employees
        for emp in employees:
            cursor.execute(f"""
            IF NOT EXISTS (SELECT * FROM nhanvien_{fragment_name} WHERE manv = ?)
            INSERT INTO nhanvien_{fragment_name} (manv, hoten, manhomnc)
            VALUES (?, ?, ?)
            """, emp[0], emp[0], emp[1], emp[2])

        # Insert projects
        for proj in projects:
            cursor.execute(f"""
            IF NOT EXISTS (SELECT * FROM dean_{fragment_name} WHERE mada = ?)
            INSERT INTO dean_{fragment_name} (mada, tenda, manhomnc)
            VALUES (?, ?, ?)
            """, proj[0], proj[0], proj[1], proj[2])

        # Insert participations
        for part in participations:
            cursor.execute(f"""
            IF NOT EXISTS (SELECT * FROM thamgia_{fragment_name} WHERE manv = ? AND mada = ?)
            INSERT INTO thamgia_{fragment_name} (manv, mada)
            VALUES (?, ?)
            """, part[0], part[1], part[0], part[1])

        logger.info(f"Inserted sample data into {fragment_name}")
    except Exception as e:
        logger.error(f"Error inserting data into {fragment_name}: {str(e)}")
        raise

def initialize_databases():
    """Main initialization function"""
    databases = [
        {
            'name': 'p1',
            'master_connection': 'DRIVER={ODBC Driver 17 for SQL Server};SERVER=localhost,14331;DATABASE=master;UID=sa;PWD=YourStrong@Pass123;TrustServerCertificate=yes',
            'db_connection': 'DRIVER={ODBC Driver 17 for SQL Server};SERVER=localhost,14331;DATABASE=ResearchDB_P1;UID=sa;PWD=YourStrong@Pass123;TrustServerCertificate=yes',
            'database_name': 'ResearchDB_P1',
            'department': 'P1'
        },
        {
            'name': 'p2',
            'master_connection': 'DRIVER={ODBC Driver 17 for SQL Server};SERVER=localhost,14332;DATABASE=master;UID=sa;PWD=YourStrong@Pass123;TrustServerCertificate=yes',
            'db_connection': 'DRIVER={ODBC Driver 17 for SQL Server};SERVER=localhost,14332;DATABASE=ResearchDB_P2;UID=sa;PWD=YourStrong@Pass123;TrustServerCertificate=yes',
            'database_name': 'ResearchDB_P2',
            'department': 'P2'
        }
    ]

    for db in databases:
        try:
            logger.info(f"Initializing database: {db['name']}")

            # Wait for server to be ready by connecting to master
            if not wait_for_sql_server(db['master_connection']):
                raise Exception(f"SQL Server {db['name']} not ready after max retries")

            # Connect to master database to create the database
            logger.info(f"Connecting to master database on {db['name']}...")
            conn = pyodbc.connect(db['master_connection'], autocommit=True)
            cursor = conn.cursor()

            # Check if database exists
            cursor.execute(f"SELECT DB_ID('{db['database_name']}')")
            result = cursor.fetchone()

            if result[0] is None:
                logger.info(f"Creating database {db['database_name']}...")
                cursor.execute(f"CREATE DATABASE {db['database_name']}")
                logger.info(f"Database {db['database_name']} created successfully")
                # Wait a moment for database to be fully ready
                time.sleep(2)
            else:
                logger.info(f"Database {db['database_name']} already exists")

            cursor.close()
            conn.close()

            # Verify we can connect to the new database
            logger.info(f"Connecting to {db['database_name']}...")
            conn = pyodbc.connect(db['db_connection'], autocommit=True)
            cursor = conn.cursor()

            create_fragment_schema(cursor, db['name'], db['department'])
            insert_sample_data(cursor, db['name'], db['department'])

            cursor.close()
            conn.close()

            logger.info(f"Successfully initialized {db['name']}")

        except Exception as e:
            logger.error(f"Failed to initialize {db['name']}: {str(e)}")
            raise

if __name__ == "__main__":
    initialize_databases()
