package com.workflow.exception;

import jakarta.persistence.OptimisticLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Global exception handler for the Approval Workflow Engine.
 * Maps domain exceptions to appropriate HTTP status codes with consistent error responses.
 * 
 * Requirements: 1.4, 1.5, 2.2, 2.3, 3.4, 3.5, 3.6, 4.3, 4.4, 4.5, 6.2, 6.3, 6.4, 
 *               9.1, 9.2, 9.3, 9.4, 10.1, 10.2, 10.3
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle ResourceNotFoundException - maps to 404 Not Found
     * Requirements: 2.2, 6.3
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFoundException(
            ResourceNotFoundException ex, WebRequest request) {
        return buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "Not Found",
            ex.getMessage(),
            request
        );
    }

    /**
     * Handle UnauthorizedException - maps to 401 Unauthorized
     * Requirements: 10.1, 10.2
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedException(
            UnauthorizedException ex, WebRequest request) {
        return buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            ex.getMessage(),
            request
        );
    }

    /**
     * Handle ForbiddenException - maps to 403 Forbidden
     * Requirements: 3.4, 3.5, 4.3, 4.4, 6.2, 10.3
     */
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<Map<String, Object>> handleForbiddenException(
            ForbiddenException ex, WebRequest request) {
        return buildErrorResponse(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            ex.getMessage(),
            request
        );
    }

    /**
     * Handle Spring Security AccessDeniedException - maps to 403 Forbidden
     * This handles authorization failures from @PreAuthorize annotations
     * Requirements: 10.3
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDeniedException(
            AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(
            HttpStatus.FORBIDDEN,
            "Forbidden",
            "User does not have permission to perform this action",
            request
        );
    }

    /**
     * Handle InvalidStateTransitionException - maps to 400 Bad Request
     * Requirements: 9.1, 9.2, 9.3, 9.4
     */
    @ExceptionHandler(InvalidStateTransitionException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidStateTransitionException(
            InvalidStateTransitionException ex, WebRequest request) {
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            ex.getMessage(),
            request
        );
    }

    /**
     * Handle WorkflowConfigurationException - maps to 400 Bad Request
     * Requirements: 1.5
     */
    @ExceptionHandler(WorkflowConfigurationException.class)
    public ResponseEntity<Map<String, Object>> handleWorkflowConfigurationException(
            WorkflowConfigurationException ex, WebRequest request) {
        return buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Bad Request",
            ex.getMessage(),
            request
        );
    }

    /**
     * Handle OptimisticLockException - maps to 409 Conflict
     * Requirements: 8.4
     */
    @ExceptionHandler(OptimisticLockException.class)
    public ResponseEntity<Map<String, Object>> handleOptimisticLockException(
            OptimisticLockException ex, WebRequest request) {
        return buildErrorResponse(
            HttpStatus.CONFLICT,
            "Conflict",
            "Request was modified by another user. Please refresh and try again",
            request
        );
    }

    /**
     * Handle generic exceptions - maps to 500 Internal Server Error
     * Catches any unexpected exceptions to prevent exposing sensitive system information
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(
            Exception ex, WebRequest request) {
        // Log the full exception for debugging (in production, use proper logging framework)
        System.err.println("Unexpected error: " + ex.getClass().getName() + " - " + ex.getMessage());
        ex.printStackTrace();
        
        return buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal Server Error",
            "An unexpected error occurred",
            request
        );
    }

    /**
     * Build a consistent error response structure
     * 
     * @param status HTTP status code
     * @param error Error type/category
     * @param message Detailed error message
     * @param request Web request context
     * @return ResponseEntity with error details
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(
            HttpStatus status, String error, String message, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", error);
        body.put("message", message);
        body.put("path", request.getDescription(false).replace("uri=", ""));
        
        return new ResponseEntity<>(body, status);
    }
}
