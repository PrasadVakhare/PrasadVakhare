package com.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning request details in API responses.
 * 
 * Requirements: 2.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestResponseDTO {
    
    private Long id;
    private String type;
    private String status;
    private String createdBy;
    private LocalDateTime createdAt;
    private Integer currentStepOrder;
    private String requestData;
}
