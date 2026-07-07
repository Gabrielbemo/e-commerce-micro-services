import http from 'k6/http';
import { check, group } from 'k6';
import { config } from '../config.js';
import { getAccessToken } from '../lib/auth.js';
import {
  buildAuthParams,
  buildJsonParams,
  createOrderPayload,
  extractCreatedId,
  pickFromPool,
} from '../lib/data.js';
import { businessFlowDuration } from '../lib/metrics.js';

export function runOrdersScenario(setupData) {
  const token = getAccessToken(config);
  const startedAt = Date.now();

  group('orders', () => {
    const customer = pickFromPool(setupData.customers);
    const product = pickFromPool(setupData.products);

    const createResponse = http.post(
      `${config.gatewayBaseUrl}/api/v1/orders`,
      JSON.stringify(createOrderPayload(customer.id, product)),
      buildJsonParams(token, {
        domain: 'orders',
        endpoint: 'orders_create',
        name: 'POST /api/v1/orders',
      })
    );

    check(createResponse, {
      'order create returns 200': (response) => response.status === 200,
    });

    if (createResponse.status !== 200) {
      return;
    }

    const orderId = extractCreatedId(createResponse);

    const listResponse = http.get(
      `${config.gatewayBaseUrl}/api/v1/orders`,
      buildAuthParams(token, {
        domain: 'orders',
        endpoint: 'orders_list',
        name: 'GET /api/v1/orders',
      })
    );

    check(listResponse, {
      'orders list returns 200': (response) => response.status === 200,
    });

    const detailResponse = http.get(
      `${config.gatewayBaseUrl}/api/v1/orders/${orderId}`,
      buildAuthParams(token, {
        domain: 'orders',
        endpoint: 'orders_get_by_id',
        name: 'GET /api/v1/orders/{id}',
      })
    );

    check(detailResponse, {
      'order by id returns 200': (response) => response.status === 200,
    });

    const orderLinesResponse = http.get(
      `${config.gatewayBaseUrl}/api/v1/order-lines/order/${orderId}`,
      buildAuthParams(token, {
        domain: 'orders',
        endpoint: 'order_lines_by_order',
        name: 'GET /api/v1/order-lines/order/{orderId}',
      })
    );

    check(orderLinesResponse, {
      'order lines returns 200': (response) => response.status === 200,
    });
  });

  businessFlowDuration.add(Date.now() - startedAt, { scenario: 'orders' });
}
