package com.research.distributed.exception;

import java.sql.SQLException;

public class DatabaseException extends Exception {
    private final String errorCode;
    private final String fragment;

    public DatabaseException(String message) {
        super(message);
        this.errorCode = "DB-UNKNOWN";
        this.fragment = null;
    }

    public DatabaseException(String message, Throwable cause) {
        super(message, cause);
        this.errorCode = generateErrorCode(cause);
        this.fragment = null;
    }

    public DatabaseException(String message, String fragment, Throwable cause) {
        super(message, cause);
        this.errorCode = generateErrorCode(cause);
        this.fragment = fragment;
    }

    private String generateErrorCode(Throwable cause) {
        if (cause instanceof SQLException) {
            SQLException sqlEx = (SQLException) cause;
            return "SQL-" + sqlEx.getErrorCode();
        }
        return "DB-UNKNOWN";
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getFragment() {
        return fragment;
    }

    public String getDetailedMessage() {
        return String.format("Error Code: %s | Fragment: %s | Message: %s | Cause: %s",
                errorCode,
                fragment != null ? fragment : "N/A",
                getMessage(),
                getCause() != null ? getCause().getMessage() : "Unknown");
    }
}
