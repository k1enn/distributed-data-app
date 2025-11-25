package com.research.distributed.exception;

public class ValidationException extends Exception {
    private final String field;
    private final Object invalidValue;

    public ValidationException(String message) {
        super(message);
        this.field = null;
        this.invalidValue = null;
    }

    public ValidationException(String message, String field) {
        super(message);
        this.field = field;
        this.invalidValue = null;
    }

    public ValidationException(String message, String field, Object invalidValue) {
        super(message);
        this.field = field;
        this.invalidValue = invalidValue;
    }

    public String getField() {
        return field;
    }

    public Object getInvalidValue() {
        return invalidValue;
    }

    public String getDetailedMessage() {
        return String.format("Field: %s | Invalid Value: %s | Message: %s",
                field != null ? field : "N/A",
                invalidValue != null ? invalidValue.toString() : "N/A",
                getMessage());
    }
}
