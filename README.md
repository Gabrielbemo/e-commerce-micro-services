# E-commerce Microservices

Backend de e-commerce desenvolvido como **projeto pessoal de estudo** para explorar uma arquitetura de microsserviços com Java e Spring.

A solução simula capacidades de catálogo, clientes, pedidos, pagamentos e notificações. O projeto reúne comunicação síncrona entre serviços, eventos Kafka, autenticação OAuth2, configuração e descoberta centralizadas, persistência por domínio, observabilidade e testes de carga.

## Visão geral

Uma chamada externa entra pelo API Gateway, que valida o JWT emitido pelo Keycloak e encontra o serviço de destino pelo Eureka. O Order Service coordena cliente, estoque e pagamento; eventos de pedido e pagamento são enviados pelo Kafka ao Notification Service, que persiste a notificação e envia e-mails ao MailDev.

O ambiente completo é reproduzível com Docker Compose e inclui ferramentas para acompanhar métricas, logs e traces.

## Microsserviços

| Serviço | Responsabilidade | Persistência | Porta |
|---|---|---|---:|
| Config Server | Configuração centralizada | — | `8888` |
| Discovery Service | Registro e descoberta com Eureka | — | `8761` |
| Gateway Server | Roteamento, JWT e Swagger centralizado | — | `8222` |
| Product Service | Catálogo, categorias e estoque | PostgreSQL | `8050` |
| Customer Service | Clientes e endereços | MongoDB | `8090` |
| Order Service | Pedidos, itens e orquestração da compra | PostgreSQL | `8070` |
| Payment Service | Registro de pagamentos | PostgreSQL | `8060` |
| Notification Service | Eventos, notificações e e-mails | MongoDB | `8040` |

## Tecnologias

| Área | Tecnologias |
|---|---|
| Aplicações | Java, Spring Boot, Spring Cloud |
| Integração | Spring Cloud Gateway, OpenFeign, RestTemplate, Kafka |
| Plataforma | Config Server, Eureka, Docker Compose |
| Segurança e API | Keycloak, OAuth2/JWT, OpenAPI/Swagger |
| Dados | PostgreSQL, MongoDB, Flyway |
| Observabilidade | Micrometer, Prometheus, Grafana, Loki, Promtail, Zipkin |
| Qualidade | JUnit, Mockito, JaCoCo, k6 |

## Início rápido

Pré-requisitos: Docker, Docker Compose, `curl` e Python 3.

```bash
docker compose up -d --build
docker compose ps
```

Depois, configure o Keycloak, gere o bearer token e execute a primeira chamada seguindo [Primeiros passos](docs/GETTING_STARTED.md).

Swagger UI: <http://localhost:8222/swagger-ui.html>

## Documentação

| Documento | Conteúdo |
|---|---|
| [Primeiros passos](docs/GETTING_STARTED.md) | Subir o ambiente, configurar Keycloak, gerar token e executar smoke test |
| [Arquitetura](docs/ARCHITECTURE.md) | Componentes, dados, integrações e fluxo de compra |
| [API e autenticação](docs/API.md) | OAuth2, Swagger, rotas e exemplos |
| [Observabilidade](docs/OBSERVABILITY.md) | Métricas, logs, traces e dashboards |
| [Testes automatizados](docs/TESTING.md) | Maven, testes e cobertura JaCoCo |
| [Testes de carga](docs/LOAD_TESTING.md) | Suítes k6, perfis, resultados e análise |
| [Troubleshooting](docs/TROUBLESHOOTING.md) | Diagnóstico de falhas comuns do ambiente local |

## O que este projeto permite estudar

- decomposição de um domínio de e-commerce em serviços independentes;
- service discovery, configuração externa e gateway;
- autenticação centralizada com OAuth2/JWT;
- integração síncrona e eventos assíncronos;
- persistência relacional e documental por serviço;
- métricas, logs e tracing distribuído;
- testes unitários, cobertura e carga local.