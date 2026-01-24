---
name: new-investment-product
description: Create a new investment product endpoint following company patterns
argument-hint: [product-name]
---

# New Investment Product Generator

When creating a new investment product endpoint for **$ARGUMENTS**:

## 1. Request DTOs

Create in `dto/request/`:

```java
// Wrapper following company pattern
public class Submit{Product}Request extends ApiRequest<{Product}ApplicationRequest> {}

// Payload with product-specific fields
public class {Product}ApplicationRequest {
    @NotNull
    private ApplicantDetails applicant;

    // Add product-specific fields with validation annotations
}
```

## 2. Response DTO

Create in `dto/response/`:

```java
public class {Product}ApplicationResponse {
    private String applicationId;
    private String status;
    private Instant submittedAt;
}
```

## 3. Controller

Create `{Product}ApplicationController.java`:

```java
@RestController
@RequestMapping("/xapi/v1/investments/{product}")
@RequiredArgsConstructor
@Validated
public class {Product}ApplicationController {

    private final {Product}ApplicationService applicationService;

    @PostMapping("/applications")
    public ResponseEntity<ApiResponse<{Product}ApplicationResponse>> submit(
            @Valid @RequestBody Submit{Product}Request request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(applicationService.createApplication(request.getData()));
    }
}
```

## 4. Service

Create interface and implementation:

```java
public interface {Product}ApplicationService {
    {Product}ApplicationResponse createApplication({Product}ApplicationRequest request);
}

@Service
@RequiredArgsConstructor
public class {Product}ApplicationServiceImpl implements {Product}ApplicationService {

    private final InvestmentArrangementClient investmentArrangementClient;
    private final {Product}ApplicationMapper mapper;

    @Override
    public {Product}ApplicationResponse createApplication({Product}ApplicationRequest request) {
        // Map and call downstream ESB
    }
}
```

## 5. Mapper

Create `{Product}ApplicationMapper.java` for ESB request/response mapping.

## 6. Ingress

Update ingress pattern if needed. Current pattern:
```
/xapi/v1/investments/(.*)/applications$
```

## Checklist

- [ ] Request wrapper extends `ApiRequest<T>`
- [ ] Payload has Bean Validation annotations
- [ ] Controller uses `@Valid` on request body
- [ ] Service interface and implementation created
- [ ] Mapper for ESB integration created
- [ ] Unit tests added
