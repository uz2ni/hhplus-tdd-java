# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

This is a TDD (Test-Driven Development) training project from 항해99 백엔드 플러스 9기. The project implements a user point management system with charge, use, and history tracking features. The emphasis is on practicing TDD methodology with Red-Green-Refactor cycles.

## Build System

**Gradle with Kotlin DSL** (build.gradle.kts)
- Java 17
- Spring Boot 3.2.0
- Uses version catalog (gradle/libs.versions.toml)
- JaCoCo for code coverage

### Common Commands

**Build and Run:**
```bash
./gradlew build          # Build the project
./gradlew bootRun        # Run the Spring Boot application
./gradlew clean build    # Clean and rebuild
```

**Testing:**
```bash
./gradlew test           # Run all tests
./gradlew test --tests ClassName                    # Run specific test class
./gradlew test --tests ClassName.testMethodName     # Run single test method
./gradlew test --rerun-tasks                        # Force re-run tests
```

**Code Coverage:**
```bash
./gradlew jacocoTestReport    # Generate test coverage report (build/reports/jacoco)
```

**Linting:**
```bash
./gradlew check          # Run checks including tests
```

## Architecture

### Package Structure

```
io.hhplus.tdd/
├── point/               # Point domain
│   ├── PointController  # REST API endpoints
│   ├── UserPoint        # User point entity
│   ├── PointHistory     # Point transaction history
│   └── TransactionType  # CHARGE/USE enum
├── database/            # In-memory data layer (DO NOT MODIFY)
│   ├── UserPointTable   # Simulated user point storage
│   └── PointHistoryTable # Simulated history storage
├── ApiControllerAdvice  # Global exception handler
└── ErrorResponse        # Error response structure
```

### Key Constraints

**Database Table Classes:**
- `UserPointTable` and `PointHistoryTable` in `database/` package are **immutable**
- Use only the public APIs provided - do NOT modify these classes
- Both have built-in throttling (random delays up to 200-300ms) to simulate DB latency
- Storage is in-memory using HashMap/ArrayList

**Table APIs:**
- `UserPointTable.selectById(Long id)` - Returns user point or empty point if not exists
- `UserPointTable.insertOrUpdate(long id, long amount)` - Upserts user point
- `PointHistoryTable.insert(userId, amount, type, updateMillis)` - Adds history record
- `PointHistoryTable.selectAllByUserId(long userId)` - Returns all user histories

### TDD Requirements

This project follows strict TDD practices:

1. **Red-Green-Refactor Cycle**: Write failing test → Make it pass → Refactor
2. **Test Coverage**: All features require unit tests before implementation
3. **Exception Handling**: Test edge cases (insufficient balance, negative amounts, etc.)
4. **Integration Tests**: Test full request-response cycles
5. **Concurrency Control**: Implement and test thread-safety for point operations

### Features to Implement

The `PointController` has 4 TODO endpoints:

1. **GET /point/{id}** - Query user points
2. **GET /point/{id}/histories** - Query point transaction history
3. **PATCH /point/{id}/charge** - Charge points (add)
4. **PATCH /point/{id}/use** - Use points (subtract with validation)

## Development Workflow

### Layered Architecture Expected

Follow this service layer pattern:

```
Controller → Service → Repository (Table classes)
```

Implement business logic in a service layer between the controller and database tables. This enables:
- Testable business logic isolation
- Transaction management
- Concurrency control
- Validation logic separation

### Testing Strategy

**Unit Tests:**
- Test service layer business logic in isolation
- Mock the Table dependencies
- Test validation rules and edge cases

**Integration Tests:**
- Use `@SpringBootTest` for full context tests
- Test concurrent scenarios for race conditions
- Verify end-to-end API behavior

**Test Libraries Available:**
- JUnit 5 (Jupiter)
- AssertJ for fluent assertions
- Spring MockK for mocking (if needed)
- Testcontainers (configured but not required for this project)

### Concurrency Considerations

Point charge/use operations must handle concurrent requests safely:
- Multiple users can update points simultaneously
- Same user might have concurrent charge/use requests
- Prevent race conditions causing incorrect balances
- Consider using locks, atomic operations, or synchronized blocks
- **Document your concurrency approach in README.md** (required by PR template)

## Pull Request Requirements

Follow the PR template checklist (.github/pull_request_template.md):

**TDD Basics (4 items):**
- All 4 point features implemented
- Unit tests for each feature
- Red-Green-Refactor cycle followed
- Testable structure design

**TDD Advanced (4 items):**
- Exception case tests (insufficient balance, etc.)
- Integration tests written
- Concurrency control implemented
- Concurrency approach documented in README.md

**AI Usage (2 items):**
- Development using Claude Code
- Custom commands or prompt optimization attempted

**Commit Links:** Provide links to specific commits for each feature
**Review Points:** List questions for reviewers
**Retrospective:** 3-line summary (what went well, challenges, next steps)

## Code Quality

- Use Lombok for reducing boilerplate (@Getter, @RequiredArgsConstructor, etc.)
- Follow Spring Boot conventions and dependency injection
- Write meaningful test names that describe the scenario
- Handle exceptions with specific error messages via ApiControllerAdvice
