# Observabilidade

O ambiente local reúne métricas, logs e traces para permitir o acompanhamento de uma requisição e o diagnóstico dos serviços.

| Pilar | Ferramentas | Fonte |
|---|---|---|
| Métricas | Micrometer, Prometheus e Grafana | `/actuator/prometheus` de cada aplicação |
| Logs | Docker, Promtail, Loki e Grafana | saída padrão dos contêineres |
| Traces | Micrometer Tracing, Zipkin e Grafana | spans exportados pelas aplicações |

## URLs locais

| Ferramenta | URL | Credenciais |
|---|---|---|
| Grafana | <http://localhost:3000> | `admin` / `admin` |
| Prometheus | <http://localhost:9090> | nenhuma |
| Zipkin | <http://localhost:9411> | nenhuma |
| Prontidão do Loki | <http://localhost:3100/ready> | nenhuma |

As credenciais do Grafana são apenas para o ambiente local.

## Métricas

O Prometheus coleta métricas a cada 15 segundos. As aplicações adicionam as labels `application` e `environment`, e habilitam histogramas para requisições HTTP.

### Endpoints de métricas

| Aplicação | Endpoint local |
|---|---|
| Gateway | <http://localhost:8222/actuator/prometheus> |
| Product | <http://localhost:8050/actuator/prometheus> |
| Customer | <http://localhost:8090/actuator/prometheus> |
| Order | <http://localhost:8070/actuator/prometheus> |
| Payment | <http://localhost:8060/actuator/prometheus> |
| Notification | <http://localhost:8040/actuator/prometheus> |
| Discovery | <http://localhost:8761/actuator/prometheus> |
| Config Server | <http://localhost:8888/actuator/prometheus> |

### Validar

Abra <http://localhost:9090/targets> e confirme os jobs com estado `UP`. Em seguida, teste:

```promql
up
```

```promql
sum by (application) (rate(http_server_requests_seconds_count[5m]))
```

```promql
histogram_quantile(0.95, sum by (le, application) (rate(http_server_requests_seconds_bucket[5m])))
```

## Logs

O Promtail descobre os contêineres pelo socket Docker, lê seus logs e os envia ao Loki. Labels como `service`, `container`, `stream` e `compose_project` permitem filtrar as mensagens.

No Grafana, abra **Explore**, selecione **Loki** e execute:

```logql
{service="gateway-server"} |= "Gateway request"
```

```logql
{service="order-service"}
```

```logql
{service="notification-service"}
```

O datasource Loki possui um campo derivado para `traceId`, que cria ligação contextual com o datasource Zipkin quando o identificador está presente na mensagem.

## Traces

As aplicações propagam contexto W3C e B3 e enviam spans ao Zipkin. O sampling local é `1.0` para facilitar o estudo; esse valor captura todas as requisições e não é indicado como padrão de produção.

Para gerar tráfego, obtenha o token conforme [Primeiros passos](GETTING_STARTED.md) e execute:

```bash
curl -sS "http://localhost:8222/api/v1/products" \
  -H "Authorization: Bearer $TOKEN"
```

Abra <http://localhost:9411>, escolha `gateway-server` e execute a busca.

No estado atual, a validação ponta a ponta registrada no projeto confirma traces do gateway. Os serviços em Spring Boot 4.0.x ainda exigem alinhamento adicional para exportar spans downstream com a mesma confiabilidade.

## Grafana

Os datasources Prometheus, Loki e Zipkin são provisionados automaticamente. Os dashboards ficam na pasta **Observability**:

| Dashboard | Finalidade |
|---|---|
| Microservices Overview | Disponibilidade, throughput, erros, latência e heap |
| Load Testing Overview | Métricas gerais das execuções k6 |
| Customer Load Overview | Cenários de carga focados em clientes |

Para validar o dashboard principal:

1. gere algumas chamadas na API;
2. abra **Dashboards > Observability > Microservices Overview**;
3. confirme dados de disponibilidade e requisições;
4. use **Explore** para correlacionar métricas, logs e traces.

## Verificação rápida da stack

```bash
curl -sS http://localhost:9090/-/ready
curl -sS http://localhost:3100/ready
curl -sS http://localhost:9411/health
```

Após um rebuild, os serviços podem levar alguns instantes para aparecer como `UP`. Veja [Troubleshooting](TROUBLESHOOTING.md) se a telemetria não surgir.

Para métricas geradas pelo k6, consulte [Testes de carga](LOAD_TESTING.md).

[Voltar ao README](../README.md)
