# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a Content Management System (CMS) with advanced permissions management. The system implements a sophisticated RBAC (Role-Based Access Control) model with resource-level permissions, featuring four main roles:
- Administrator (ROLE_ADMIN): Full system access
- Sub-Administrator (ROLE_SUB_ADMIN): Limited administrative functions
- Editor (ROLE_EDITOR): Document editing and publishing capabilities
- User (ROLE_USER): Basic document viewing and commenting

The system also includes comprehensive caching with Redis for performance optimization, audit logging, and extensive API documentation using Swagger.

## Architecture Overview

### Core Components

1. **Permissions Management System**
   - Hierarchical role structure with inheritance
   - Resource-level permission checking
   - Dynamic permission assignment and revocation
   - Custom permission evaluator integrated with Spring Security

2. **Redis Cache System**
   - Multi-layer cache architecture for user permissions and document assignments
   - Cache performance monitoring and optimization
   - Automatic cache warming strategies
   - Health checks and memory optimization

3. **Audit Trail System**
   - Comprehensive operation logging
   - Performance monitoring and analysis
   - Security event tracking

4. **API Documentation**
   - Full Swagger/OpenAPI documentation
   - Interactive API testing interface
   - JWT authentication integration

### Package Structure

```
com.cms.permissions
├── aspect/              # Aspect-oriented programming components (auditing, logging)
├── cache/               # Redis cache implementation and optimization
├── config/              # Application configuration classes
├── controller/          # REST API controllers
├── dto/                 # Data Transfer Objects
├── entity/              # JPA entities
├── exception/            # Custom exception classes
├── repository/          # Spring Data JPA repositories
├── scheduler/           # Scheduled tasks and jobs
├── security/            # Security configuration and custom evaluators
├── service/             # Business logic services
└── util/                # Utility classes
```

## Common Development Tasks

### Building the Project
```bash
# Clean and compile
mvn clean compile

# Run tests
mvn test

# Package the application
mvn package

# Run the application
mvn spring-boot:run
```

### Running Specific Tests
```bash
# Run a specific test class
mvn test -Dtest=CacheOptimizationTest

# Run tests with specific profile
mvn test -Dspring.profiles.active=test
```

### Working with Redis
The system uses Redis for caching permissions and document assignments. Key Redis operations:
- User permissions cache: `user:permissions:{username}`
- Document assignments cache: `user:doc:permissions:{userId}:{documentId}`
- Document public status cache: `document:public:{documentId}`

### API Documentation
Swagger UI is available at `http://localhost:8080/swagger-ui.html` when the application is running.

## Important Configuration Files

- `application.properties`: Main configuration file
- `application-cache.yml`: Cache-specific configuration
- `application-test.yml`: Test environment configuration

## Key Dependencies

- Spring Boot 3.5.6
- Spring Security for authentication and authorization
- Spring Data JPA for database operations
- Spring Data Redis for caching
- MySQL for primary database
- Swagger/Springdoc for API documentation
- H2 database for testing

## Database Schema

The system uses a relational database with tables for:
- Users
- Roles
- Permissions
- Documents
- Document Assignments
- Comments
- Audit Logs

## Security Model

The system implements a multi-layered security approach:
1. Role-based access control (RBAC)
2. Resource-level permission checking
3. JWT token authentication
4. Method-level security annotations
5. Custom permission evaluators

## Cache Architecture

The caching layer consists of:
1. **PermissionCacheService**: Core cache service for user permissions
2. **CacheMemoryOptimizer**: Memory usage monitoring and optimization
3. **CachePerformanceAnalyzer**: Cache performance metrics and analysis
4. **CacheWarmupService**: Pre-loading frequently accessed data
5. **CacheHealthService**: Cache system health monitoring

Cache entries are automatically invalidated when underlying data changes, ensuring data consistency.