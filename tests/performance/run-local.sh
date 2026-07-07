#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(dirname "$0")
ROOT_DIR=$(cd "$SCRIPT_DIR/../.." && pwd)
RESULTS_DIR="$ROOT_DIR/tests/performance/results"

mkdir -p "$RESULTS_DIR"

TEST_ID=${TEST_ID:-local-$(date +%Y%m%d-%H%M%S)}
export K6_PROMETHEUS_RW_SERVER_URL=${K6_PROMETHEUS_RW_SERVER_URL:-http://localhost:9090/api/v1/write}
export K6_PROMETHEUS_RW_TREND_STATS=${K6_PROMETHEUS_RW_TREND_STATS:-p(95),p(99),avg,max}
export K6_PROMETHEUS_RW_STALE_MARKERS=${K6_PROMETHEUS_RW_STALE_MARKERS:-false}
export K6_PROMETHEUS_RW_PUSH_INTERVAL=${K6_PROMETHEUS_RW_PUSH_INTERVAL:-10s}
export K6_SUMMARY_DIR="$RESULTS_DIR"

"$SCRIPT_DIR/preflight.sh"

k6 run \
  --tag testid="$TEST_ID" \
  -o experimental-prometheus-rw \
  "$ROOT_DIR/tests/performance/k6/main.js"

printf 'testid=%s\n' "$TEST_ID"
