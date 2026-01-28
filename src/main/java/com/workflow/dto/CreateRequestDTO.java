package com.workflow.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating a new approval request.
 * 
 * Requirements: 1.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateRequestDTO {
    
    @NotBlank(message = "Request type is required")
    private String type;
    
    @NotBlank(message = "Request data is required")
    private String requestData;
}
