package com.research.distributed.exception;

public class FragmentException extends Exception {
    private final String fragmentName;
    private final String operation;

    public FragmentException(String message) {
        super(message);
        this.fragmentName = null;
        this.operation = null;
    }

    public FragmentException(String message, String fragmentName) {
        super(message);
        this.fragmentName = fragmentName;
        this.operation = null;
    }

    public FragmentException(String message, String fragmentName, String operation) {
        super(message);
        this.fragmentName = fragmentName;
        this.operation = operation;
    }

    public FragmentException(String message, String fragmentName, Throwable cause) {
        super(message, cause);
        this.fragmentName = fragmentName;
        this.operation = null;
    }

    public String getFragmentName() {
        return fragmentName;
    }

    public String getOperation() {
        return operation;
    }

    public String getDetailedMessage() {
        return String.format("Fragment: %s | Operation: %s | Message: %s",
                fragmentName != null ? fragmentName : "N/A",
                operation != null ? operation : "N/A",
                getMessage());
    }
}
