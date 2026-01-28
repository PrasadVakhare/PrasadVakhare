package com.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for returning error responses in API responses.
 * Provides a consistent error response format across all endpoints.
 * 
 * Requirements: 1.4, 1.5, 2.2, 2.3, 3.4, 3.5, 3.6, 4.3, 4.4, 4.5, 6.2, 6.3, 6.4
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponseDTO {
    
    private String timestamp;
    private Integer status;
    private String error;
    private String message;
    private String path;
}
