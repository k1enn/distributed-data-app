package com.research.distributed.dao;

import com.research.distributed.connection.FragmentConnectionManager;
import com.research.distributed.connection.TransparencyLevel;
import com.research.distributed.exception.DatabaseException;
import com.research.distributed.exception.FragmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseDAO<T> {
    protected static final Logger logger = LoggerFactory.getLogger(BaseDAO.class);
    protected final FragmentConnectionManager connectionManager;
    protected final String baseTableName;

    protected BaseDAO(String baseTableName) {
        this.connectionManager = FragmentConnectionManager.getInstance();
        this.baseTableName = baseTableName;
    }

    protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

    protected String getTableName(String fragment) {
        return connectionManager.getTableName(baseTableName, fragment);
    }

    protected List<T> executeQueryAllFragments(String sqlTemplate, TransparencyLevel level)
            throws DatabaseException {
        List<T> results = new ArrayList<>();

        for (String fragment : connectionManager.getAllFragments()) {
            String tableName = getTableName(fragment);
            String sql = sqlTemplate.replace("{TABLE}", tableName);

            try (Connection conn = connectionManager.getConnection(fragment);
                 PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs));
                }
            } catch (SQLException | FragmentException e) {
                logger.error("Error querying fragment {}: {}", fragment, e.getMessage());
                if (level == TransparencyLevel.FRAGMENT_TRANSPARENCY) {
                    throw new DatabaseException("Error querying fragment " + fragment, fragment, e);
                }
                // For location transparency, continue to next fragment
            }
        }

        return results;
    }

    protected List<T> executeQuerySingleFragment(String sqlTemplate, String fragment,
                                                  Object... params) throws DatabaseException {
        List<T> results = new ArrayList<>();
        String tableName = getTableName(fragment);
        String sql = sqlTemplate.replace("{TABLE}", tableName);

        try (Connection conn = connectionManager.getConnection(fragment);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    results.add(mapResultSetToEntity(rs));
                }
            }
        } catch (SQLException | FragmentException e) {
            throw new DatabaseException("Error querying fragment " + fragment, fragment, e);
        }

        return results;
    }

    protected int executeUpdateSingleFragment(String sqlTemplate, String fragment,
                                               Object... params) throws DatabaseException {
        String tableName = getTableName(fragment);
        String sql = sqlTemplate.replace("{TABLE}", tableName);

        try (Connection conn = connectionManager.getConnection(fragment);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            int affected = stmt.executeUpdate();
            logger.debug("Updated {} rows in fragment {}", affected, fragment);
            return affected;
        } catch (SQLException | FragmentException e) {
            throw new DatabaseException("Error updating fragment " + fragment, fragment, e);
        }
    }

    protected void executeInsertSingleFragment(String sqlTemplate, String fragment,
                                                Object... params) throws DatabaseException {
        String tableName = getTableName(fragment);
        String sql = sqlTemplate.replace("{TABLE}", tableName);

        try (Connection conn = connectionManager.getConnection(fragment);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            stmt.executeUpdate();
            logger.debug("Inserted into fragment {}", fragment);
        } catch (SQLException | FragmentException e) {
            throw new DatabaseException("Error inserting into fragment " + fragment, fragment, e);
        }
    }

    protected int executeDeleteSingleFragment(String sqlTemplate, String fragment,
                                               Object... params) throws DatabaseException {
        String tableName = getTableName(fragment);
        String sql = sqlTemplate.replace("{TABLE}", tableName);

        try (Connection conn = connectionManager.getConnection(fragment);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            int affected = stmt.executeUpdate();
            logger.debug("Deleted {} rows from fragment {}", affected, fragment);
            return affected;
        } catch (SQLException | FragmentException e) {
            throw new DatabaseException("Error deleting from fragment " + fragment, fragment, e);
        }
    }
}
