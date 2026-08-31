# Troubleshooting

Este documento reúne problemas comuns do ambiente local. Execute os comandos a partir da raiz do projeto.

## Diagnóstico inicial

```bash
docker compose ps
docker compose logs --tail=200 gateway-server
docker compose logs --tail=200 <NOME_DO_SERVICO>
```

Consulte o Eureka em <http://localhost:8761> para verificar quais aplicações foram registradas.

## `401 Unauthorized`

O token pode estar ausente ou expirado. Gere outro conforme [API e autenticação](API.md) e confirme que o cabeçalho contém a palavra `Bearer`.

## Erro de issuer no gateway

O gateway espera o issuer `http://keycloak-ms:8080/realms/micro-services`. Execute novamente o bootstrap de [Primeiros passos](GETTING_STARTED.md#3-configurar-o-keycloak), que ajusta o `frontendUrl` do realm.

Confira também os logs:

```bash
docker compose logs --tail=200 keycloak gateway-server
```

## Kafka encerra com erro de estado antigo

Erros como `NodeExistsException` podem ser causados por estado incompatível nos volumes locais. A operação abaixo apaga somente os dados locais de Kafka e Zookeeper:

```bash
docker compose down
docker volume rm e-commerce-micro-services_kafka-data e-commerce-micro-services_zookeeper-data
docker compose up -d --build
```

> A remoção de volumes apaga dados persistidos e não pode ser desfeita pelo Docker Compose.

## Product Service relata tabelas ausentes

O Product Service usa Flyway e valida o esquema. Para recriar apenas o banco local `product`:

```bash
docker exec ms_pg_sql psql -U gabriel -d postgres \
  -c "DROP DATABASE IF EXISTS product WITH (FORCE);"
docker exec ms_pg_sql psql -U gabriel -d postgres \
  -c "CREATE DATABASE product;"
docker compose restart product-service
```

Essa operação remove os dados do catálogo local. As migrations recriam o esquema e os dados iniciais.

## `503 Service Unavailable`

O gateway não encontrou uma instância registrada. Verifique:

1. o serviço aparece no Eureka;
2. Config Server e Discovery Service estão ativos;
3. o serviço de negócio concluiu a inicialização;
4. não há erro de banco ou configuração nos logs.

```bash
docker compose ps
docker compose logs --tail=200 config-server discovery-service gateway-server
```

## Targets ausentes no Prometheus

Abra <http://localhost:9090/targets>. Após um rebuild, aguarde a estabilização das aplicações. Se um target continuar `DOWN`, teste diretamente:

```bash
curl -i http://localhost:8050/actuator/prometheus
```

Troque a porta conforme a tabela em [Observabilidade](OBSERVABILITY.md#endpoints-de-métricas).

## Logs não aparecem no Loki

Verifique Loki e Promtail:

```bash
curl -sS http://localhost:3100/ready
docker compose logs --tail=200 loki promtail
```

O Promtail depende do socket Docker e do diretório de logs de contêineres montados pelo Compose. Ambientes Docker que não expõem esses caminhos podem exigir adaptação da coleta.

## Traces ausentes no Zipkin

Gere tráfego autenticado e consulte novamente o Zipkin. O gateway usa Spring Boot 3.2.5; os demais serviços usam Spring Boot 4.0.x e, conforme a validação já registrada no projeto, a exportação downstream ainda requer alinhamento adicional para atingir a mesma confiabilidade do gateway.

## Falha ao enviar métricas k6 ao Prometheus

Para erros de remote write e procedimentos específicos, consulte [Testes de carga](LOAD_TESTING.md#troubleshooting).

## Reinicialização completa dos dados locais

Somente quando a perda de todos os dados locais for aceitável:

```bash
docker compose down -v
docker compose up -d --build
```

Depois, execute novamente o bootstrap do Keycloak.

[Voltar ao README](../README.md)
