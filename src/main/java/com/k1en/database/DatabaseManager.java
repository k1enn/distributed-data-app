package com.k1en.database;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * DatabaseManager implementing Fragmentation Transparency (Level 1) and Location Transparency (Level 2)
 *
 * Level 1 (Fragmentation Transparency): Users query global schema without knowing about fragments
 * Level 2 (Location Transparency): Users specify fragments without knowing their physical locations
 */
public class DatabaseManager {
    private static final Logger LOGGER = Logger.getLogger(DatabaseManager.class.getName());
    private static final String SERVER1_URL = "jdbc:sqlserver://localhost:1433;databaseName=DistributedDB_P1;encrypt=false";
    private static final String SERVER2_URL = "jdbc:sqlserver://localhost:1434;databaseName=DistributedDB_P2;encrypt=false";
    private static final String USERNAME = "sa";
    private static final String PASSWORD = "YourStrong!Pass123";

    private Connection conn1;
    private Connection conn2;

    // Fragment to Server mapping for Level 2 (Location Transparency)
    private Map<String, Connection> fragmentLocationMap;

    public DatabaseManager() throws SQLException {
        try {
            conn1 = DriverManager.getConnection(SERVER1_URL, USERNAME, PASSWORD);
            LOGGER.info("Connected to Server 1 (P1) successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to Server 1 (P1)", e);
            throw new SQLException("Connection to Server 1 failed: " + e.getMessage(), e);
        }

        try {
            conn2 = DriverManager.getConnection(SERVER2_URL, USERNAME, PASSWORD);
            LOGGER.info("Connected to Server 2 (P2) successfully");
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Failed to connect to Server 2 (P2)", e);
            if (conn1 != null) {
                try {
                    conn1.close();
                } catch (SQLException ex) {
                    LOGGER.log(Level.WARNING, "Failed to close connection 1 during cleanup", ex);
                }
            }
            throw new SQLException("Connection to Server 2 failed: " + e.getMessage(), e);
        }

        // Initialize fragment location mapping for Level 2 (Location Transparency)
        initializeFragmentLocationMap();
    }

    /**
     * Initialize fragment-to-location mapping
     * This mapping allows Level 2 transparency: users know fragment names but not their locations
     */
    private void initializeFragmentLocationMap() {
        fragmentLocationMap = new HashMap<>();

        // Map fragments to physical servers
        // P1 fragments are on Server 1
        fragmentLocationMap.put("NHOMNC_P1", conn1);
        fragmentLocationMap.put("DEAN_P1", conn1);
        fragmentLocationMap.put("NHANVIEN_P1", conn1);
        fragmentLocationMap.put("THAMGIA_P1", conn1);

        // P2 fragments are on Server 2
        fragmentLocationMap.put("NHOMNC_P2", conn2);
        fragmentLocationMap.put("DEAN_P2", conn2);
        fragmentLocationMap.put("NHANVIEN_P2", conn2);
        fragmentLocationMap.put("THAMGIA_P2", conn2);

        LOGGER.info("Fragment location map initialized with " + fragmentLocationMap.size() + " fragments");
    }

    /**
     * Get connection for a specific fragment (Level 2: Location Transparency)
     * User knows fragment name but not its physical location
     */
    private Connection getConnectionForFragment(String fragmentName) throws SQLException {
        Connection conn = fragmentLocationMap.get(fragmentName);
        if (conn == null) {
            throw new SQLException("Unknown fragment: " + fragmentName);
        }
        return conn;
    }

    public Connection getConnection1() {
        return conn1;
    }

    public Connection getConnection2() {
        return conn2;
    }

    // ==================== QUERY 1: PROJECTS WITH EXTERNAL PARTICIPANTS ====================

    /**
     * Câu 1 - LEVEL 1 (Fragmentation Transparency)
     * User queries global schema without knowing about fragmentation
     *
     * User perspective: "SELECT projects from DEAN where research group = maNhom AND has external participants"
     * System handles: Querying all fragments transparently
     *
     * @param maNhom Research group code
     * @return List of project info [mada, tenda]
     * @throws SQLException if database error occurs
     */
    public List<String[]> query1_Level1_FragmentationTransparency(String maNhom) throws SQLException {
        if (maNhom == null || maNhom.trim().isEmpty()) {
            throw new IllegalArgumentException("Research group code (maNhom) cannot be empty");
        }

        List<String[]> results = new ArrayList<>();
        Set<String> processedProjects = new HashSet<>();

        // Query across both fragments
        String query = "SELECT DISTINCT da.mada, da.tenda FROM dean da " +
                "JOIN thamgia tg ON da.mada = tg.mada " +
                "JOIN nhanvien nv ON tg.manv = nv.manv " +
                "WHERE da.manhom = ? AND nv.manhom != ?";

        // Execute on Server 1 (P1)
        try (PreparedStatement ps1 = conn1.prepareStatement(query)) {
            ps1.setString(1, maNhom.trim());
            ps1.setString(2, maNhom.trim());

            try (ResultSet rs1 = ps1.executeQuery()) {
                while (rs1.next()) {
                    String maDA = rs1.getString("mada");
                    if (!processedProjects.contains(maDA)) {
                        results.add(new String[]{maDA, rs1.getString("tenda")});
                        processedProjects.add(maDA);
                    }
                }
            }
            LOGGER.info("Query 1 executed on Server 1, found " + results.size() + " records");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error executing query on Server 1", e);
            // Continue to Server 2 even if Server 1 fails
        }

        // Execute on Server 2 (P2)
        try (PreparedStatement ps2 = conn2.prepareStatement(query)) {
            ps2.setString(1, maNhom.trim());
            ps2.setString(2, maNhom.trim());

            try (ResultSet rs2 = ps2.executeQuery()) {
                while (rs2.next()) {
                    String maDA = rs2.getString("mada");
                    if (!processedProjects.contains(maDA)) {
                        results.add(new String[]{maDA, rs2.getString("tenda")});
                        processedProjects.add(maDA);
                    }
                }
            }
            LOGGER.info("Query 1 executed on Server 2, total unique records: " + results.size());
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error executing query on Server 2", e);
            // If both servers fail, throw the exception
            if (results.isEmpty()) {
                throw new SQLException("Failed to execute query on both servers", e);
            }
        }

        return results;
    }

    /**
     * Câu 1 - LEVEL 2 (Location Transparency)
     * User specifies fragment names but system determines their physical locations
     *
     * User perspective: "Query DEAN_P1 and DEAN_P2 fragments" (knows fragments exist, not where they are)
     * System handles: Mapping fragment names to actual server connections
     *
     * @param maNhom Research group code
     * @param fragmentNames List of fragment names to query (e.g., ["DEAN_P1", "DEAN_P2"])
     * @return List of project info [mada, tenda]
     * @throws SQLException if database error occurs
     */
    public List<String[]> query1_Level2_LocationTransparency(String maNhom, List<String> fragmentNames) throws SQLException {
        if (maNhom == null || maNhom.trim().isEmpty()) {
            throw new IllegalArgumentException("Research group code (maNhom) cannot be empty");
        }
        if (fragmentNames == null || fragmentNames.isEmpty()) {
            throw new IllegalArgumentException("Fragment names cannot be empty");
        }

        List<String[]> results = new ArrayList<>();
        Set<String> processedProjects = new HashSet<>();

        // Query to find projects with external participants
        String query = "SELECT DISTINCT da.mada, da.tenda FROM dean da " +
                "JOIN thamgia tg ON da.mada = tg.mada " +
                "JOIN nhanvien nv ON tg.manv = nv.manv " +
                "WHERE da.manhom = ? AND nv.manhom != ?";

        // Query each specified fragment (user knows fragment names, not locations)
        for (String fragmentName : fragmentNames) {
            try {
                Connection conn = getConnectionForFragment(fragmentName);
                try (PreparedStatement ps = conn.prepareStatement(query)) {
                    ps.setString(1, maNhom.trim());
                    ps.setString(2, maNhom.trim());

                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) {
                            String maDA = rs.getString("mada");
                            if (!processedProjects.contains(maDA)) {
                                results.add(new String[]{maDA, rs.getString("tenda")});
                                processedProjects.add(maDA);
                            }
                        }
                    }
                }
                LOGGER.info("Query 1 Level 2 executed on fragment: " + fragmentName);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error querying fragment: " + fragmentName, e);
            }
        }

        return results;
    }

    /**
     * Backward compatibility wrapper - calls Level 1 method
     */
    public List<String[]> executeQueryFragmentTransparency(String maNhom) throws SQLException {
        return query1_Level1_FragmentationTransparency(maNhom);
    }

    // ==================== QUERY 2: UPDATE ROOM NAME ====================

    /**
     * Câu 2 - LEVEL 1 (Fragmentation Transparency)
     * User updates global table without knowing about fragmentation
     *
     * User perspective: "UPDATE NHOMNC SET tenphong='P2' WHERE manhom='NC01' AND tenphong='P1'"
     * System handles: Finding and updating records across all fragments
     *
     * @param maNhom Research group code (e.g., "NC01")
     * @param newTenPhong New room name (e.g., "P2")
     * @return Number of rows updated
     * @throws SQLException if database error occurs
     */
    public int query2_Level1_FragmentationTransparency(String maNhom, String newTenPhong) throws SQLException {
        if (maNhom == null || maNhom.trim().isEmpty()) {
            throw new IllegalArgumentException("Research group code (maNhom) cannot be empty");
        }
        if (newTenPhong == null || newTenPhong.trim().isEmpty()) {
            throw new IllegalArgumentException("New room name (newTenPhong) cannot be empty");
        }

        int totalRowsUpdated = 0;
        String updateQuery = "UPDATE nhomnc SET tenphong = ? WHERE manhom = ? AND tenphong = 'P1'";

        // Query all fragments automatically (user doesn't know about fragments)
        List<Connection> allConnections = Arrays.asList(conn1, conn2);

        for (Connection conn : allConnections) {
            try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                ps.setString(1, newTenPhong.trim());
                ps.setString(2, maNhom.trim());
                int rowsAffected = ps.executeUpdate();
                totalRowsUpdated += rowsAffected;
                if (rowsAffected > 0) {
                    LOGGER.info("Query 2 Level 1: Updated " + rowsAffected + " row(s) for " + maNhom);
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error updating on one server", e);
            }
        }

        if (totalRowsUpdated == 0) {
            throw new SQLException("No records found with manhom='" + maNhom + "' and tenphong='P1'");
        }

        return totalRowsUpdated;
    }

    /**
     * Câu 2 - LEVEL 2 (Location Transparency)
     * User specifies which fragments to update but system determines their locations
     *
     * User perspective: "Update NHOMNC_P1 fragment" (knows fragment name, not its server location)
     * System handles: Mapping NHOMNC_P1 to its physical server connection
     *
     * @param maNhom Research group code (e.g., "NC01")
     * @param newTenPhong New room name (e.g., "P2")
     * @param fragmentNames List of fragment names to update (e.g., ["NHOMNC_P1"])
     * @return Number of rows updated
     * @throws SQLException if database error occurs
     */
    public int query2_Level2_LocationTransparency(String maNhom, String newTenPhong, List<String> fragmentNames) throws SQLException {
        if (maNhom == null || maNhom.trim().isEmpty()) {
            throw new IllegalArgumentException("Research group code (maNhom) cannot be empty");
        }
        if (newTenPhong == null || newTenPhong.trim().isEmpty()) {
            throw new IllegalArgumentException("New room name (newTenPhong) cannot be empty");
        }
        if (fragmentNames == null || fragmentNames.isEmpty()) {
            throw new IllegalArgumentException("Fragment names cannot be empty");
        }

        int totalRowsUpdated = 0;
        String updateQuery = "UPDATE nhomnc SET tenphong = ? WHERE manhom = ? AND tenphong = 'P1'";

        // Update only specified fragments (user knows fragment names, not their locations)
        for (String fragmentName : fragmentNames) {
            try {
                Connection conn = getConnectionForFragment(fragmentName);
                try (PreparedStatement ps = conn.prepareStatement(updateQuery)) {
                    ps.setString(1, newTenPhong.trim());
                    ps.setString(2, maNhom.trim());
                    int rowsAffected = ps.executeUpdate();
                    totalRowsUpdated += rowsAffected;
                    if (rowsAffected > 0) {
                        LOGGER.info("Query 2 Level 2: Updated " + rowsAffected + " row(s) in fragment: " + fragmentName);
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error updating fragment: " + fragmentName, e);
            }
        }

        if (totalRowsUpdated == 0) {
            throw new SQLException("No records found with manhom='" + maNhom + "' and tenphong='P1' in specified fragments");
        }

        return totalRowsUpdated;
    }

    /**
     * Backward compatibility wrapper - calls Level 1 method
     */
    public int updateTenPhongNC01(String maNhom) throws SQLException {
        if (maNhom == null || maNhom.trim().isEmpty()) {
            throw new IllegalArgumentException("Research group code (maNhom) cannot be empty");
        }

        int totalRowsUpdated = 0;
        String updateQuery = "UPDATE nhomnc SET tenphong = 'P2' WHERE manhom = ? AND tenphong = 'P1'";

        // Try Server 1 first
        try (PreparedStatement ps = conn1.prepareStatement(updateQuery)) {
            ps.setString(1, maNhom.trim());
            int rowsAffected = ps.executeUpdate();
            totalRowsUpdated += rowsAffected;
            LOGGER.info("Server 1: Updated " + rowsAffected + " row(s) for " + maNhom);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error updating on Server 1", e);
        }

        // Try Server 2
        try (PreparedStatement ps2 = conn2.prepareStatement(updateQuery)) {
            ps2.setString(1, maNhom.trim());
            int rowsAffected = ps2.executeUpdate();
            totalRowsUpdated += rowsAffected;
            LOGGER.info("Server 2: Updated " + rowsAffected + " row(s) for " + maNhom);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error updating on Server 2", e);
            if (totalRowsUpdated == 0) {
                throw new SQLException("Failed to update on both servers", e);
            }
        }

        if (totalRowsUpdated == 0) {
            throw new SQLException("No records found with manhom='" + maNhom + "' and tenphong='P1'");
        }

        return totalRowsUpdated;
    }

    /**
     * Original updateTenPhong method - kept for backward compatibility
     */
    public void updateTenPhong(String maNhom) throws SQLException {
        query2_Level1_FragmentationTransparency(maNhom, "P2");
    }

    // ==================== QUERY 3: PROJECTS WITHOUT PARTICIPANTS ====================

    /**
     * Câu 3 - LEVEL 1 (Fragmentation Transparency)
     * User queries global schema without knowing about fragmentation
     *
     * User perspective: "SELECT projects from DEAN where NOT EXISTS participation"
     * System handles: Querying all fragments transparently
     *
     * @return List of project info [mada, tenda]
     * @throws SQLException if database error occurs
     */
    public List<String[]> query3_Level1_FragmentationTransparency() throws SQLException {
        List<String[]> results = new ArrayList<>();
        Set<String> processedProjects = new HashSet<>();

        // Query to find projects without any participation
        String query = "SELECT da.mada, da.tenda FROM dean da " +
                "WHERE NOT EXISTS (SELECT 1 FROM thamgia tg WHERE tg.mada = da.mada)";

        // Execute on Server 1 (P1)
        try (Statement stmt1 = conn1.createStatement();
             ResultSet rs1 = stmt1.executeQuery(query)) {
            while (rs1.next()) {
                String maDA = rs1.getString("mada");
                if (!processedProjects.contains(maDA)) {
                    results.add(new String[]{maDA, rs1.getString("tenda")});
                    processedProjects.add(maDA);
                }
            }
            LOGGER.info("Query 3 executed on Server 1, found " + results.size() + " records");
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error executing query on Server 1", e);
            // Continue to Server 2 even if Server 1 fails
        }

        // Execute on Server 2 (P2)
        try (Statement stmt2 = conn2.createStatement();
             ResultSet rs2 = stmt2.executeQuery(query)) {
            while (rs2.next()) {
                String maDA = rs2.getString("mada");
                if (!processedProjects.contains(maDA)) {
                    results.add(new String[]{maDA, rs2.getString("tenda")});
                    processedProjects.add(maDA);
                }
            }
            LOGGER.info("Query 3 executed on Server 2, total unique records: " + results.size());
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "Error executing query on Server 2", e);
            // If both servers fail, throw the exception
            if (results.isEmpty()) {
                throw new SQLException("Failed to execute query on both servers", e);
            }
        }

        return results;
    }

    /**
     * Câu 3 - LEVEL 2 (Location Transparency)
     * User specifies which fragments to query but system determines their locations
     *
     * User perspective: "Query DEAN_P1 and DEAN_P2 fragments for projects without participants"
     * System handles: Mapping fragment names to actual server connections
     *
     * @param fragmentNames List of fragment names to query (e.g., ["DEAN_P1", "DEAN_P2"])
     * @return List of project info [mada, tenda]
     * @throws SQLException if database error occurs
     */
    public List<String[]> query3_Level2_LocationTransparency(List<String> fragmentNames) throws SQLException {
        if (fragmentNames == null || fragmentNames.isEmpty()) {
            throw new IllegalArgumentException("Fragment names cannot be empty");
        }

        List<String[]> results = new ArrayList<>();
        Set<String> processedProjects = new HashSet<>();

        // Query to find projects without any participation
        String query = "SELECT da.mada, da.tenda FROM dean da " +
                "WHERE NOT EXISTS (SELECT 1 FROM thamgia tg WHERE tg.mada = da.mada)";

        // Query each specified fragment (user knows fragment names, not locations)
        for (String fragmentName : fragmentNames) {
            try {
                Connection conn = getConnectionForFragment(fragmentName);
                try (Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(query)) {
                    while (rs.next()) {
                        String maDA = rs.getString("mada");
                        if (!processedProjects.contains(maDA)) {
                            results.add(new String[]{maDA, rs.getString("tenda")});
                            processedProjects.add(maDA);
                        }
                    }
                }
                LOGGER.info("Query 3 Level 2 executed on fragment: " + fragmentName);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Error querying fragment: " + fragmentName, e);
            }
        }

        return results;
    }

    /**
     * Backward compatibility wrapper - calls Level 1 method
     */
    public List<String[]> getProjectsWithoutParticipants() throws SQLException {
        return query3_Level1_FragmentationTransparency();
    }

    // ==================== UTILITY METHODS ====================

    /**
     * Get list of all available fragment names
     * Useful for Level 2 queries where user needs to know fragment names
     */
    public List<String> getAvailableFragments() {
        return new ArrayList<>(fragmentLocationMap.keySet());
    }

    /**
     * Validate connection health
     *
     * @return true if both connections are valid
     */
    public boolean isConnectionHealthy() {
        try {
            return conn1 != null && !conn1.isClosed() && conn1.isValid(5) &&
                   conn2 != null && !conn2.isClosed() && conn2.isValid(5);
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Error checking connection health", e);
            return false;
        }
    }

    /**
     * Close all database connections safely
     */
    public void closeConnections() throws SQLException {
        SQLException exception = null;

        if (conn1 != null) {
            try {
                if (!conn1.isClosed()) {
                    conn1.close();
                    LOGGER.info("Connection 1 closed successfully");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing connection 1", e);
                exception = e;
            }
        }

        if (conn2 != null) {
            try {
                if (!conn2.isClosed()) {
                    conn2.close();
                    LOGGER.info("Connection 2 closed successfully");
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error closing connection 2", e);
                if (exception == null) {
                    exception = e;
                } else {
                    exception.addSuppressed(e);
                }
            }
        }

        if (exception != null) {
            throw exception;
        }
    }
}
