---
name: api-dto
description: Create request/response DTOs following company API envelope pattern
argument-hint: [dto-name]
---

# API DTO Generator

When creating DTOs for **$ARGUMENTS**, follow these company standards:

## Request Envelope Pattern

All API requests must use the company standard `Data` wrapper:

```java
// Base envelope class (already exists in dto/common/)
public class ApiRequest<T> {
    @JsonProperty("Data")
    private T data;

    public T getData() { return data; }
    public void setData(T data) { this.data = data; }
}

// Concrete wrapper for your request
public class Submit{Name}Request extends ApiRequest<{Name}Request> {}
```

## Request Payload

```java
public class {Name}Request {

    @NotNull(message = "Applicant details are required")
    private ApplicantDetails applicant;

    @NotBlank(message = "Field X is required")
    private String requiredField;

    // Optional fields - no validation annotation
    private String optionalField;

    // Nested objects
    @Valid  // Cascades validation to nested object
    @NotNull
    private NestedDetails nestedDetails;
}
```

## Validation Annotations

| Annotation | Use For |
|------------|---------|
| `@NotNull` | Required objects |
| `@NotBlank` | Required strings (not null, not empty, not whitespace) |
| `@NotEmpty` | Required collections |
| `@Valid` | Cascade validation to nested objects |
| `@Size(min=, max=)` | String/collection length |
| `@Pattern(regexp=)` | Regex validation |
| `@Email` | Email format |

## Response DTO

```java
public class {Name}Response {

    private String id;
    private String status;
    private Instant timestamp;

    // Use @JsonProperty if JSON name differs from Java field
    @JsonProperty("created_at")
    private Instant createdAt;
}
```

## Nested Objects

Create separate classes for reusable nested structures:

```java
// Shared across products
public class ApplicantDetails {
    @NotBlank
    private String irdNumber;

    @NotBlank
    private String pieRate;
}

// Product-specific
public class EmployerDetails {
    @NotBlank
    private String name;

    @NotBlank
    private String irdNumber;
}
```

## JSON Structure Result

```json
{
  "Data": {
    "applicant": {
      "irdNumber": "123-456-789",
      "pieRate": "28"
    },
    "requiredField": "value",
    "nestedDetails": { }
  }
}
```

## Checklist

- [ ] Request wrapper extends `ApiRequest<T>`
- [ ] All required fields have validation annotations
- [ ] Nested objects use `@Valid` for cascaded validation
- [ ] Response DTO created with appropriate fields
- [ ] Lombok annotations added (@Data, @Builder, etc.)
