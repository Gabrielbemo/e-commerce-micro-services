function numberFromEnv(name, fallback) {
  const rawValue = __ENV[name];

  if (!rawValue) {
    return fallback;
  }

  const parsedValue = Number(rawValue);
  return Number.isFinite(parsedValue) ? parsedValue : fallback;
}

function stringFromEnv(name, fallback) {
  return __ENV[name] || fallback;
}

export const config = {
  gatewayBaseUrl: stringFromEnv('GATEWAY_BASE_URL', 'http://localhost:8222'),
  authBaseUrl: stringFromEnv('AUTH_BASE_URL', 'http://localhost:9098'),
  oauthRealm: stringFromEnv('OAUTH_REALM', 'micro-services'),
  oauthClientId: stringFromEnv('OAUTH_CLIENT_ID', 'gateway-client'),
  oauthUsername: stringFromEnv('OAUTH_USERNAME', 'demo-user'),
  oauthPassword: stringFromEnv('OAUTH_PASSWORD', 'demo123'),
  fallbackCategoryId: stringFromEnv('CATEGORY_ID', '018d2f1a-b123-7a45-8c67-d1e2f3a4b5c6'),
  setupCustomers: numberFromEnv('SETUP_CUSTOMERS', 8),
  setupProducts: numberFromEnv('SETUP_PRODUCTS', 4),
  productsRate: numberFromEnv('PRODUCTS_RATE', 4),
  customersRate: numberFromEnv('CUSTOMERS_RATE', 2),
  ordersRate: numberFromEnv('ORDERS_RATE', 2),
  paymentsRate: numberFromEnv('PAYMENTS_RATE', 2),
  rampUp: stringFromEnv('RAMP_UP', '30s'),
  steadyState: stringFromEnv('STEADY_STATE', '3m'),
  rampDown: stringFromEnv('RAMP_DOWN', '30s'),
  tokenRefreshWindowSeconds: numberFromEnv('TOKEN_REFRESH_WINDOW_SECONDS', 240),
};

export function buildScenario(rate, exec) {
  return {
    executor: 'ramping-arrival-rate',
    exec,
    startRate: 1,
    timeUnit: '1s',
    preAllocatedVUs: Math.max(4, rate * 2),
    maxVUs: Math.max(8, rate * 4),
    stages: [
      { target: rate, duration: config.rampUp },
      { target: rate, duration: config.steadyState },
      { target: 0, duration: config.rampDown },
    ],
    gracefulStop: '15s',
  };
}
