# Refatoração da documentação — Plano de implementação

> **Para execução assistida:** implemente as tarefas em ordem, verificando cada entrega antes de avançar.

**Objetivo:** Reorganizar a documentação do projeto pessoal de estudo em uma página inicial profissional e documentos especializados, todos tecnicamente verificáveis.

**Arquitetura:** O `README.md` será uma página de apresentação e navegação. Procedimentos operacionais serão distribuídos em sete documentos sob `docs/`, com uma única fonte de verdade por assunto e links relativos entre eles.

**Tecnologias documentadas:** Java, Spring Boot, Spring Cloud, Docker Compose, PostgreSQL, MongoDB, Kafka, Keycloak, OpenAPI, Prometheus, Grafana, Loki, Promtail, Zipkin, Maven, JaCoCo e k6.

**Especificação:** `docs/superpowers/specs/2026-08-31-documentation-refactor-design.md`

## Restrições globais

- Escrever todo o conteúdo em português e manter os nomes dos arquivos em inglês.
- Apresentar a solução como projeto pessoal de estudo de microsserviços para e-commerce.
- Não usar diagramas Mermaid, badges decorativos ou convites para contribuição.
- Não alterar código-fonte nem configuração executável da aplicação.
- Não executar comandos Git, adicionar arquivos ao índice ou criar commits.
- Confirmar instruções pelo código, pelas configurações ou por execução; declarar limitações quando uma validação não for possível.

---

### Tarefa 1: Consolidar a fonte técnica

**Arquivos:**

- Ler: `README.md`
- Ler: `docker-compose.yml`
- Ler: `services/*/pom.xml`
- Ler: `services/config-server/src/main/resources/configurations/*.yml`
- Ler: `services/*/src/main/java/**/*.java`
- Ler: `observability/**/*.yml`
- Ler: `tests/performance/**/*`

**Entrega:** Inventário confirmado de serviços, responsabilidades, bancos, integrações, rotas, portas, ferramentas, comandos e variáveis.

- [ ] Conferir os controladores e a configuração do gateway para enumerar as rotas públicas e protegidas.
- [ ] Conferir o fluxo de pedidos no código para distinguir chamadas HTTP de eventos Kafka.
- [ ] Conferir POMs e Dockerfiles para registrar requisitos e diferenças de versão sem generalizações incorretas.
- [ ] Conferir Compose e configurações de observabilidade para validar portas, credenciais locais e URLs.
- [ ] Conferir scripts e README de performance para preservar todos os modos e variáveis suportados.

### Tarefa 2: Criar a documentação operacional principal

**Arquivos:**

- Criar: `docs/GETTING_STARTED.md`
- Criar: `docs/API.md`
- Criar: `docs/TROUBLESHOOTING.md`

**Entrega:** Um novo leitor consegue iniciar a stack, autenticar-se, chamar a API e diagnosticar falhas comuns.

- [ ] Escrever pré-requisitos, inicialização, verificação, bootstrap do Keycloak, geração de token e smoke test em `GETTING_STARTED.md`.
- [ ] Escrever autenticação, Swagger, rotas do gateway e exemplos copiáveis em `API.md`.
- [ ] Mover e revisar diagnósticos gerais em `TROUBLESHOOTING.md`, usando links para procedimentos relacionados.
- [ ] Verificar que credenciais são identificadas como locais e educacionais.

### Tarefa 3: Criar a documentação conceitual e de qualidade

**Arquivos:**

- Criar: `docs/ARCHITECTURE.md`
- Criar: `docs/OBSERVABILITY.md`
- Criar: `docs/TESTING.md`

**Entrega:** Leitores compreendem a arquitetura do e-commerce, observam a aplicação e executam testes automatizados.

- [ ] Descrever componentes, responsabilidades, persistência, comunicação síncrona e assíncrona e fluxo de pedido em `ARCHITECTURE.md`.
- [ ] Documentar métricas, logs, traces, ferramentas, URLs, consultas e validação em `OBSERVABILITY.md`.
- [ ] Documentar testes Maven, execução por serviço, suíte completa, JaCoCo e relatórios em `TESTING.md`.
- [ ] Usar tabelas, texto e listas; não incluir diagramas.

### Tarefa 4: Centralizar os testes de carga

**Arquivos:**

- Criar: `docs/LOAD_TESTING.md`
- Modificar: `tests/performance/README.md`

**Entrega:** `docs/LOAD_TESTING.md` torna-se a única fonte detalhada para k6.

- [ ] Consolidar suíte geral, suíte de customer, scripts locais e Docker, variáveis, cenários, saídas e observabilidade.
- [ ] Confirmar nomes e valores padrão diretamente em `tests/performance/k6/config.js` e nos scripts.
- [ ] Reduzir `tests/performance/README.md` a contexto breve e link relativo para `docs/LOAD_TESTING.md`.
- [ ] Manter troubleshooting específico de k6 no documento central e apontar para o troubleshooting geral quando necessário.

### Tarefa 5: Reescrever a página inicial

**Arquivos:**

- Modificar: `README.md`

**Entrega:** Página inicial curta, profissional e adequada tanto a portfólio quanto ao uso técnico.

- [ ] Escrever propósito e aviso de projeto pessoal de estudo.
- [ ] Resumir capacidades do e-commerce e decisões arquiteturais.
- [ ] Criar tabelas de serviços e tecnologias.
- [ ] Adicionar início rápido e índice para todos os documentos especializados.
- [ ] Referenciar a licença discretamente e remover instruções detalhadas duplicadas.

### Tarefa 6: Verificar documentação e projeto

**Arquivos:**

- Verificar: `README.md`
- Verificar: `docs/*.md`
- Verificar: `tests/performance/README.md`

**Entrega:** Documentação coerente, navegável e acompanhada de evidência de validação.

- [ ] Verificar todos os destinos de links Markdown relativos com um script local somente leitura.
- [ ] Procurar placeholders, referências antigas e duplicação operacional relevante.
- [ ] Executar `./mvnw test` em cada diretório de serviço e registrar resultados.
- [ ] Executar `./mvnw verify` em cada serviço para confirmar JaCoCo, quando suportado.
- [ ] Consultar `docker compose config` para validar a configuração resolvida.
- [ ] Se o Docker estiver acessível, subir a stack, confirmar o estado dos contêineres, executar bootstrap, obter token e realizar smoke tests.
- [ ] Revisar o conteúdo final contra todos os critérios da especificação e relatar limitações objetivamente.
