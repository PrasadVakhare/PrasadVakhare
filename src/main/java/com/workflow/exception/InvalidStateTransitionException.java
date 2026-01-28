package com.workflow.exception;

/**
 * Exception thrown when an invalid state transition is attempted.
 * Maps to HTTP 400 Bad Request.
 * 
 * Requirements: 9.1, 9.2, 9.3, 9.4
 */
public class InvalidStateTransitionException extends RuntimeException {
    
    public InvalidStateTransitionException(String message) {
        super(message);
    }
    
    public InvalidStateTransitionException(String message, Throwable cause) {
        super(message, cause);
    }
}
