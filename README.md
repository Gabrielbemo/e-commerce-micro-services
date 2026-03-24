# e-commerce-micro-services

Guia rapido para subir, autenticar via Keycloak e testar as APIs pelo gateway.

## 1) Pre requisitos

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
- client publico: `gateway-client` (com password grant habilitado)
- usuario de teste: `demo-user` / `demo123`
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

## 5) Rotas para teste (somente via gateway)

Base URL: `http://localhost:8222`

Todas as rotas abaixo exigem:

```bash
-H "Authorization: Bearer $TOKEN"
```

### Products

| Metodo | Rota | Descricao |
|---|---|---|
| GET | `/api/v1/products` | Lista todos os produtos |
| GET | `/api/v1/products/{productId}` | Busca um produto por id |
| POST | `/api/v1/products` | Cria um produto |
| POST | `/api/v1/products/purchase` | Valida e baixa estoque para uma compra |

### Customers

| Metodo | Rota | Descricao |
|---|---|---|
| GET | `/api/v1/customers` | Lista clientes |
| GET | `/api/v1/customers/{customerId}` | Busca cliente por id |
| GET | `/api/v1/customers/exists/{customerId}` | Verifica se cliente existe |
| POST | `/api/v1/customers` | Cria cliente |
| PUT | `/api/v1/customers` | Atualiza cliente |
| DELETE | `/api/v1/customers/{customerId}` | Remove cliente |

### Orders

| Metodo | Rota | Descricao |
|---|---|---|
| GET | `/api/v1/orders` | Lista pedidos |
| GET | `/api/v1/orders/{orderId}` | Busca pedido por id |
| POST | `/api/v1/orders` | Cria pedido (integra cliente, produto e pagamento) |

### Order Lines

| Metodo | Rota | Descricao |
|---|---|---|
| GET | `/api/v1/order-lines/order/{orderId}` | Lista os itens de um pedido |

### Payments

| Metodo | Rota | Descricao |
|---|---|---|
| POST | `/api/v1/payments` | Cria um pagamento |

## 6) Exemplos rapidos de teste

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

Criar pedido (use ids validos de cliente e produto):

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

## 7) Visao grafica simples das tabelas/colecoes

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
  - product_id (referencia logica ao product-service)
  - quantity


PostgreSQL (database: payment)

payment
  - id (PK)
  - amount
  - payment_method
  - order_id (referencia logica ao customer_order.id)
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

## 8) Troubleshooting rapido

- `401 Unauthorized`: token ausente ou expirado, gere outro token.
- `issuer mismatch` no gateway: rode novamente o bootstrap do Keycloak (secao 3).
- `ms_kafka Exited (1)`: limpe volumes de Kafka/Zookeeper (secao 2).
- `product-service` com `missing table [category]`: recrie o DB `product`.

Comando para recriar apenas o DB `product`:

```bash
docker exec ms_pg_sql psql -U gabriel -d postgres -c "DROP DATABASE IF EXISTS product WITH (FORCE);"
docker exec ms_pg_sql psql -U gabriel -d postgres -c "CREATE DATABASE product;"
```
