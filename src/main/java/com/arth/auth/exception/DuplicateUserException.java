package com.arth.auth.exception;

public class DuplicateUserException extends RuntimeException {
    private String statusCode;
    private String message;

    public DuplicateUserException(String statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
        this.message = message;
    }

    public String getStatusCode() {
        return statusCode;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
