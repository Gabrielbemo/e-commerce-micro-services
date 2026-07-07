import http from 'k6/http';
import { fail } from 'k6';

let cachedToken = null;
let cachedAtSeconds = 0;

export function fetchAccessToken(config) {
  const response = http.post(
    `${config.authBaseUrl}/realms/${config.oauthRealm}/protocol/openid-connect/token`,
    {
      client_id: config.oauthClientId,
      grant_type: 'password',
      username: config.oauthUsername,
      password: config.oauthPassword,
    },
    {
      headers: {
        'Content-Type': 'application/x-www-form-urlencoded',
      },
      tags: {
        type: 'auth',
        domain: 'auth',
        endpoint: 'oauth_token',
        name: 'POST /oauth/token',
      },
    }
  );

  if (response.status !== 200) {
    fail(`Failed to obtain OAuth token. Status=${response.status} body=${response.body}`);
  }

  const body = response.json();
  if (!body || !body.access_token) {
    fail('OAuth token response does not contain access_token');
  }

  return body.access_token;
}

export function getAccessToken(config) {
  const nowSeconds = Math.floor(Date.now() / 1000);

  if (!cachedToken || nowSeconds - cachedAtSeconds >= config.tokenRefreshWindowSeconds) {
    cachedToken = fetchAccessToken(config);
    cachedAtSeconds = nowSeconds;
  }

  return cachedToken;
}
