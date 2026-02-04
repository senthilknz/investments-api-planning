# Copilot Instructions for xapi-wone-investments

## Project Overview
This is a Spring Boot API for Westpac Digital Investments. All code must adhere to enterprise-grade security, performance, and maintainability standards.

## Package Structure
```
nz.co.westpac.digital.investments
├── controller/     # REST controllers only, no business logic
├── dto/            # Request/Response DTOs, use records where possible
├── filter/         # Servlet filters for cross-cutting concerns
├── validation/     # Custom validators and validation logic
├── service/        # Business logic layer
├── domain/         # Entity classes and domain objects
├── handler/        # Exception handlers and event handlers
├── configuration/  # Spring configuration classes
└── repository/     # Data access layer
```

## Coding Standards

### General
- Java 17+ features preferred (records, pattern matching, sealed classes)
- Constructor injection only, never field injection with @Autowired
- All public methods must have Javadoc
- Maximum method length: 30 lines
- Maximum class length: 300 lines
- Use Lombok sparingly (@Slf4j, @RequiredArgsConstructor allowed)

### Controllers
```java
// CORRECT
@RestController
@RequestMapping("/api/v1/investments")
@RequiredArgsConstructor
@Validated
public class InvestmentController {
    private final InvestmentService investmentService;
    
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('INVESTMENT_READ')")
    public ResponseEntity<InvestmentResponse> getInvestment(
            @PathVariable @NotBlank String id) {
        return ResponseEntity.ok(investmentService.findById(id));
    }
}

// INCORRECT - Don't do this
@RestController
public class InvestmentController {
    @Autowired  // NO: Use constructor injection
    private InvestmentService service;
    
    @GetMapping("/getInvestment")  // NO: Use RESTful naming
    public Investment get(String id) {  // NO: Missing validation, ResponseEntity
        return service.get(id);
    }
}
```

### DTOs
```java
// PREFERRED: Use records for immutable DTOs
public record InvestmentRequest(
    @NotBlank String accountId,
    @NotNull @Positive BigDecimal amount,
    @NotNull InvestmentType type
) {}

public record InvestmentResponse(
    String id,
    String accountId,
    BigDecimal amount,
    LocalDateTime createdAt
) {}
```

### Services
```java
@Service
@RequiredArgsConstructor
@Slf4j
public class InvestmentService {
    private final InvestmentRepository repository;
    private final InvestmentMapper mapper;
    
    @Transactional(readOnly = true)  // Always specify readOnly for queries
    public InvestmentResponse findById(String id) {
        log.debug("Fetching investment: {}", id);  // Never log sensitive data
        return repository.findById(id)
            .map(mapper::toResponse)
            .orElseThrow(() -> new ResourceNotFoundException("Investment", id));
    }
    
    @Transactional  // Write operations
    public InvestmentResponse create(InvestmentRequest request) {
        // Business logic here
    }
}
```

### Exception Handling
```java
// Use centralized exception handling
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex) {
        // Never expose internal details in error responses
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(new ErrorResponse("RESOURCE_NOT_FOUND", ex.getMessage()));
    }
}
```

## Security Requirements

### Mandatory
- All endpoints must have @PreAuthorize or @Secured annotations
- No hardcoded secrets - use environment variables or Vault
- Input validation on all request parameters and bodies
- SQL parameters must use parameterized queries (never string concatenation)
- HTTPS only in production

### Authentication
```java
// All endpoints require authentication unless explicitly public
@PreAuthorize("hasRole('ROLE_USER')")           // Basic access
@PreAuthorize("hasRole('ROLE_ADMIN')")          // Admin operations
@PreAuthorize("hasAuthority('investment:read')") // Fine-grained
```

### Sensitive Data Handling
```java
// Never log PII directly
log.info("Processing request for customer");  // CORRECT
log.info("Processing request for customer: {}", customerId);  // INCORRECT

// Mask sensitive data in responses if needed
@JsonIgnore  // Exclude from serialization
private String taxFileNumber;

@JsonSerialize(using = MaskedSerializer.class)  // Custom masking
private String accountNumber;
```

## PII and Data Protection

### Sensitive Fields (must be protected)
- Customer names, addresses, phone numbers, emails
- Tax file numbers, IRD numbers
- Bank account numbers
- Date of birth
- Any government-issued ID

### Rules
1. Never log PII at INFO level or above
2. Mask PII in all non-essential responses
3. Use audit logging for all PII access
4. Encrypt PII at rest and in transit
5. Implement data retention policies
```java
// Example: Masking utility usage
public String maskAccountNumber(String accountNumber) {
    if (accountNumber == null || accountNumber.length() < 4) {
        return "****";
    }
    return "****" + accountNumber.substring(accountNumber.length() - 4);
}
```

## Performance Guidelines

### Database
```java
// Use pagination for list endpoints
@GetMapping
public ResponseEntity<Page<InvestmentResponse>> getAll(
        @PageableDefault(size = 20, sort = "createdAt", direction = DESC) 
        Pageable pageable) {
    return ResponseEntity.ok(service.findAll(pageable));
}

// Avoid N+1 queries - use JOIN FETCH
@Query("SELECT i FROM Investment i JOIN FETCH i.transactions WHERE i.accountId = :accountId")
List<Investment> findByAccountIdWithTransactions(@Param("accountId") String accountId);

// Use projections for read-only queries
public interface InvestmentSummary {
    String getId();
    BigDecimal getAmount();
}
```

### Caching
```java
@Cacheable(value = "investments", key = "#id")
public InvestmentResponse findById(String id) { }

@CacheEvict(value = "investments", key = "#id")
public void update(String id, InvestmentRequest request) { }
```

### Async Processing
```java
@Async("taskExecutor")
@Transactional(propagation = Propagation.REQUIRES_NEW)
public CompletableFuture<Void> processLargeDataset(List<String> ids) {
    // Long-running operations
}
```

## Testing Requirements

### Minimum Coverage
- Unit tests: 80% line coverage
- Integration tests for all controller endpoints
- Security tests for authentication/authorization

### Test Naming
```java
@Test
void findById_WhenInvestmentExists_ReturnsInvestment() { }

@Test
void findById_WhenInvestmentNotFound_ThrowsResourceNotFoundException() { }

@Test
void create_WhenInvalidRequest_ReturnsBadRequest() { }
```

## Code Review Checklist

When reviewing code, verify:

- [ ] No hardcoded credentials or secrets
- [ ] All endpoints have proper authorization
- [ ] Input validation present on all inputs
- [ ] No PII in logs at INFO level or above
- [ ] Proper exception handling (no stack traces to client)
- [ ] Transactions properly configured
- [ ] No N+1 query issues
- [ ] Pagination on list endpoints
- [ ] Unit tests present and meaningful
- [ ] Javadoc on public methods
- [ ] Constructor injection used
- [ ] DTOs used (no entities in controllers)
- [ ] Proper HTTP status codes returned

## Dependency Security

### Prohibited
- Any dependency with known critical CVEs
- Outdated Spring Boot versions (must be within 2 minor versions of latest)
- XML parsers without XXE protection configured

### Required Dependencies
```xml
<!-- Security -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>

<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```

## API Design

### URL Conventions
- Use lowercase with hyphens: `/api/v1/investment-accounts`
- Use nouns, not verbs: `/investments` not `/getInvestments`
- Version in URL: `/api/v1/...`
- Use proper HTTP methods: GET (read), POST (create), PUT (full update), PATCH (partial), DELETE

### Response Standards
```java
// Success responses
200 OK - Successful GET, PUT, PATCH
201 Created - Successful POST (include Location header)
204 No Content - Successful DELETE

// Error responses  
400 Bad Request - Validation errors
401 Unauthorized - Authentication required
403 Forbidden - Insufficient permissions
404 Not Found - Resource doesn't exist
409 Conflict - Business rule violation
500 Internal Server Error - Unexpected errors (never expose details)
```