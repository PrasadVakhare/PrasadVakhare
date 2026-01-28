package com.workflow.repository;

import com.workflow.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    
    List<ApprovalStep> findByRequestTypeOrderByStepOrder(String requestType);
    
    ApprovalStep findByRequestTypeAndStepOrder(String requestType, Integer stepOrder);
}
