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

## Suíte focada em customer

Além da suíte mista, o projeto agora possui uma entrada dedicada para carga do domínio de clientes:

- `run-customer-local.sh`: executa apenas os cenários de customer com `k6` local
- `run-customer-docker.sh`: executa apenas os cenários de customer via container `grafana/k6`

Essa suíte separa o tráfego de customer em cenários próprios:

- `customers_list`: `GET /api/v1/customers`
- `customers_detail`: `GET /api/v1/customers/{id}` e `GET /api/v1/customers/exists/{id}`
- `customers_create`: `POST /api/v1/customers`
- `customers_update`: `PUT /api/v1/customers`
- `customers_lifecycle`: fluxo CRUD fim a fim para smoke

## Arquivos principais

- `k6/main.js`: orquestra setup, cenários, thresholds e resumo final
- `k6/scenarios/*.js`: cenários de carga por domínio
- `preflight.sh`: espera Prometheus, Keycloak e gateway ficarem realmente prontos antes da carga
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
- `CUSTOMER_PROFILE`: perfil da suíte dedicada de customer, `baseline` ou `capacity`, default `baseline`
- `CUSTOMER_SEED_COUNT`: volume de clientes criados no `setup()` da suíte dedicada; default `24` em `baseline` e `120` em `capacity`
- `CUSTOMERS_LIST_RATE`: taxa alvo do cenário `customers_list`
- `CUSTOMERS_DETAIL_RATE`: taxa alvo do cenário `customers_detail`
- `CUSTOMERS_CREATE_RATE`: taxa alvo do cenário `customers_create`
- `CUSTOMERS_UPDATE_RATE`: taxa alvo do cenário `customers_update`
- `CUSTOMERS_LIFECYCLE_RATE`: taxa alvo do cenário `customers_lifecycle`
- `ORDERS_RATE`: taxa alvo por segundo do cenário de pedidos, default `2`
- `PAYMENTS_RATE`: taxa alvo por segundo do cenário de pagamentos, default `2`
- `RAMP_UP`: default `30s`
- `STEADY_STATE`: default `3m`
- `RAMP_DOWN`: default `30s`
- `K6_PROMETHEUS_RW_PUSH_INTERVAL`: default `10s`
- `K6_PROMETHEUS_RW_STALE_MARKERS`: default `false`
- `K6_PROMETHEUS_RW_TREND_STATS`: default `p(95),p(99),avg,max`
- `K6_PROMETHEUS_RW_TREND_AS_NATIVE_HISTOGRAM`: default `false`

## Saídas geradas

Após a execução, o `k6` escreve:

- `tests/performance/results/latest-summary.json`
- `tests/performance/results/latest-summary.txt`

O runner também imprime o `testid` usado na execução, permitindo filtrar métricas no Grafana.

## Observações práticas

- os runners falham cedo se o bootstrap do Keycloak ainda não tiver sido executado ou se o gateway autenticado ainda não estiver pronto
- o `handleSummary()` agora grava os artefatos usando o diretório resolvido pelo runner, evitando depender implicitamente do diretório atual do shell
- a suíte dedicada de customer grava no mesmo diretório de resultados, mas inclui `customer_profile` e `seeded_customers` no resumo final
- o endpoint `GET /api/v1/customers` é o principal ponto de atenção porque o serviço atual faz listagem sem paginação

## Como rodar apenas customer

Perfil `baseline`, com feedback rápido local:

```bash
./tests/performance/run-customer-local.sh
```

Perfil `capacity`, com mais carga e janela maior:

```bash
CUSTOMER_PROFILE=capacity ./tests/performance/run-customer-local.sh
```

Via Docker:

```bash
CUSTOMER_PROFILE=capacity ./tests/performance/run-customer-docker.sh
```

Se precisar ajustar as proporções manualmente:

```bash
CUSTOMER_PROFILE=capacity \
CUSTOMER_SEED_COUNT=180 \
CUSTOMERS_LIST_RATE=10 \
CUSTOMERS_DETAIL_RATE=5 \
CUSTOMERS_CREATE_RATE=2 \
CUSTOMERS_UPDATE_RATE=1 \
CUSTOMERS_LIFECYCLE_RATE=1 \
./tests/performance/run-customer-local.sh
```

## Observabilidade para customer

No Grafana, além do dashboard genérico, use:

- `Dashboards > Observability > Customer Load Overview`

Esse dashboard ajuda a acompanhar:

- volume total de requests de customer
- RPS por cenário de customer
- latência média e p99 por cenário
- resumo por endpoint
- comparação de latência entre `gateway-server` e `customer-service`

Ao analisar os resultados:

- se o p99 subir primeiro em `customers_list`, o gargalo mais provável é a listagem sem paginação
- se a latência do gateway subir sem o `customer-service` acompanhar, o gargalo não está no serviço de customer

## Troubleshooting rápido

- erro `503` no setup: a malha ainda não estabilizou ou o bootstrap do Keycloak não foi feito
- erro `400` no `Prometheus remote write`: limpe o volume `prometheus-data` do projeto e suba apenas o Prometheus novamente

```bash
docker compose rm -sf prometheus
docker volume rm e-commerce-micro-services_prometheus-data
docker compose up -d prometheus
```
