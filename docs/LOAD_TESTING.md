# Testes de carga com k6

A suíte em `tests/performance` exercita as APIs pelo Gateway e envia métricas em tempo real ao Prometheus. Ela foi projetada como baseline de estudo em ambiente local, não como certificação de capacidade para produção.

## Pré-requisitos

- stack iniciada e estabilizada;
- bootstrap do Keycloak executado;
- Prometheus e Grafana ativos;
- k6 local ou Docker para executar a imagem `grafana/k6:0.55.0`.

Antes da carga, os runners executam `preflight.sh`, que aguarda Prometheus, Keycloak e Gateway e realiza um smoke test autenticado em produtos.

## Cobertura

| Domínio | Operações exercitadas |
|---|---|
| Produtos | listar, consultar, criar e reservar estoque |
| Clientes | listar, consultar, verificar existência, criar, atualizar e excluir |
| Pedidos | criar, listar, consultar e listar itens |
| Pagamentos | criar pagamento |

## Executar a suíte geral

Com k6 instalado localmente:

```bash
./tests/performance/run-local.sh
```

Com Docker:

```bash
./tests/performance/run-docker.sh
```

As taxas padrão da suíte geral são:

| Variável | Padrão | Finalidade |
|---|---:|---|
| `PRODUCTS_RATE` | `4` | Iterações por segundo de produtos |
| `CUSTOMERS_RATE` | `2` | Iterações por segundo de clientes |
| `ORDERS_RATE` | `2` | Iterações por segundo de pedidos |
| `PAYMENTS_RATE` | `2` | Iterações por segundo de pagamentos |
| `RAMP_UP` | `30s` | Subida da carga |
| `STEADY_STATE` | `3m` | Carga estável |
| `RAMP_DOWN` | `30s` | Redução da carga |

Exemplo de ajuste:

```bash
PRODUCTS_RATE=8 \
CUSTOMERS_RATE=4 \
ORDERS_RATE=3 \
PAYMENTS_RATE=3 \
STEADY_STATE=5m \
TEST_TYPE=load \
./tests/performance/run-local.sh
```

## Executar somente Customer Service

A suíte dedicada separa o tráfego em cinco cenários:

| Cenário | Operações |
|---|---|
| `customers_list` | listar clientes |
| `customers_detail` | consultar cliente e verificar existência |
| `customers_create` | criar cliente |
| `customers_update` | atualizar cliente |
| `customers_lifecycle` | executar um ciclo CRUD |

Perfil baseline com k6 local:

```bash
./tests/performance/run-customer-local.sh
```

Perfil capacity:

```bash
CUSTOMER_PROFILE=capacity ./tests/performance/run-customer-local.sh
```

Execução via Docker:

```bash
CUSTOMER_PROFILE=capacity ./tests/performance/run-customer-docker.sh
```

### Perfis de Customer

| Parâmetro | `baseline` | `capacity` |
|---|---:|---:|
| Clientes no seed | `24` | `120` |
| Listagem/s | `2` | `8` |
| Detalhes/s | `2` | `4` |
| Criações/s | `1` | `2` |
| Atualizações/s | `1` | `1` |
| Ciclos/s | `1` | `1` |
| Ramp-up | `30s` | `45s` |
| Carga estável | `3m` | `5m` |
| Ramp-down | `30s` | `45s` |

É possível sobrescrever cada valor:

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

## Variáveis de ambiente

| Variável | Padrão |
|---|---|
| `GATEWAY_BASE_URL` | `http://localhost:8222` |
| `AUTH_BASE_URL` | `http://localhost:9098` |
| `OAUTH_REALM` | `micro-services` |
| `OAUTH_CLIENT_ID` | `gateway-client` |
| `OAUTH_USERNAME` | `demo-user` |
| `OAUTH_PASSWORD` | `demo123` |
| `SETUP_CUSTOMERS` | `8` |
| `SETUP_PRODUCTS` | `4` |
| `TOKEN_REFRESH_WINDOW_SECONDS` | `240` |
| `K6_PROMETHEUS_RW_SERVER_URL` | `http://localhost:9090/api/v1/write` |
| `K6_PROMETHEUS_RW_PUSH_INTERVAL` | `10s` |
| `K6_PROMETHEUS_RW_STALE_MARKERS` | `false` |
| `K6_PROMETHEUS_RW_TREND_STATS` | `p(95),p(99),avg,max` |

Os scripts Docker também desabilitam native histograms do output k6 por padrão com `K6_PROMETHEUS_RW_TREND_AS_NATIVE_HISTOGRAM=false`.

## Resultados

Ao final, o terminal exibe métricas de latência, percentis, falhas, requisições e checks. Os resumos mais recentes ficam em:

```text
tests/performance/results/latest-summary.json
tests/performance/results/latest-summary.txt
```

Cada runner imprime um `testid`, por exemplo:

```text
testid=local-20260422-231500
```

Use esse valor para filtrar a execução nos dashboards.

## Analisar no Grafana

Abra <http://localhost:3000> e use:

- **Load Testing Overview** para a suíte geral;
- **Customer Load Overview** para a suíte focada;
- **Microservices Overview** para correlacionar carga com os serviços.

Observe throughput, média, p95/p99, erros e latência por cenário. A listagem de clientes não possui paginação no estado atual e merece atenção em testes de capacidade.

## Consultar no Prometheus

RPS por aplicação:

```promql
sum by (application) (rate(http_server_requests_seconds_count{application!=""}[1m]))
```

P99 por aplicação:

```promql
histogram_quantile(0.99, sum by (le, application) (rate(http_server_requests_seconds_bucket{application!=""}[5m])))
```

RPS k6 por cenário:

```promql
sum by (scenario) (rate(k6_http_reqs_total{testid="<TEST_ID>"}[1m]))
```

P99 k6 por cenário:

```promql
avg by (scenario) (k6_http_req_duration_p99{testid="<TEST_ID>"})
```

## Estrutura da suíte

| Caminho | Responsabilidade |
|---|---|
| `tests/performance/k6/main.js` | Entrada da suíte geral |
| `tests/performance/k6/customer-main.js` | Entrada da suíte de clientes |
| `tests/performance/k6/config.js` | Valores padrão e construção dos cenários |
| `tests/performance/k6/scenarios/` | Operações por domínio |
| `tests/performance/k6/lib/` | Autenticação, dados e métricas auxiliares |
| `tests/performance/preflight.sh` | Verificação prévia do ambiente |
| `tests/performance/run-*.sh` | Runners locais e Docker |

Os cenários preparam dados no `setup()` e renovam o token antes da janela de expiração para reduzir flakiness em testes longos.

## Troubleshooting

### `503` durante o setup

A malha pode não ter estabilizado ou o Keycloak não foi configurado. Execute [Primeiros passos](GETTING_STARTED.md), confirme o Eureka e rode novamente.

### Erro `400` no Prometheus remote write

Uma execução interrompida pode deixar séries incompatíveis no TSDB local. A operação abaixo remove somente o volume de métricas do Prometheus:

```bash
docker compose rm -sf prometheus
docker volume rm e-commerce-micro-services_prometheus-data
docker compose up -d prometheus
```

> A remoção apaga o histórico local de métricas.

Valide <http://localhost:9090/targets> antes de repetir a carga. Para outros problemas, consulte [Troubleshooting geral](TROUBLESHOOTING.md).

[Voltar ao README](../README.md)
