# Primeiros passos

Este guia inicia todo o ambiente local, configura a autenticação e executa a primeira chamada à API. Para entender os componentes antes de executar o projeto, consulte [Arquitetura](ARCHITECTURE.md).

## Pré-requisitos

- Docker com o plugin Docker Compose;
- `curl`;
- Python 3, usado no bootstrap do Keycloak e na extração do token.

O build dos serviços ocorre dentro dos contêineres. Java e Maven locais são necessários apenas para executar os testes fora do Docker.

## 1. Acessar o projeto

Depois de obter uma cópia do repositório, acesse sua raiz:

```bash
cd e-commerce-micro-services
```

Os próximos comandos consideram esse diretório como ponto de partida.

## 2. Iniciar o ambiente

```bash
docker compose up -d --build
```

Confira o estado dos contêineres:

```bash
docker compose ps
```

A inicialização envolve bancos, Kafka, Keycloak e oito aplicações Spring. Aguarde até que o gateway esteja disponível antes de continuar. Para acompanhar a inicialização:

```bash
docker compose logs -f gateway-server
```

Use `Ctrl+C` para sair dos logs sem interromper os contêineres.

## 3. Configurar o Keycloak

Execute o bootstrap abaixo uma vez por ambiente. Ele cria ou atualiza:

- realm `micro-services`;
- cliente público `gateway-client`, com Direct Access Grants habilitado;
- usuário `demo-user`, com senha `demo123`;
- `frontendUrl` compatível com o issuer esperado pelo gateway.

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
    with urllib.request.urlopen(req, timeout=20) as response:
        return response.status, response.read().decode()

def req_json(method, url, token, payload=None):
    data=json.dumps(payload).encode() if payload is not None else None
    req=urllib.request.Request(url, data=data, method=method)
    req.add_header('Authorization', f'Bearer {token}')
    if payload is not None:
        req.add_header('Content-Type','application/json')
    try:
        with urllib.request.urlopen(req, timeout=20) as response:
            return response.status, response.read().decode()
    except urllib.error.HTTPError as error:
        return error.code, error.read().decode()

_, body = post_form(f'{BASE}/realms/master/protocol/openid-connect/token', {
    'client_id':'admin-cli',
    'username':ADMIN_USER,
    'password':ADMIN_PASS,
    'grant_type':'password'
})
admin_token=json.loads(body)['access_token']

code, _ = req_json('GET', f'{BASE}/admin/realms/{REALM}', admin_token)
if code == 404:
    code, body = req_json('POST', f'{BASE}/admin/realms', admin_token, {'realm': REALM, 'enabled': True})
    if code not in (201,204):
        raise SystemExit(f'erro criando realm: {code} {body}')

code, body = req_json('GET', f'{BASE}/admin/realms/{REALM}/clients?clientId={urllib.parse.quote(CLIENT_ID)}', admin_token)
clients=json.loads(body) if code == 200 else []
if not clients:
    code, body = req_json('POST', f'{BASE}/admin/realms/{REALM}/clients', admin_token, {
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
        raise SystemExit(f'erro criando client: {code} {body}')

code, body = req_json('GET', f'{BASE}/admin/realms/{REALM}/users?username={urllib.parse.quote(USERNAME)}', admin_token)
users=json.loads(body) if code == 200 else []
if not users:
    code, body = req_json('POST', f'{BASE}/admin/realms/{REALM}/users', admin_token, {
        'username': USERNAME,
        'enabled': True,
        'emailVerified': True,
        'email': 'demo@example.com',
        'firstName': 'Demo',
        'lastName': 'User'
    })
    if code not in (201,204):
        raise SystemExit(f'erro criando user: {code} {body}')
    code, body = req_json('GET', f'{BASE}/admin/realms/{REALM}/users?username={urllib.parse.quote(USERNAME)}', admin_token)
    users=json.loads(body)

uid=users[0]['id']
code, body = req_json('PUT', f'{BASE}/admin/realms/{REALM}/users/{uid}', admin_token, {
    'username': USERNAME,
    'enabled': True,
    'emailVerified': True,
    'email': 'demo@example.com',
    'firstName': 'Demo',
    'lastName': 'User',
    'requiredActions': []
})
if code not in (200,204):
    raise SystemExit(f'erro atualizando user: {code} {body}')

code, body = req_json('PUT', f'{BASE}/admin/realms/{REALM}/users/{uid}/reset-password', admin_token, {
    'type':'password',
    'temporary':False,
    'value':PASSWORD
})
if code not in (200,204):
    raise SystemExit(f'erro definindo senha: {code} {body}')

code, body = req_json('GET', f'{BASE}/admin/realms/{REALM}', admin_token)
realm=json.loads(body)
realm.setdefault('attributes', {})['frontendUrl']='http://keycloak-ms:8080/'
code, body = req_json('PUT', f'{BASE}/admin/realms/{REALM}', admin_token, realm)
if code not in (200,204):
    raise SystemExit(f'erro ajustando frontendUrl: {code} {body}')

print('Keycloak pronto: realm=micro-services, client=gateway-client, user=demo-user/demo123')
PY
```

## 4. Gerar o bearer token

```bash
TOKEN=$(curl -sS -X POST "http://localhost:9098/realms/micro-services/protocol/openid-connect/token" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "client_id=gateway-client" \
  -d "grant_type=password" \
  -d "username=demo-user" \
  -d "password=demo123" \
  | python3 -c 'import sys,json; print(json.load(sys.stdin)["access_token"])')
```

O valor permanece na variável `TOKEN` apenas no terminal atual. Para visualizá-lo:

```bash
printf '%s\n' "$TOKEN"
```

## 5. Executar o smoke test

```bash
curl -i "http://localhost:8222/api/v1/products" \
  -H "Authorization: Bearer $TOKEN"
```

Uma resposta HTTP `200` confirma que gateway, autenticação, descoberta e Product Service estão respondendo ao fluxo básico.

## 6. Acessar as interfaces locais

| Interface | URL | Credenciais locais |
|---|---|---|
| Swagger UI | <http://localhost:8222/swagger-ui.html> | bearer token |
| Eureka | <http://localhost:8761> | não exige login |
| Keycloak | <http://localhost:9098> | `admin` / `admin` |
| Grafana | <http://localhost:3000> | `admin` / `admin` |
| Prometheus | <http://localhost:9090> | não exige login |
| Zipkin | <http://localhost:9411> | não exige login |
| MailDev | <http://localhost:1080> | não exige login |
| pgAdmin | <http://localhost:5050> | `pgadmin@pgadmin.org` / `admin` |
| Mongo Express | <http://localhost:8081> | `gabriel` / `gabriel` |

## Encerrar o ambiente

```bash
docker compose down
```

Esse comando mantém os volumes. Consulte [Troubleshooting](TROUBLESHOOTING.md) antes de remover dados persistidos.

## Próximos passos

- Consulte a [referência da API](API.md) para conhecer as rotas.
- Entenda os componentes em [Arquitetura](ARCHITECTURE.md).
- Valide métricas, logs e traces em [Observabilidade](OBSERVABILITY.md).

[Voltar ao README](../README.md)
