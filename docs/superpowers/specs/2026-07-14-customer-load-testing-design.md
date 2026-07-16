# Customer Load Testing Design

## Goal

Implement customer-domain load testing on top of the existing `k6` + Prometheus + Grafana stack without introducing a second toolchain.

The solution must support two use cases:

- a fast local `baseline` profile for regressions
- a stronger `capacity` profile for customer-focused performance validation

Scope is limited to the public customer API exposed by the gateway.

## Existing Context

The repository already includes a working performance harness under `tests/performance`:

- `k6` scripts in `tests/performance/k6`
- local and docker runners in `tests/performance/run-local.sh` and `tests/performance/run-docker.sh`
- Prometheus remote write from `k6`
- Grafana dashboards under `observability/grafana/dashboards`
- Spring Boot Actuator and Micrometer metrics on `gateway-server` and `customer-service`

The current customer workload is a single CRUD-oriented scenario in `tests/performance/k6/scenarios/customers.js`.

That scenario is useful for smoke coverage, but it is not enough for customer-focused performance conclusions because:

- it is executed together with products, orders, and payments
- mixed-domain totals hide customer-specific behavior
- it keeps the dataset roughly flat, which under-tests `GET /api/v1/customers`
- it treats all customer endpoints as one latency bucket

## Tooling Decision

Use the existing `k6` stack.

Do not introduce JMeter, Gatling, or any second load-generation framework.

Reasons:

- the repository is already wired for `k6`
- the current suite already tags requests by scenario and endpoint
- Prometheus and Grafana already ingest and visualize `k6` metrics
- keeping one toolchain reduces maintenance, docs drift, and onboarding cost

## Architecture

Add a customer-only load entrypoint while preserving the current mixed-domain suite.

The implementation will:

- keep `tests/performance/k6/main.js` for the existing mixed-domain run
- add a new customer-only `k6` entrypoint
- split customer traffic into focused scenarios instead of one monolithic CRUD flow
- add customer-specific runners and documentation
- add a customer-focused Grafana dashboard

This produces clean customer-domain measurements without breaking the existing repository behavior.

## Profiles

Two customer profiles will be supported.

### Baseline

Purpose:

- fast local regression detection
- repeatable developer workflow

Characteristics:

- lower arrival rates
- shorter steady-state duration
- includes lifecycle smoke coverage

### Capacity

Purpose:

- customer-focused load validation
- clearer bottleneck discovery for the customer API

Characteristics:

- higher arrival rates
- longer steady-state duration
- emphasizes read-heavy traffic
- keeps lifecycle smoke coverage light

## Flows Covered

### 1. Customers List

Endpoint:

- `GET /api/v1/customers`

Purpose:

- expose the highest-risk path in the service

Rationale:

- the service currently performs an unpaginated `findAll()`
- this path is the most likely to degrade first as dataset size grows

### 2. Customers Detail Lookup

Endpoints:

- `GET /api/v1/customers/{id}`
- `GET /api/v1/customers/exists/{id}`

Purpose:

- represent read-heavy lookup traffic on existing customers

### 3. Customers Create

Endpoint:

- `POST /api/v1/customers`

Purpose:

- measure onboarding throughput and write latency

### 4. Customers Update

Endpoint:

- `PUT /api/v1/customers`

Purpose:

- measure read-then-write update behavior on existing records

### 5. Customers Lifecycle Smoke

Endpoints:

- `POST /api/v1/customers`
- `GET /api/v1/customers/exists/{id}`
- `GET /api/v1/customers/{id}`
- `PUT /api/v1/customers`
- `DELETE /api/v1/customers/{id}`

Purpose:

- preserve end-to-end CRUD validation as a smoke flow

Role:

- important for `baseline`
- secondary for `capacity`

## Workload Strategy

Customer load will be split across separate scenarios instead of one all-in-one flow.

Recommended emphasis for the `capacity` profile:

- 50% list traffic
- 25% detail and exists traffic
- 15% create traffic
- 10% update and lifecycle cleanup traffic

This keeps the workload realistic for a customer API and ensures the unpaginated list endpoint receives enough pressure to reveal regressions.

## Data Strategy

Seed customer data during `setup()`.

The seeded pool is used to:

- support detail lookups
- support update operations
- give the list endpoint a meaningful dataset size

Create scenarios continue generating unique customer payloads so writes do not collide on email values.

Cleanup remains controlled so the dataset does not collapse or grow uncontrollably during a test run.

## Metrics

Primary client-side metrics:

- `http_req_failed`
- `checks`
- request count and request rate
- latency `avg`, `p95`, `p99`, `max`
- per-scenario flow duration

Primary server-side metrics:

- `http_server_requests_seconds_*` on `customer-service`
- the same metric on `gateway-server`
- application RPS by service
- 4xx and 5xx rates
- JVM memory and heap pressure
- `up`

Customer results must remain explorable by:

- `testid`
- `scenario`
- `endpoint`
- `application=customer-service`

## Threshold Strategy

Thresholds should be scenario-specific rather than one generic customer threshold.

Baseline expectations:

- `http_req_failed < 1%`
- `checks > 99%`
- tighter latency thresholds for reads than writes

Capacity expectations:

- `http_req_failed < 2%`
- `checks > 98%`
- read thresholds slightly looser than baseline but still scenario-specific

The list endpoint may have a looser latency threshold than detail and exists because it is structurally heavier.

## Reporting And Observability

Keep the existing summary artifacts:

- `tests/performance/results/latest-summary.json`
- `tests/performance/results/latest-summary.txt`

Extend customer-only runs so summaries include:

- selected profile
- seeded customer count
- per-scenario latency summaries
- endpoint-level latency summaries when available

Grafana should gain a dedicated customer dashboard instead of overloading the generic load dashboard.

The new dashboard should show:

- customer request totals
- request rate by customer scenario
- latency by customer scenario
- endpoint summary by name or endpoint tag
- `gateway-server` versus `customer-service` latency comparison
- application RPS and error-rate panels for the selected test run

## Error Handling

The load suite should fail early if setup is invalid.

Hard-stop conditions:

- auth token cannot be obtained
- gateway is not ready
- seeded customers cannot be created

Functional failures and performance failures should remain distinguishable:

- functional failures are wrong statuses, wrong bodies, or missing IDs
- performance failures are threshold breaches on otherwise valid responses

## Out Of Scope

The first iteration does not include:

- indirect customer reads caused by order-service
- negative-path load as a primary scenario
- production-capacity claims from local hardware
- pagination or search scenarios that the API does not currently expose

## Rollout

Implementation should proceed in this order:

1. add the customer-only entrypoint and profile config
2. split the customer workload into focused scenarios
3. add customer-only runners
4. add customer-focused dashboarding
5. update docs

## Constraints

- use `k6` only
- preserve the existing mixed-domain suite
- keep the old lifecycle scenario as smoke coverage
- keep the implementation minimal and aligned with current repository patterns
