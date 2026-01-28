package com.workflow.exception;

/**
 * Exception thrown when workflow configuration is missing or invalid.
 * Maps to HTTP 400 Bad Request.
 * 
 * Requirements: 2.2, 3.4, 3.5, 9.1, 9.2, 9.3, 9.4
 */
public class WorkflowConfigurationException extends RuntimeException {
    
    public WorkflowConfigurationException(String message) {
        super(message);
    }
    
    public WorkflowConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
