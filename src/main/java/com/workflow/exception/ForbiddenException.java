package com.workflow.exception;

/**
 * Exception thrown when a user lacks permission to perform an action.
 * Maps to HTTP 403 Forbidden.
 * 
 * Requirements: 3.4, 3.5
 */
public class ForbiddenException extends RuntimeException {
    
    public ForbiddenException(String message) {
        super(message);
    }
    
    public ForbiddenException(String message, Throwable cause) {
        super(message, cause);
    }
}
