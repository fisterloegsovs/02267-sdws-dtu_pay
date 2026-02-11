# 02267-SDWS-DTU_Pay

## Project Context

This was a collaborative group project developed for course 02267 (Software Development in Large-Scale Distributed Systems) at DTU.

**Team Size:** 6 DTU students

**Development Approach:** Pair programming in team of two

**My Contributions:**
- Participated in all development sessions through pair programming
- Authored significant portions of the technical report and architecture documentation
- Contributed to microservices design, REST API implementation, and system integration

**Note:** This repository is migrated from DTU's private GitLab instance. The original development history with all team member contributions is not publicly accessible due to DTU authentication requirements. This public version serves as a portfolio demonstration of the project.

## Learning goals

1. work in a team and to build a larger service-oriented application
2. create services based on their description using agile methods
3. use existing services according to their description
4. compose new services from existing ones
5. develop, test, and document a larger service-oriented application in a team using agile practices
6. build, deploy, and run a service-oriented application
7. discuss coordination and the security of Web services
8. discuss service-oriented architectures


# DTU Pay - Distributed Mobile Payment System

A microservices-based mobile payment platform that enables secure, token-based transactions between customers and merchants. Built with a focus on distributed systems architecture, domain-driven design, and comprehensive testing.

## Overview

DTU Pay is a payment processing system that allows customers to make purchases using unique, disposable tokens instead of directly sharing payment information. The system handles user registration, token generation, payment processing, and transaction reporting through a distributed microservices architecture.

**Key Feature**: Privacy-preserving token system where merchants never see customer identity - only secure, single-use payment tokens.

## Architecture

### Microservices Design

The system implements a hexagonal architecture with clear separation between:
- **Customer Facade**: REST API for customer mobile applications
- **Merchant Facade**: REST API for merchant point-of-sale systems  
- **Manager/Reporting Facade**: REST API for administrative reporting
- **Internal Services**: Token Management, Payment Processing, Account Management, Report Generation

### Communication Patterns

- **External Communication**: REST APIs for customer/merchant/manager interfaces
- **Internal Communication**: Message queues for asynchronous service-to-service communication
- **Bank Integration**: SOAP protocol for secure bank transaction processing

### System Flow

```
Customer App → REST → Customer Facade → Message Queue → Internal Services
                                              ↓
Merchant App → REST → Merchant Facade → Payment Service → SOAP → Bank
                                              ↓
                                        Report Service
```

## Core Functionality

### Token Management
- Customers request 1-5 unique, cryptographically secure tokens
- Maximum of 6 unused tokens per customer
- Single-use tokens that cannot be guessed or forged
- Tokens contain no customer information (privacy-preserving)

### Payment Processing
- Token-based payment initiation by merchant
- Automated validation (token validity, merchant/customer registration)
- Bank account verification and fund transfer via SOAP
- Comprehensive error handling for failed transactions

### Account Management
- Customer and merchant self-registration
- Bank account association (no account creation by DTU Pay)
- Account deregistration with cleanup

### Reporting
- Customer transaction history (with amounts, merchants, tokens used)
- Merchant transaction history (amount and token only - no customer identity)
- Manager view of all system transactions and financial summaries

## Technology Stack

### Backend
- **Java**: Primary development language
- **Maven**: Build automation and dependency management
- **Docker**: Containerization for all microservices
- **Docker Compose**: Multi-container orchestration

### APIs & Communication
- **REST**: External API design following REST principles
- **SOAP**: Bank integration protocol
- **Message Queues**: Asynchronous inter-service communication
- **Swagger/OpenAPI**: API documentation

### Testing
- **Cucumber**: Behavior-driven development (BDD) and end-to-end testing
- **JUnit**: Unit and integration testing
- **High code coverage**: Test-driven development approach

### DevOps
- **Jenkins**: Continuous integration and deployment
- **Git**: Version control
- **Linux**: Deployment environment

## Design Principles Applied

- **Domain-Driven Design (DDD)**: Clear domain models and bounded contexts
- **Hexagonal Architecture**: Separation of business logic from external dependencies
- **SOLID Principles**: Maintainable, extensible codebase
- **Repository Pattern**: Data persistence abstraction
- **Event Storming**: Domain modeling and service decomposition

## Running the Project

### Prerequisites
```bash
- Java 11+
- Docker & Docker Compose
- Maven 3.6+
- Linux environment (tested on Ubuntu)
```

### Quick Start
```bash
# Clone repository
git clone [repository-url]

# Build all services
mvn clean install

# Deploy with Docker Compose
docker-compose up -d

# Run end-to-end tests
./run-tests.sh
```

### API Endpoints

**Customer API** (Port 8080)
- `POST /customers/register` - Register new customer
- `POST /customers/tokens` - Request payment tokens
- `GET /customers/{id}/report` - View transaction history
- `DELETE /customers/{id}` - Deregister customer

**Merchant API** (Port 8081)
- `POST /merchants/register` - Register new merchant
- `POST /merchants/payment` - Process payment with token
- `GET /merchants/{id}/report` - View merchant transactions
- `DELETE /merchants/{id}` - Deregister merchant

**Manager API** (Port 8082)
- `GET /reports/all` - View all system transactions

## Testing Strategy

### End-to-End Tests
Cucumber scenarios testing complete user journeys:
- Successful payment flow
- Token generation and limits
- Error scenarios (invalid tokens, insufficient funds)
- Registration and deregistration

### Service-Level Tests
Unit tests for individual microservices ensuring:
- Business logic correctness
- Error handling
- Edge case coverage

### Test Coverage
High coverage maintained through:
- Test-driven development
- Continuous integration enforcement
- No production code without tests

## Key Challenges Solved

1. **Token Security**: Implemented cryptographically secure, unpredictable token generation
2. **Privacy**: Merchants cannot identify customers through transaction data
3. **Distributed Transactions**: Coordinated payment flow across microservices and external bank
4. **Idempotency**: Ensured single-use tokens prevent duplicate payments
5. **Fault Tolerance**: Comprehensive error handling for service and network failures

## Development Approach

- **Agile Methodology**: Iterative development with continuous integration
- **Mob Programming**: Collaborative development sessions
- **Event Storming**: Domain modeling before implementation
- **CI/CD**: Automated build, test, and deployment pipeline via Jenkins

## Project Structure

```
dtupay/
├── customer-facade/       # Customer REST API
├── merchant-facade/       # Merchant REST API  
├── manager-facade/        # Reporting REST API
├── token-service/         # Token generation and validation
├── payment-service/       # Payment processing logic
├── account-service/       # Customer/merchant account management
├── report-service/        # Transaction reporting
├── end-to-end-tests/      # Cucumber integration tests
├── docker-compose.yml     # Service orchestration
└── docs/                  # Architecture diagrams and API specs
```

## Skills Demonstrated

- Distributed systems architecture and microservices design
- RESTful API design following industry best practices
- Message-driven architecture for service decoupling
- Domain-driven design and hexagonal architecture
- Containerization and orchestration with Docker
- Integration with external services (SOAP)
- Comprehensive testing strategies (BDD, TDD, integration tests)
- CI/CD pipeline implementation
- Security considerations (token generation, authentication boundaries)
- System design for scalability and maintainability

---

*Developed as part of advanced software engineering coursework at DTU, demonstrating practical application of distributed systems, software architecture, and modern development practices.*
