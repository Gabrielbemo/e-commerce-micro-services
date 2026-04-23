#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(dirname "$0")
ROOT_DIR=$(cd "$SCRIPT_DIR/../.." && pwd)
RESULTS_DIR="$ROOT_DIR/tests/performance/results"

mkdir -p "$RESULTS_DIR"

TEST_ID=${TEST_ID:-docker-$(date +%Y%m%d-%H%M%S)}
export K6_PROMETHEUS_RW_SERVER_URL=${K6_PROMETHEUS_RW_SERVER_URL:-http://localhost:9090/api/v1/write}
export K6_PROMETHEUS_RW_TREND_STATS=${K6_PROMETHEUS_RW_TREND_STATS:-p(95),p(99),avg,max}
export K6_PROMETHEUS_RW_STALE_MARKERS=${K6_PROMETHEUS_RW_STALE_MARKERS:-true}

docker run --rm \
  --network host \
  --user "$(id -u):$(id -g)" \
  -e K6_PROMETHEUS_RW_SERVER_URL \
  -e K6_PROMETHEUS_RW_TREND_STATS \
  -e K6_PROMETHEUS_RW_STALE_MARKERS \
  -e GATEWAY_BASE_URL \
  -e AUTH_BASE_URL \
  -e OAUTH_REALM \
  -e OAUTH_CLIENT_ID \
  -e OAUTH_USERNAME \
  -e OAUTH_PASSWORD \
  -e CATEGORY_ID \
  -e SETUP_CUSTOMERS \
  -e SETUP_PRODUCTS \
  -e PRODUCTS_RATE \
  -e CUSTOMERS_RATE \
  -e ORDERS_RATE \
  -e PAYMENTS_RATE \
  -e RAMP_UP \
  -e STEADY_STATE \
  -e RAMP_DOWN \
  -e TOKEN_REFRESH_WINDOW_SECONDS \
  -e TEST_TYPE \
  -v "$ROOT_DIR:/work" \
  -w /work \
  grafana/k6:0.49.0 run \
  --tag testid="$TEST_ID" \
  -o experimental-prometheus-rw \
  tests/performance/k6/main.js

printf 'testid=%s\n' "$TEST_ID"
