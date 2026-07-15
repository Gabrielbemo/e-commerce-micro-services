import http from 'k6/http';
import { check, group } from 'k6';
import { config } from '../config.js';
import { getAccessToken } from '../lib/auth.js';
import { buildAuthParams } from '../lib/data.js';
import { businessFlowDuration } from '../lib/metrics.js';

export function runCustomersListScenario() {
  const token = getAccessToken(config);
  const startedAt = Date.now();

  group('customers_list', () => {
    const response = http.get(
      `${config.gatewayBaseUrl}/api/v1/customers`,
      buildAuthParams(token, {
        domain: 'customers',
        endpoint: 'customers_list',
        name: 'GET /api/v1/customers',
      })
    );

    check(response, {
      'customers list returns 200': (result) => result.status === 200,
    });
  });

  businessFlowDuration.add(Date.now() - startedAt, { scenario: 'customers_list' });
}
