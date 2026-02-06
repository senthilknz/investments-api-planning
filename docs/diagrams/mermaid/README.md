# Mermaid Diagram Fallback

These are Mermaid versions of the component testing diagrams. Use these if your platform supports Mermaid rendering natively (GitHub, GitLab, Azure DevOps).

The primary diagrams are maintained as PlantUML (`.puml`) with exported PNGs for Bitbucket and Confluence compatibility.

## 01 - Request Flow

```mermaid
graph LR
    subgraph test ["Test (RestClient)"]
        T[HTTP Request]
    end

    subgraph app ["Application Boundary"]
        direction LR
        F[Servlet Filters] --> C[Controller]
        C --> S[Service Layer]
        S --> E[ESB Client]
    end

    subgraph mock ["WireMock"]
        W[(ESB Stub<br/>JSON Mappings)]
    end

    T -->|Real HTTP| F
    E -->|Stubbed HTTP| W

    style test fill:#e1f5fe,stroke:#0288d1
    style app fill:#e8f5e9,stroke:#388e3c
    style mock fill:#fce4ec,stroke:#c62828
```

## 02 - CI/CD Pipeline

```mermaid
graph LR
    A[git push] --> B[CI Pipeline]
    B --> C[mvn compile]
    C --> D[Unit Tests]
    D --> E[Component Tests<br/>SpringBoot + WireMock]
    E --> F[mvn package]
    F --> G{All Passed?}
    G -->|Yes| H[Deploy to DEV]
    G -->|No| I[Fail Build<br/>Notify Team]

    style E fill:#c8e6c9,stroke:#2e7d32,color:#000
    style I fill:#ffcdd2,stroke:#c62828,color:#000
    style H fill:#e1f5fe,stroke:#0288d1,color:#000
```

## 03 - Stub Routing

```mermaid
graph TD
    R[Incoming ESB Request] --> P{Match by Priority}
    P -->|Priority 1| S1{Customer ID?}
    S1 -->|UNKNOWN| R404[404 Not Found]
    S1 -->|ESB_ERROR| R500[500 Server Error]
    S1 -->|ESB_SLOW| RT[200 + 10s Delay]
    S1 -->|LOCKED_ACCT| R403[403 Forbidden]
    S1 -->|CUST_NEW| R200E[200 Empty Holdings]
    P -->|Priority 5 - Default| R200[200 Success Response]

    style R404 fill:#ffcdd2,stroke:#c62828,color:#000
    style R500 fill:#ffcdd2,stroke:#c62828,color:#000
    style RT fill:#fff9c4,stroke:#f9a825,color:#000
    style R403 fill:#ffcdd2,stroke:#c62828,color:#000
    style R200E fill:#e1f5fe,stroke:#0288d1,color:#000
    style R200 fill:#c8e6c9,stroke:#2e7d32,color:#000
```

## 04 - Test Pyramid

```mermaid
block-beta
    columns 3

    space E2E["E2E Tests\n(few, slow, real ESB)"] space
    space Component["Component Tests\n(full stack, mocked ESB)"] space
    space Unit["Unit Tests\n(fast, isolated, single class)"] space

    style E2E fill:#ffcdd2,stroke:#c62828,color:#000
    style Component fill:#c8e6c9,stroke:#2e7d32,color:#000
    style Unit fill:#bbdefb,stroke:#1565c0,color:#000
```

## 05 - Team Sharing

```mermaid
graph TB
    subgraph stubs ["WireMock JSON Stubs (Shared Asset)"]
        M[mappings/*.json]
        F[__files/*.json]
    end

    subgraph teams ["Teams"]
        BE[Backend Team<br/>Component tests<br/>during development]
        FE[Frontend Team<br/>Stub the API<br/>during UI development]
        QA[QA Team<br/>Add new scenarios<br/>without Java changes]
        CT[Contract Testing<br/>Lightweight API contract<br/>between xAPI and ESB]
    end

    M --> BE
    M --> FE
    M --> QA
    F --> CT

    style stubs fill:#fff9c4,stroke:#f9a825,color:#000
    style BE fill:#e1f5fe,stroke:#0288d1,color:#000
    style FE fill:#e1f5fe,stroke:#0288d1,color:#000
    style QA fill:#e1f5fe,stroke:#0288d1,color:#000
    style CT fill:#e1f5fe,stroke:#0288d1,color:#000
```
