import http from 'k6/http';
import { fail } from 'k6';
import { config, buildScenario } from './config.js';
import { fetchAccessToken } from './lib/auth.js';
import {
  buildJsonParams,
  createCustomerPayload,
  extractCreatedId,
} from './lib/data.js';
import { runCustomersListScenario } from './scenarios/customers-list.js';
import { runCustomersDetailScenario } from './scenarios/customers-detail.js';
import { runCustomersCreateScenario } from './scenarios/customers-create.js';
import { runCustomersUpdateScenario } from './scenarios/customers-update.js';
import { runCustomersLifecycleScenario } from './scenarios/customers-lifecycle.js';

function buildCustomerScenarios() {
  const scenarios = {};
  const rates = [
    ['customers_list', config.customersListRate, 'runCustomersListScenario'],
    ['customers_detail', config.customersDetailRate, 'runCustomersDetailScenario'],
    ['customers_create', config.customersCreateRate, 'runCustomersCreateScenario'],
    ['customers_update', config.customersUpdateRate, 'runCustomersUpdateScenario'],
    ['customers_lifecycle', config.customersLifecycleRate, 'runCustomersLifecycleScenario'],
  ];

  rates.forEach(([name, rate, exec]) => {
    if (rate > 0) {
      scenarios[name] = buildScenario(rate, exec);
    }
  });

  return scenarios;
}

function buildThresholds() {
  const isCapacity = config.customerProfile === 'capacity';

  return {
    http_req_failed: [isCapacity ? 'rate<0.02' : 'rate<0.01'],
    checks: [isCapacity ? 'rate>0.98' : 'rate>0.99'],
    'http_req_duration{scenario:customers_list,type:api}': [
      isCapacity ? 'p(99)<1800' : 'p(99)<800',
      isCapacity ? 'avg<600' : 'avg<250',
    ],
    'http_req_duration{scenario:customers_detail,type:api}': [
      isCapacity ? 'p(99)<1500' : 'p(99)<800',
      isCapacity ? 'avg<500' : 'avg<250',
    ],
    'http_req_duration{scenario:customers_create,type:api}': [
      isCapacity ? 'p(99)<2000' : 'p(99)<1200',
      isCapacity ? 'avg<800' : 'avg<400',
    ],
    'http_req_duration{scenario:customers_update,type:api}': [
      isCapacity ? 'p(99)<2000' : 'p(99)<1200',
      isCapacity ? 'avg<800' : 'avg<400',
    ],
    'http_req_duration{scenario:customers_lifecycle,type:api}': [
      isCapacity ? 'p(99)<2500' : 'p(99)<1800',
      isCapacity ? 'avg<900' : 'avg<600',
    ],
    'business_flow_duration{scenario:customers_list}': [
      isCapacity ? 'p(99)<1800' : 'p(99)<1000',
      isCapacity ? 'avg<700' : 'avg<300',
    ],
    'business_flow_duration{scenario:customers_detail}': [
      isCapacity ? 'p(99)<1800' : 'p(99)<1200',
      isCapacity ? 'avg<700' : 'avg<350',
    ],
    'business_flow_duration{scenario:customers_create}': [
      isCapacity ? 'p(99)<2200' : 'p(99)<1500',
      isCapacity ? 'avg<900' : 'avg<500',
    ],
    'business_flow_duration{scenario:customers_update}': [
      isCapacity ? 'p(99)<2200' : 'p(99)<1500',
      isCapacity ? 'avg<900' : 'avg<500',
    ],
    'business_flow_duration{scenario:customers_lifecycle}': [
      isCapacity ? 'p(99)<3500' : 'p(99)<2500',
      isCapacity ? 'avg<1200' : 'avg<800',
    ],
  };
}

export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
  scenarios: buildCustomerScenarios(),
  thresholds: buildThresholds(),
  tags: {
    system: 'e-commerce-micro-services',
    test_type: __ENV.TEST_TYPE || 'customer-load',
    test_scope: 'customers',
    customer_profile: config.customerProfile,
  },
};

function seedCustomers(token) {
  const customers = [];

  for (let index = 0; index < config.customerSeedCount; index += 1) {
    const payload = createCustomerPayload(`customer-seed-${index}-${Date.now()}`);
    const response = http.post(
      `${config.gatewayBaseUrl}/api/v1/customers`,
      JSON.stringify(payload),
      buildJsonParams(token, {
        domain: 'setup',
        endpoint: 'setup_customers_seed',
        name: 'POST /api/v1/customers [customer-setup]',
      })
    );

    if (response.status !== 200) {
      fail(`Failed to create customer seed. Status=${response.status} body=${response.body}`);
    }

    customers.push({
      id: extractCreatedId(response),
      firstName: payload.firstName,
      lastName: payload.lastName,
      email: payload.email,
    });
  }

  return customers;
}

export function setup() {
  const token = fetchAccessToken(config);

  return {
    customerProfile: config.customerProfile,
    customerSeedCount: config.customerSeedCount,
    customers: seedCustomers(token),
  };
}

export {
  runCustomersListScenario,
  runCustomersDetailScenario,
  runCustomersCreateScenario,
  runCustomersUpdateScenario,
  runCustomersLifecycleScenario,
};

export function handleSummary(data) {
  const summaryDir = __ENV.K6_SUMMARY_DIR || 'tests/performance/results';
  const metrics = data && data.metrics ? data.metrics : {};
  const httpDuration = metrics.http_req_duration && metrics.http_req_duration.values ? metrics.http_req_duration.values : {};
  const checks = metrics.checks && metrics.checks.values ? metrics.checks.values : {};
  const httpReqFailed = metrics.http_req_failed && metrics.http_req_failed.values ? metrics.http_req_failed.values : {};
  const httpReqs = metrics.http_reqs && metrics.http_reqs.values ? metrics.http_reqs.values : {};
  const scenarios = ['customers_list', 'customers_detail', 'customers_create', 'customers_update', 'customers_lifecycle'];

  const lines = [
    'Customer load test summary',
    `customer_profile=${config.customerProfile}`,
    `seeded_customers=${config.customerSeedCount}`,
    `checks_rate=${checks.rate !== undefined ? checks.rate : 'n/a'}`,
    `http_req_failed_rate=${httpReqFailed.rate !== undefined ? httpReqFailed.rate : 'n/a'}`,
    `http_reqs_count=${httpReqs.count !== undefined ? httpReqs.count : 'n/a'}`,
    `overall_http_req_duration_avg_ms=${httpDuration.avg !== undefined ? httpDuration.avg : 'n/a'}`,
    `overall_http_req_duration_p95_ms=${httpDuration['p(95)'] !== undefined ? httpDuration['p(95)'] : 'n/a'}`,
    `overall_http_req_duration_p99_ms=${httpDuration['p(99)'] !== undefined ? httpDuration['p(99)'] : 'n/a'}`,
  ];

  scenarios.forEach((scenario) => {
    const apiMetricKey = `http_req_duration{scenario:${scenario},type:api}`;
    const apiMetric = metrics[apiMetricKey] && metrics[apiMetricKey].values ? metrics[apiMetricKey].values : null;
    const flowMetricKey = `business_flow_duration{scenario:${scenario}}`;
    const flowMetric = metrics[flowMetricKey] && metrics[flowMetricKey].values ? metrics[flowMetricKey].values : null;

    if (apiMetric) {
      lines.push(`${scenario}_api_avg_ms=${apiMetric.avg !== undefined ? apiMetric.avg : 'n/a'}`);
      lines.push(`${scenario}_api_p95_ms=${apiMetric['p(95)'] !== undefined ? apiMetric['p(95)'] : 'n/a'}`);
      lines.push(`${scenario}_api_p99_ms=${apiMetric['p(99)'] !== undefined ? apiMetric['p(99)'] : 'n/a'}`);
    }

    if (flowMetric) {
      lines.push(`${scenario}_flow_avg_ms=${flowMetric.avg !== undefined ? flowMetric.avg : 'n/a'}`);
      lines.push(`${scenario}_flow_p95_ms=${flowMetric['p(95)'] !== undefined ? flowMetric['p(95)'] : 'n/a'}`);
      lines.push(`${scenario}_flow_p99_ms=${flowMetric['p(99)'] !== undefined ? flowMetric['p(99)'] : 'n/a'}`);
    }
  });

  return {
    stdout: `${lines.join('\n')}\n`,
    [`${summaryDir}/latest-summary.json`]: JSON.stringify(data, null, 2),
    [`${summaryDir}/latest-summary.txt`]: `${lines.join('\n')}\n`,
  };
}
