# Code Review Prompts for GitHub Copilot (Claude Opus 4.5)

## Best Approach for Codebase Reviews

### Should You Review the "Entire Codebase" at Once?

**Short answer: No.** Even with Claude Opus 4.5's large context window, reviewing an entire codebase in one go is not optimal.

### Recommended Approach

1. **Review by module/package** - Focus on one logical unit at a time (e.g., `service`, `controller`, `repository` layers)

2. **Review by concern** - Do multiple passes with specific focus:
   - Pass 1: Security & input validation
   - Pass 2: Performance & memory
   - Pass 3: Thread safety
   - Pass 4: Test coverage
   - Pass 5: Code quality & patterns

3. **Start with high-risk areas:**
   - Authentication/authorization code
   - Payment/financial transactions
   - Data persistence layer
   - External API integrations
   - Async/concurrent code

4. **Use IDE selection** - In IntelliJ, select specific files or folders before prompting

### Context Window Strategy

```
For a typical Spring Boot project, review in this order:
1. Configuration classes (@Configuration, application.yml)
2. Security configuration
3. Controllers (one at a time or grouped by domain)
4. Services (one at a time or grouped by domain)
5. Repositories and entities
6. DTOs and mappers
7. Utilities and helpers
8. Tests (compare against implementation)
```

---

## General Code Review Prompts

### Full Module Review
```
Review this Spring Boot module for:
1. SOLID principles violations
2. Spring Boot best practices (dependency injection, bean scoping, configuration)
3. Code duplication and opportunities for abstraction
4. Naming conventions and readability
5. Error handling patterns
```

### Quick Health Check
```
Give me a quick assessment of this code:
- Top 3 critical issues to fix immediately
- Top 3 improvements for maintainability
- Overall code quality score (1-10) with justification
```

---

## Memory Leaks & Resource Management

### Comprehensive Memory Analysis
```
Analyze this code for potential memory leaks:
- Unclosed resources (streams, connections, files, ResultSets)
- Static collections that grow unbounded
- Event listener/observer patterns without cleanup
- @Autowired circular dependencies
- Missing @PreDestroy cleanup methods
- ThreadLocal variables not removed
- Caching without eviction policies
- Large objects held in session scope
```

### Resource Management Specific
```
Check for proper resource handling:
- Are try-with-resources used for AutoCloseable objects?
- Are database connections properly returned to pool?
- Are HTTP client connections closed?
- Are file handles released?
- Are scheduled tasks properly cancelled on shutdown?
```

---

## Thread Safety & Concurrency

### Full Concurrency Review
```
Review for thread safety issues:
- Shared mutable state without synchronization
- Non-thread-safe collections in concurrent contexts (HashMap, ArrayList in @Service)
- Race conditions in lazy initialization
- Double-checked locking problems
- Improper use of @Async and CompletableFuture
- Missing @Transactional isolation levels
- Deadlock potential in lock ordering
- Visibility issues (missing volatile)
```

### Spring-Specific Concurrency
```
Check Spring concurrency concerns:
- Are singleton beans (@Service, @Component) thread-safe?
- Is mutable state stored in request-scoped beans?
- Are @Async methods properly configured?
- Is the TaskExecutor properly sized and configured?
- Are @Scheduled methods thread-safe?
- Is @Transactional propagation correct for concurrent access?
```

---

## Unit Test Coverage

### Test Gap Analysis
```
Analyze test coverage gaps:
- Which public methods lack tests?
- Are edge cases covered (null, empty, boundary values)?
- Are exceptions and error paths tested?
- Is mocking used appropriately for dependencies?
- Are @MockBean and @SpyBean used correctly?
- Are integration points tested with @WebMvcTest or @DataJpaTest?
- Is test data representative of production scenarios?
```

### Test Quality Review
```
Review test quality:
- Do tests follow AAA pattern (Arrange, Act, Assert)?
- Are assertions meaningful and specific?
- Is test isolation maintained (no shared state)?
- Are test names descriptive of the scenario?
- Is there over-mocking that hides bugs?
- Are flaky tests present?
```

---

## Spring Boot Best Practices

### Anti-Pattern Detection
```
Check for Spring Boot anti-patterns:
- Field injection vs constructor injection
- Missing @Transactional where needed (or unnecessary @Transactional)
- N+1 query problems in JPA repositories
- Improper exception handling (@ControllerAdvice usage)
- Security misconfigurations
- Missing input validation (@Valid, @Validated)
- Hardcoded values instead of @Value or @ConfigurationProperties
- Business logic in controllers
- Fat services (god classes)
```

### Configuration Review
```
Review configuration for issues:
- Are secrets externalized (not in application.yml)?
- Are profiles used correctly for environments?
- Is @ConfigurationProperties used with validation?
- Are connection pools properly sized?
- Are timeouts configured for external calls?
- Is actuator properly secured?
```

### JPA/Hibernate Specific
```
Review JPA implementation:
- Are entities properly designed (equals/hashCode)?
- Is fetch strategy appropriate (LAZY vs EAGER)?
- Are cascades correctly configured?
- Is @Version used for optimistic locking where needed?
- Are projections used instead of full entities where appropriate?
- Are batch operations used for bulk updates?
- Is the second-level cache configured correctly?
```

---

## Security Review

### Security Checklist
```
Review security implementation:
- Is input validated and sanitized?
- Are SQL injection risks mitigated (parameterized queries)?
- Is XSS prevented in responses?
- Are authentication tokens properly validated?
- Is authorization checked at service layer (not just controller)?
- Are sensitive data logged?
- Is HTTPS enforced?
- Are CORS settings restrictive enough?
- Is CSRF protection enabled where needed?
- Are dependencies free of known vulnerabilities?
```

### API Security
```
Check API security:
- Is rate limiting implemented?
- Are endpoints properly authorized (@PreAuthorize, @Secured)?
- Is method-level security consistent?
- Are error messages leaking internal details?
- Is pagination enforced to prevent DoS?
- Are file uploads validated and restricted?
```

---

## Performance Review

### Performance Analysis
```
Analyze for performance issues:
- N+1 queries in JPA relationships
- Missing database indexes for query patterns
- Unbounded queries without pagination
- Synchronous calls that should be async
- Missing caching for repeated computations
- Large objects in memory unnecessarily
- String concatenation in loops
- Inefficient collection operations
```

### Database Performance
```
Review database interaction performance:
- Are queries optimized (check generated SQL)?
- Is connection pool sized correctly?
- Are batch inserts/updates used?
- Is lazy loading causing unexpected queries?
- Are database indexes aligned with query patterns?
- Is pagination implemented for list endpoints?
```

---

## Code Quality & Readability

### Clean Code Review
```
Review for clean code principles:
- Are methods small and focused (single responsibility)?
- Are names meaningful and consistent?
- Is there code duplication to extract?
- Are comments explaining "why" not "what"?
- Is the code self-documenting?
- Are magic numbers replaced with constants?
- Is cyclomatic complexity acceptable?
- Are nested conditionals simplified?
```

### Architecture Review
```
Review architectural concerns:
- Is layering respected (no repository calls from controller)?
- Are cross-cutting concerns handled consistently (logging, validation)?
- Is dependency direction correct (no circular dependencies)?
- Are interfaces used appropriately for abstraction?
- Is the package structure logical and maintainable?
```

---

## Prompt Templates for Iterative Review

### Initial Assessment
```
I'm starting a code review of this Spring Boot application.
Give me an initial assessment:
1. What is the overall structure and purpose?
2. What are the most critical areas to review?
3. What patterns do you see being used?
4. Any immediate red flags?
```

### Deep Dive Request
```
You identified [ISSUE] in the previous review.
Please provide:
1. All occurrences of this issue in the codebase
2. The specific risk/impact of each occurrence
3. Recommended fix with code examples
4. How to prevent this in future development
```

### Summary Request
```
Based on our review, provide a summary report:
1. Critical issues (must fix before production)
2. High priority improvements
3. Medium priority refactoring suggestions
4. Low priority nice-to-haves
5. Overall assessment and recommended next steps
```

---

## Tips for Effective Reviews

1. **Be specific about what you want** - "Review for thread safety" is better than "review this code"

2. **Provide context** - "This is a high-traffic payment service" helps prioritize findings

3. **Ask for examples** - "Show me the problematic code and the fix"

4. **Use follow-ups** - "Explain why this is a problem and its production impact"

5. **Request prioritization** - "Rank these issues by severity and effort to fix"

6. **Ask for patterns** - "What pattern should we use instead?"

7. **Validate findings** - Not all suggestions are applicable; use your judgment

---

## Sample Review Workflow

```
1. Start: "Review the configuration classes in src/main/java/config/"
2. Then:  "Now review the security configuration specifically"
3. Then:  "Review the user service and its dependencies"
4. Then:  "Check the user controller for security and validation"
5. Then:  "Review the corresponding tests for coverage gaps"
6. Finally: "Summarize all findings prioritized by severity"
```

This iterative approach gives better results than asking for an entire codebase review at once.
