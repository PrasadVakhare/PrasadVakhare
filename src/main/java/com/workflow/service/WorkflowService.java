package com.workflow.service;

import com.workflow.entity.ApprovalHistory;
import com.workflow.entity.ApprovalStep;
import com.workflow.entity.Request;
import com.workflow.exception.InvalidStateTransitionException;
import com.workflow.exception.ResourceNotFoundException;
import com.workflow.exception.WorkflowConfigurationException;
import com.workflow.repository.ApprovalHistoryRepository;
import com.workflow.repository.RequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing workflow operations including request creation, approval, and rejection.
 * All operations are transactional to ensure data consistency.
 * 
 * Requirements: 1.1, 1.2, 1.3, 2.1, 2.4, 3.1, 3.2, 3.3, 3.4, 3.5, 4.1, 4.2, 5.1, 6.1
 */
@Service
public class WorkflowService {
    
    private final RequestRepository requestRepository;
    private final ApprovalHistoryRepository approvalHistoryRepository;
    private final WorkflowConfigurationService workflowConfigurationService;
    private final SecurityService securityService;
    
    public WorkflowService(
            RequestRepository requestRepository,
            ApprovalHistoryRepository approvalHistoryRepository,
            WorkflowConfigurationService workflowConfigurationService,
            SecurityService securityService) {
        this.requestRepository = requestRepository;
        this.approvalHistoryRepository = approvalHistoryRepository;
        this.workflowConfigurationService = workflowConfigurationService;
        this.securityService = securityService;
    }
    
    /**
     * Creates a new approval request and initializes the workflow.
     * 
     * @param requestType the type of request (e.g., LEAVE, EXPENSE)
     * @param requestData the request data as a string (JSON or text)
     * @param userId the ID of the user creating the request
     * @return the created Request entity
     * @throws WorkflowConfigurationException if no workflow configuration exists for the request type
     * 
     * Requirements: 1.1, 1.2, 1.3
     */
    @Transactional
    public Request createRequest(String requestType, String requestData, String userId) {
        // Validate workflow configuration exists for this request type
        List<ApprovalStep> approvalSteps = workflowConfigurationService.getApprovalSteps(requestType);
        
        if (approvalSteps.isEmpty()) {
            throw new WorkflowConfigurationException(
                "No workflow configuration found for request type: " + requestType
            );
        }
        
        // Create Request entity with PENDING status
        Request request = new Request();
        request.setType(requestType);
        request.setStatus("PENDING");
        request.setCreatedBy(userId);
        request.setCreatedAt(LocalDateTime.now());
        request.setCurrentStepOrder(1);
        request.setRequestData(requestData);
        
        // Save request
        request = requestRepository.save(request);
        
        // Create ApprovalHistory record with CREATED action
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestId(request.getId());
        history.setAction("CREATED");
        history.setActionBy(userId);
        history.setActionAt(LocalDateTime.now());
        history.setStepOrder(1);
        
        approvalHistoryRepository.save(history);
        
        return request;
    }
    
    /**
     * Retrieves a request by its ID.
     * 
     * @param requestId the ID of the request to retrieve
     * @return the Request entity
     * @throws ResourceNotFoundException if the request is not found
     * 
     * Requirements: 2.1, 2.4
     */
    @Transactional(readOnly = true)
    public Request getRequest(Long requestId) {
        return requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Request with id " + requestId + " not found"
            ));
    }
    
    /**
     * Approves a request and advances the workflow.
     * 
     * @param requestId the ID of the request to approve
     * @param userId the ID of the user approving the request
     * @param userRole the role of the user approving the request
     * @return the updated Request entity
     * @throws ResourceNotFoundException if the request is not found
     * @throws InvalidStateTransitionException if the request is not in PENDING status
     * @throws ForbiddenException if security validations fail
     * 
     * Requirements: 3.1, 3.2, 3.3, 3.4, 3.5, 5.1
     */
    @Transactional
    public Request approveRequest(Long requestId, String userId, String userRole) {
        // Validate request exists
        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Request with id " + requestId + " not found"
            ));
        
        // Validate request is in PENDING status
        if (!"PENDING".equals(request.getStatus())) {
            throw new InvalidStateTransitionException(
                "Cannot approve request with status " + request.getStatus()
            );
        }
        
        // Validate not self-approval
        securityService.validateNotSelfApproval(request, userId);
        
        // Validate role for current step (unless ADMIN)
        securityService.validateRoleForCurrentStep(request, userRole);
        
        // Create ApprovalHistory record with APPROVED action
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestId(request.getId());
        history.setAction("APPROVED");
        history.setActionBy(userId);
        history.setActionAt(LocalDateTime.now());
        history.setStepOrder(request.getCurrentStepOrder());
        
        approvalHistoryRepository.save(history);
        
        // Check if more steps exist
        if (workflowConfigurationService.hasMoreSteps(request)) {
            // More steps exist: increment currentStepOrder
            request.setCurrentStepOrder(request.getCurrentStepOrder() + 1);
        } else {
            // No more steps: set status to APPROVED
            request.setStatus("APPROVED");
        }
        
        // Save all changes in transaction
        return requestRepository.save(request);
    }
    
    /**
     * Rejects a request and terminates the workflow.
     * 
     * @param requestId the ID of the request to reject
     * @param userId the ID of the user rejecting the request
     * @param userRole the role of the user rejecting the request
     * @return the updated Request entity
     * @throws ResourceNotFoundException if the request is not found
     * @throws InvalidStateTransitionException if the request is not in PENDING status
     * @throws ForbiddenException if security validations fail
     * 
     * Requirements: 4.1, 4.2, 4.3, 4.4, 5.2
     */
    @Transactional
    public Request rejectRequest(Long requestId, String userId, String userRole) {
        // Validate request exists
        Request request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Request with id " + requestId + " not found"
            ));
        
        // Validate request is in PENDING status
        if (!"PENDING".equals(request.getStatus())) {
            throw new InvalidStateTransitionException(
                "Cannot reject request with status " + request.getStatus()
            );
        }
        
        // Validate not self-approval
        securityService.validateNotSelfApproval(request, userId);
        
        // Validate role for current step (unless ADMIN)
        securityService.validateRoleForCurrentStep(request, userRole);
        
        // Create ApprovalHistory record with REJECTED action
        ApprovalHistory history = new ApprovalHistory();
        history.setRequestId(request.getId());
        history.setAction("REJECTED");
        history.setActionBy(userId);
        history.setActionAt(LocalDateTime.now());
        history.setStepOrder(request.getCurrentStepOrder());
        
        approvalHistoryRepository.save(history);
        
        // Set status to REJECTED
        request.setStatus("REJECTED");
        
        // Save all changes in transaction
        return requestRepository.save(request);
    }
    
    /**
     * Retrieves the complete approval history for a request.
     * 
     * @param requestId the ID of the request
     * @return list of ApprovalHistory records ordered by actionAt timestamp
     * 
     * Requirements: 6.1
     */
    @Transactional(readOnly = true)
    public List<ApprovalHistory> getApprovalHistory(Long requestId) {
        return approvalHistoryRepository.findByRequestIdOrderByActionAt(requestId);
    }
}
