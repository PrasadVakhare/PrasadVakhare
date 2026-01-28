package com.workflow.service;

import com.workflow.entity.ApprovalStep;
import com.workflow.entity.Request;
import com.workflow.exception.ForbiddenException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Collection;

/**
 * Service for security validation and authorization checks.
 * Enforces business rules like self-approval prevention and role-based access.
 * 
 * Requirements: 3.4, 3.5, 4.3, 4.4, 5.1, 5.2, 10.4
 */
@Service
public class SecurityService {
    
    private final WorkflowConfigurationService workflowConfigurationService;
    
    public SecurityService(WorkflowConfigurationService workflowConfigurationService) {
        this.workflowConfigurationService = workflowConfigurationService;
    }
    
    /**
     * Validates that the user is not attempting to approve or reject their own request.
     * 
     * @param request the request being acted upon
     * @param userId the user attempting the action
     * @throws ForbiddenException if the user is the creator of the request
     * 
     * Requirements: 3.4, 4.3
     */
    public void validateNotSelfApproval(Request request, String userId) {
        if (request.getCreatedBy().equals(userId)) {
            throw new ForbiddenException("Cannot approve or reject own request");
        }
    }
    
    /**
     * Validates that the user has the required role for the current workflow step.
     * Admins can bypass role requirements.
     * 
     * @param request the request being acted upon
     * @param userRole the role of the user attempting the action
     * @throws ForbiddenException if the user does not have the required role
     * 
     * Requirements: 3.5, 4.4, 5.1, 5.2
     */
    public void validateRoleForCurrentStep(Request request, String userRole) {
        // Admin can override any step
        if (isAdmin(userRole)) {
            return;
        }
        
        ApprovalStep currentStep = workflowConfigurationService.getCurrentStep(request);
        
        if (!currentStep.getRole().equals(userRole)) {
            throw new ForbiddenException(
                "User does not have permission to perform this action. Required role: " + 
                currentStep.getRole()
            );
        }
    }
    
    /**
     * Checks if the given role is ADMIN.
     * 
     * @param userRole the role to check
     * @return true if the role is ADMIN, false otherwise
     * 
     * Requirements: 5.1, 5.2
     */
    public boolean isAdmin(String userRole) {
        return "ADMIN".equals(userRole);
    }
    
    /**
     * Extracts the current authenticated user from SecurityContext.
     * 
     * @return UserContext containing user ID and roles
     * @throws IllegalStateException if no authentication found
     * 
     * Requirements: 10.4
     */
    public UserContext getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        
        String userId = authentication.getName();
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        
        // Extract first role (simplified for this implementation)
        String role = authorities.isEmpty() ? "" : 
            authorities.iterator().next().getAuthority().replace("ROLE_", "");
        
        return new UserContext(userId, role);
    }
    
    /**
     * Simple data class to hold user context information.
     */
    public static class UserContext {
        private final String userId;
        private final String role;
        
        public UserContext(String userId, String role) {
            this.userId = userId;
            this.role = role;
        }
        
        public String getUserId() {
            return userId;
        }
        
        public String getRole() {
            return role;
        }
    }
}
