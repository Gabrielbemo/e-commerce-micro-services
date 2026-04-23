import execution from 'k6/execution';

export const paymentMethods = ['CREDIT_CARD', 'PAYPAL', 'VISA', 'MASTER_CARD'];

export function buildJsonParams(token, tags) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
      'Content-Type': 'application/json',
    },
    tags: Object.assign({ type: 'api' }, tags),
  };
}

export function buildAuthParams(token, tags) {
  return {
    headers: {
      Authorization: `Bearer ${token}`,
    },
    tags: Object.assign({ type: 'api' }, tags),
  };
}

export function uniqueSuffix(prefix) {
  const scenarioName = execution.scenario && execution.scenario.name ? execution.scenario.name : 'setup';
  const vuId = execution.vu && execution.vu.idInTest ? execution.vu.idInTest : 0;
  const iteration = execution.scenario && execution.scenario.iterationInTest ? execution.scenario.iterationInTest : Date.now();

  return `${prefix}-${scenarioName}-${vuId}-${iteration}`;
}

export function fakeUuid(sequence) {
  const tail = String(sequence).padStart(12, '0').slice(-12);
  return `00000000-0000-4000-8000-${tail}`;
}

export function pickFromPool(items) {
  if (!items || items.length === 0) {
    throw new Error('Cannot pick from an empty pool');
  }

  const index = execution.scenario.iterationInTest % items.length;
  return items[index];
}

export function createCustomerPayload(customSuffix) {
  const suffix = customSuffix || uniqueSuffix('customer');

  return {
    firstName: 'Load',
    lastName: suffix,
    email: `${suffix}@example.com`,
    address: {
      street: 'Performance Street',
      houseNumber: '100',
      zipCode: '12345',
    },
  };
}

export function createCustomerUpdatePayload(customerId) {
  const payload = createCustomerPayload();

  return {
    id: customerId,
    firstName: 'Updated',
    lastName: payload.lastName,
    email: payload.email,
    address: payload.address,
  };
}

export function createProductPayload(categoryId, availableQuantity) {
  const suffix = uniqueSuffix('product');

  return {
    name: `Load Product ${suffix}`,
    description: `Generated for load testing ${suffix}`,
    availableQuantity,
    price: 199.9,
    categoryId,
  };
}

export function createOrderPayload(customerId, product) {
  const suffix = uniqueSuffix('order');

  return {
    reference: `ORDER-${suffix}`,
    amount: product.price,
    paymentMethod: paymentMethods[execution.scenario.iterationInTest % paymentMethods.length],
    customerId,
    productPurchaseRequests: [
      {
        productId: product.id,
        quantity: 1,
      },
    ],
  };
}

export function createPaymentPayload(customer) {
  const sequence = execution.scenario.iterationInTest + execution.vu.idInTest;
  const suffix = uniqueSuffix('payment');

  return {
    amount: 149.9,
    paymentMethod: paymentMethods[sequence % paymentMethods.length],
    orderId: fakeUuid(sequence),
    orderReference: `PAY-${suffix}`,
    customer: {
      id: customer.id,
      firstName: customer.firstName,
      lastName: customer.lastName,
      email: customer.email,
    },
  };
}

export function createSetupProductPayload(categoryId, index) {
  return {
    name: `Setup Load Product ${index}`,
    description: `Dedicated product ${index} for load scenarios`,
    availableQuantity: 10000,
    price: 99.9 + index,
    categoryId,
  };
}

export function extractCreatedId(response) {
  const body = response.body ? String(response.body).trim() : '';

  if (!body) {
    return null;
  }

  if (body.charAt(0) !== '{' && body.charAt(0) !== '[' && body.charAt(0) !== '"') {
    return body;
  }

  try {
    const parsedBody = response.json();
    return typeof parsedBody === 'string' ? parsedBody : parsedBody && parsedBody.id;
  } catch (error) {
    return body.replace(/^"|"$/g, '');
  }
}
