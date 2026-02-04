Review this Spring Boot API codebase comprehensively. Analyze all files and provide findings organized by category:

## Security Review
- SQL/NoSQL injection vulnerabilities
- Authentication and authorization issues (Spring Security configuration)
- CSRF protection
- Input validation gaps
- Insecure deserialization
- Hardcoded secrets, API keys, or credentials
- Improper error handling exposing sensitive details
- Missing security headers

## PII & Data Protection
- Personal data exposure in logs, responses, or error messages
- Missing data masking/anonymization
- Sensitive fields without @JsonIgnore or proper serialization controls
- PII in URL parameters or query strings
- Audit trail gaps for sensitive data access
- GDPR/privacy compliance concerns

## Vulnerability Assessment
- Outdated dependencies with known CVEs (check pom.xml/build.gradle)
- OWASP Top 10 vulnerabilities
- Insecure cryptographic implementations
- XXE vulnerabilities in XML processing
- SSRF risks in external API calls
- Path traversal vulnerabilities

## Performance
- N+1 query problems in JPA/Hibernate
- Missing database indexes (based on query patterns)
- Unbounded collections or pagination issues
- Missing caching opportunities
- Inefficient loops or stream operations
- Connection pool configuration
- Async processing opportunities

## Spring Boot Conventions & Best Practices
- Proper layered architecture (Controller → Service → Repository)
- Constructor injection vs field injection
- Correct use of @Transactional
- Exception handling with @ControllerAdvice
- Proper DTO usage and validation (@Valid, @Validated)
- Configuration management (@ConfigurationProperties)
- Actuator and health check setup
- Profile-based configuration
- Proper use of ResponseEntity and HTTP status codes

## Code Quality
- SOLID principles adherence
- Code duplication
- Method complexity and length
- Null safety handling
- Proper logging practices (no sensitive data in logs)
- Test coverage gaps
- Documentation completeness

For each finding, provide:
1. File and line reference
2. Severity (Critical/High/Medium/Low)
3. Description of the issue
4. Recommended fix with code example

## Output Instructions

After completing the review, generate a summary report and save it as a markdown file:

- **Location**: `docs/code-reviews/`
- **Filename format**: `code-review-YYYY-MM-DD-HHmmss.md`
- **Example**: `docs/code-reviews/code-review-2026-02-05-143052.md`

The report should include:
1. **Summary** - Overview with total findings count by severity
2. **Findings by Category** - Grouped by the categories above
3. **Recommendations** - Prioritized action items
4. **Review Metadata** - Date, time, reviewer, files analyzed