-- Sample Workflow Configuration Data
-- This file initializes the approval_steps table with sample workflow configurations
-- for LEAVE and EXPENSE request types

-- LEAVE Workflow Configuration
-- Two-step approval process: Manager -> HR
INSERT INTO approval_steps (request_type, step_order, role, description) 
VALUES 
  ('LEAVE', 1, 'APPROVER', 'Manager Approval'),
  ('LEAVE', 2, 'APPROVER', 'HR Approval');

-- EXPENSE Workflow Configuration
-- Two-step approval process: Manager -> Finance
INSERT INTO approval_steps (request_type, step_order, role, description) 
VALUES 
  ('EXPENSE', 1, 'APPROVER', 'Manager Approval'),
  ('EXPENSE', 2, 'APPROVER', 'Finance Approval');
