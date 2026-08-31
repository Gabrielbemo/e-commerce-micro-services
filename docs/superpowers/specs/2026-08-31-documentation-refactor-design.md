# Refatoração da documentação — Design

## Objetivo

Refatorar a documentação do projeto para apresentar, de forma profissional e verificável, um projeto pessoal de estudo que simula o backend de um e-commerce com arquitetura de microsserviços.

A documentação deve atender igualmente a dois públicos:

- recrutadores que desejam compreender rapidamente o propósito, o escopo e as tecnologias do projeto;
- desenvolvedores que desejam executar, testar e estudar a solução.

Toda a documentação será escrita em português. Os nomes dos arquivos permanecerão em inglês.

## Posicionamento do projeto

O projeto será descrito explicitamente como um projeto pessoal de estudo sobre microsserviços aplicados ao domínio de e-commerce. A apresentação não deve sugerir uso em produção, maturidade comercial ou participação de uma equipe.

A documentação não destacará o fato de o código estar publicamente disponível e não conterá convites para contribuição. A licença existente será referenciada de forma discreta.

Credenciais, senhas e configurações presentes nos exemplos serão identificadas como valores exclusivos do ambiente local e educacional, inadequados para produção.

## Estrutura documental

### `README.md`

Será a página de entrada do projeto e conterá:

- identificação clara como projeto pessoal de estudo;
- resumo do backend de e-commerce e da arquitetura de microsserviços;
- capacidades de catálogo, clientes, pedidos, pagamentos e notificações;
- tecnologias agrupadas por finalidade;
- tabela resumida dos serviços;
- início rápido com link para `docs/GETTING_STARTED.md`;
- índice para os documentos especializados;
- nota sobre o caráter local e educacional das configurações;
- referência discreta à licença.

O README deve favorecer leitura rápida e não repetir procedimentos detalhados existentes em `docs/`.

### `docs/GETTING_STARTED.md`

Conterá pré-requisitos, clonagem, inicialização por Docker Compose, verificação dos contêineres, bootstrap do Keycloak, geração do bearer token, smoke test e URLs essenciais.

### `docs/ARCHITECTURE.md`

Explicará o domínio do e-commerce, as responsabilidades dos serviços, Config Server, Eureka, Gateway, persistência por serviço, comunicação HTTP e Kafka e o fluxo completo de criação de pedido. Incluirá tabelas de componentes e portas, sem diagramas Mermaid.

### `docs/API.md`

Documentará autenticação OAuth2, geração e uso do token, Swagger centralizado, rotas expostas pelo gateway e exemplos de requisição.

### `docs/OBSERVABILITY.md`

Explicará os três pilares de observabilidade — métricas, logs e traces — e o papel de Prometheus, Grafana, Loki, Promtail e Zipkin. Conterá passos e consultas para verificar cada integração.

### `docs/TESTING.md`

Documentará testes automatizados dos serviços, execução individual e completa, cobertura JaCoCo e localização dos relatórios.

### `docs/LOAD_TESTING.md`

Centralizará a documentação da suíte k6, incluindo a suíte geral e os cenários focados no serviço de clientes, variáveis, execução, resultados e diagnóstico de falhas.

O arquivo `tests/performance/README.md` será reduzido a uma introdução breve e a um link para este documento, evitando duas fontes de verdade.

### `docs/TROUBLESHOOTING.md`

Reunirá falhas de inicialização, problemas de Kafka e Zookeeper, configuração do Keycloak, expiração de token, indisponibilidade dos serviços, ausência de telemetria e falhas do k6.

## Navegação

Todos os links serão relativos e compatíveis com a renderização do GitHub. O README apontará para cada documento especializado. Cada arquivo em `docs/` terá um link de retorno ao README e links contextuais para assuntos diretamente relacionados.

## Conteúdo técnico confirmado no repositório

A documentação deverá refletir a implementação, que contém:

- serviços de configuração, descoberta, gateway, produtos, clientes, pedidos, pagamentos e notificações;
- PostgreSQL para produtos, pedidos e pagamentos;
- MongoDB para clientes e notificações;
- Kafka e Zookeeper para eventos de pedidos e pagamentos;
- Keycloak e OAuth2/JWT para proteção das rotas do gateway;
- Swagger/OpenAPI centralizado no gateway;
- Prometheus, Grafana, Loki, Promtail e Zipkin para observabilidade;
- testes automatizados Maven com JaCoCo;
- testes de carga com k6.

Versões e requisitos não serão generalizados entre serviços. Quando houver diferenças — como as versões de Java, Spring Boot ou Spring Cloud — a documentação usará os valores efetivamente definidos nos arquivos de cada serviço ou evitará uma afirmação global incorreta.

## Estratégia de validação

Cada informação operacional será validada proporcionalmente ao seu risco:

1. Conferência estática com `docker-compose.yml`, configurações Spring, POMs, scripts k6, controladores e configuração do gateway.
2. Verificação dos links Markdown e das referências entre arquivos.
3. Execução dos testes Maven de cada serviço e conferência da geração dos relatórios JaCoCo.
4. Quando o Docker estiver disponível, inicialização da stack, inspeção do estado dos contêineres, bootstrap do Keycloak, geração de token e smoke tests pelo gateway.

Comandos ou integrações não verificáveis no ambiente serão identificados como não validados. A documentação não declarará sucesso sem evidência.

## Correções permitidas

A refatoração poderá corrigir instruções, comandos, portas, versões, rotas e descrições inconsistentes, desde que a correção seja comprovada pelo código, pela configuração ou pela execução do projeto.

Mudanças no código da aplicação ou na arquitetura executável não fazem parte deste trabalho. Caso uma falha real exija alteração de código para que a documentação seja verdadeira, ela será relatada separadamente, sem correção presumida.

## Estilo editorial

- Português técnico, direto e consistente.
- Nomes de arquivos em inglês.
- Parágrafos curtos e títulos descritivos.
- Tabelas para serviços, portas, tecnologias, persistência e ferramentas.
- Comandos completos e copiáveis.
- Notas explícitas sobre limitações educacionais e segurança local.
- Sem diagramas Mermaid, badges decorativos, slogans promocionais ou convite para contribuições.
- Uso consistente de “arquitetura de microsserviços”, “serviço”, “gateway” e “bearer token”.

## Critérios de aceite

- O README comunica em poucos minutos o propósito, o domínio de e-commerce, o caráter pessoal e educacional e a arquitetura de microsserviços.
- Um novo leitor encontra um caminho inequívoco para iniciar o ambiente e realizar a primeira chamada autenticada.
- Arquitetura, API, observabilidade, testes automatizados, carga e diagnóstico possuem documentos próprios.
- Não há conteúdo operacional detalhado duplicado entre o README e os documentos especializados.
- A documentação de k6 possui uma única fonte de verdade em `docs/LOAD_TESTING.md`.
- Links relativos internos são válidos.
- Comandos testáveis foram executados ou estão acompanhados de uma limitação explícita de validação.
- Nenhuma alteração funcional no código da aplicação é incluída no escopo.
