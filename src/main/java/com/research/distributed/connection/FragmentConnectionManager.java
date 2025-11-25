package com.research.distributed.connection;

import com.research.distributed.config.DatabaseConfig;
import com.research.distributed.config.FragmentationConfig;
import com.research.distributed.exception.FragmentException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragmentConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(FragmentConnectionManager.class);
    private static FragmentConnectionManager instance;

    private final Map<String, ConnectionPool> fragmentPools;
    private final FragmentationConfig fragmentConfig;
    private boolean initialized = false;

    private FragmentConnectionManager() {
        fragmentPools = new HashMap<>();
        fragmentConfig = FragmentationConfig.getInstance();
    }

    public static synchronized FragmentConnectionManager getInstance() {
        if (instance == null) {
            instance = new FragmentConnectionManager();
        }
        return instance;
    }

    public synchronized void initialize() {
        if (initialized) {
            logger.warn("FragmentConnectionManager already initialized");
            return;
        }

        DatabaseConfig dbConfig = DatabaseConfig.getInstance();

        // Initialize P1 connection pool
        ConnectionPool p1Pool = new ConnectionPool(
                dbConfig.getP1Url(),
                dbConfig.getP1Username(),
                dbConfig.getP1Password(),
                "pool-p1"
        );
        fragmentPools.put("p1", p1Pool);

        // Initialize P2 connection pool
        ConnectionPool p2Pool = new ConnectionPool(
                dbConfig.getP2Url(),
                dbConfig.getP2Username(),
                dbConfig.getP2Password(),
                "pool-p2"
        );
        fragmentPools.put("p2", p2Pool);

        initialized = true;
        logger.info("FragmentConnectionManager initialized with {} fragments", fragmentPools.size());
    }

    public Connection getConnection(String fragment) throws SQLException, FragmentException {
        if (!initialized) {
            throw new FragmentException("FragmentConnectionManager not initialized");
        }

        if (!fragmentConfig.isValidFragment(fragment)) {
            throw new FragmentException("Invalid fragment: " + fragment, fragment);
        }

        ConnectionPool pool = fragmentPools.get(fragment);
        if (pool == null) {
            throw new FragmentException("No connection pool for fragment: " + fragment, fragment);
        }

        return pool.getConnection();
    }

    public Connection getConnectionForDepartment(String department) throws SQLException, FragmentException {
        String fragment = fragmentConfig.getFragmentForDepartment(department);
        if (fragment == null) {
            throw new FragmentException("No fragment found for department: " + department);
        }
        return getConnection(fragment);
    }

    public Connection getConnectionForGroup(String groupId) throws SQLException, FragmentException {
        String fragment = fragmentConfig.getFragmentForGroup(groupId);
        if (fragment == null) {
            throw new FragmentException("Cannot determine fragment for group: " + groupId);
        }
        return getConnection(fragment);
    }

    public List<String> getAllFragments() {
        return fragmentConfig.getAllFragments();
    }

    public void shutdown() {
        logger.info("Shutting down FragmentConnectionManager");
        for (Map.Entry<String, ConnectionPool> entry : fragmentPools.entrySet()) {
            try {
                entry.getValue().close();
                logger.info("Closed connection pool for fragment: {}", entry.getKey());
            } catch (Exception e) {
                logger.error("Error closing connection pool for fragment {}: {}",
                        entry.getKey(), e.getMessage());
            }
        }
        fragmentPools.clear();
        initialized = false;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public String getFragmentForGroup(String groupId) {
        return fragmentConfig.getFragmentForGroup(groupId);
    }

    public String getTableName(String baseTable, String fragment) {
        return fragmentConfig.getTableName(baseTable, fragment);
    }

    public void logPoolStats() {
        for (Map.Entry<String, ConnectionPool> entry : fragmentPools.entrySet()) {
            ConnectionPool pool = entry.getValue();
            logger.info("Pool {} - Active: {}, Idle: {}, Total: {}",
                    entry.getKey(),
                    pool.getActiveConnections(),
                    pool.getIdleConnections(),
                    pool.getTotalConnections());
        }
    }
}
