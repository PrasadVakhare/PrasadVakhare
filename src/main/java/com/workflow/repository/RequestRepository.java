package com.workflow.repository;

import com.workflow.entity.Request;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequestRepository extends JpaRepository<Request, Long> {
    
    List<Request> findByCreatedBy(String createdBy);
    
    List<Request> findByStatus(String status);
}
