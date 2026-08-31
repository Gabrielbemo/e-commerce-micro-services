# Arquitetura

Este projeto pessoal de estudo representa o backend de um e-commerce dividido em microsserviços. O objetivo é exercitar separação de responsabilidades, descoberta de serviços, configuração centralizada, segurança no gateway, persistência por domínio, comunicação síncrona e mensageria assíncrona.

Não se trata de uma plataforma pronta para produção. Decisões como credenciais locais, tracing integral e execução de toda a infraestrutura em um único Docker Compose favorecem aprendizado e observação do sistema.

## Capacidades do e-commerce

O domínio cobre o fluxo essencial de uma compra:

- manutenção do catálogo e controle de estoque;
- cadastro e consulta de clientes;
- criação de pedidos e itens;
- registro de pagamentos;
- geração de notificações e e-mails de confirmação.

## Aplicações

| Aplicação | Porta | Responsabilidade |
|---|---:|---|
| Config Server | `8888` | Centralizar as configurações das aplicações Spring |
| Discovery Service | `8761` | Registrar e localizar serviços com Eureka |
| Gateway Server | `8222` | Expor as APIs, rotear por descoberta, validar JWT e centralizar o Swagger |
| Product Service | `8050` | Gerenciar catálogo, categorias, estoque e reserva de produtos |
| Customer Service | `8090` | Gerenciar perfis e endereços de clientes |
| Order Service | `8070` | Orquestrar o pedido, persistir pedido e itens e integrar os demais domínios |
| Payment Service | `8060` | Registrar pagamentos e publicar confirmação de pagamento |
| Notification Service | `8040` | Consumir eventos, persistir notificações e enviar e-mails |

## Componentes de infraestrutura

| Componente | Porta local | Uso |
|---|---:|---|
| PostgreSQL | `5432` | Bancos relacionais de produtos, pedidos e pagamentos |
| pgAdmin | `5050` | Administração local do PostgreSQL |
| MongoDB | `27017` | Documentos de clientes e notificações |
| Mongo Express | `8081` | Administração local do MongoDB |
| Kafka | `9092` | Transporte dos eventos de pedido e pagamento |
| Zookeeper | `22181` | Coordenação da instância Kafka usada no projeto |
| Keycloak | `9098` | Emissão de tokens OAuth2/JWT |
| MailDev | `1080` (UI), `1025` (SMTP) | Captura local dos e-mails enviados |

A pilha de telemetria está detalhada em [Observabilidade](OBSERVABILITY.md).

## Persistência por domínio

Cada serviço de negócio acessa somente sua persistência:

| Serviço | Tecnologia | Banco ou coleção principal |
|---|---|---|
| Product Service | PostgreSQL + Flyway | banco `product`; tabelas `category` e `product` |
| Order Service | PostgreSQL + JPA | banco `order`; tabelas de pedido e itens |
| Payment Service | PostgreSQL + JPA | banco `payment`; tabela de pagamentos |
| Customer Service | MongoDB | banco `customer`; coleção de clientes |
| Notification Service | MongoDB | banco `notification`; coleção de notificações |

Referências entre domínios, como `customerId` e `productId` dentro de um pedido, são identificadores lógicos. Não existem chaves estrangeiras entre bancos de serviços diferentes.

## Comunicação síncrona

O gateway localiza os serviços pelo Eureka e encaminha as rotas `/api/v1/**`. No fluxo de criação de pedido, o Order Service realiza três integrações síncronas:

| Origem | Destino | Tecnologia | Finalidade |
|---|---|---|---|
| Order Service | Customer Service | OpenFeign | Consultar e validar o cliente |
| Order Service | Product Service | RestTemplate | Validar produtos e reservar estoque |
| Order Service | Payment Service | OpenFeign | Registrar o pagamento |

O Config Server fornece configurações no início de cada aplicação. Se ele estiver indisponível, os serviços não obtêm suas configurações externas.

## Comunicação assíncrona

Dois tópicos Kafka desacoplam as notificações do processamento principal:

| Tópico | Produtor | Consumidor | Conteúdo |
|---|---|---|---|
| `order-topic` | Order Service | Notification Service | Referência, valor, cliente e produtos do pedido |
| `payment-topic` | Payment Service | Notification Service | Referência, valor, método e dados do cliente |

O Notification Service persiste cada evento consumido no MongoDB e envia o e-mail correspondente pelo MailDev.

## Fluxo de criação de pedido

1. O cliente envia `POST /api/v1/orders` ao Gateway com um bearer token.
2. O Gateway valida o JWT no Keycloak e encaminha a chamada ao Order Service.
3. O Order Service consulta o Customer Service e interrompe o fluxo se o cliente não existir.
4. O Product Service valida os itens e reduz o estoque solicitado.
5. O Order Service persiste o pedido e suas linhas no PostgreSQL.
6. O Payment Service registra o pagamento no próprio PostgreSQL.
7. O Payment Service publica uma confirmação em `payment-topic`.
8. O Order Service publica uma confirmação em `order-topic`.
9. O Notification Service consome ambos os eventos, persiste as notificações e envia e-mails ao MailDev.

O fluxo demonstra orquestração e eventos, mas não implementa uma saga com compensações. Uma falha depois da reserva de estoque pode exigir tratamento adicional em uma evolução futura.

## Segurança

As rotas de negócio entram pelo Gateway, configurado como OAuth2 Resource Server. O JWT deve ter o issuer do realm `micro-services`. A interface do Swagger e endpoints técnicos permitidos pela configuração de segurança permanecem públicos para facilitar o uso local.

Consulte [API e autenticação](API.md) para gerar o token e conhecer as rotas.

## Versões do runtime

Os Dockerfiles compilam e executam todas as aplicações com Eclipse Temurin 21. Alguns POMs não estão uniformes:

- Product Service declara `java.version` 17;
- as demais aplicações declaram Java 21;
- Gateway usa Spring Boot 3.2.5 e Spring Cloud 2023.0.1;
- os demais serviços usam Spring Boot 4.0.x e Spring Cloud 2025.1.0.

Essas diferenças são registradas para descrever o estado atual, não como recomendação arquitetural.

[Voltar ao README](../README.md)
