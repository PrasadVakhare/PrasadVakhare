package com.workflow.service;

import com.workflow.entity.ApprovalStep;
import com.workflow.entity.Request;
import com.workflow.exception.WorkflowConfigurationException;
import com.workflow.repository.ApprovalStepRepository;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing workflow configuration loaded from database.
 * Provides methods to query approval steps and determine workflow progression.
 * 
 * Requirements: 7.1, 7.2, 7.3, 2.4, 3.2, 3.3
 */
@Service
public class WorkflowConfigurationService {
    
    private final ApprovalStepRepository approvalStepRepository;
    
    public WorkflowConfigurationService(ApprovalStepRepository approvalStepRepository) {
        this.approvalStepRepository = approvalStepRepository;
    }
    
    /**
     * Load all approval steps for a given request type from database.
     * 
     * @param requestType the type of request (e.g., LEAVE, EXPENSE)
     * @return list of approval steps ordered by step order
     * @throws WorkflowConfigurationException if no steps found for request type
     * 
     * Requirements: 7.1
     */
    public List<ApprovalStep> getApprovalSteps(String requestType) {
        List<ApprovalStep> steps = approvalStepRepository.findByRequestTypeOrderByStepOrder(requestType);
        
        if (steps == null || steps.isEmpty()) {
            throw new WorkflowConfigurationException(
                "No workflow configuration found for request type: " + requestType
            );
        }
        
        return steps;
    }
    
    /**
     * Get the current approval step for a request based on its current state.
     * 
     * @param request the request entity
     * @return the current approval step
     * @throws WorkflowConfigurationException if current step not found
     * 
     * Requirements: 2.4, 7.2
     */
    public ApprovalStep getCurrentStep(Request request) {
        if (request.getCurrentStepOrder() == null) {
            throw new WorkflowConfigurationException(
                "Request does not have a current step order set"
            );
        }
        
        ApprovalStep currentStep = approvalStepRepository.findByRequestTypeAndStepOrder(
            request.getType(),
            request.getCurrentStepOrder()
        );
        
        if (currentStep == null) {
            throw new WorkflowConfigurationException(
                "No approval step found for request type " + request.getType() + 
                " at step order " + request.getCurrentStepOrder()
            );
        }
        
        return currentStep;
    }
    
    /**
     * Get the next approval step in the workflow.
     * 
     * @param request the request entity
     * @return the next approval step, or null if no more steps exist
     * 
     * Requirements: 3.2, 7.3
     */
    public ApprovalStep getNextStep(Request request) {
        if (request.getCurrentStepOrder() == null) {
            return null;
        }
        
        Integer nextStepOrder = request.getCurrentStepOrder() + 1;
        
        return approvalStepRepository.findByRequestTypeAndStepOrder(
            request.getType(),
            nextStepOrder
        );
    }
    
    /**
     * Check if the workflow has more steps after the current one.
     * 
     * @param request the request entity
     * @return true if more steps exist, false otherwise
     * 
     * Requirements: 3.2, 3.3
     */
    public boolean hasMoreSteps(Request request) {
        return getNextStep(request) != null;
    }
}
