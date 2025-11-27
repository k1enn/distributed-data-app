package com.research.distributed.service;

import com.research.distributed.connection.FragmentConnectionManager;
import com.research.distributed.connection.TransparencyLevel;
import com.research.distributed.exception.DatabaseException;
import com.research.distributed.exception.FragmentException;
import com.research.distributed.exception.ValidationException;
import com.research.distributed.model.DeAn;
import com.research.distributed.model.NhanVien;
import com.research.distributed.model.NhomNC;
import com.research.distributed.model.ThamGia;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class QueryService {
    private static final Logger logger = LoggerFactory.getLogger(QueryService.class);
    private final FragmentConnectionManager connectionManager;

    public QueryService() {
        this.connectionManager = FragmentConnectionManager.getInstance();
    }

    /**
     * Query 1: Get projects with external participants (employees from other groups)
     * Level 1 (Fragment Transparency): User knows about fragments
     */
    public List<DeAn> getProjectsWithExternalParticipantsLevel1(String groupId) throws DatabaseException {
        List<DeAn> results = new ArrayList<>();

        try {
            String fragment = connectionManager.getFragmentForGroup(groupId);
            if (fragment == null) {
                throw new DatabaseException("Cannot determine fragment for group: " + groupId);
            }

            logger.info("Level 1 Query: Querying fragment {} for group {}", fragment, groupId);

            try (Connection conn = connectionManager.getConnection(fragment)) {
                String sql = String.format("""
                    SELECT DISTINCT d.mada, d.tenda, d.manhomnc
                    FROM dean_%s d
                    INNER JOIN thamgia_%s t ON d.mada = t.mada
                    INNER JOIN nhanvien_%s nv ON t.manv = nv.manv
                    WHERE d.manhomnc = ? AND nv.manhomnc != ?
                    """, fragment, fragment, fragment);

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, groupId);
                    stmt.setString(2, groupId);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        results.add(new DeAn(
                                rs.getString("mada"),
                                rs.getString("tenda"),
                                rs.getString("manhomnc")
                        ));
                    }
                }
            }

            logger.info("Level 1 Query: Found {} projects with external participants", results.size());
        } catch (SQLException | FragmentException e) {
            throw new DatabaseException("Error executing level 1 query", e);
        }

        return results;
    }

    /**
     * Query 1: Get projects with external participants
     * Level 2 (Location Transparency): User doesn't know about fragments
     */
    public List<DeAn> getProjectsWithExternalParticipantsLevel2(String groupId) throws DatabaseException {
        List<DeAn> results = new ArrayList<>();

        logger.info("Level 2 Query: Searching all fragments for group {}", groupId);

        for (String fragment : connectionManager.getAllFragments()) {
            try (Connection conn = connectionManager.getConnection(fragment)) {
                String sql = String.format("""
                    SELECT DISTINCT d.mada, d.tenda, d.manhomnc
                    FROM dean_%s d
                    INNER JOIN thamgia_%s t ON d.mada = t.mada
                    INNER JOIN nhanvien_%s nv ON t.manv = nv.manv
                    WHERE d.manhomnc = ? AND nv.manhomnc != ?
                    """, fragment, fragment, fragment);

                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, groupId);
                    stmt.setString(2, groupId);
                    ResultSet rs = stmt.executeQuery();

                    while (rs.next()) {
                        results.add(new DeAn(
                                rs.getString("mada"),
                                rs.getString("tenda"),
                                rs.getString("manhomnc")
                        ));
                    }
                }
            } catch (SQLException | FragmentException e) {
                logger.warn("Error querying fragment {}: {}", fragment, e.getMessage());
                // Continue to next fragment for location transparency
            }
        }

        logger.info("Level 2 Query: Found {} projects with external participants", results.size());
        return results;
    }

    /**
     * Query 2: Update department - requires migration between fragments
     */
    public void updateDepartment(String groupId, String newDepartment, TransparencyLevel level)
            throws DatabaseException, ValidationException {
        try {
            String oldFragment;

            if (level == TransparencyLevel.FRAGMENT_TRANSPARENCY) {
                // Level 1: Use hardcoded fragment mapping (user "knows" where data is)
                oldFragment = connectionManager.getFragmentForGroup(groupId);
                if (oldFragment == null) {
                    throw new ValidationException("Cannot determine fragment for group: " + groupId, "groupId", groupId);
                }
                // Verify group actually exists in this fragment
                String dept = getDepartmentForGroup(groupId, oldFragment);
                if (dept == null) {
                    throw new ValidationException("Group " + groupId + " not found in expected fragment " + oldFragment,
                            "groupId", groupId);
                }
            } else {
                // Level 2: Search all fragments to find where group actually is (location transparency)
                oldFragment = findGroupFragment(groupId);
                if (oldFragment == null) {
                    throw new ValidationException("Cannot find group: " + groupId, "groupId", groupId);
                }
            }

            String currentDepartment = getDepartmentForGroup(groupId, oldFragment);
            if (currentDepartment != null && currentDepartment.equalsIgnoreCase(newDepartment)) {
                throw new ValidationException("Group already in department " + newDepartment,
                        "department", newDepartment);
            }

            String newFragment = newDepartment.equalsIgnoreCase("P1") ? "p1" : "p2";

            if (oldFragment.equals(newFragment)) {
                // Just update the department in same fragment
                updateDepartmentInPlace(groupId, newDepartment, oldFragment);
            } else {
                // Need to migrate to different fragment
                migrateGroupBetweenFragments(groupId, oldFragment, newFragment, newDepartment);
            }

            logger.info("Successfully updated department for group {} to {} (Level: {})",
                    groupId, newDepartment, level);
        } catch (SQLException | FragmentException e) {
            throw new DatabaseException("Error updating department", e);
        }
    }

    /**
     * Search all fragments to find where a group actually exists (for Location Transparency)
     */
    private String findGroupFragment(String groupId) throws SQLException, FragmentException {
        for (String fragment : connectionManager.getAllFragments()) {
            try (Connection conn = connectionManager.getConnection(fragment)) {
                String sql = String.format("SELECT 1 FROM nhomnc_%s WHERE manhomnc = ?", fragment);
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, groupId);
                    ResultSet rs = stmt.executeQuery();
                    if (rs.next()) {
                        logger.info("Found group {} in fragment {}", groupId, fragment);
                        return fragment;
                    }
                }
            }
        }
        return null;
    }

    private String getDepartmentForGroup(String groupId, String fragment)
            throws SQLException, FragmentException {
        try (Connection conn = connectionManager.getConnection(fragment)) {
            String sql = String.format("SELECT tenphong FROM nhomnc_%s WHERE manhomnc = ?", fragment);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, groupId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getString("tenphong");
                }
            }
        }
        return null;
    }

    private void updateDepartmentInPlace(String groupId, String newDepartment, String fragment)
            throws SQLException, FragmentException {
        try (Connection conn = connectionManager.getConnection(fragment)) {
            String sql = String.format(
                    "UPDATE nhomnc_%s SET tenphong = ?, updated_at = GETDATE() WHERE manhomnc = ?",
                    fragment);
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, newDepartment);
                stmt.setString(2, groupId);
                stmt.executeUpdate();
            }
        }
    }

    private void migrateGroupBetweenFragments(String groupId, String oldFragment,
                                               String newFragment, String newDepartment)
            throws SQLException, FragmentException, DatabaseException {
        logger.info("Migrating group {} from fragment {} to {}", groupId, oldFragment, newFragment);

        Connection oldConn = null;
        Connection newConn = null;

        try {
            oldConn = connectionManager.getConnection(oldFragment);
            newConn = connectionManager.getConnection(newFragment);

            // Disable auto-commit for transaction-like behavior
            oldConn.setAutoCommit(false);
            newConn.setAutoCommit(false);

            // Step 1: Fetch all data from old fragment
            NhomNC group = fetchGroup(oldConn, groupId, oldFragment);
            if (group == null) {
                throw new DatabaseException("Group not found: " + groupId);
            }
            group.setTenPhong(newDepartment);

            List<NhanVien> employees = fetchEmployeesByGroup(oldConn, groupId, oldFragment);
            List<DeAn> projects = fetchProjectsByGroup(oldConn, groupId, oldFragment);
            List<ThamGia> participations = fetchParticipationsByGroup(oldConn, groupId, oldFragment);

            logger.info("Found {} employees, {} projects, {} participations to migrate",
                    employees.size(), projects.size(), participations.size());

            // Step 2: Clean up any existing data in target fragment (handles retry scenarios)
            deleteParticipationsByGroup(newConn, groupId, newFragment);
            deleteProjectsByGroup(newConn, groupId, newFragment);
            deleteEmployeesByGroup(newConn, groupId, newFragment);
            deleteGroup(newConn, groupId, newFragment);
            logger.info("Cleaned up any existing data for group {} in target fragment {}", groupId, newFragment);

            // Step 3: Insert data into new fragment
            insertGroup(newConn, group, newFragment);
            for (NhanVien emp : employees) {
                insertEmployee(newConn, emp, newFragment);
            }
            for (DeAn proj : projects) {
                insertProject(newConn, proj, newFragment);
            }
            for (ThamGia tg : participations) {
                insertParticipation(newConn, tg, newFragment);
            }

            // Step 4: Delete data from old fragment (in reverse order due to FK constraints)
            deleteParticipationsByGroup(oldConn, groupId, oldFragment);
            deleteProjectsByGroup(oldConn, groupId, oldFragment);
            deleteEmployeesByGroup(oldConn, groupId, oldFragment);
            deleteGroup(oldConn, groupId, oldFragment);

            // Commit both connections
            newConn.commit();
            oldConn.commit();

            logger.info("Successfully migrated group {} from {} to {}", groupId, oldFragment, newFragment);

        } catch (Exception e) {
            // Rollback on any error
            if (oldConn != null) {
                try { oldConn.rollback(); } catch (SQLException ex) { logger.error("Rollback failed", ex); }
            }
            if (newConn != null) {
                try { newConn.rollback(); } catch (SQLException ex) { logger.error("Rollback failed", ex); }
            }
            throw new DatabaseException("Migration failed: " + e.getMessage(), e);
        } finally {
            // Restore auto-commit and close connections
            if (oldConn != null) {
                try { oldConn.setAutoCommit(true); oldConn.close(); } catch (SQLException e) { /* ignore */ }
            }
            if (newConn != null) {
                try { newConn.setAutoCommit(true); newConn.close(); } catch (SQLException e) { /* ignore */ }
            }
        }
    }

    // Helper methods for fetching data
    private NhomNC fetchGroup(Connection conn, String groupId, String fragment) throws SQLException {
        String sql = String.format("SELECT manhomnc, tennhomnc, tenphong FROM nhomnc_%s WHERE manhomnc = ?", fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new NhomNC(rs.getString("manhomnc"), rs.getString("tennhomnc"), rs.getString("tenphong"));
            }
        }
        return null;
    }

    private List<NhanVien> fetchEmployeesByGroup(Connection conn, String groupId, String fragment) throws SQLException {
        List<NhanVien> list = new ArrayList<>();
        String sql = String.format("SELECT manv, hoten, manhomnc FROM nhanvien_%s WHERE manhomnc = ?", fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new NhanVien(rs.getString("manv"), rs.getString("hoten"), rs.getString("manhomnc")));
            }
        }
        return list;
    }

    private List<DeAn> fetchProjectsByGroup(Connection conn, String groupId, String fragment) throws SQLException {
        List<DeAn> list = new ArrayList<>();
        String sql = String.format("SELECT mada, tenda, manhomnc FROM dean_%s WHERE manhomnc = ?", fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new DeAn(rs.getString("mada"), rs.getString("tenda"), rs.getString("manhomnc")));
            }
        }
        return list;
    }

    private List<ThamGia> fetchParticipationsByGroup(Connection conn, String groupId, String fragment) throws SQLException {
        List<ThamGia> list = new ArrayList<>();
        // Only fetch participations where BOTH the employee AND the project belong to the group
        String sql = String.format(
                "SELECT t.manv, t.mada, t.ngaythamgia FROM thamgia_%s t " +
                "INNER JOIN nhanvien_%s nv ON t.manv = nv.manv " +
                "INNER JOIN dean_%s d ON t.mada = d.mada " +
                "WHERE nv.manhomnc = ? AND d.manhomnc = ?", fragment, fragment, fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            stmt.setString(2, groupId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Date date = rs.getDate("ngaythamgia");
                LocalDate localDate = date != null ? date.toLocalDate() : null;
                list.add(new ThamGia(rs.getString("manv"), rs.getString("mada"), localDate));
            }
        }
        return list;
    }

    // Helper methods for inserting data
    private void insertGroup(Connection conn, NhomNC group, String fragment) throws SQLException {
        String sql = String.format(
                "INSERT INTO nhomnc_%s (manhomnc, tennhomnc, tenphong, created_at) VALUES (?, ?, ?, GETDATE())", fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, group.getMaHomnc());
            stmt.setString(2, group.getTenNhomnc());
            stmt.setString(3, group.getTenPhong());
            stmt.executeUpdate();
        }
    }

    private void insertEmployee(Connection conn, NhanVien emp, String fragment) throws SQLException {
        String sql = String.format(
                "INSERT INTO nhanvien_%s (manv, hoten, manhomnc, created_at) VALUES (?, ?, ?, GETDATE())", fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, emp.getMaNv());
            stmt.setString(2, emp.getHoTen());
            stmt.setString(3, emp.getMaHomnc());
            stmt.executeUpdate();
        }
    }

    private void insertProject(Connection conn, DeAn proj, String fragment) throws SQLException {
        String sql = String.format(
                "INSERT INTO dean_%s (mada, tenda, manhomnc, created_at) VALUES (?, ?, ?, GETDATE())", fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, proj.getMaDa());
            stmt.setString(2, proj.getTenDa());
            stmt.setString(3, proj.getMaHomnc());
            stmt.executeUpdate();
        }
    }

    private void insertParticipation(Connection conn, ThamGia tg, String fragment) throws SQLException {
        String sql = String.format(
                "INSERT INTO thamgia_%s (manv, mada, ngaythamgia, created_at) VALUES (?, ?, ?, GETDATE())", fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tg.getMaNv());
            stmt.setString(2, tg.getMaDa());
            if (tg.getNgayThamGia() != null) {
                stmt.setDate(3, Date.valueOf(tg.getNgayThamGia()));
            } else {
                stmt.setNull(3, Types.DATE);
            }
            stmt.executeUpdate();
        }
    }

    // Helper methods for deleting data
    private void deleteParticipationsByGroup(Connection conn, String groupId, String fragment) throws SQLException {
        // Delete ALL participations that reference either employees OR projects from this group
        // This is needed because we're about to delete those employees and projects
        String sql = String.format(
                "DELETE FROM thamgia_%s WHERE manv IN (SELECT manv FROM nhanvien_%s WHERE manhomnc = ?) " +
                "OR mada IN (SELECT mada FROM dean_%s WHERE manhomnc = ?)",
                fragment, fragment, fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            stmt.setString(2, groupId);
            stmt.executeUpdate();
        }
    }

    private void deleteProjectsByGroup(Connection conn, String groupId, String fragment) throws SQLException {
        String sql = String.format("DELETE FROM dean_%s WHERE manhomnc = ?", fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            stmt.executeUpdate();
        }
    }

    private void deleteEmployeesByGroup(Connection conn, String groupId, String fragment) throws SQLException {
        String sql = String.format("DELETE FROM nhanvien_%s WHERE manhomnc = ?", fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            stmt.executeUpdate();
        }
    }

    private void deleteGroup(Connection conn, String groupId, String fragment) throws SQLException {
        String sql = String.format("DELETE FROM nhomnc_%s WHERE manhomnc = ?", fragment);
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, groupId);
            stmt.executeUpdate();
        }
    }

    /**
     * Query 3: Get projects without participants
     * Level 1 (Fragment Transparency)
     */
    public List<DeAn> getProjectsWithoutParticipantsLevel1(String fragment) throws DatabaseException {
        List<DeAn> results = new ArrayList<>();

        try (Connection conn = connectionManager.getConnection(fragment)) {
            String sql = String.format("""
                SELECT d.mada, d.tenda, d.manhomnc
                FROM dean_%s d
                LEFT JOIN thamgia_%s t ON d.mada = t.mada
                WHERE t.mada IS NULL
                """, fragment, fragment);

            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    results.add(new DeAn(
                            rs.getString("mada"),
                            rs.getString("tenda"),
                            rs.getString("manhomnc")
                    ));
                }
            }

            logger.info("Level 1 Query: Found {} projects without participants in fragment {}",
                    results.size(), fragment);
        } catch (SQLException | FragmentException e) {
            throw new DatabaseException("Error getting projects without participants", fragment, e);
        }

        return results;
    }

    /**
     * Query 3: Get projects without participants
     * Level 2 (Location Transparency)
     */
    public List<DeAn> getProjectsWithoutParticipantsLevel2() throws DatabaseException {
        List<DeAn> results = new ArrayList<>();

        for (String fragment : connectionManager.getAllFragments()) {
            try (Connection conn = connectionManager.getConnection(fragment)) {
                String sql = String.format("""
                    SELECT d.mada, d.tenda, d.manhomnc
                    FROM dean_%s d
                    LEFT JOIN thamgia_%s t ON d.mada = t.mada
                    WHERE t.mada IS NULL
                    """, fragment, fragment);

                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        results.add(new DeAn(
                                rs.getString("mada"),
                                rs.getString("tenda"),
                                rs.getString("manhomnc")
                        ));
                    }
                }
            } catch (SQLException | FragmentException e) {
                logger.warn("Error querying fragment {}: {}", fragment, e.getMessage());
            }
        }

        logger.info("Level 2 Query: Found {} total projects without participants", results.size());
        return results;
    }

    /**
     * Get all research groups
     */
    public List<NhomNC> getAllGroups(TransparencyLevel level) throws DatabaseException {
        List<NhomNC> results = new ArrayList<>();

        for (String fragment : connectionManager.getAllFragments()) {
            try (Connection conn = connectionManager.getConnection(fragment)) {
                String sql = String.format(
                        "SELECT manhomnc, tennhomnc, tenphong FROM nhomnc_%s", fragment);

                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {

                    while (rs.next()) {
                        results.add(new NhomNC(
                                rs.getString("manhomnc"),
                                rs.getString("tennhomnc"),
                                rs.getString("tenphong")
                        ));
                    }
                }
            } catch (SQLException | FragmentException e) {
                if (level == TransparencyLevel.FRAGMENT_TRANSPARENCY) {
                    throw new DatabaseException("Error querying fragment " + fragment, fragment, e);
                }
                logger.warn("Error querying fragment {}: {}", fragment, e.getMessage());
            }
        }

        return results;
    }
}
