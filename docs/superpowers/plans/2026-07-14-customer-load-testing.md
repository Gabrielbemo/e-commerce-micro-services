# Customer Load Testing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add customer-domain load testing that stays on the existing `k6`/Prometheus/Grafana stack, supports both `baseline` and `capacity` profiles, and isolates customer API performance from the mixed-domain suite.

**Architecture:** Keep the current mixed `tests/performance/k6/main.js` intact. Add a separate customer-only entrypoint plus customer-specific scenarios, thresholds, runners, and dashboarding so customer performance can be evaluated independently by `testid`, `scenario`, `endpoint`, and `application`.

**Tech Stack:** `k6`, Prometheus remote write, Grafana, Spring Boot Actuator, Micrometer, shell runners

## Global Constraints

- Use `k6` only; do not introduce JMeter or Gatling.
- Scope is only the public customer API through the gateway.
- Preserve the existing mixed-domain load suite.
- Keep the old lifecycle flow as a smoke-style scenario, not the main capacity signal.
- Optimize for two profiles: `baseline` and `capacity`.
- Keep changes minimal and aligned with current repo patterns.

---

### Task 1: Add Customer-Only Entrypoint And Profile Config

**Files:**
- Create: `tests/performance/k6/customer-main.js`
- Modify: `tests/performance/k6/config.js`

**Interfaces:**
- Consumes: `fetchAccessToken(config)`, `buildScenario(rate, exec)`, `createCustomerPayload(customSuffix)`, `extractCreatedId(response)`
- Produces: `config.customerProfile`, `config.customerSeedCount`, `config.customersListRate`, `config.customersDetailRate`, `config.customersCreateRate`, `config.customersUpdateRate`, `config.customersLifecycleRate`

- [ ] Write a failing verification by running the future customer entrypoint before it exists.
- [ ] Add profile-aware customer config to `tests/performance/k6/config.js`.
- [ ] Create `tests/performance/k6/customer-main.js` with customer-only `options`, `setup()`, and `handleSummary()`.
- [ ] Re-run the customer entrypoint and confirm the failure changes from missing file to missing scenarios or passing setup.

### Task 2: Split Customer Workload Into Focused Scenarios

**Files:**
- Create: `tests/performance/k6/scenarios/customers-list.js`
- Create: `tests/performance/k6/scenarios/customers-detail.js`
- Create: `tests/performance/k6/scenarios/customers-create.js`
- Create: `tests/performance/k6/scenarios/customers-update.js`
- Create: `tests/performance/k6/scenarios/customers-lifecycle.js`
- Modify: `tests/performance/k6/lib/data.js`
- Modify: `tests/performance/k6/lib/metrics.js`

**Interfaces:**
- Consumes: seeded customers returned from `setup()` in `customer-main.js`
- Produces: `runCustomersListScenario(setupData)`, `runCustomersDetailScenario(setupData)`, `runCustomersCreateScenario()`, `runCustomersUpdateScenario(setupData)`, `runCustomersLifecycleScenario()`

- [ ] Add failing verification by importing the new scenario functions from `customer-main.js` before they exist.
- [ ] Create focused customer scenarios with endpoint-specific tags.
- [ ] Extend shared data and metrics helpers only where needed.
- [ ] Run the customer suite and confirm each scenario executes independently.

### Task 3: Add Profile-Specific Thresholds And Summaries

**Files:**
- Modify: `tests/performance/k6/customer-main.js`
- Modify: `tests/performance/k6/config.js`
- Modify: `tests/performance/k6/lib/metrics.js`

**Interfaces:**
- Consumes: scenario names and customer profile config
- Produces: profile-aware thresholds and profile-aware summary output

- [ ] Add failing verification by asserting the summary output does not yet contain customer profile metadata.
- [ ] Add profile-specific thresholds and profile/seed metadata to summary output.
- [ ] Re-run the customer suite and confirm scenario-targeted threshold keys and summary lines are present.

### Task 4: Add Customer-Only Runners

**Files:**
- Create: `tests/performance/run-customer-local.sh`
- Create: `tests/performance/run-customer-docker.sh`

**Interfaces:**
- Consumes: `tests/performance/k6/customer-main.js`
- Produces: executable customer-only local and docker entry paths

- [ ] Add failing verification by trying to execute the missing customer-only runners.
- [ ] Create local and docker customer runners by following the existing runner structure.
- [ ] Re-run both runners and confirm they print `testid` and target the customer-only entrypoint.

### Task 5: Add Customer-Focused Grafana Dashboard

**Files:**
- Create: `observability/grafana/dashboards/customer-load-overview.json`

**Interfaces:**
- Consumes: Prometheus datasource UID `prometheus`, `testid`, `scenario`, `endpoint`, and `application` labels
- Produces: a dedicated `Observability` dashboard for customer load runs

- [ ] Add failing verification by confirming the customer dashboard file does not exist.
- [ ] Create a customer-specific Grafana dashboard JSON using the existing dashboard patterns.
- [ ] Confirm the new dashboard file is provisionable under the existing dashboards folder.

### Task 6: Update Documentation

**Files:**
- Modify: `tests/performance/README.md`
- Modify: `README.md`

**Interfaces:**
- Consumes: new runners, profile names, env vars, and dashboard path
- Produces: documented customer load workflow and interpretation guidance

- [ ] Add failing verification by searching docs for the missing customer-only runner names.
- [ ] Update `tests/performance/README.md` with customer-only usage, env vars, profile guidance, and observability guidance.
- [ ] Update the root `README.md` with a concise customer load section that links to the performance docs.
- [ ] Follow the docs once to verify they describe the implemented commands and files accurately.
