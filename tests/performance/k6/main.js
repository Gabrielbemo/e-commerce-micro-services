import http from 'k6/http';
import { fail } from 'k6';
import { config, buildScenario } from './config.js';
import { fetchAccessToken } from './lib/auth.js';
import {
  buildAuthParams,
  buildJsonParams,
  createCustomerPayload,
  createSetupProductPayload,
  extractCreatedId,
} from './lib/data.js';
import { runProductsScenario } from './scenarios/products.js';
import { runCustomersScenario } from './scenarios/customers.js';
import { runOrdersScenario } from './scenarios/orders.js';
import { runPaymentsScenario } from './scenarios/payments.js';

export const options = {
  summaryTrendStats: ['avg', 'min', 'med', 'max', 'p(95)', 'p(99)'],
  scenarios: {
    products: buildScenario(config.productsRate, 'runProductsScenario'),
    customers: buildScenario(config.customersRate, 'runCustomersScenario'),
    orders: buildScenario(config.ordersRate, 'runOrdersScenario'),
    payments: buildScenario(config.paymentsRate, 'runPaymentsScenario'),
  },
  thresholds: {
    http_req_failed: ['rate<0.05'],
    checks: ['rate>0.95'],
    'http_req_duration{scenario:products,type:api}': ['p(99)<2000', 'avg<800'],
    'http_req_duration{scenario:customers,type:api}': ['p(99)<2000', 'avg<800'],
    'http_req_duration{scenario:orders,type:api}': ['p(99)<3000', 'avg<1200'],
    'http_req_duration{scenario:payments,type:api}': ['p(99)<2000', 'avg<800'],
    'business_flow_duration{scenario:products}': ['p(99)<4000', 'avg<1500'],
    'business_flow_duration{scenario:customers}': ['p(99)<4000', 'avg<1500'],
    'business_flow_duration{scenario:orders}': ['p(99)<5000', 'avg<2000'],
    'business_flow_duration{scenario:payments}': ['p(99)<2500', 'avg<1000'],
  },
  tags: {
    system: 'e-commerce-micro-services',
    test_type: __ENV.TEST_TYPE || 'load',
  },
};

function seedProducts(token, categoryId) {
  const products = [];

  for (let index = 1; index <= config.setupProducts; index += 1) {
    const payload = createSetupProductPayload(categoryId, index);
    const response = http.post(
      `${config.gatewayBaseUrl}/api/v1/products`,
      JSON.stringify(payload),
      buildJsonParams(token, {
        domain: 'setup',
        endpoint: 'setup_products_create',
        name: 'POST /api/v1/products [setup]',
      })
    );

    if (response.status !== 200) {
      fail(`Failed to create setup product. Status=${response.status} body=${response.body}`);
    }

    products.push({
      id: extractCreatedId(response),
      price: payload.price,
    });
  }

  return products;
}

function seedCustomers(token) {
  const customers = [];

  for (let index = 0; index < config.setupCustomers; index += 1) {
    const payload = createCustomerPayload(`setup-${index}-${Date.now()}`);
    const response = http.post(
      `${config.gatewayBaseUrl}/api/v1/customers`,
      JSON.stringify(payload),
      buildJsonParams(token, {
        domain: 'setup',
        endpoint: 'setup_customers_create',
        name: 'POST /api/v1/customers [setup]',
      })
    );

    if (response.status !== 200) {
      fail(`Failed to create setup customer. Status=${response.status} body=${response.body}`);
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
  const productsResponse = http.get(
    `${config.gatewayBaseUrl}/api/v1/products`,
    buildAuthParams(token, {
      domain: 'setup',
      endpoint: 'setup_products_list',
      name: 'GET /api/v1/products [setup]',
    })
  );

  if (productsResponse.status !== 200) {
    fail(`Failed to list products during setup. Status=${productsResponse.status} body=${productsResponse.body}`);
  }

  const listedProducts = productsResponse.json();
  const categoryId = listedProducts.length > 0 ? listedProducts[0].categoryId : config.fallbackCategoryId;

  return {
    categoryId,
    customers: seedCustomers(token),
    products: seedProducts(token, categoryId),
  };
}

export { runProductsScenario, runCustomersScenario, runOrdersScenario, runPaymentsScenario };

export function handleSummary(data) {
  const metrics = data && data.metrics ? data.metrics : {};
  const httpDuration = metrics.http_req_duration && metrics.http_req_duration.values ? metrics.http_req_duration.values : {};
  const checks = metrics.checks && metrics.checks.values ? metrics.checks.values : {};
  const httpReqFailed = metrics.http_req_failed && metrics.http_req_failed.values ? metrics.http_req_failed.values : {};
  const httpReqs = metrics.http_reqs && metrics.http_reqs.values ? metrics.http_reqs.values : {};
  const scenarios = ['products', 'customers', 'orders', 'payments'];

  const lines = [
    'Load test summary',
    `checks_rate=${checks.rate !== undefined ? checks.rate : 'n/a'}`,
    `http_req_failed_rate=${httpReqFailed.rate !== undefined ? httpReqFailed.rate : 'n/a'}`,
    `http_reqs_count=${httpReqs.count !== undefined ? httpReqs.count : 'n/a'}`,
    `overall_http_req_duration_avg_ms=${httpDuration.avg !== undefined ? httpDuration.avg : 'n/a'}`,
    `overall_http_req_duration_p95_ms=${httpDuration['p(95)'] !== undefined ? httpDuration['p(95)'] : 'n/a'}`,
    `overall_http_req_duration_p99_ms=${httpDuration['p(99)'] !== undefined ? httpDuration['p(99)'] : 'n/a'}`,
  ];

  scenarios.forEach((scenario) => {
    const metricKey = `http_req_duration{scenario:${scenario},type:api}`;
    const metric = metrics[metricKey] && metrics[metricKey].values ? metrics[metricKey].values : null;

    if (!metric) {
      return;
    }

    lines.push(`${scenario}_api_avg_ms=${metric.avg !== undefined ? metric.avg : 'n/a'}`);
    lines.push(`${scenario}_api_p95_ms=${metric['p(95)'] !== undefined ? metric['p(95)'] : 'n/a'}`);
    lines.push(`${scenario}_api_p99_ms=${metric['p(99)'] !== undefined ? metric['p(99)'] : 'n/a'}`);
  });

  return {
    stdout: `${lines.join('\n')}\n`,
    'tests/performance/results/latest-summary.json': JSON.stringify(data, null, 2),
    'tests/performance/results/latest-summary.txt': `${lines.join('\n')}\n`,
  };
}
