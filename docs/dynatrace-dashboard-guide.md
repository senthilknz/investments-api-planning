# Dynatrace Dashboard Guide — Investments API

> **Purpose:** Reference guide for the team to understand what metrics are monitored on our Dynatrace dashboard, what each metric means, and what action to take when something looks wrong.

---

## Table of Contents

1. [How to Read the Dashboard](#1-how-to-read-the-dashboard)
2. [Availability & Health](#2-availability--health)
3. [Performance & Latency](#3-performance--latency)
4. [Throughput](#4-throughput)
5. [Errors & Failures](#5-errors--failures)
6. [Infrastructure & JVM Health](#6-infrastructure--jvm-health)
7. [External Dependencies](#7-external-dependencies)
8. [SLO & Business Metrics](#8-slo--business-metrics)
9. [Dashboard Layout](#9-dashboard-layout)
10. [Alert Thresholds Reference](#10-alert-thresholds-reference)
11. [On-Call Runbook — What to Do When Something is Red](#11-on-call-runbook--what-to-do-when-something-is-red)

---

## 1. How to Read the Dashboard

### Tile Colour Meaning

| Colour | Meaning | Action Required |
|--------|---------|----------------|
| **Green** | Within normal thresholds | None |
| **Yellow** | Approaching threshold / degraded | Monitor closely, investigate if persists |
| **Red** | Threshold breached / critical | Immediate investigation required |
| **Grey** | No data received | Check if service is running / agent is connected |

### Time Window

- **Default view:** Last 2 hours (auto-refresh every 5 minutes)
- **Incident investigation:** Zoom to the spike window — use the time picker top-right
- **Trend analysis:** Switch to last 7 days or 30 days

### Two Audiences, Two Dashboards

| Dashboard | Audience | Focus |
|-----------|---------|-------|
| **Ops / On-Call** | Support & SRE | Real-time errors, availability, pod health |
| **Dev Team** | Developers | Endpoint breakdown, slow traces, DB queries |

---

## 2. Availability & Health

> **Goal:** Know immediately whether the API is up and serving requests.

---

### 2.1 Service Availability (%)

| Field | Detail |
|-------|--------|
| **What it measures** | Percentage of time the service is responding successfully |
| **Visualization** | Large single-value tile at the top of the dashboard |
| **Healthy range** | >= 99.9% |
| **Warning threshold** | 99.0% – 99.9% |
| **Critical threshold** | < 99.0% |
| **Where in Dynatrace** | Services > `xapi-wone-investments` > Availability |

**What to do if red:**
- Check the Problems feed (Davis AI tile) for root cause
- Check pod/container restarts
- Check if a recent deployment was made

---

### 2.2 HTTP Error Rate (%)

| Field | Detail |
|-------|--------|
| **What it measures** | Percentage of all requests returning 4xx or 5xx HTTP responses |
| **Visualization** | Line chart with coloured threshold bands |
| **Healthy range** | < 1% |
| **Warning threshold** | 1% – 5% |
| **Critical threshold** | > 5% |

**What to do if elevated:**
- Split by 4xx vs 5xx — see [Section 5](#5-errors--failures) for what each means
- Check the "Top Failing Endpoints" tile to identify which API is affected

---

### 2.3 Request Failure Count

| Field | Detail |
|-------|--------|
| **What it measures** | Absolute number of failed requests per minute |
| **Visualization** | Bar chart grouped by endpoint |
| **Use case** | Even a low error rate can mean thousands of failures at high traffic — this tile shows raw impact |

---

## 3. Performance & Latency

> **Goal:** Detect slowness before users notice. Always look at P90/P99, not just average.

---

### Why Percentiles Matter

```
Example — 1000 requests in one minute:

  Average response time : 120ms  ← looks fine
  P50 (median)          : 80ms   ← half of users get this
  P90                   : 350ms  ← 100 users waited 350ms
  P99                   : 1200ms ← 10 users waited over 1 second

The average completely hides the slow tail.
Always check P90 and P99.
```

---

### 3.1 Response Time — P50 / P90 / P99

| Field | Detail |
|-------|--------|
| **What it measures** | Response time at the 50th, 90th, and 99th percentile |
| **Visualization** | Single line chart with all three lines overlaid |
| **P50 healthy** | < 200ms |
| **P90 healthy** | < 500ms |
| **P99 healthy** | < 1500ms |
| **Warning** | P90 > 800ms |
| **Critical** | P90 > 1500ms or P99 > 3000ms |

**Reading the chart:**
- If **P99 spikes but P50 is stable** → a small subset of requests is slow (likely a specific endpoint or DB query)
- If **all three rise together** → systemic issue (infrastructure, downstream dependency, or GC pause)

---

### 3.2 Response Time by Endpoint (Top N Slowest)

| Field | Detail |
|-------|--------|
| **What it measures** | P90 response time ranked per API endpoint |
| **Visualization** | Horizontal bar chart — longest bar = slowest endpoint |
| **Use case** | Immediately see which endpoint needs attention without digging through traces |

---

### 3.3 Time to First Byte (TTFB)

| Field | Detail |
|-------|--------|
| **What it measures** | Time from request received to first byte of response sent |
| **Visualization** | Line chart |
| **Use case** | Helps separate **application processing time** from **network/client transfer time** |

---

## 4. Throughput

> **Goal:** Understand traffic volume and detect unexpected spikes or drops.

---

### 4.1 Requests Per Minute (RPM)

| Field | Detail |
|-------|--------|
| **What it measures** | Total inbound requests per minute across all endpoints |
| **Visualization** | Line chart — know your normal baseline pattern |
| **What to watch** | Sudden spike = possible DDoS or upstream retry storm. Sudden drop = possible upstream outage or routing issue |

**Normal pattern for Investment APIs:**
- Business hours (9am–5pm AEST): higher traffic
- Overnight / weekends: lower traffic
- A drop to zero outside a deployment window is always worth investigating

---

### 4.2 Requests by HTTP Method

| Field | Detail |
|-------|--------|
| **What it measures** | Breakdown of GET vs POST vs PUT vs DELETE |
| **Visualization** | Stacked bar chart or pie chart |
| **Use case** | Unusual spike in POST/PUT requests can indicate a client retry loop or misuse |

---

### 4.3 Requests by Endpoint (Top N Busiest)

| Field | Detail |
|-------|--------|
| **What it measures** | Request volume ranked per endpoint |
| **Visualization** | Top-N list |
| **Use case** | Know which endpoints carry the most load — helps prioritise performance tuning and capacity planning |

---

## 5. Errors & Failures

> **Goal:** Quickly distinguish between client errors (their problem) and server errors (our problem).

---

### 5.1 4xx vs 5xx Breakdown

| Error Class | Meaning | Typical Cause | Owner |
|-------------|---------|--------------|-------|
| **4xx** | Client error | Bad request, auth failure, not found | Calling team / consumer |
| **400** | Bad request | Invalid request body or params | Consumer bug |
| **401 / 403** | Auth failure | Missing or expired token | Consumer / auth config |
| **404** | Not found | Wrong endpoint URL or invalid ID | Consumer bug |
| **422** | Validation error | Business rule violation | Consumer / data issue |
| **5xx** | Server error | Our API failed to process the request | **Our team** |
| **500** | Internal error | Unhandled exception | Bug — fix urgently |
| **502 / 503** | Gateway/service unavailable | Pod crash, deployment in progress | Infra / deployment |
| **504** | Gateway timeout | Downstream dependency too slow | Downstream team |

**Visualization:** Stacked bar chart (4xx in amber, 5xx in red) over time

**Rule of thumb:**
- Elevated **4xx only** → likely a consumer issue, notify them
- Any **5xx** → our problem, investigate immediately

---

### 5.2 Top Error Codes Table

| Field | Detail |
|-------|--------|
| **What it shows** | Table: HTTP status code | count | affected endpoint |
| **Visualization** | Data table, sortable by count |
| **Use case** | Instantly see which error is most impactful without reading logs |

---

### 5.3 Failed Transactions Over Time

| Field | Detail |
|-------|--------|
| **What it measures** | Count of transactions Dynatrace classifies as failed (error response or timeout) |
| **Visualization** | Line chart |
| **Use case** | Trend view — is the error rate growing, stable, or recovering? |

---

### 5.4 Exception Count by Service

| Field | Detail |
|-------|--------|
| **What it measures** | Number of unhandled Java exceptions thrown per service |
| **Visualization** | Bar chart grouped by service name |
| **Use case** | Catch exceptions that are swallowed before returning a 500 — useful for silent failures |

---

## 6. Infrastructure & JVM Health

> **Goal:** Correlate API slowness or failures with underlying resource pressure.

---

### 6.1 JVM Heap Usage (%)

| Field | Detail |
|-------|--------|
| **What it measures** | Percentage of JVM heap memory in use |
| **Visualization** | Line chart with threshold bands |
| **Healthy range** | < 70% |
| **Warning** | 70% – 85% |
| **Critical** | > 85% |

**What sustained high heap means:**
- Memory leak — objects not being garbage collected
- Heap is undersized for current load
- **Risk:** at ~95%, the JVM will spend all its time in GC and stop processing requests (GC thrashing)

---

### 6.2 Garbage Collection (GC) Pause Time

| Field | Detail |
|-------|--------|
| **What it measures** | Duration of GC stop-the-world pauses in milliseconds |
| **Visualization** | Line chart |
| **Healthy** | < 100ms average |
| **Warning** | > 300ms |
| **Critical** | > 1000ms (1 second pauses will directly cause latency spikes) |

**Key correlation:** If you see P99 latency spikes that align exactly with GC pause spikes, the GC is your root cause.

---

### 6.3 CPU Usage (%)

| Field | Detail |
|-------|--------|
| **What it measures** | CPU utilisation of the service host/container |
| **Visualization** | Gauge or line chart |
| **Healthy** | < 60% sustained |
| **Warning** | 60% – 80% |
| **Critical** | > 80% sustained |

**Correlation tips:**
- CPU spike + latency spike → likely a computation-heavy request or a spike in traffic
- CPU spike + no traffic increase → possible infinite loop or runaway background thread

---

### 6.4 Thread Pool Utilisation

| Field | Detail |
|-------|--------|
| **What it measures** | Active threads vs. available thread pool capacity |
| **Visualization** | Line chart |
| **Healthy** | < 70% of pool capacity |
| **Critical** | > 90% — requests will queue and timeout |

**What thread exhaustion looks like:**
- Response times climb steadily even though CPU is not maxed
- Requests start timing out with 504s from the gateway

---

### 6.5 Pod / Container Restarts

| Field | Detail |
|-------|--------|
| **What it measures** | Number of pod restart events (OOMKilled, crash, liveness probe failure) |
| **Visualization** | Counter tile — any non-zero value is notable |
| **Healthy** | 0 restarts |
| **Action** | Any restart → check pod logs immediately for OOMKilled or crash stack trace |

---

## 7. External Dependencies

> **Goal:** Know when slowness or failures are caused by a system we call, not by our own code.

---

### 7.1 Downstream Service Response Time

| Field | Detail |
|-------|--------|
| **What it measures** | P90 response time of each external service we call (e.g., core banking, pricing service) |
| **Visualization** | Line chart, one line per downstream service |
| **Use case** | If our API is slow but our own code is fast, a downstream service is the culprit |

---

### 7.2 Database Call Duration

| Field | Detail |
|-------|--------|
| **What it measures** | Average and P90 time for database queries |
| **Visualization** | Line chart |
| **Healthy** | < 50ms average |
| **Warning** | > 200ms average |
| **Critical** | > 500ms average |

**Common causes of slow DB calls:**
- Missing index on a frequently queried column
- Long-running transaction blocking other queries
- Connection pool exhaustion causing queries to queue

---

### 7.3 Database Connection Pool Usage

| Field | Detail |
|-------|--------|
| **What it measures** | Active connections vs. total pool size |
| **Visualization** | Gauge |
| **Critical** | > 90% — new requests cannot get a connection and will fail |

---

### 7.4 3rd Party API Error Rate

| Field | Detail |
|-------|--------|
| **What it measures** | Error rate of calls to external APIs (e.g., downstream banking systems) |
| **Visualization** | Bar chart per dependency |
| **Use case** | Distinguish between "we are broken" and "they are broken" — critical for fast incident triage |

---

## 8. SLO & Business Metrics

> **Goal:** High-level health that can be understood by the whole team, including non-technical stakeholders.

---

### 8.1 SLO Compliance (%)

| Field | Detail |
|-------|--------|
| **What it measures** | Whether the service is meeting its defined Service Level Objective |
| **Visualization** | Large single-value tile (green / red) |
| **Example SLO** | 99.9% availability over a rolling 30-day window |
| **Use case** | Instant pass/fail against our committed service level |

---

### 8.2 Apdex Score

| Field | Detail |
|-------|--------|
| **What it measures** | Industry-standard user satisfaction score (0.0 to 1.0) based on response time thresholds |
| **Visualization** | Gauge |
| **Score guide** | 1.0 = Excellent, 0.7–0.85 = Fair, < 0.7 = Poor |
| **How it works** | Satisfied (fast) requests score 1.0, Tolerating (medium) score 0.5, Frustrated (slow/error) score 0 |

---

### 8.3 Error Budget Remaining

| Field | Detail |
|-------|--------|
| **What it measures** | How much of our allowed downtime/error budget we have left for the month |
| **Visualization** | Burn rate chart (should be a gradual slope, not a cliff) |
| **Use case** | If the budget burns fast after a deployment, roll back before the SLO is breached |

---

## 9. Dashboard Layout

The dashboard is arranged so that the most critical information is at the top, with drill-down detail below.

```
┌─────────────────────────────────────────────────────────────────┐
│  ROW 1 — HEALTH AT A GLANCE                                     │
│  [Availability %]   [Error Rate %]   [P99 Latency]   [Apdex]   │
├─────────────────────────────────────────────────────────────────┤
│  ROW 2 — TRAFFIC & ERRORS                                       │
│  [Requests/min (line chart)]  │  [4xx vs 5xx stacked bar]       │
├─────────────────────────────────────────────────────────────────┤
│  ROW 3 — LATENCY TRENDS                                         │
│  [P50 / P90 / P99 overlaid line chart — full width]             │
├─────────────────────────────────────────────────────────────────┤
│  ROW 4 — ENDPOINT DRILL-DOWN                                    │
│  [Top 5 slowest endpoints]    │  [Top 5 failing endpoints]      │
├─────────────────────────────────────────────────────────────────┤
│  ROW 5 — INFRASTRUCTURE                                         │
│  [JVM Heap %]  [GC Pause]  [CPU %]  [Pod Restarts]             │
├─────────────────────────────────────────────────────────────────┤
│  ROW 6 — DEPENDENCIES                                           │
│  [DB call duration]  │  [Downstream service response times]     │
├─────────────────────────────────────────────────────────────────┤
│  ROW 7 — ALERTS                                                 │
│  [Davis AI Problems Feed — live alert list]                     │
└─────────────────────────────────────────────────────────────────┘
```

---

## 10. Alert Thresholds Reference

Quick reference for all thresholds in one place.

| Metric | Warning | Critical |
|--------|---------|----------|
| Service availability | < 99.9% | < 99.0% |
| HTTP error rate | > 1% | > 5% |
| P90 response time | > 800ms | > 1500ms |
| P99 response time | > 2000ms | > 3000ms |
| JVM heap usage | > 70% | > 85% |
| GC pause time | > 300ms | > 1000ms |
| CPU usage | > 60% | > 80% |
| Thread pool usage | > 70% | > 90% |
| DB connection pool | > 70% | > 90% |
| DB query duration (avg) | > 200ms | > 500ms |
| Pod restarts | > 0 | > 2 in 10 min |

---

## 11. On-Call Runbook — What to Do When Something is Red

### High Error Rate (5xx)

1. Check the **Top Failing Endpoints** tile — identify which endpoint
2. Check the **Exception Count** tile — find the exception type
3. Go to Dynatrace > Services > select endpoint > **View Traces** — find a failing trace
4. Expand the trace to see the exact line/call that failed
5. Check for a recent deployment — consider rollback if error rate > 10%

### High Latency (P90 spike)

1. Check if the latency spike aligns with a **GC Pause spike** — if yes, JVM tuning needed
2. Check **Downstream Service Response Time** — if a dependency is slow, escalate to that team
3. Check **DB Call Duration** — if elevated, check for slow query or connection pool exhaustion
4. Check **CPU and Thread Pool** — if saturated, the service may need scaling

### Service Availability Drop

1. Check **Pod Restarts** — if > 0, check pod logs for OOMKilled or crash
2. Check if the service is returning any response at all — if not, check deployment / load balancer
3. Check **Davis AI Problems feed** — Dynatrace often identifies root cause automatically

### Sudden Traffic Drop (RPM goes to near zero)

1. Check if a deployment or config change happened in the last 30 minutes
2. Check upstream systems — the drop may be on the calling side
3. Check load balancer / API gateway health outside Dynatrace

### Pod Restarts

1. Immediately check pod logs: look for `OOMKilled` (memory issue) or stack trace (crash)
2. If OOMKilled: check **JVM Heap** trend — was it climbing before the restart?
3. If crash: find the stack trace and raise a bug

---

*Last updated: March 2026 | Maintained by the Investments API team*
