package com.workflow.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for returning approval history records in API responses.
 * 
 * Requirements: 6.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalHistoryResponseDTO {
    
    private Long id;
    private Long requestId;
    private String action;
    private String actionBy;
    private LocalDateTime actionAt;
    private String comments;
    private Integer stepOrder;
}
