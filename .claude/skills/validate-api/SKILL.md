---
name: validate-api
description: Validate API endpoints follow company standards and best practices
argument-hint: [endpoint-or-class-name]
---

# API Validation Checklist

Validate that **$ARGUMENTS** follows company standards:

## 1. URL Path Convention

```
/xapi/v1/{domain}/{product}/{resource}
```

| Check | Expected |
|-------|----------|
| Prefix | `/xapi/v1/` |
| Lowercase | All segments lowercase |
| Plural resources | `/applications` not `/application` |
| Hyphens for multi-word | `/loan-repayment` not `/loanRepayment` |

## 2. Request Envelope

```java
// Must extend ApiRequest<T>
public class Submit{X}Request extends ApiRequest<{X}Request> {}
```

Verify JSON structure:
```json
{
  "Data": {
    // payload here
  }
}
```

## 3. Validation Annotations

Check request payload class:

- [ ] `@NotNull` on required objects
- [ ] `@NotBlank` on required strings
- [ ] `@Valid` on nested objects (for cascaded validation)
- [ ] Custom validation messages provided

```java
@NotBlank(message = "Fund selection is required")
private String fundSelection;
```

## 4. Controller Standards

```java
@RestController
@RequestMapping("/xapi/v1/investments/{product}")
@RequiredArgsConstructor
@Validated
public class {X}Controller {

    @PostMapping("/applications")
    public ResponseEntity<ApiResponse<{X}Response>> submit(
            @Valid @RequestBody Submit{X}Request request) {  // @Valid is critical!
        // ...
    }
}
```

- [ ] `@Validated` on class
- [ ] `@Valid` on `@RequestBody`
- [ ] Proper HTTP status codes (201 for POST create)
- [ ] Consistent response wrapper

## 5. Service Layer

- [ ] Interface defined
- [ ] Implementation uses `@Service`
- [ ] Logging with masked sensitive data (IRD numbers, etc.)
- [ ] Proper exception handling

## 6. Error Handling

- [ ] `@ControllerAdvice` handles validation errors
- [ ] Consistent error response format
- [ ] Appropriate HTTP status codes

```java
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex) {
    // Return 400 with field errors
}
```

## 7. Ingress Configuration

- [ ] Path pattern added to microservices-ingress-controller
- [ ] Pattern matches endpoint: `/xapi/v1/investments/(.*)/applications$`

## 8. Security

- [ ] Endpoint requires authentication
- [ ] Sensitive data not logged
- [ ] Input sanitization where needed

## Validation Commands

Run these to verify:

```bash
# Check for missing @Valid
grep -r "@RequestBody" --include="*.java" | grep -v "@Valid"

# Check for missing validation annotations
grep -r "private String" --include="*Request.java" | grep -v "@Not"
```

## Common Issues

| Issue | Fix |
|-------|-----|
| Missing `@Valid` | Add to `@RequestBody` parameter |
| Wrong JSON field name | Use `@JsonProperty("Data")` |
| Validation not triggered | Ensure `@Validated` on controller class |
| 500 instead of 400 | Add `@ControllerAdvice` exception handler |
