# xapi-one-investments API

A Spring Boot Experience API enabling banking customers to create KiwiSaver or ActiveSeries fund accounts.

## Overview

This API serves as the experience layer (xAPI) called from web/mobile UI, allowing authenticated customers to apply for:

- **Westpac KiwiSaver Scheme** - Retirement savings with government/employer contributions
- **Westpac Active Series (Managed Funds)** - Flexible, non-locked-in investments

## Product Comparison

| Aspect | KiwiSaver | Active Series |
|--------|-----------|---------------|
| **Liquidity** | Locked until age 65 (exceptions: first home, hardship) | Withdraw anytime |
| **Contributions** | Employee (3-10%), Employer (3%), Government ($260.72/yr) | Lump sum or voluntary only |
| **Fund Options** | 6 funds (Cash to High Growth) | 4 trusts (Conservative to Growth) |
| **Use Case** | Retirement, first home purchase | Medium-term goals (3-5 years) |

## Application Flow

```
┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐    ┌──────────┐
│  Web UI  │───▶│   xAPI   │───▶│   ESB    │───▶│   BPM    │───▶│  Sonata  │
└──────────┘    └──────────┘    └──────────┘    └──────────┘    └──────────┘
                     │                              │                 │
                     ▼                              ▼                 ▼
              "Application              ID/AML verification    Account created
               submitted"                                      (Wealth product)
```

1. Customer submits application via Web/Mobile UI
2. xAPI validates and forwards to ESB (`/investmentservicearrangement/v1`)
3. ESB triggers BPM workflows for ID/AML checks (existing customer verification)
4. Upon approval, account is created in Sonata (Wealth product)
5. Customer receives immediate confirmation that application was submitted

## REST API Endpoints

Base path: `/xapi/v1`

### KiwiSaver

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/investments/kiwisaver/applications` | Submit application to open a KiwiSaver account |

### Active Series

| Method | Endpoint | Description |
|--------|----------|-------------|
| `POST` | `/investments/activeseries/applications` | Submit application to open an Active Series account |

## Request Schemas

All requests follow the company standard API envelope pattern with a `Data` wrapper.

### KiwiSaver Application Request

```json
POST /xapi/v1/investments/kiwisaver/applications

{
  "Data": {
    "applicant": {
      "irdNumber": "123-456-789",
      "pieRate": "28"
    },
    "fundSelection": "growth",
    "contributionRate": "4",
    "employer": {
      "name": "Acme Ltd",
      "irdNumber": "111-222-333"
    },
    "transferFrom": {
      "provider": "ANZ",
      "transferFullBalance": true
    },
    "firstHomeBuyerIntent": true
  }
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `Data.applicant.irdNumber` | string | Yes | Customer's IRD number |
| `Data.applicant.pieRate` | string | Yes | PIE tax rate (10.5, 17.5, 28) |
| `Data.fundSelection` | string | Yes | One of: `cash`, `conservative`, `moderate`, `balanced`, `growth`, `high-growth` |
| `Data.contributionRate` | string | Yes | One of: `3`, `4`, `6`, `8`, `10` |
| `Data.employer.name` | string | Yes | Employer company name |
| `Data.employer.irdNumber` | string | Yes | Employer's IRD number |
| `Data.transferFrom.provider` | string | No | Existing KiwiSaver provider name |
| `Data.transferFrom.transferFullBalance` | boolean | No | Transfer full balance flag |
| `Data.firstHomeBuyerIntent` | boolean | No | Intends to use for first home |

### Active Series Application Request

```json
POST /xapi/v1/investments/activeseries/applications

{
  "Data": {
    "applicant": {
      "irdNumber": "123-456-789",
      "pieRate": "28"
    },
    "trustSelection": "balanced",
    "initialInvestment": {
      "amount": 10000,
      "fundingMethod": "direct-debit"
    },
    "regularContribution": {
      "amount": 500,
      "frequency": "monthly"
    }
  }
}
```

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `Data.applicant.irdNumber` | string | Yes | Customer's IRD number |
| `Data.applicant.pieRate` | string | Yes | PIE tax rate (10.5, 17.5, 28) |
| `Data.trustSelection` | string | Yes | One of: `conservative`, `moderate`, `balanced`, `growth` |
| `Data.initialInvestment.amount` | number | Yes | Initial lump sum amount |
| `Data.initialInvestment.fundingMethod` | string | Yes | One of: `direct-debit`, `bank-transfer` |
| `Data.regularContribution.amount` | number | No | Regular contribution amount |
| `Data.regularContribution.frequency` | string | No | One of: `weekly`, `fortnightly`, `monthly` |

## Spring Boot Project Structure

### Package Layout

```
com.westpac.xapi.investments
├── controller/
│   ├── KiwisaverApplicationController.java
│   └── ActiveSeriesApplicationController.java
│
├── service/
│   ├── KiwisaverApplicationService.java
│   ├── KiwisaverApplicationServiceImpl.java
│   ├── ActiveSeriesApplicationService.java
│   └── ActiveSeriesApplicationServiceImpl.java
│
├── client/
│   ├── InvestmentArrangementClient.java
│   ├── InvestmentArrangementClientImpl.java
│   └── dto/
│       ├── InvestmentArrangementRequest.java
│       └── InvestmentArrangementResponse.java
│
├── dto/
│   ├── common/
│   │   └── ApiRequest.java                        # Company standard envelope
│   ├── request/
│   │   ├── SubmitKiwisaverRequest.java            # extends ApiRequest<KiwisaverApplicationRequest>
│   │   ├── SubmitActiveSeriesRequest.java         # extends ApiRequest<ActiveSeriesApplicationRequest>
│   │   ├── KiwisaverApplicationRequest.java       # Payload for KiwiSaver
│   │   ├── ActiveSeriesApplicationRequest.java    # Payload for Active Series
│   │   ├── ApplicantDetails.java
│   │   ├── EmployerDetails.java
│   │   ├── TransferDetails.java
│   │   ├── InitialInvestment.java
│   │   └── RegularContribution.java
│   └── response/
│       ├── KiwisaverApplicationResponse.java
│       └── ActiveSeriesApplicationResponse.java
│
├── mapper/
│   ├── KiwisaverApplicationMapper.java
│   └── ActiveSeriesApplicationMapper.java
│
├── config/
│   ├── RestClientConfig.java
│   └── OpenApiConfig.java
│
└── exception/
    ├── ApplicationNotFoundException.java
    ├── DownstreamServiceException.java
    └── GlobalExceptionHandler.java
```

### Naming Conventions

| Layer | Pattern | Example |
|-------|---------|---------|
| Controller | `{Product}{Resource}Controller` | `KiwisaverApplicationController` |
| Service Interface | `{Product}{Resource}Service` | `KiwisaverApplicationService` |
| Service Implementation | `{Product}{Resource}ServiceImpl` | `KiwisaverApplicationServiceImpl` |
| Client Interface | `{Domain}Client` | `InvestmentArrangementClient` |
| Client Implementation | `{Domain}ClientImpl` | `InvestmentArrangementClientImpl` |
| API Envelope | `ApiRequest<T>` | `ApiRequest<KiwisaverApplicationRequest>` |
| Request Wrapper | `Submit{Product}Request` | `SubmitKiwisaverRequest` |
| Request Payload | `{Product}{Action}Request` | `KiwisaverApplicationRequest` |
| Response DTO | `{Product}{Action}Response` | `KiwisaverApplicationResponse` |
| Mapper | `{Product}{Resource}Mapper` | `KiwisaverApplicationMapper` |

### Request Wrapper Pattern

Following the company standard API envelope pattern:

```java
// Company standard envelope (reusable across APIs)
public class ApiRequest<T> {
    @JsonProperty("Data")
    private T data;

    // getters/setters
}

// Concrete request wrappers
public class SubmitKiwisaverRequest extends ApiRequest<KiwisaverApplicationRequest> {}

public class SubmitActiveSeriesRequest extends ApiRequest<ActiveSeriesApplicationRequest> {}

// Payload classes contain the actual business data
public class KiwisaverApplicationRequest {
    private ApplicantDetails applicant;
    private String fundSelection;
    private String contributionRate;
    private EmployerDetails employer;
    private TransferDetails transferFrom;
    private Boolean firstHomeBuyerIntent;
}

public class ActiveSeriesApplicationRequest {
    private ApplicantDetails applicant;
    private String trustSelection;
    private InitialInvestment initialInvestment;
    private RegularContribution regularContribution;
}
```

### Controller Usage

```java
@PostMapping("/kiwisaver/applications")
public ResponseEntity<ApiResponse<KiwisaverApplicationResponse>> submit(
        @RequestBody SubmitKiwisaverRequest request) {

    KiwisaverApplicationRequest payload = request.getData();
    // process...
}
```

## Ingress Configuration

Path pattern for microservices-ingress-controller:

```
/xapi/v1/investments/(.*)/applications$
```

This pattern is flexible and future-proof, allowing for additional investment products without ingress changes.

## Downstream Integration

This API integrates with the ESB endpoint:

```
POST /investmentservicearrangement/v1
```

The `InvestmentArrangementClient` handles all communication with this downstream service, mapping xAPI DTOs to ESB request format.

## Design Decisions

### Why Separate Endpoints for Each Product?

1. **Different data requirements** - KiwiSaver requires employer details; Active Series requires initial investment amount
2. **Different validation rules** - Contribution rates vs lump sum amounts
3. **Different fund/trust options** - 6 funds vs 4 trusts
4. **Regulatory differences** - KiwiSaver is government-regulated with specific compliance requirements
5. **Independent evolution** - Products can change without affecting each other
6. **Clear API documentation** - Self-documenting endpoints per product

### Why Not a Unified Endpoint?

A single `/investments/applications` endpoint with product type in the body would:
- Create complex conditional validation
- Make the schema harder to understand
- Mix unrelated fields (employer details with lump sum amounts)
- Complicate OpenAPI documentation

## Claude Code Skills

This project includes custom skills to help maintain consistency and speed up development.

### Available Skills

| Command | Description |
|---------|-------------|
| `/new-investment-product [product-name]` | Generate full endpoint structure for a new investment product |
| `/api-dto [dto-name]` | Create request/response DTOs following company envelope pattern |
| `/validate-api [endpoint-or-class]` | Validate API endpoints follow company standards |

### Skill Locations

```
.claude/skills/
├── new-investment-product/
│   └── SKILL.md
├── api-dto/
│   └── SKILL.md
└── validate-api/
    └── SKILL.md
```

### Usage Examples

```bash
# Create a new investment product (e.g., Term Deposits)
/new-investment-product termdeposit

# Create DTOs for a new feature
/api-dto LoanApplication

# Validate an existing controller follows standards
/validate-api KiwisaverApplicationController
```
