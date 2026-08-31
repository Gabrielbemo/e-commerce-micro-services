# API e autenticação

As operações de negócio são expostas pelo API Gateway em `http://localhost:8222`. As rotas listadas abaixo exigem um JWT emitido pelo Keycloak.

Para preparar o ambiente, siga [Primeiros passos](GETTING_STARTED.md).

## Autenticação OAuth2

O gateway atua como OAuth2 Resource Server e valida tokens do realm `micro-services`. O ambiente educacional usa o password grant para facilitar testes locais.

```bash
TOKEN=$(curl -sS -X POST "http://localhost:9098/realms/micro-services/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=gateway-client" \
  -d "grant_type=password" \
  -d "username=demo-user" \
  -d "password=demo123" \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["access_token"])')
```

Envie o token em cada chamada:

```bash
-H "Authorization: Bearer $TOKEN"
```

## Swagger UI

Abra <http://localhost:8222/swagger-ui.html>. No seletor superior, escolha Product, Customer, Order ou Payment Service.

Clique em **Authorize** e informe:

```text
Bearer <SEU_TOKEN>
```

## Rotas pelo gateway

### Produtos

| Método | Rota | Responsabilidade |
|---|---|---|
| `GET` | `/api/v1/products` | Listar produtos |
| `GET` | `/api/v1/products/{productId}` | Consultar um produto |
| `POST` | `/api/v1/products` | Criar um produto |
| `POST` | `/api/v1/products/purchase` | Validar disponibilidade e reservar estoque |

### Clientes

| Método | Rota | Responsabilidade |
|---|---|---|
| `GET` | `/api/v1/customers` | Listar clientes |
| `GET` | `/api/v1/customers/{customerId}` | Consultar um cliente |
| `GET` | `/api/v1/customers/exists/{customerId}` | Verificar a existência do cliente |
| `POST` | `/api/v1/customers` | Criar um cliente |
| `PUT` | `/api/v1/customers` | Atualizar um cliente |
| `DELETE` | `/api/v1/customers/{customerId}` | Excluir um cliente |

### Pedidos e itens

| Método | Rota | Responsabilidade |
|---|---|---|
| `GET` | `/api/v1/orders` | Listar pedidos |
| `GET` | `/api/v1/orders/{orderId}` | Consultar um pedido |
| `POST` | `/api/v1/orders` | Orquestrar cliente, estoque, pagamento e notificações |
| `GET` | `/api/v1/order-lines/order/{orderId}` | Listar os itens de um pedido |

### Pagamentos

| Método | Rota | Responsabilidade |
|---|---|---|
| `POST` | `/api/v1/payments` | Registrar um pagamento |

## Exemplos

### Listar produtos

```bash
curl -sS "http://localhost:8222/api/v1/products" \
  -H "Authorization: Bearer $TOKEN"
```

### Criar cliente

```bash
curl -sS -X POST "http://localhost:8222/api/v1/customers" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@example.com",
    "address": {
      "street": "Main St",
      "houseNumber": "100",
      "zipCode": "12345"
    }
  }'
```

### Criar pedido

Substitua `<CUSTOMER_ID>` por um cliente existente. O Product Service inclui dados iniciais por migrations; confirme um identificador com `GET /api/v1/products`.

```bash
curl -sS -X POST "http://localhost:8222/api/v1/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "reference": "ORDER-001",
    "amount": 2500.00,
    "paymentMethod": "CREDIT_CARD",
    "customerId": "<CUSTOMER_ID>",
    "productPurchaseRequests": [
      {
        "productId": "018d2f1a-c101-7123-8234-a1b2c3d4e5f6",
        "quantity": 1
      }
    ]
  }'
```

Métodos de pagamento aceitos pelo código: `PAYPAL`, `CREDIT_CARD`, `VISA`, `MASTER_CARD` e `BITCOIN`.

## Respostas de autenticação

| Status | Causa comum | Ação |
|---|---|---|
| `401 Unauthorized` | Token ausente, inválido ou expirado | Gere um novo token e reenvie o cabeçalho |
| `403 Forbidden` | Requisição autenticada sem autorização suficiente | Confira as políticas e os dados do token |
| `503 Service Unavailable` | Serviço ainda não registrado ou indisponível | Verifique Eureka e os logs dos contêineres |

Veja mais soluções em [Troubleshooting](TROUBLESHOOTING.md).

[Voltar ao README](../README.md)
