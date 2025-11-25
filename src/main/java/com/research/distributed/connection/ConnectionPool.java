package com.research.distributed.connection;

import com.research.distributed.config.DatabaseConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

public class ConnectionPool {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionPool.class);
    private final HikariDataSource dataSource;
    private final String poolName;

    public ConnectionPool(String jdbcUrl, String username, String password, String poolName) {
        this.poolName = poolName;
        DatabaseConfig config = DatabaseConfig.getInstance();

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(jdbcUrl);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
        hikariConfig.setPoolName(poolName);
        hikariConfig.setMaximumPoolSize(config.getPoolMaxSize());
        hikariConfig.setMinimumIdle(config.getPoolMinIdle());
        hikariConfig.setConnectionTimeout(config.getPoolConnectionTimeout());
        hikariConfig.setIdleTimeout(config.getPoolIdleTimeout());
        hikariConfig.setMaxLifetime(config.getPoolMaxLifetime());
        hikariConfig.setConnectionTestQuery("SELECT 1");

        // Additional MSSQL specific settings
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        this.dataSource = new HikariDataSource(hikariConfig);
        logger.info("Connection pool '{}' initialized for URL: {}", poolName, jdbcUrl);
    }

    public Connection getConnection() throws SQLException {
        try {
            Connection conn = dataSource.getConnection();
            logger.debug("Connection obtained from pool '{}'", poolName);
            return conn;
        } catch (SQLException e) {
            logger.error("Failed to obtain connection from pool '{}': {}", poolName, e.getMessage());
            throw new SQLException("Failed to obtain connection from pool: " + poolName, e);
        }
    }

    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("Connection pool '{}' closed", poolName);
        }
    }

    public boolean isActive() {
        return dataSource != null && !dataSource.isClosed();
    }

    public String getPoolName() {
        return poolName;
    }

    public int getActiveConnections() {
        return dataSource.getHikariPoolMXBean().getActiveConnections();
    }

    public int getIdleConnections() {
        return dataSource.getHikariPoolMXBean().getIdleConnections();
    }

    public int getTotalConnections() {
        return dataSource.getHikariPoolMXBean().getTotalConnections();
    }
}
