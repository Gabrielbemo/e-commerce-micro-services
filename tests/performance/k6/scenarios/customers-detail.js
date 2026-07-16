import http from 'k6/http';
import { check, group } from 'k6';
import { config } from '../config.js';
import { getAccessToken } from '../lib/auth.js';
import { buildAuthParams, pickFromPool } from '../lib/data.js';
import { businessFlowDuration } from '../lib/metrics.js';

export function runCustomersDetailScenario(setupData) {
  const token = getAccessToken(config);
  const startedAt = Date.now();
  const customer = pickFromPool(setupData.customers);

  group('customers_detail', () => {
    const existsResponse = http.get(
      `${config.gatewayBaseUrl}/api/v1/customers/exists/${customer.id}`,
      buildAuthParams(token, {
        domain: 'customers',
        endpoint: 'customers_exists',
        name: 'GET /api/v1/customers/exists/{id}',
      })
    );

    check(existsResponse, {
      'customer exists returns 200': (response) => response.status === 200,
      'customer exists is true': (response) => response.status === 200 && response.json() === true,
    });

    const detailResponse = http.get(
      `${config.gatewayBaseUrl}/api/v1/customers/${customer.id}`,
      buildAuthParams(token, {
        domain: 'customers',
        endpoint: 'customers_get_by_id',
        name: 'GET /api/v1/customers/{id}',
      })
    );

    check(detailResponse, {
      'customer by id returns 200': (response) => response.status === 200,
    });
  });

  businessFlowDuration.add(Date.now() - startedAt, { scenario: 'customers_detail' });
}
