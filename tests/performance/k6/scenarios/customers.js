import http from 'k6/http';
import { check, group } from 'k6';
import { config } from '../config.js';
import { getAccessToken } from '../lib/auth.js';
import {
  buildAuthParams,
  buildJsonParams,
  createCustomerPayload,
  createCustomerUpdatePayload,
  extractCreatedId,
} from '../lib/data.js';
import { businessFlowDuration } from '../lib/metrics.js';

export function runCustomersScenario() {
  const token = getAccessToken(config);
  const startedAt = Date.now();

  group('customers', () => {
    const listResponse = http.get(
      `${config.gatewayBaseUrl}/api/v1/customers`,
      buildAuthParams(token, {
        domain: 'customers',
        endpoint: 'customers_list',
        name: 'GET /api/v1/customers',
      })
    );

    check(listResponse, {
      'customers list returns 200': (response) => response.status === 200,
    });

    const createPayload = createCustomerPayload();
    const createResponse = http.post(
      `${config.gatewayBaseUrl}/api/v1/customers`,
      JSON.stringify(createPayload),
      buildJsonParams(token, {
        domain: 'customers',
        endpoint: 'customers_create',
        name: 'POST /api/v1/customers',
      })
    );

    check(createResponse, {
      'customer create returns 200': (response) => response.status === 200,
    });

    if (createResponse.status !== 200) {
      return;
    }

    const customerId = extractCreatedId(createResponse);

    const existsResponse = http.get(
      `${config.gatewayBaseUrl}/api/v1/customers/exists/${customerId}`,
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
      `${config.gatewayBaseUrl}/api/v1/customers/${customerId}`,
      buildAuthParams(token, {
        domain: 'customers',
        endpoint: 'customers_get_by_id',
        name: 'GET /api/v1/customers/{id}',
      })
    );

    check(detailResponse, {
      'customer by id returns 200': (response) => response.status === 200,
    });

    const updateResponse = http.put(
      `${config.gatewayBaseUrl}/api/v1/customers`,
      JSON.stringify(createCustomerUpdatePayload(customerId)),
      buildJsonParams(token, {
        domain: 'customers',
        endpoint: 'customers_update',
        name: 'PUT /api/v1/customers',
      })
    );

    check(updateResponse, {
      'customer update returns 202': (response) => response.status === 202,
    });

    const deleteResponse = http.del(
      `${config.gatewayBaseUrl}/api/v1/customers/${customerId}`,
      null,
      buildAuthParams(token, {
        domain: 'customers',
        endpoint: 'customers_delete',
        name: 'DELETE /api/v1/customers/{id}',
      })
    );

    check(deleteResponse, {
      'customer delete returns 204': (response) => response.status === 204,
    });
  });

  businessFlowDuration.add(Date.now() - startedAt, { scenario: 'customers' });
}
