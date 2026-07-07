# e-commerce-micro-services

Guia rápido para subir, autenticar via Keycloak e testar as APIs pelo gateway.

## 1) Pré-requisitos

- Docker + Docker Compose
- `curl`
- `python3`

## 2) Subir o ambiente

```bash
docker compose up -d --build
```

Confira o status:

```bash
docker compose ps
```

Se o Kafka falhar com erro de estado antigo (ex.: `NodeExistsException`), limpe os volumes do Kafka/Zookeeper e suba novamente:

```bash
docker compose down
docker volume rm e-commerce-micro-services_kafka-data e-commerce-micro-services_zookeeper-data
docker compose up -d --build
```

## 3) Bootstrap do Keycloak (rodar 1 vez)

Este bootstrap cria/ajusta:
- realm: `micro-services`
- cliente público: `gateway-client` (com password grant habilitado)
- usuário de teste: `demo-user` / `demo123`
- `frontendUrl` do realm para alinhar o issuer esperado pelo gateway

```bash
python3 - <<'PY'
import json, urllib.request, urllib.parse, urllib.error

BASE='http://localhost:9098'
ADMIN_USER='admin'
ADMIN_PASS='admin'
REALM='micro-services'
CLIENT_ID='gateway-client'
USERNAME='demo-user'
PASSWORD='demo123'

def post_form(url, data, token=None):
    req=urllib.request.Request(url, data=urllib.parse.urlencode(data).encode(), method='POST')
    req.add_header('Content-Type','application/x-www-form-urlencoded')
    if token:
        req.add_header('Authorization', f'Bearer {token}')
    with urllib.request.urlopen(req, timeout=20) as r:
        return r.status, r.read().decode()

def req_json(method, url, token, payload=None):
    data=None
    if payload is not None:
        data=json.dumps(payload).encode()
    req=urllib.request.Request(url, data=data, method=method)
    req.add_header('Authorization', f'Bearer {token}')
    if payload is not None:
        req.add_header('Content-Type','application/json')
    try:
        with urllib.request.urlopen(req, timeout=20) as r:
            return r.status, r.read().decode()
    except urllib.error.HTTPError as e:
        return e.code, e.read().decode()

_, body = post_form(f'{BASE}/realms/master/protocol/openid-connect/token', {
    'client_id':'admin-cli',
    'username':ADMIN_USER,
    'password':ADMIN_PASS,
    'grant_type':'password'
})
admin_token=json.loads(body)['access_token']

code, _ = req_json('GET', f'{BASE}/admin/realms/{REALM}', admin_token)
if code == 404:
    code, resp = req_json('POST', f'{BASE}/admin/realms', admin_token, {'realm': REALM, 'enabled': True})
    if code not in (201,204):
        raise SystemExit(f'erro criando realm: {code} {resp}')

code, body = req_json('GET', f'{BASE}/admin/realms/{REALM}/clients?clientId={urllib.parse.quote(CLIENT_ID)}', admin_token)
clients = json.loads(body) if code == 200 else []
if not clients:
    code, resp = req_json('POST', f'{BASE}/admin/realms/{REALM}/clients', admin_token, {
        'clientId': CLIENT_ID,
        'enabled': True,
        'protocol': 'openid-connect',
        'publicClient': True,
        'directAccessGrantsEnabled': True,
        'standardFlowEnabled': True,
        'redirectUris': ['*'],
        'webOrigins': ['*']
    })
    if code not in (201,204):
        raise SystemExit(f'erro criando client: {code} {resp}')

code, body = req_json('GET', f'{BASE}/admin/realms/{REALM}/users?username={urllib.parse.quote(USERNAME)}', admin_token)
users = json.loads(body) if code == 200 else []
if not users:
    code, resp = req_json('POST', f'{BASE}/admin/realms/{REALM}/users', admin_token, {
        'username': USERNAME,
        'enabled': True,
        'emailVerified': True,
        'email': 'demo@example.com',
        'firstName': 'Demo',
        'lastName': 'User'
    })
    if code not in (201,204):
        raise SystemExit(f'erro criando user: {code} {resp}')
    code, body = req_json('GET', f'{BASE}/admin/realms/{REALM}/users?username={urllib.parse.quote(USERNAME)}', admin_token)
    users = json.loads(body)

uid = users[0]['id']
code, resp = req_json('PUT', f'{BASE}/admin/realms/{REALM}/users/{uid}', admin_token, {
    'username': USERNAME,
    'enabled': True,
    'emailVerified': True,
    'email': 'demo@example.com',
    'firstName': 'Demo',
    'lastName': 'User',
    'requiredActions': []
})
if code not in (200,204):
    raise SystemExit(f'erro atualizando user: {code} {resp}')

code, resp = req_json('PUT', f'{BASE}/admin/realms/{REALM}/users/{uid}/reset-password', admin_token, {
    'type':'password',
    'temporary':False,
    'value':PASSWORD
})
if code not in (200,204):
    raise SystemExit(f'erro definindo senha: {code} {resp}')

code, body = req_json('GET', f'{BASE}/admin/realms/{REALM}', admin_token)
realm = json.loads(body)
realm.setdefault('attributes', {})['frontendUrl'] = 'http://keycloak-ms:8080/'
code, resp = req_json('PUT', f'{BASE}/admin/realms/{REALM}', admin_token, realm)
if code not in (200,204):
    raise SystemExit(f'erro ajustando frontendUrl: {code} {resp}')

print('Keycloak pronto: realm=micro-services, client=gateway-client, user=demo-user/demo123')
PY
```

## 4) Gerar token OAuth2 e testar

```bash
TOKEN=$(curl -sS -X POST "http://localhost:9098/realms/micro-services/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=gateway-client" \
  -d "grant_type=password" \
  -d "username=demo-user" \
  -d "password=demo123" \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["access_token"])')
```

Smoke test da rota de produtos (via gateway):

```bash
curl -i "http://localhost:8222/api/v1/products" \
  -H "Authorization: Bearer $TOKEN"
```

## 5) Swagger centralizado (via gateway)

UI única da API (pública, sem token):

```bash
http://localhost:8222/swagger-ui.html
```

No Swagger UI, use o seletor no canto superior para alternar entre os docs de:
- Product Service
- Customer Service
- Order Service
- Payment Service

Os endpoints de negócio continuam protegidos. Para testar pelo Swagger, clique em **Authorize** e informe:

```text
Bearer <SEU_TOKEN>
```

## 6) Rotas para teste (somente via gateway)

Base URL: `http://localhost:8222`

Todas as rotas abaixo exigem:

```bash
-H "Authorization: Bearer $TOKEN"
```

### Products

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/products` | Lista todos os produtos |
| GET | `/api/v1/products/{productId}` | Busca um produto por id |
| POST | `/api/v1/products` | Cria um produto |
| POST | `/api/v1/products/purchase` | Valida e baixa estoque para uma compra |

### Customers

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/customers` | Lista clientes |
| GET | `/api/v1/customers/{customerId}` | Busca cliente por id |
| GET | `/api/v1/customers/exists/{customerId}` | Verifica se cliente existe |
| POST | `/api/v1/customers` | Cria cliente |
| PUT | `/api/v1/customers` | Atualiza cliente |
| DELETE | `/api/v1/customers/{customerId}` | Remove cliente |

### Orders

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/orders` | Lista pedidos |
| GET | `/api/v1/orders/{orderId}` | Busca pedido por id |
| POST | `/api/v1/orders` | Cria pedido (integra cliente, produto e pagamento) |

### Order Lines

| Método | Rota | Descrição |
|---|---|---|
| GET | `/api/v1/order-lines/order/{orderId}` | Lista os itens de um pedido |

### Payments

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/payments` | Cria um pagamento |

## 7) Exemplos rápidos de teste

Listar produtos:

```bash
curl -sS "http://localhost:8222/api/v1/products" -H "Authorization: Bearer $TOKEN"
```

Criar cliente:

```bash
curl -sS -X POST "http://localhost:8222/api/v1/customers" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "firstName":"John",
    "lastName":"Doe",
    "email":"john.doe@example.com",
    "address":{"street":"Main St","houseNumber":"100","zipCode":"12345"}
  }'
```

Criar pedido (use ids válidos de cliente e produto):

```bash
curl -sS -X POST "http://localhost:8222/api/v1/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "reference":"ORDER-001",
    "amount":2500.00,
    "paymentMethod":"CREDIT_CARD",
    "customerId":"<CUSTOMER_ID>",
    "productPurchaseRequests":[
      {"productId":"018d2f1a-c101-7123-8234-a1b2c3d4e5f6","quantity":1}
    ]
  }'
```

## 8) Visão gráfica simples das tabelas/coleções

```text
PostgreSQL (database: product)

category
  - id (PK)
  - name
  - description
        |
        | 1:N
        v
product
  - id (PK)
  - name
  - description
  - available_quantity
  - price
  - category_id (FK -> category.id)


PostgreSQL (database: order)

customer_order
  - id (PK)
  - reference
  - total_amount
  - payment_method
  - customer_id
  - created_at
  - updated_at
        |
        | 1:N
        v
order_line
  - id (PK)
  - order_id (FK -> customer_order.id)
  - product_id (referência lógica ao product-service)
  - quantity


PostgreSQL (database: payment)

payment
  - id (PK)
  - amount
  - payment_method
  - order_id (referência lógica ao customer_order.id)
  - created_at
  - updated_at


MongoDB (database: customer)

collection: customer
  - id
  - firstName
  - lastName
  - email
  - address
      - street
      - houseNumber
      - zipCode
```

## 9) Troubleshooting rápido

- `401 Unauthorized`: token ausente ou expirado, gere outro token.
- `issuer mismatch` no gateway: rode novamente o bootstrap do Keycloak (seção 3).
- `ms_kafka Exited (1)`: limpe volumes de Kafka/Zookeeper (seção 2).
- `product-service` com `missing table [category]`: recrie o DB `product`.

Comando para recriar apenas o DB `product`:

```bash
docker exec ms_pg_sql psql -U gabriel -d postgres -c "DROP DATABASE IF EXISTS product WITH (FORCE);"
docker exec ms_pg_sql psql -U gabriel -d postgres -c "CREATE DATABASE product;"
```

## 10) Testes unitários e cobertura (JaCoCo)

Os serviços estão configurados com:
- testes unitários com JUnit 5 + Mockito
- relatório de cobertura com JaCoCo
- gate de cobertura mínima no `mvn verify`

### Rodar todos os serviços com validação de cobertura

Na raiz do projeto:

```bash
for service in config-server discovery gateway-server customer product order payment notification; do
  (cd services/$service && sh mvnw clean verify)
done
```

### Rodar um serviço específico

Exemplo (order-service):

```bash
cd services/order
sh mvnw clean verify
```

### Onde ver os relatórios

Após o `verify`, abra no navegador:

```text
services/<nome-do-servico>/target/site/jacoco/index.html
```

Exemplo:

```text
services/order/target/site/jacoco/index.html
```

### Observações

- O `jacoco:check` quebra o build quando a cobertura mínima configurada para o serviço não é atendida.
- Nos serviços de infraestrutura (`config-server` e `discovery`), a classe de bootstrap (`*Application`) é excluída da análise de cobertura por não conter regra de negócio.

## 11) Observabilidade completa mantendo o Zipkin

O projeto agora sobe uma stack de observabilidade completa com:

- `Zipkin` para traces distribuídos
- `Prometheus` para métricas
- `Loki` para logs centralizados
- `Promtail` para coleta de logs dos containers
- `Grafana` para dashboards, logs e consulta unificada

### URLs

- Grafana: `http://localhost:3000` (`admin` / `admin`)
- Prometheus: `http://localhost:9090`
- Zipkin: `http://localhost:9411`
- Loki: `http://localhost:3100/ready`

### O que já está configurado

- todos os serviços exportam métricas em `/actuator/prometheus`
- logs dos containers são coletados no Loki e o gateway possui log explícito de requisição para validação operacional
- Grafana já sobe com data sources provisionados para `Prometheus`, `Loki` e `Zipkin`
- dashboard inicial `Microservices Overview` já fica disponível em `Dashboards > Observability`

### Passo a passo para validar a observabilidade

#### 1. Suba todo o ambiente

```bash
docker compose up -d --build
```

Confira se a stack subiu:

```bash
docker compose ps
```

Valide os componentes de observabilidade:

```bash
curl -sS http://localhost:9090/-/ready
curl -sS http://localhost:3100/ready
curl -sS http://localhost:9411/health
```

#### 2. Gere tráfego na aplicação

Para validar rapidamente a stack sem depender de autenticação nem de descoberta de serviços, gere tráfego no gateway:

```bash
curl -sS "http://localhost:8222/actuator/prometheus" > /dev/null
curl -sS "http://localhost:8222/actuator/prometheus" > /dev/null
```

Se quiser validar também o fluxo funcional da API, gere o token conforme a seção 4 e execute chamadas de negócio pelo gateway:

```bash
curl -i "http://localhost:8222/api/v1/products" \
  -H "Authorization: Bearer $TOKEN"

curl -i "http://localhost:8222/api/v1/customers" \
  -H "Authorization: Bearer $TOKEN"
```

Para gerar mais volume para métricas:

```bash
for i in 1 2 3 4 5; do
  curl -sS "http://localhost:8222/actuator/prometheus" > /dev/null
done
```

#### 3. Validar traces no Zipkin

Abra `http://localhost:9411`.

No campo de busca:

- selecione o serviço `gateway-server`
- clique em `Run Query`

Resultado esperado:

- traces HTTP do gateway preservados no Zipkin
- spans de segurança e requisição encadeados para cada chamada validada

#### 4. Validar métricas no Prometheus

Abra `http://localhost:9090/targets`.

Resultado esperado:

- todos os jobs dos serviços com status `UP`

Observação: depois de um `docker compose up -d --build`, aguarde cerca de `30-60s` para a malha estabilizar e todos os targets ficarem `UP`.

Depois, em `Graph`, teste consultas como:

```promql
up
```

```promql
sum by (application) (rate(http_server_requests_seconds_count[5m]))
```

```promql
histogram_quantile(0.95, sum by (le, application) (rate(http_server_requests_seconds_bucket[5m])))
```

#### 5. Validar dashboards no Grafana

Abra `http://localhost:3000` e faça login com `admin` / `admin`.

Confira:

- `Connections > Data sources`: os data sources `Prometheus`, `Loki` e `Zipkin` devem estar `healthy`
- `Dashboards > Observability > Microservices Overview`: o dashboard inicial deve carregar dados

O dashboard exibe:

- disponibilidade dos serviços
- throughput HTTP
- taxa de erro `5xx`
- latência `p95`
- uso de heap por serviço

#### 6. Validar logs no Grafana com Loki

No Grafana, abra `Explore` e selecione o data source `Loki`.

Consultas úteis:

```logql
{service="gateway-server"} |= "Gateway request"
```

```logql
{service="order-service"}
```

```logql
{service="notification-service"}
```

Resultado esperado:

- logs centralizados de todos os containers
- logs de requisição explícitos do gateway disponíveis no Loki
- logs de negócio e infraestrutura pesquisáveis por `service`, `container` e período

### Endpoints úteis por serviço

- Gateway: `http://localhost:8222/actuator/prometheus`
- Product: `http://localhost:8050/actuator/prometheus`
- Customer: `http://localhost:8090/actuator/prometheus`
- Order: `http://localhost:8070/actuator/prometheus`
- Payment: `http://localhost:8060/actuator/prometheus`
- Notification: `http://localhost:8040/actuator/prometheus`
- Discovery: `http://localhost:8761/actuator/prometheus`
- Config Server: `http://localhost:8888/actuator/prometheus`

### Observações operacionais

- o sampling continua no `Zipkin`; por padrão está em `1.0` para facilitar validação local
- para produção, ajuste `MANAGEMENT_TRACING_SAMPLING_PROBABILITY` para reduzir custo de tracing
- as credenciais padrão do Grafana são adequadas apenas para ambiente local de desenvolvimento
- a validação ponta a ponta confirmada neste repositório cobre `métricas` em todos os serviços, `logs centralizados` no Loki e `traces` confirmados no `gateway-server`; os serviços Spring Boot `4.0.x` ainda exigem alinhamento adicional para exportar spans downstream ao Zipkin com a mesma confiabilidade do gateway

## 12) Testes de carga com k6

O repositório agora inclui uma suíte simples de carga em `tests/performance` para exercitar todos os endpoints públicos do gateway e correlacionar os resultados com `Prometheus` e `Grafana`.

### O que a suíte cobre

- Products: `GET /api/v1/products`, `GET /api/v1/products/{id}`, `POST /api/v1/products`, `POST /api/v1/products/purchase`
- Customers: `GET /api/v1/customers`, `GET /api/v1/customers/{id}`, `GET /api/v1/customers/exists/{id}`, `POST /api/v1/customers`, `PUT /api/v1/customers`, `DELETE /api/v1/customers/{id}`
- Orders: `POST /api/v1/orders`, `GET /api/v1/orders`, `GET /api/v1/orders/{id}`, `GET /api/v1/order-lines/order/{orderId}`
- Payments: `POST /api/v1/payments`

### Pré-requisitos para carga

- stack completa no ar com `docker compose up -d --build`
- bootstrap do Keycloak executado conforme a seção 3
- `Prometheus` e `Grafana` ativos na stack local
- uma das opções abaixo para rodar o `k6`:
  - `k6` instalado localmente
  - Docker disponível para usar a imagem `grafana/k6`

Observação: o `Prometheus` desta stack já está configurado com `remote write receiver`, permitindo que o `k6` envie métricas em tempo real para o Grafana local.
O runner agora faz um preflight antes de iniciar a carga, validando `Prometheus`, `Keycloak`, `gateway` e um smoke autenticado em `GET /api/v1/products`.

### 1. Suba o ambiente

```bash
docker compose up -d --build
docker compose ps
```

Valide os componentes principais:

```bash
curl -sS http://localhost:9090/-/ready
curl -sS http://localhost:3100/ready
curl -sS http://localhost:9411/health
```

Observação: após um `docker compose up -d --build`, ainda pode levar alguns segundos para `gateway` e `Keycloak` estabilizarem. Os runners de carga esperam automaticamente por essa prontidão.

### 2. Execute o bootstrap do Keycloak

Se ainda não executou o bootstrap deste ambiente, rode a seção 3 deste README antes do teste de carga.

### 3. Escolha como rodar o k6

Opção A, com `k6` instalado localmente:

```bash
./tests/performance/run-local.sh
```

Opção B, usando Docker:

```bash
./tests/performance/run-docker.sh
```

Ao final, o runner imprime um `testid`. Exemplo:

```text
testid=local-20260422-231500
```

Use esse valor para filtrar o dashboard no Grafana.

Se o bootstrap do Keycloak ainda não tiver sido executado, o runner falha cedo no preflight com uma mensagem explícita em vez de começar um teste inválido.

### 4. Ajuste de carga, se necessário

Os cenários sobem com taxas conservadoras para ambiente local:

- `PRODUCTS_RATE=4`
- `CUSTOMERS_RATE=2`
- `ORDERS_RATE=2`
- `PAYMENTS_RATE=2`
- `RAMP_UP=30s`
- `STEADY_STATE=3m`
- `RAMP_DOWN=30s`

Exemplo com carga maior:

```bash
PRODUCTS_RATE=8 \
CUSTOMERS_RATE=4 \
ORDERS_RATE=3 \
PAYMENTS_RATE=3 \
STEADY_STATE=5m \
TEST_TYPE=load \
./tests/performance/run-local.sh
```

### 5. Verificar os resultados do k6

O `k6` mostra no terminal o resumo com métricas como:

- `avg response time`
- `p95`
- `p99`
- `http_req_failed`
- `http_reqs`
- `checks`

Além da saída no terminal, a suíte grava:

```text
tests/performance/results/latest-summary.json
tests/performance/results/latest-summary.txt
```

Esses arquivos permitem registrar o baseline local de cada execução.

### 6. Verificar os resultados no Grafana

Abra `http://localhost:3000` e faça login com `admin` / `admin`.

Dashboards relevantes:

- `Dashboards > Observability > Load Testing Overview`
- `Dashboards > Observability > Microservices Overview`

No dashboard `Load Testing Overview`, filtre pelo `testid` da execução.

Você verá, entre outros painéis:

- `k6 Total Requests`
- `k6 Peak RPS`
- `k6 Avg Response`
- `k6 P99 Response`
- `k6 Request Rate by Scenario`
- `k6 Response Time by Scenario`
- `Application RPS by Service`
- `Application Latency by Service`
- `k6 Endpoint Summary`

Interpretação prática:

- `k6 Peak RPS`: pico de throughput gerado pelo teste
- `k6 Avg Response`: latência média percebida pelo cliente de carga
- `k6 P99 Response`: cauda de latência; útil para detectar degradação em piores casos
- `Application RPS by Service`: distribuição da carga entre gateway e microserviços
- `Application Latency by Service`: ajuda a identificar o serviço que virou gargalo

### 7. Verificar no Prometheus

Abra `http://localhost:9090` e rode consultas como:

RPS por serviço:

```promql
sum by (application) (rate(http_server_requests_seconds_count{application!=""}[1m]))
```

P99 por serviço:

```promql
histogram_quantile(0.99, sum by (le, application) (rate(http_server_requests_seconds_bucket{application!=""}[5m])))
```

Latência média por serviço:

```promql
sum by (application) (rate(http_server_requests_seconds_sum{application!=""}[5m]))
/
sum by (application) (rate(http_server_requests_seconds_count{application!=""}[5m]))
```

RPS do k6 por cenário:

```promql
sum by (scenario) (rate(k6_http_reqs_total{testid="<TEST_ID>"}[1m]))
```

P99 do k6 por cenário:

```promql
avg by (scenario) (k6_http_req_duration_p99{testid="<TEST_ID>"})
```

Latência média do k6 por cenário:

```promql
avg by (scenario) (k6_http_req_duration_avg{testid="<TEST_ID>"})
```

### 8. Onde ajustar a suíte

- script principal: `tests/performance/k6/main.js`
- cenários: `tests/performance/k6/scenarios`
- configuração de taxas e duração: `tests/performance/k6/config.js`
- preflight do ambiente: `tests/performance/preflight.sh`
- documentação local da suíte: `tests/performance/README.md`

### 9. Observações operacionais

- os cenários criam dados de apoio no `setup()` para reduzir flakiness em ambiente local
- o token OAuth2 é renovado automaticamente durante a execução para evitar falha por expiração em testes mais longos
- por padrão, o teste é conservador e serve como baseline local; não trate esses números como capacidade de produção
- se quiser comparar execuções diferentes no Grafana, rode cada teste com um `testid` distinto
- os runners usam `K6_PROMETHEUS_RW_PUSH_INTERVAL=10s` e mantêm `K6_PROMETHEUS_RW_STALE_MARKERS=false` por padrão para reduzir instabilidade no remote write local

### 10. Troubleshooting específico do k6

- `503 Service Unavailable` logo no começo do teste: normalmente a malha ainda não estabilizou ou o bootstrap do Keycloak não foi executado; rode a seção 3 deste README e tente novamente.
- `Failed to send the time series data to the endpoint` com `status code: 400`: em ambiente local isso costuma indicar que o TSDB do Prometheus ficou contaminado por uma execução anterior interrompida ou com timestamps inválidos.

Para recriar apenas o volume do Prometheus e voltar a um estado limpo:

```bash
docker compose rm -sf prometheus
docker volume rm e-commerce-micro-services_prometheus-data
docker compose up -d prometheus
```

Depois valide os targets novamente em `http://localhost:9090/targets` antes de rerodar o `k6`.
