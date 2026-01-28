package com.workflow.controller;

import com.workflow.entity.ApprovalHistory;
import com.workflow.entity.Request;
import com.workflow.service.SecurityService;
import com.workflow.service.WorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller for managing approval workflow requests.
 * All endpoints require JWT authentication.
 * 
 * Requirements: 1.1, 1.4, 2.1, 2.3, 3.1, 3.6, 4.1, 4.5, 6.1, 6.2, 6.4
 */
@RestController
@RequestMapping("/requests")
public class RequestController {
    
    private final WorkflowService workflowService;
    private final SecurityService securityService;
    
    public RequestController(WorkflowService workflowService, SecurityService securityService) {
        this.workflowService = workflowService;
        this.securityService = securityService;
    }
    
    /**
     * Creates a new approval request.
     * 
     * @param requestBody map containing "type" and "requestData" fields
     * @return 201 Created with the created request
     * 
     * Requirements: 1.1, 1.4
     */
    @PostMapping
    @PreAuthorize("hasRole('REQUESTER')")
    public ResponseEntity<Request> createRequest(@RequestBody Map<String, String> requestBody) {
        // Extract user from SecurityContext
        SecurityService.UserContext currentUser = securityService.getCurrentUser();
        
        // Extract request data
        String requestType = requestBody.get("type");
        String requestData = requestBody.get("requestData");
        
        // Call WorkflowService.createRequest
        Request createdRequest = workflowService.createRequest(requestType, requestData, currentUser.getUserId());
        
        // Return 201 Created with request details
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRequest);
    }
    
    /**
     * Retrieves a request by its ID.
     * 
     * @param id the request ID
     * @return 200 OK with the request details
     * 
     * Requirements: 2.1, 2.3
     */
    @GetMapping("/{id}")
    public ResponseEntity<Request> getRequest(@PathVariable Long id) {
        // Extract user from SecurityContext (authentication check)
        securityService.getCurrentUser();
        
        // Call WorkflowService.getRequest
        Request request = workflowService.getRequest(id);
        
        // Return 200 OK with request details
        return ResponseEntity.ok(request);
    }
    
    /**
     * Approves a request at the current workflow step.
     * 
     * @param id the request ID
     * @return 200 OK with the updated request
     * 
     * Requirements: 3.1, 3.6
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('APPROVER') or hasRole('ADMIN')")
    public ResponseEntity<Request> approveRequest(@PathVariable Long id) {
        // Extract user and roles from SecurityContext
        SecurityService.UserContext currentUser = securityService.getCurrentUser();
        
        // Call WorkflowService.approveRequest
        Request updatedRequest = workflowService.approveRequest(id, currentUser.getUserId(), currentUser.getRole());
        
        // Return 200 OK with updated request
        return ResponseEntity.ok(updatedRequest);
    }
    
    /**
     * Rejects a request at the current workflow step.
     * 
     * @param id the request ID
     * @return 200 OK with the updated request
     * 
     * Requirements: 4.1, 4.5
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('APPROVER') or hasRole('ADMIN')")
    public ResponseEntity<Request> rejectRequest(@PathVariable Long id) {
        // Extract user and roles from SecurityContext
        SecurityService.UserContext currentUser = securityService.getCurrentUser();
        
        // Call WorkflowService.rejectRequest
        Request updatedRequest = workflowService.rejectRequest(id, currentUser.getUserId(), currentUser.getRole());
        
        // Return 200 OK with updated request
        return ResponseEntity.ok(updatedRequest);
    }
    
    /**
     * Retrieves the approval history for a request.
     * Only accessible by the requester (for their own requests) or ADMIN.
     * 
     * @param id the request ID
     * @return 200 OK with the approval history records
     * 
     * Requirements: 6.1, 6.2, 6.4
     */
    @GetMapping("/history/{id}")
    public ResponseEntity<List<ApprovalHistory>> getApprovalHistory(@PathVariable Long id) {
        // Extract user and roles from SecurityContext
        SecurityService.UserContext currentUser = securityService.getCurrentUser();
        
        // Get the request to check ownership
        Request request = workflowService.getRequest(id);
        
        // Validate REQUESTER (for own requests) or ADMIN role
        boolean isAdmin = securityService.isAdmin(currentUser.getRole());
        boolean isOwner = request.getCreatedBy().equals(currentUser.getUserId());
        
        if (!isAdmin && !isOwner) {
            throw new com.workflow.exception.ForbiddenException(
                "User does not have permission to view this request's history"
            );
        }
        
        // Call WorkflowService.getApprovalHistory
        List<ApprovalHistory> history = workflowService.getApprovalHistory(id);
        
        // Return 200 OK with history records
        return ResponseEntity.ok(history);
    }
}
