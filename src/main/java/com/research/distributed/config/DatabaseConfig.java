package com.research.distributed.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {
    private static final String CONFIG_FILE = "/application.properties";
    private static DatabaseConfig instance;
    private final Properties properties;

    private DatabaseConfig() {
        properties = new Properties();
        loadProperties();
    }

    public static synchronized DatabaseConfig getInstance() {
        if (instance == null) {
            instance = new DatabaseConfig();
        }
        return instance;
    }

    private void loadProperties() {
        try (InputStream input = getClass().getResourceAsStream(CONFIG_FILE)) {
            if (input != null) {
                properties.load(input);
            } else {
                setDefaultProperties();
            }
        } catch (IOException e) {
            setDefaultProperties();
        }
    }

    private void setDefaultProperties() {
        // P1 Database configuration
        properties.setProperty("db.p1.url", "jdbc:sqlserver://localhost:14331;databaseName=ResearchDB_P1;encrypt=false;trustServerCertificate=true");
        properties.setProperty("db.p1.username", "sa");
        properties.setProperty("db.p1.password", "YourStrong@Pass123");
        properties.setProperty("db.p1.department", "P1");

        // P2 Database configuration
        properties.setProperty("db.p2.url", "jdbc:sqlserver://localhost:14332;databaseName=ResearchDB_P2;encrypt=false;trustServerCertificate=true");
        properties.setProperty("db.p2.username", "sa");
        properties.setProperty("db.p2.password", "YourStrong@Pass123");
        properties.setProperty("db.p2.department", "P2");

        // Connection pool configuration
        properties.setProperty("pool.maxSize", "10");
        properties.setProperty("pool.minIdle", "2");
        properties.setProperty("pool.connectionTimeout", "5000");
        properties.setProperty("pool.idleTimeout", "300000");
        properties.setProperty("pool.maxLifetime", "600000");
    }

    public String getProperty(String key) {
        return properties.getProperty(key);
    }

    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }

    public int getIntProperty(String key, int defaultValue) {
        String value = properties.getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }

    public String getP1Url() {
        return getProperty("db.p1.url");
    }

    public String getP1Username() {
        return getProperty("db.p1.username");
    }

    public String getP1Password() {
        return getProperty("db.p1.password");
    }

    public String getP2Url() {
        return getProperty("db.p2.url");
    }

    public String getP2Username() {
        return getProperty("db.p2.username");
    }

    public String getP2Password() {
        return getProperty("db.p2.password");
    }

    public int getPoolMaxSize() {
        return getIntProperty("pool.maxSize", 10);
    }

    public int getPoolMinIdle() {
        return getIntProperty("pool.minIdle", 2);
    }

    public int getPoolConnectionTimeout() {
        return getIntProperty("pool.connectionTimeout", 5000);
    }

    public int getPoolIdleTimeout() {
        return getIntProperty("pool.idleTimeout", 300000);
    }

    public int getPoolMaxLifetime() {
        return getIntProperty("pool.maxLifetime", 600000);
    }
}
