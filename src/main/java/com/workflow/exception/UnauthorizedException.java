package com.workflow.exception;

/**
 * Exception thrown when authentication is missing or invalid.
 * Maps to HTTP 401 Unauthorized.
 * 
 * Requirements: 3.4, 3.5
 */
public class UnauthorizedException extends RuntimeException {
    
    public UnauthorizedException(String message) {
        super(message);
    }
    
    public UnauthorizedException(String message, Throwable cause) {
        super(message, cause);
    }
}
