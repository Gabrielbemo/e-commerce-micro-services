import http from 'k6/http';
import { check, group } from 'k6';
import { config } from '../config.js';
import { getAccessToken } from '../lib/auth.js';
import { buildJsonParams, createCustomerPayload, extractCreatedId } from '../lib/data.js';
import { businessFlowDuration } from '../lib/metrics.js';

export function runCustomersCreateScenario() {
  const token = getAccessToken(config);
  const startedAt = Date.now();

  group('customers_create', () => {
    const response = http.post(
      `${config.gatewayBaseUrl}/api/v1/customers`,
      JSON.stringify(createCustomerPayload()),
      buildJsonParams(token, {
        domain: 'customers',
        endpoint: 'customers_create',
        name: 'POST /api/v1/customers',
      })
    );

    check(response, {
      'customer create returns 200': (result) => result.status === 200,
      'customer create returns id': (result) => result.status === 200 && Boolean(extractCreatedId(result)),
    });
  });

  businessFlowDuration.add(Date.now() - startedAt, { scenario: 'customers_create' });
}
