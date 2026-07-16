import http from 'k6/http';
import { check, group } from 'k6';
import { config } from '../config.js';
import { getAccessToken } from '../lib/auth.js';
import { buildJsonParams, createCustomerUpdatePayload, pickFromPool } from '../lib/data.js';
import { businessFlowDuration } from '../lib/metrics.js';

export function runCustomersUpdateScenario(setupData) {
  const token = getAccessToken(config);
  const startedAt = Date.now();
  const customer = pickFromPool(setupData.customers);

  group('customers_update', () => {
    const response = http.put(
      `${config.gatewayBaseUrl}/api/v1/customers`,
      JSON.stringify(createCustomerUpdatePayload(customer.id)),
      buildJsonParams(token, {
        domain: 'customers',
        endpoint: 'customers_update',
        name: 'PUT /api/v1/customers',
      })
    );

    check(response, {
      'customer update returns 202': (result) => result.status === 202,
    });
  });

  businessFlowDuration.add(Date.now() - startedAt, { scenario: 'customers_update' });
}
