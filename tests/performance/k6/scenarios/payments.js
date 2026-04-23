import http from 'k6/http';
import { check, group } from 'k6';
import { config } from '../config.js';
import { getAccessToken } from '../lib/auth.js';
import { buildJsonParams, createPaymentPayload, pickFromPool } from '../lib/data.js';
import { businessFlowDuration } from '../lib/metrics.js';

export function runPaymentsScenario(setupData) {
  const token = getAccessToken(config);
  const startedAt = Date.now();

  group('payments', () => {
    const customer = pickFromPool(setupData.customers);
    const response = http.post(
      `${config.gatewayBaseUrl}/api/v1/payments`,
      JSON.stringify(createPaymentPayload(customer)),
      buildJsonParams(token, {
        domain: 'payments',
        endpoint: 'payments_create',
        name: 'POST /api/v1/payments',
      })
    );

    check(response, {
      'payment create returns 200': (result) => result.status === 200,
    });
  });

  businessFlowDuration.add(Date.now() - startedAt, { scenario: 'payments' });
}
