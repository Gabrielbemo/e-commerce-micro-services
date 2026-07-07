#!/usr/bin/env sh
set -eu

AUTH_BASE_URL=${AUTH_BASE_URL:-http://localhost:9098}
GATEWAY_BASE_URL=${GATEWAY_BASE_URL:-http://localhost:8222}
OAUTH_REALM=${OAUTH_REALM:-micro-services}
OAUTH_CLIENT_ID=${OAUTH_CLIENT_ID:-gateway-client}
OAUTH_USERNAME=${OAUTH_USERNAME:-demo-user}
OAUTH_PASSWORD=${OAUTH_PASSWORD:-demo123}
K6_PROMETHEUS_RW_SERVER_URL=${K6_PROMETHEUS_RW_SERVER_URL:-http://localhost:9090/api/v1/write}

PROMETHEUS_READY_URL=${K6_PROMETHEUS_RW_SERVER_URL%/api/v1/write}/-/ready
TOKEN_URL="$AUTH_BASE_URL/realms/$OAUTH_REALM/protocol/openid-connect/token"
PRODUCTS_URL="$GATEWAY_BASE_URL/api/v1/products"

wait_for_url() {
  name=$1
  url=$2
  attempts=${3:-24}
  delay_seconds=${4:-5}
  attempt=1

  while [ "$attempt" -le "$attempts" ]; do
    if curl -fsS -o /dev/null "$url" >/dev/null 2>&1; then
      return 0
    fi

    if [ "$attempt" -eq "$attempts" ]; then
      printf '%s\n' "preflight failed: $name not ready at $url" >&2
      return 1
    fi

    sleep "$delay_seconds"
    attempt=$((attempt + 1))
  done
}

fetch_token() {
  curl -sS -X POST "$TOKEN_URL" \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "client_id=$OAUTH_CLIENT_ID" \
    -d "grant_type=password" \
    -d "username=$OAUTH_USERNAME" \
    -d "password=$OAUTH_PASSWORD" \
    | python3 -c 'import json,sys; print(json.load(sys.stdin)["access_token"])'
}

wait_for_api() {
  attempts=${1:-24}
  delay_seconds=${2:-5}
  attempt=1

  while [ "$attempt" -le "$attempts" ]; do
    token=''
    token=$(fetch_token 2>/dev/null || true)

    if [ -n "$token" ]; then
      status=$(curl -sS -o /dev/null -w "%{http_code}" "$PRODUCTS_URL" \
        -H "Authorization: Bearer $token" || true)

      if [ "$status" = "200" ]; then
        return 0
      fi
    fi

    if [ "$attempt" -eq "$attempts" ]; then
      printf '%s\n' "preflight failed: gateway auth flow is not ready or bootstrap was not executed" >&2
      return 1
    fi

    sleep "$delay_seconds"
    attempt=$((attempt + 1))
  done
}

wait_for_url "Prometheus" "$PROMETHEUS_READY_URL"
wait_for_url "Keycloak" "$AUTH_BASE_URL/"
wait_for_url "Gateway health" "$GATEWAY_BASE_URL/actuator/health"
wait_for_api
