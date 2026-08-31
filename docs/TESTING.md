# Testes automatizados e cobertura

Os serviços possuem testes com o ecossistema Spring Test, JUnit e Mockito. O JaCoCo gera relatórios e aplica um limite mínimo de cobertura durante a fase Maven `verify`.

## Pré-requisitos

- JDK compatível com o serviço;
- acesso às dependências Maven na primeira execução.

Os Dockerfiles usam JDK 21. Para evitar alternância local entre versões, Java 21 é a opção prática para executar toda a suíte, embora o POM de Product Service declare `java.version` 17.

## Executar todos os serviços

Na raiz do projeto:

```bash
for service in config-server discovery gateway-server customer product order payment notification; do
  (cd "services/$service" && sh mvnw clean verify)
done
```

O loop continua para o serviço seguinte mesmo se um subshell falhar; confira a saída de cada build. Para interromper na primeira falha:

```bash
for service in config-server discovery gateway-server customer product order payment notification; do
  (cd "services/$service" && sh mvnw clean verify) || exit 1
done
```

## Executar um serviço

Exemplo com Order Service:

```bash
cd services/order
sh mvnw clean verify
```

No Windows, use o wrapper correspondente:

```powershell
cd services/order
.\mvnw.cmd clean verify
```

## Testes sem validação de cobertura

Para executar somente a fase de testes:

```bash
cd services/customer
sh mvnw test
```

## Relatórios JaCoCo

Após `verify`, cada serviço grava o relatório HTML em:

```text
services/<servico>/target/site/jacoco/index.html
```

Exemplo:

```text
services/order/target/site/jacoco/index.html
```

O goal `jacoco:check` falha quando a cobertura de linhas do bundle fica abaixo do mínimo definido no POM. Config Server e Discovery Service excluem suas classes `*Application` da análise porque elas contêm apenas o bootstrap.

## Escopo da suíte

Há testes para controllers, services, mappers, handlers, configurações, clientes HTTP, produtores e consumidores Kafka, conforme a responsabilidade de cada aplicação. Os testes automatizados não substituem o smoke test autenticado do ambiente completo descrito em [Primeiros passos](GETTING_STARTED.md).

Para avaliar comportamento sob concorrência, consulte [Testes de carga](LOAD_TESTING.md).

[Voltar ao README](../README.md)
