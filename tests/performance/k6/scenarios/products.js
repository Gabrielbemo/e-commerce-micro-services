import http from 'k6/http';
import { check, group } from 'k6';
import { config } from '../config.js';
import { getAccessToken } from '../lib/auth.js';
import {
  buildAuthParams,
  buildJsonParams,
  createProductPayload,
  extractCreatedId,
  pickFromPool,
} from '../lib/data.js';
import { businessFlowDuration } from '../lib/metrics.js';

export function runProductsScenario(setupData) {
  const token = getAccessToken(config);
  const startedAt = Date.now();

  group('products', () => {
    const listResponse = http.get(
      `${config.gatewayBaseUrl}/api/v1/products`,
      buildAuthParams(token, {
        domain: 'products',
        endpoint: 'products_list',
        name: 'GET /api/v1/products',
      })
    );

    check(listResponse, {
      'products list returns 200': (response) => response.status === 200,
    });

    const listBody = listResponse.status === 200 ? listResponse.json() : [];
    const lookupProduct = listBody.length > 0 ? listBody[0] : pickFromPool(setupData.products);

    const detailResponse = http.get(
      `${config.gatewayBaseUrl}/api/v1/products/${lookupProduct.id}`,
      buildAuthParams(token, {
        domain: 'products',
        endpoint: 'products_get_by_id',
        name: 'GET /api/v1/products/{id}',
      })
    );

    check(detailResponse, {
      'product by id returns 200': (response) => response.status === 200,
    });

    const createResponse = http.post(
      `${config.gatewayBaseUrl}/api/v1/products`,
      JSON.stringify(createProductPayload(setupData.categoryId, 2500)),
      buildJsonParams(token, {
        domain: 'products',
        endpoint: 'products_create',
        name: 'POST /api/v1/products',
      })
    );

    check(createResponse, {
      'product create returns 200': (response) => response.status === 200,
    });

    if (createResponse.status === 200) {
      const createdId = extractCreatedId(createResponse);
      const createdDetailResponse = http.get(
        `${config.gatewayBaseUrl}/api/v1/products/${createdId}`,
        buildAuthParams(token, {
          domain: 'products',
          endpoint: 'products_get_created',
          name: 'GET /api/v1/products/{createdId}',
        })
      );

      check(createdDetailResponse, {
        'created product lookup returns 200': (response) => response.status === 200,
      });
    }

    const purchaseProduct = pickFromPool(setupData.products);
    const purchaseResponse = http.post(
      `${config.gatewayBaseUrl}/api/v1/products/purchase`,
      JSON.stringify([
        {
          productId: purchaseProduct.id,
          quantity: 1,
        },
      ]),
      buildJsonParams(token, {
        domain: 'products',
        endpoint: 'products_purchase',
        name: 'POST /api/v1/products/purchase',
      })
    );

    check(purchaseResponse, {
      'product purchase returns 200': (response) => response.status === 200,
    });
  });

  businessFlowDuration.add(Date.now() - startedAt, { scenario: 'products' });
}
