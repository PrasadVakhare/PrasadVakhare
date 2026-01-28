# Approval Workflow Engine


## Table of Contents

- [Overview](#overview)
- [Key Features](#key-features)
- [Architecture](#architecture)
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [API Documentation](#api-documentation)
- [Workflow Configuration](#workflow-configuration)
- [Security](#security)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Development Workflow](#development-workflow)
- [End-to-End Testing](#end-to-end-testing)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)
- [Contributing](#contributing)
- [License](#license)

---

## Overview

The Approval Workflow Engine is an enterprise-grade backend system that eliminates hardcoded approval logic by storing all workflow definitions in a database. This enables organizations to configure and modify approval workflows without code changes, providing unprecedented flexibility and maintainability.


### What Makes This Project Unique?

1. **Database-Driven Configuration**: Zero hardcoded if-else statements for workflow logic
2. **Formal Correctness**: 16 correctness properties validated through property-based testing
3. **Complete Documentation**: Full requirements, design, and implementation specifications
4. **Production-Ready**: Transaction safety, optimistic locking, comprehensive error handling
5. **Security-First**: JWT authentication, role-based authorization, self-approval prevention

### Use Cases

- **Leave Request Management**: Multi-level approval workflows for employee leave requests
- **Expense Approvals**: Configurable approval chains for expense reports
- **Purchase Orders**: Dynamic approval routing based on amount thresholds
- **Document Approvals**: Content review and approval workflows
- **Any Multi-Step Approval Process**: Fully extensible to any business workflow

### Why This Engine?

- **Zero Code Changes**: Add new workflows by inserting database records
- **Dynamic Configuration**: Modify approval chains without redeployment
- **Enterprise Security**: JWT-based authentication with role-based access control
- **Audit Trail**: Complete immutable history of all workflow actions
- **Transaction Safety**: ACID compliance with optimistic locking
- **Property-Based Testing**: Formal correctness guarantees through advanced testing

---

## Key Features

###  Database-Driven Configuration
- All workflow logic stored in database tables
- No hardcoded if-else or switch-case statements
- Add new request types without code modifications
- Modify approval chains dynamically

###  Enterprise Security
- JWT-based authentication on all endpoints
- Role-based authorization (REQUESTER, APPROVER, ADMIN)
- Self-approval prevention
- Admin override capabilities
- Spring Security integration

###  State Machine Pattern
- Clear state transitions (PENDING → APPROVED/REJECTED)
- Multi-step approval progression
- Workflow termination on rejection
- Optimistic locking for concurrent modifications

###  Complete Audit Trail
- Immutable approval history
- Chronological action recording
- User identity and timestamp tracking
- Comments and metadata support

###  Comprehensive Testing
- 80%+ code coverage
- Property-based testing with jqwik
- Unit and integration tests
- Formal correctness properties

### Production Ready
- Transaction management
- Error handling and validation
- H2 in-memory database (easily swappable)
- RESTful API design
- Comprehensive logging

---

## Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Applications                     │
│            (Web, Mobile, Desktop with JWT tokens)            │
└─────────────────────────────────────────────────────────────┘
                            ↓ HTTPS
┌─────────────────────────────────────────────────────────────┐
│                    REST API Layer                            │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  RequestController (Spring MVC)                      │   │
│  │  - POST /requests                                    │   │
│  │  - GET /requests/{id}                                │   │
│  │  - POST /requests/{id}/approve                       │   │
│  │  - POST /requests/{id}/reject                        │   │
│  │  - GET /requests/history/{id}                        │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   Security Layer                             │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  JwtAuthenticationFilter                             │   │
│  │  - Validates JWT tokens                              │   │
│  │  - Extracts user identity and roles                  │   │
│  │  - Populates SecurityContext                         │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                   Service Layer                              │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  WorkflowService                                     │   │
│  │  - Orchestrates workflow operations                  │   │
│  │  - Enforces business rules                           │   │
│  │  - Manages state transitions                         │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  WorkflowConfigurationService                        │   │
│  │  - Loads workflow definitions from database          │   │
│  │  - Determines current and next steps                 │   │
│  └──────────────────────────────────────────────────────┘   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  SecurityService                                     │   │
│  │  - Validates security rules                          │   │
│  │  - Checks self-approval prevention                   │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                 Repository Layer (Spring Data JPA)           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  RequestRepository                                   │   │
│  │  ApprovalStepRepository                              │   │
│  │  ApprovalHistoryRepository                           │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↓
┌─────────────────────────────────────────────────────────────┐
│                    H2 Database (In-Memory)                   │
│  ┌──────────────────────────────────────────────────────┐   │
│  │  Tables:                                             │   │
│  │  - requests (workflow instances)                     │   │
│  │  - approval_steps (workflow definitions)             │   │
│  │  - approval_history (audit trail)                    │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Workflow State Machine

The engine implements a state machine pattern for request lifecycle management:

```
┌─────────┐
│ CREATED │
└────┬────┘
     │
     ↓
┌─────────┐     Approve (more steps)     ┌─────────┐
│ PENDING ├──────────────────────────────→│ PENDING │
└────┬────┘                               └────┬────┘
     │                                         │
     │ Approve (final step)                    │ Approve (final step)
     ↓                                         ↓
┌──────────┐                            ┌──────────┐
│ APPROVED │                            │ APPROVED │
└──────────┘                            └──────────┘
     
┌─────────┐
│ PENDING │
└────┬────┘
     │
     │ Reject (any step)
     ↓
┌──────────┐
│ REJECTED │
└──────────┘
```

**State Transitions**:
- **CREATED → PENDING**: Automatic on request creation
- **PENDING → PENDING**: Approval at non-final step (increments currentStepOrder)
- **PENDING → APPROVED**: Approval at final step
- **PENDING → REJECTED**: Rejection at any step (terminates workflow)
- **APPROVED/REJECTED**: Terminal states (no further transitions)

### Design Principles

### Database-Driven Workflow Approach

The Approval Workflow Engine eliminates hardcoded approval logic by storing all workflow definitions in the database. This design provides:

- **Dynamic Configuration**: Workflows are defined in the `approval_steps` table, allowing changes without code modifications
- **Extensibility**: New request types can be added by inserting configuration records
- **Maintainability**: Business rules are separated from application code
- **Flexibility**: Different workflows can have different numbers of steps and role requirements

**Key Principle**: The engine queries the database to determine workflow behavior rather than using if-else or switch-case statements based on request types.

### State Machine Pattern

Requests transition through states based on approval actions:

```
[CREATED] → PENDING → PENDING (step 1) → PENDING (step 2) → ... → APPROVED
                   ↓
                REJECTED
```

**State Transitions**:
- **PENDING**: Request is awaiting approval at current step
- **APPROVED**: All approval steps completed successfully
- **REJECTED**: Request was rejected at any step (workflow terminates)

**Workflow Progression**:
1. Request created with `status = PENDING` and `currentStepOrder = 1`
2. Each approval increments `currentStepOrder` if more steps exist
3. Final approval changes `status = APPROVED`
4. Any rejection changes `status = REJECTED` and terminates workflow

### Security Architecture

**JWT-Based Authentication**:
- All API endpoints require valid JWT tokens in the `Authorization` header
- Tokens contain user identity and roles (REQUESTER, APPROVER, ADMIN)
- `JwtAuthenticationFilter` validates tokens and populates Spring Security context

**Role-Based Authorization**:
- **REQUESTER**: Can create requests and view their own approval history
- **APPROVER**: Can approve/reject requests at steps matching their role
- **ADMIN**: Can override any workflow decision at any step

**Security Rules**:
- Self-approval prevention: Users cannot approve their own requests
- Role validation: Only users with matching roles can act on specific steps
- Admin override: ADMIN role bypasses step-specific role requirements

**Layered Security**:
1. Spring Security filter chain validates JWT tokens
2. Controller methods enforce role-based access control
3. Service layer validates business rules (self-approval, role matching)

---

## Technology Stack

### Backend Framework
- **Spring Boot 3.2.0**: Modern Java application framework
- **Spring Web**: RESTful API development
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database access and ORM

### Database
- **H2 Database**: In-memory database (easily swappable with PostgreSQL, MySQL, etc.)
- **Hibernate**: JPA implementation

### Security
- **JWT (jjwt 0.12.3)**: Token-based authentication
- **Spring Security**: Security filter chain and method-level security

### Testing
- **JUnit 5**: Unit testing framework
- **jqwik 1.8.2**: Property-based testing library
- **Mockito**: Mocking framework
- **Spring Boot Test**: Integration testing support
- **JaCoCo**: Code coverage reporting

### Build & Development
- **Maven 3.6+**: Dependency management and build automation
- **Java 17**: Programming language
- **Lombok**: Boilerplate code reduction

---

###  Run the Application

```bash
mvn spring-boot:run
```

The application starts on `http://localhost:8080`

You should see:
```
Started ApprovalWorkflowEngineApplication in X.XXX seconds
```

### 4. Verify Installation

Access the H2 Database Console: `http://localhost:8080/h2-console`

**Connection Settings**:
- **JDBC URL**: `jdbc:h2:mem:workflowdb`
- **Username**: `sa`
- **Password**: (leave empty)

You should see three tables:
- `requests`
- `approval_steps`
- `approval_history`

### 5. Test the API

Generate a test JWT token and create your first request:

```bash
# Generate JWT token (using PowerShell script)
.\generate-tokens.ps1

# Create a request (replace <token> with generated token)
curl -X POST http://localhost:8080/requests ^
  -H "Content-Type: application/json" ^
  -H "Authorization: Bearer <token>" ^
  -d "{\"type\":\"LEAVE\",\"requestData\":\"Leave from 2024-01-15 to 2024-01-20\"}"
```

---

## API Documentation

### Authentication

All endpoints require a JWT token in the `Authorization` header:

```
Authorization: Bearer <jwt-token>
```

**JWT Token Structure**:
```json
{
  "sub": "user123",
  "roles": ["REQUESTER", "APPROVER"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

### Generating Test JWT Tokens

For testing purposes, you can use the `JwtTokenProvider` class to generate tokens:

```java
// Example token generation (for testing)
String token = jwtTokenProvider.generateToken("user123", List.of("REQUESTER", "APPROVER"));
```

Or use online JWT generators with the secret key from `application.properties`.

---

### Endpoints

#### 1. Create Request

**POST** `/requests`

Creates a new approval request and initializes the workflow.

**Required Role**: REQUESTER

**Request Body**:
```json
{
  "type": "LEAVE",
  "requestData": "Leave from 2024-01-15 to 2024-01-20"
}
```

**Response** (201 Created):
```json
{
  "id": 1,
  "type": "LEAVE",
  "status": "PENDING",
  "createdBy": "user123",
  "createdAt": "2024-01-10T10:00:00",
  "currentStepOrder": 1,
  "requestData": "Leave from 2024-01-15 to 2024-01-20",
  "version": 0
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "type": "LEAVE",
    "requestData": "Leave from 2024-01-15 to 2024-01-20"
  }'
```

---

#### 2. Get Request

**GET** `/requests/{id}`

Retrieves details of a specific request.

**Required Role**: Any authenticated user

**Response** (200 OK):
```json
{
  "id": 1,
  "type": "LEAVE",
  "status": "PENDING",
  "createdBy": "user123",
  "createdAt": "2024-01-10T10:00:00",
  "currentStepOrder": 1,
  "requestData": "Leave from 2024-01-15 to 2024-01-20",
  "version": 0
}
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/requests/1 \
  -H "Authorization: Bearer <jwt-token>"
```

---

#### 3. Approve Request

**POST** `/requests/{id}/approve`

Approves a request at the current workflow step.

**Required Role**: APPROVER (with role matching current step) or ADMIN

**Request Body**:
```json
{
  "comments": "Approved by manager"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "type": "LEAVE",
  "status": "PENDING",
  "createdBy": "user123",
  "createdAt": "2024-01-10T10:00:00",
  "currentStepOrder": 2,
  "requestData": "Leave from 2024-01-15 to 2024-01-20",
  "version": 1
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/requests/1/approve \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "comments": "Approved by manager"
  }'
```

---

#### 4. Reject Request

**POST** `/requests/{id}/reject`

Rejects a request and terminates the workflow.

**Required Role**: APPROVER (with role matching current step) or ADMIN

**Request Body**:
```json
{
  "comments": "Insufficient documentation"
}
```

**Response** (200 OK):
```json
{
  "id": 1,
  "type": "LEAVE",
  "status": "REJECTED",
  "createdBy": "user123",
  "createdAt": "2024-01-10T10:00:00",
  "currentStepOrder": 1,
  "requestData": "Leave from 2024-01-15 to 2024-01-20",
  "version": 1
}
```

**cURL Example**:
```bash
curl -X POST http://localhost:8080/requests/1/reject \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "comments": "Insufficient documentation"
  }'
```

---

#### 5. Get Approval History

**GET** `/requests/history/{id}`

Retrieves the complete approval history for a request.

**Required Role**: REQUESTER (for own requests) or ADMIN

**Response** (200 OK):
```json
[
  {
    "id": 1,
    "requestId": 1,
    "action": "CREATED",
    "actionBy": "user123",
    "actionAt": "2024-01-10T10:00:00",
    "comments": null,
    "stepOrder": 1
  },
  {
    "id": 2,
    "requestId": 1,
    "action": "APPROVED",
    "actionBy": "manager456",
    "actionAt": "2024-01-10T11:00:00",
    "comments": "Approved by manager",
    "stepOrder": 1
  }
]
```

**cURL Example**:
```bash
curl -X GET http://localhost:8080/requests/history/1 \
  -H "Authorization: Bearer <jwt-token>"
```

---

### Error Responses

**401 Unauthorized** (Missing or invalid JWT):
```json
{
  "error": "Unauthorized",
  "message": "Valid JWT token required"
}
```

**403 Forbidden** (Insufficient permissions):
```json
{
  "error": "Forbidden",
  "message": "User does not have permission to perform this action"
}
```

**404 Not Found** (Request doesn't exist):
```json
{
  "error": "Not Found",
  "message": "Request with id 999 not found"
}
```

**400 Bad Request** (Invalid state transition):
```json
{
  "error": "Bad Request",
  "message": "Cannot perform action on request with status APPROVED"
}
```

**409 Conflict** (Concurrent modification):
```json
{
  "error": "Conflict",
  "message": "Request was modified by another user. Please refresh and try again"
}
```

---

## Workflow Configuration

### Sample Workflows

The system includes two pre-configured workflows in `data.sql`:

#### LEAVE Workflow

```sql
INSERT INTO approval_steps (request_type, step_order, role, description) 
VALUES 
  ('LEAVE', 1, 'APPROVER', 'Manager Approval'),
  ('LEAVE', 2, 'APPROVER', 'HR Approval');
```

**Flow**:
1. Employee creates LEAVE request (status: PENDING, step: 1)
2. Manager approves (status: PENDING, step: 2)
3. HR approves (status: APPROVED)

#### EXPENSE Workflow

```sql
INSERT INTO approval_steps (request_type, step_order, role, description) 
VALUES 
  ('EXPENSE', 1, 'APPROVER', 'Manager Approval'),
  ('EXPENSE', 2, 'APPROVER', 'Finance Approval');
```

**Flow**:
1. Employee creates EXPENSE request (status: PENDING, step: 1)
2. Manager approves (status: PENDING, step: 2)
3. Finance approves (status: APPROVED)

### Adding New Workflows

To add a new workflow type (e.g., PURCHASE_ORDER):

1. **Insert approval steps** into the database:

```sql
INSERT INTO approval_steps (request_type, step_order, role, description) 
VALUES 
  ('PURCHASE_ORDER', 1, 'APPROVER', 'Department Manager Approval'),
  ('PURCHASE_ORDER', 2, 'APPROVER', 'Finance Director Approval'),
  ('PURCHASE_ORDER', 3, 'APPROVER', 'CEO Approval');
```

2. **Create a request** with the new type:

```bash
curl -X POST http://localhost:8080/requests \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <jwt-token>" \
  -d '{
    "type": "PURCHASE_ORDER",
    "requestData": "Purchase order for office equipment - $5000"
  }'
```

**No code changes required!** The engine automatically processes the new workflow based on database configuration.

### Workflow Configuration Rules

- **step_order**: Must be sequential integers starting from 1
- **request_type**: Must match the type used when creating requests
- **role**: Must be a valid role (typically APPROVER, but can be customized)
- **description**: Optional human-readable description of the step

### Querying Workflow Configuration

View configured workflows in H2 console:

```sql
-- View all workflows
SELECT * FROM approval_steps ORDER BY request_type, step_order;

-- View specific workflow
SELECT * FROM approval_steps WHERE request_type = 'LEAVE' ORDER BY step_order;

-- Count steps per workflow
SELECT request_type, COUNT(*) as step_count 
FROM approval_steps 
GROUP BY request_type;
```

---

### Test Coverage

**Current Coverage**: 80%+ line coverage

**Coverage by Component**:
- Controllers: API endpoint coverage with security validation
- Services: Business logic and workflow orchestration
- Repositories: Data access patterns
- Security: Authentication and authorization rules

### Property-Based Testing

**What is Property-Based Testing?**

Property-based testing verifies that certain properties (invariants) hold true for ALL valid inputs, not just specific examples. The testing framework (jqwik) generates hundreds of random test cases to find edge cases that humans might miss.

**Example Property**:
```java
@Property
@Label("Property 1: Request Creation Initialization")
void requestCreationInitializesWorkflow(@ForAll("validRequests") RequestData data) {
    // For ANY valid request, creation should initialize workflow correctly
    Request request = workflowService.createRequest(data.getType(), data.getData(), data.getUserId());
    
    assertThat(request.getStatus()).isEqualTo("PENDING");
    assertThat(request.getCurrentStepOrder()).isEqualTo(1);
}
```

**16 Correctness Properties Validated**:
1. Request creation initialization
2. Request retrieval completeness
3. Approval history recording
4. Multi-step approval advancement
5. Final step approval completion
6. Self-approval prevention
7. Role-based step authorization
8. Rejection termination
9. Admin override authority
10. Approval history chronological ordering
11. History access authorization
12. Approval history immutability
13. Database-driven workflow configuration
14. Workflow extensibility
15. Transaction rollback on failure
16. Optimistic locking for concurrency
