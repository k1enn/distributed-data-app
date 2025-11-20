# Distributed Database Implementation Guide

## Overview
This implementation demonstrates **Fragmentation Transparency (Level 1)** and **Location Transparency (Level 2)** in a distributed database system using Java, JavaFX, and SQL Server.

---

## Key Concepts

### Level 1: Fragmentation Transparency (Highest Transparency)
- **User Perspective**: Queries the **global schema** as if data is in a single, centralized database
- **User Knowledge**: Does NOT know that tables are fragmented across servers
- **System Responsibility**: Automatically translates global queries into fragment queries
- **Example**:
  ```sql
  -- User writes:
  SELECT * FROM DEAN WHERE manhom = 'NC01'

  -- System automatically queries:
  SELECT * FROM Server1.DEAN UNION SELECT * FROM Server2.DEAN WHERE manhom = 'NC01'
  ```

### Level 2: Location Transparency (Middle Transparency)
- **User Perspective**: Knows **fragment names** but not their physical locations
- **User Knowledge**: Knows fragments exist (e.g., `DEAN_P1`, `DEAN_P2`) but NOT which server hosts them
- **System Responsibility**: Maps fragment names to physical server connections
- **Example**:
  ```sql
  -- User writes:
  SELECT * FROM DEAN_P1 WHERE manhom = 'NC01'

  -- System maps DEAN_P1 → Server 1 (user doesn't know this)
  ```

---

## Database Architecture

### Server Configuration
- **Server 1** (Port 1433): `DistributedDB_P1`
  - Hosts P1 fragments: `nhomnc`, `dean`, `nhanvien`, `thamgia`

- **Server 2** (Port 1434): `DistributedDB_P2`
  - Hosts P2 fragments: `nhomnc`, `dean`, `nhanvien`, `thamgia`

### Fragmentation Strategy
Data is **horizontally partitioned** by `tenphong` (room):
- Records with `tenphong = 'P1'` → Server 1
- Records with `tenphong = 'P2'` → Server 2

---

## Implementation Details

### 1. Fragment Location Mapping (`DatabaseManager.java`)

```java
private Map<String, Connection> fragmentLocationMap;

private void initializeFragmentLocationMap() {
    fragmentLocationMap = new HashMap<>();

    // P1 fragments → Server 1
    fragmentLocationMap.put("NHOMNC_P1", conn1);
    fragmentLocationMap.put("DEAN_P1", conn1);
    fragmentLocationMap.put("NHANVIEN_P1", conn1);
    fragmentLocationMap.put("THAMGIA_P1", conn1);

    // P2 fragments → Server 2
    fragmentLocationMap.put("NHOMNC_P2", conn2);
    fragmentLocationMap.put("DEAN_P2", conn2);
    fragmentLocationMap.put("NHANVIEN_P2", conn2);
    fragmentLocationMap.put("THAMGIA_P2", conn2);
}
```

This mapping enables **Location Transparency**: The system knows where each fragment resides, but the user doesn't need to.

---

## Query Implementations

### Query 1: Projects with External Participants

#### Requirement
Input: Research group ID (e.g., 'NC01')
Output: Projects belonging to this group that have participants from OTHER research groups

#### Level 1 Implementation
**Method**: `query1_Level1_FragmentationTransparency(String maNhom)`

```java
// User queries global schema (doesn't know about fragmentation)
String query = "SELECT DISTINCT da.mada, da.tenda FROM dean da " +
               "JOIN thamgia tg ON da.mada = tg.mada " +
               "JOIN nhanvien nv ON tg.manv = nv.manv " +
               "WHERE da.manhom = ? AND nv.manhom != ?";

// System automatically queries both servers
executeOn(conn1, query);  // User doesn't know this happens
executeOn(conn2, query);  // User doesn't know this happens
```

**User Experience**: "Give me projects for NC01 with external participants"
**System Action**: Transparently queries both servers and merges results

#### Level 2 Implementation
**Method**: `query1_Level2_LocationTransparency(String maNhom, List<String> fragmentNames)`

```java
// User specifies fragments (knows they exist, not where they are)
List<String> fragments = Arrays.asList("DEAN_P1", "DEAN_P2");

for (String fragmentName : fragments) {
    Connection conn = getConnectionForFragment(fragmentName);  // Map to server
    executeOn(conn, query);
}
```

**User Experience**: "Query DEAN_P1 and DEAN_P2 for NC01"
**System Action**: Maps DEAN_P1 → Server1, DEAN_P2 → Server2

---

### Query 2: Update Room Name

#### Requirement
Update room name from 'P1' to a user-specified new room name for a given research group
- Input 1: Research group code in `txtMaNhom` (e.g., 'NC01')
- Input 2: New room name in `txtTenPhong` (e.g., 'P2')

#### Level 1 Implementation
**Method**: `query2_Level1_FragmentationTransparency(String maNhom, String newTenPhong)`

```java
// User updates global table (doesn't know about fragments)
// Inputs: maNhom from txtMaNhom, newTenPhong from txtTenPhong
String updateQuery = "UPDATE nhomnc SET tenphong = ? WHERE manhom = ? AND tenphong = 'P1'";

// System automatically searches all servers
for (Connection conn : Arrays.asList(conn1, conn2)) {
    executeUpdateOn(conn, updateQuery);
}
```

**User Experience**:
- Enter "NC01" in Mã Nhóm field
- Enter "P2" in Tên Phòng field
- Click "Query 2: Update Room Name"
- Result: "Update NC01's room from P1 to P2"

**System Action**: Searches both servers automatically, updates wherever found

#### Level 2 Implementation
**Method**: `query2_Level2_LocationTransparency(String maNhom, String newTenPhong, List<String> fragmentNames)`

```java
// User specifies which fragments to update
// Inputs: maNhom from txtMaNhom, newTenPhong from txtTenPhong
List<String> fragments = Arrays.asList("NHOMNC_P1", "NHOMNC_P2");

for (String fragmentName : fragments) {
    Connection conn = getConnectionForFragment(fragmentName);  // Map to server
    executeUpdateOn(conn, updateQuery);
}
```

**User Experience**:
- Enter "NC01" in Mã Nhóm field
- Enter "P2" in Tên Phòng field
- Select "Level 2: Location Transparency"
- Click "Query 2: Update Room Name"
- Result: "Update fragments NHOMNC_P1, NHOMNC_P2 for NC01 to P2"

**System Action**: Maps NHOMNC_P1 → Server1, NHOMNC_P2 → Server2

---

### Query 3: Projects Without Participants

#### Requirement
Find all projects that have NO employees assigned

#### Level 1 Implementation
**Method**: `query3_Level1_FragmentationTransparency()`

```java
// User queries global schema
String query = "SELECT da.mada, da.tenda FROM dean da " +
               "WHERE NOT EXISTS (SELECT 1 FROM thamgia tg WHERE tg.mada = da.mada)";

// System automatically queries both servers
executeOn(conn1, query);
executeOn(conn2, query);
```

**User Experience**: "Find projects without participants"
**System Action**: Queries both servers transparently

#### Level 2 Implementation
**Method**: `query3_Level2_LocationTransparency(List<String> fragmentNames)`

```java
// User specifies fragments to query
List<String> fragments = Arrays.asList("DEAN_P1", "DEAN_P2");

for (String fragmentName : fragments) {
    Connection conn = getConnectionForFragment(fragmentName);
    executeOn(conn, query);
}
```

**User Experience**: "Query DEAN_P1 and DEAN_P2 for projects without participants"
**System Action**: Maps fragments to their servers

---

## CRUD Operations with Transparency

### Create (Add)
- Determines target server based on `tenphong` value
- If `tenphong = 'P1'` → Insert into Server 1
- If `tenphong = 'P2'` → Insert into Server 2
- **Fragmentation Rule**: Automatic routing based on fragmentation key

### Read (Query)
- Queries both servers in parallel
- Merges results and removes duplicates
- **Transparency**: User sees unified result set

### Update
- **Level 1**: Searches all servers automatically
- **Level 2**: Updates specified fragments
- **Location Transparency**: System maps fragments to servers

### Delete
- Executes DELETE on both servers
- Reports total rows deleted
- **Safety**: Confirmation dialog required

---

## User Interface Features

### Transparency Level Selector
A ComboBox allows users to switch between transparency levels:
- **Level 1**: Fragmentation Transparency
- **Level 2**: Location Transparency

```java
ComboBox<String> cmbTransparencyLevel;
cmbTransparencyLevel.getItems().addAll(
    "Level 1: Fragmentation Transparency",
    "Level 2: Location Transparency"
);
```

### Query Buttons
Three color-coded buttons for the three queries:
- **Query 1** (Green): Projects with External Participants
- **Query 2** (Blue): Update Room Name
- **Query 3** (Orange): Projects Without Participants

Each button adapts based on selected transparency level.

---

## Error Handling Best Practices

### 1. Comprehensive Try-Catch Blocks
```java
try {
    // Database operations
} catch (IllegalArgumentException e) {
    // Validation errors
} catch (SQLException e) {
    // Database errors
} catch (Exception e) {
    // Unexpected errors
} finally {
    // Resource cleanup
}
```

### 2. Input Validation
- Checks for empty/null inputs
- Validates field formats
- Confirms destructive operations

### 3. Connection Health Checks
```java
public boolean isConnectionHealthy() {
    return conn1 != null && !conn1.isClosed() && conn1.isValid(5) &&
           conn2 != null && !conn2.isClosed() && conn2.isValid(5);
}
```

### 4. Partial Failure Handling
- If one server fails, continues with other servers
- Logs warnings but doesn't stop execution
- Only throws exception if ALL servers fail

### 5. Resource Cleanup
```java
finally {
    if (ps != null) {
        try {
            ps.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Failed to close PreparedStatement", e);
        }
    }
}
```

---

## Running the Application

### Prerequisites
1. Java 11 or higher
2. Maven 3.6+
3. Two SQL Server instances running on:
   - Port 1433 (Server 1)
   - Port 1434 (Server 2)
4. Databases created: `DistributedDB_P1` and `DistributedDB_P2`

### Build and Run
```bash
# Compile the project
mvn clean compile

# Run the application
mvn javafx:run
```

### Testing Transparency Levels

#### Test Level 1
1. Select "Level 1: Fragmentation Transparency" from dropdown
2. Enter research group code (e.g., "NC01")
3. Click "Query 1" button
4. Observe: System queries both servers automatically
5. Result: Unified view of all projects

#### Test Level 2
1. Select "Level 2: Location Transparency" from dropdown
2. Enter research group code (e.g., "NC01")
3. Click "Query 1" button
4. Observe: User specifies fragments (DEAN_P1, DEAN_P2)
5. Result: System maps fragments to servers

---

## Key Differences: Level 1 vs Level 2

| Aspect | Level 1 (Fragmentation) | Level 2 (Location) |
|--------|------------------------|-------------------|
| **User Knowledge** | Knows nothing about fragments | Knows fragment names |
| **Query Style** | `SELECT * FROM DEAN` | `SELECT * FROM DEAN_P1` |
| **System Work** | More (automatic fragment discovery) | Less (user specifies fragments) |
| **Transparency** | Highest | Medium |
| **Flexibility** | User can't choose fragments | User can select specific fragments |
| **Use Case** | End users, applications | Database administrators, advanced users |

---

## Logging and Debugging

### Logger Configuration
```java
private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());

LOGGER.info("Query 1 executed on Server 1, found " + results.size() + " records");
LOGGER.log(Level.WARNING, "Error executing query on Server 2", e);
```

### Log Levels
- **INFO**: Successful operations, connection status
- **WARNING**: Non-fatal errors (one server fails but others succeed)
- **SEVERE**: Fatal errors (all servers fail, connection failures)

---

## Security Considerations

### 1. SQL Injection Prevention
- Uses **PreparedStatement** for all queries
- Parameterized queries with `?` placeholders
- Never concatenates user input into SQL strings

### 2. Connection Management
- Connections closed in `finally` blocks
- Connection health validated before operations
- Proper exception handling for connection failures

### 3. User Confirmation
- Confirmation dialogs for UPDATE and DELETE operations
- Clear messaging about what will be changed
- Cancel option for destructive operations

---

## Performance Optimizations

### 1. Duplicate Prevention
Uses `HashSet` to track processed records:
```java
Set<String> processedProjects = new HashSet<>();
if (!processedProjects.contains(maDA)) {
    results.add(new String[]{maDA, tenDA});
    processedProjects.add(maDA);
}
```

### 2. Parallel Queries
Queries both servers independently, doesn't wait for one to finish

### 3. Resource Reuse
Connection objects reused throughout application lifecycle

---

## Troubleshooting

### Build Errors
```bash
# Clean and recompile
mvn clean compile

# Skip tests if needed
mvn clean compile -DskipTests
```

### Connection Errors
- Verify SQL Server instances are running
- Check ports 1433 and 1434 are not blocked
- Verify credentials in `DatabaseManager.java`

### Fragment Not Found
- Check fragment name spelling (case-sensitive)
- Verify fragment mapping in `initializeFragmentLocationMap()`

---

## Conclusion

This implementation demonstrates true distributed database transparency by:

1. **Hiding complexity** from end users (Level 1)
2. **Providing control** for advanced users (Level 2)
3. **Maintaining data consistency** across servers
4. **Handling failures gracefully**
5. **Following best practices** for error handling and security

The application successfully implements both transparency levels as defined in distributed database theory, making it suitable for educational purposes and real-world applications.
