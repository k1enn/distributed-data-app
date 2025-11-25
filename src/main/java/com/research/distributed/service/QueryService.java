package com.research.distributed.service;

import com.research.distributed.connection.FragmentConnectionManager;
import com.research.distributed.connection.TransparencyLevel;
import com.research.distributed.exception.DatabaseException;
import com.research.distributed.exception.FragmentException;
import com.research.distributed.exception.ValidationException;
import com.research.distributed.model.DeAn;
import com.research.distributed.model.NhomNC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
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
            String oldFragment = connectionManager.getFragmentForGroup(groupId);
            if (oldFragment == null) {
                throw new ValidationException("Cannot find group: " + groupId, "groupId", groupId);
            }

            String currentDepartment = getDepartmentForGroup(groupId, oldFragment);
            if (currentDepartment != null && currentDepartment.equals(newDepartment)) {
                throw new ValidationException("Group already in department " + newDepartment,
                        "department", newDepartment);
            }

            String newFragment = newDepartment.equals("P1") ? "p1" : "p2";

            if (oldFragment.equals(newFragment)) {
                // Just update the department in same fragment
                updateDepartmentInPlace(groupId, newDepartment, oldFragment);
            } else {
                // Need to migrate to different fragment
                migrateGroupBetweenFragments(groupId, oldFragment, newFragment, newDepartment);
            }

            logger.info("Successfully updated department for group {} to {}", groupId, newDepartment);
        } catch (SQLException | FragmentException e) {
            throw new DatabaseException("Error updating department", e);
        }
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
        // This is a complex operation that requires:
        // 1. Get all data for the group from old fragment
        // 2. Insert into new fragment
        // 3. Delete from old fragment
        // All in a coordinated manner

        logger.info("Migrating group {} from fragment {} to {}", groupId, oldFragment, newFragment);

        // For simplicity, we'll throw an exception indicating this requires manual migration
        throw new DatabaseException(
                "Migration between fragments requires careful handling of all related data. " +
                        "Please use the CRUD operations to manually migrate group " + groupId +
                        " from " + oldFragment + " to " + newFragment);
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
