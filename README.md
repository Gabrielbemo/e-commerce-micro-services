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
