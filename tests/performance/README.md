# Testes de carga com k6

Esta pasta concentra a suíte de carga local do projeto com `k6`, cobrindo os endpoints públicos expostos pelo gateway.

## O que a suíte cobre

- `GET /api/v1/products`
- `GET /api/v1/products/{id}`
- `POST /api/v1/products`
- `POST /api/v1/products/purchase`
- `GET /api/v1/customers`
- `GET /api/v1/customers/{id}`
- `GET /api/v1/customers/exists/{id}`
- `POST /api/v1/customers`
- `PUT /api/v1/customers`
- `DELETE /api/v1/customers/{id}`
- `POST /api/v1/orders`
- `GET /api/v1/orders`
- `GET /api/v1/orders/{id}`
- `GET /api/v1/order-lines/order/{orderId}`
- `POST /api/v1/payments`

## Arquivos principais

- `k6/main.js`: orquestra setup, cenários, thresholds e resumo final
- `k6/scenarios/*.js`: cenários de carga por domínio
- `run-local.sh`: execução com `k6` instalado na máquina
- `run-docker.sh`: execução usando o container oficial `grafana/k6`

## Variáveis úteis

- `GATEWAY_BASE_URL`: default `http://localhost:8222`
- `AUTH_BASE_URL`: default `http://localhost:9098`
- `OAUTH_REALM`: default `micro-services`
- `OAUTH_CLIENT_ID`: default `gateway-client`
- `OAUTH_USERNAME`: default `demo-user`
- `OAUTH_PASSWORD`: default `demo123`
- `SETUP_CUSTOMERS`: quantidade de clientes criados no setup, default `8`
- `SETUP_PRODUCTS`: quantidade de produtos dedicados criados no setup, default `4`
- `PRODUCTS_RATE`: taxa alvo por segundo do cenário de produtos, default `4`
- `CUSTOMERS_RATE`: taxa alvo por segundo do cenário de clientes, default `2`
- `ORDERS_RATE`: taxa alvo por segundo do cenário de pedidos, default `2`
- `PAYMENTS_RATE`: taxa alvo por segundo do cenário de pagamentos, default `2`
- `RAMP_UP`: default `30s`
- `STEADY_STATE`: default `3m`
- `RAMP_DOWN`: default `30s`

## Saídas geradas

Após a execução, o `k6` escreve:

- `tests/performance/results/latest-summary.json`
- `tests/performance/results/latest-summary.txt`

O runner também imprime o `testid` usado na execução, permitindo filtrar métricas no Grafana.
