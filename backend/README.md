# Enterprise E-commerce API

<p align="center">
  <strong>API REST corporativa para e-commerce construída com Java 21, Spring Boot 3.5, segurança JWT/OAuth2, PostgreSQL, Redis, RabbitMQ e documentação OpenAPI.</strong>
</p>

<p align="center">
  <img alt="Java" src="https://img.shields.io/badge/Java-21-red?style=for-the-badge&logo=openjdk">
  <img alt="Spring Boot" src="https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?style=for-the-badge&logo=springboot">
  <img alt="Spring Security" src="https://img.shields.io/badge/Spring%20Security-JWT%20%2F%20OAuth2-6DB33F?style=for-the-badge&logo=springsecurity">
  <img alt="OpenAPI" src="https://img.shields.io/badge/OpenAPI-Swagger-85EA2D?style=for-the-badge&logo=swagger">
  <img alt="PostgreSQL" src="https://img.shields.io/badge/PostgreSQL-17-blue?style=for-the-badge&logo=postgresql">
  <img alt="Flyway" src="https://img.shields.io/badge/Flyway-Migrations-CC0200?style=for-the-badge&logo=flyway">
  <img alt="Redis" src="https://img.shields.io/badge/Redis-Cache%20%2F%20Tokens-red?style=for-the-badge&logo=redis">
  <img alt="RabbitMQ" src="https://img.shields.io/badge/RabbitMQ-Spring%20AMQP-orange?style=for-the-badge&logo=rabbitmq">
  <img alt="Architecture" src="https://img.shields.io/badge/Architecture-Modular%20Monolith-5A45FF?style=for-the-badge">
  <img alt="Maven" src="https://img.shields.io/badge/Maven-Build-blue?style=for-the-badge&logo=apachemaven">
  <img alt="Docker" src="https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker">
</p>

---

## Visão geral

**Enterprise E-commerce API** é um projeto backend desenvolvido para representar uma base sólida de e-commerce corporativo. A aplicação cobre autenticação, catálogo, estoque, carrinho, pedidos, pagamentos e envios, mantendo uma organização modular clara e preparada para evolução.

O projeto utiliza **Spring Boot 3.5** com **Java 21**, persistência com **PostgreSQL**, versionamento de schema com **Flyway**, autenticação com **JWT** e **Google OAuth2**, suporte a **Redis** para tokens/cache e **RabbitMQ** para iniciar uma arquitetura orientada a eventos.

O principal diferencial arquitetural é o uso de **Monólito Modular**: a aplicação permanece simples de executar e manter, mas o domínio é separado por módulos bem definidos. A mensageria foi adicionada de forma incremental, sem substituir fluxos síncronos existentes, reduzindo risco e preparando o sistema para futuras integrações assíncronas.

---

## Sumário

- [Features](#features)
- [Tecnologias](#tecnologias)
- [Arquitetura](#arquitetura)
- [Fluxo REST](#fluxo-rest)
- [Fluxo RabbitMQ](#fluxo-rabbitmq)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Configuração](#configuração)
- [Docker](#docker)
- [Como executar](#como-executar)
- [Screenshots](#screenshots)
- [Swagger](#swagger)
- [RabbitMQ Management](#rabbitmq-management)
- [Exemplos de autenticação](#exemplos-de-autenticação)
- [Exemplos de endpoints](#exemplos-de-endpoints)
- [Testes](#testes)[pablo@archlinux backend]$ git status
  On branch main
  Your branch is up to date with 'origin/main'.

Changes to be committed:
(use "git restore --staged <file>..." to unstage)
modified:   README.md

Untracked files:
(use "git add <file>..." to include in what will be committed)
../.idea/vcs.xml
../docs/images/rabbitmq-queues.png

[pablo@archlinux backend]$ git add .
[pablo@archlinux backend]$ git commit git commit -m "docs(readme): improve project documentation and architecture overview"
error: pathspec 'git' did not match any file(s) known to git
error: pathspec 'commit' did not match any file(s) known to git
[pablo@archlinux backend]$ git status
On branch main
Your branch is up to date with 'origin/main'.

Changes to be committed:
(use "git restore --staged <file>..." to unstage)
modified:   README.md

Untracked files:
(use "git add <file>..." to include in what will be committed)
../.idea/vcs.xml
../docs/images/rabbitmq-queues.png

[pablo@archlinux backend]$ git push
Everything up-to-date
[pablo@archlinux backend]$
- [Roadmap](#roadmap)
- [Autor](#autor)

---

## Features

- [x] Cadastro de usuários
- [x] Login com JWT
- [x] Login com Google OAuth2
- [x] Refresh token com rotação
- [x] Logout com revogação de refresh token
- [x] CRUD de categorias
- [x] CRUD de produtos
- [x] Controle de estoque
- [x] Carrinho de compras
- [x] Criação e consulta de pedidos
- [x] Aprovação, rejeição e estorno de pagamentos
- [x] Criação e acompanhamento de envios
- [x] Documentação Swagger/OpenAPI
- [x] Migrações versionadas com Flyway
- [x] Redis para cache/token store
- [x] RabbitMQ com exchange topic e eventos de domínio
- [x] Docker Compose para infraestrutura local

---

## Tecnologias

| Categoria | Tecnologia | Uso no projeto |
| --- | --- | --- |
| Linguagem | Java 21 | Plataforma principal da aplicação |
| Framework | Spring Boot 3.5 | Bootstrap, configuração e runtime da API |
| Web | Spring Web | Construção dos endpoints REST |
| Segurança | Spring Security | Autenticação, autorização e filtros de segurança |
| Autenticação | JWT | Access token no padrão Bearer |
| OAuth2 | Google OAuth2 Login | Login social com provedor externo |
| Persistência | Spring Data JPA | Repositories e abstração de acesso a dados |
| Banco de dados | PostgreSQL | Persistência relacional principal |
| Migrações | Flyway | Versionamento e evolução do schema |
| Cache/Tokens | Redis | Suporte a tokens e dados temporários |
| Mensageria | RabbitMQ + Spring AMQP | Publicação e consumo de eventos |
| Documentação | Springdoc OpenAPI | Swagger UI e contrato OpenAPI |
| Build | Maven | Build, testes e empacotamento |
| Infra local | Docker Compose | PostgreSQL, PgAdmin e RabbitMQ |

---

## Arquitetura

O projeto segue o estilo **Modular Monolith**. Isso significa que a aplicação é entregue como um único deploy, mas seu código é dividido por capacidades de negócio, com módulos internos coesos e responsabilidades explícitas.

Essa arquitetura foi escolhida porque oferece um equilíbrio pragmático para um sistema de e-commerce em evolução: menor complexidade operacional do que microsserviços, menor acoplamento do que um monólito tradicional e uma base mais limpa para crescimento progressivo.

Vantagens práticas:

- Desenvolvimento local mais simples.
- Menos infraestrutura obrigatória para executar o sistema.
- Transações síncronas mais diretas para regras críticas.
- Separação clara por domínio.
- Menor custo cognitivo para evoluir features.
- Caminho mais seguro para futura extração de módulos.

Uma migração para microsserviços faria sentido quando houver necessidade concreta de escalar módulos de forma independente, times com ownership separado, deploys autônomos, limites transacionais mais maduros e observabilidade suficiente para operar um ambiente distribuído.

| Módulo | Responsabilidade |
| --- | --- |
| `auth` | Cadastro, login, JWT, OAuth2, refresh token e logout |
| `category` | Gestão de categorias de produtos |
| `product` | Catálogo de produtos |
| `inventory` | Controle de estoque |
| `cart` | Carrinho de compras |
| `order` | Criação, consulta, status e cancelamento de pedidos |
| `payment` | Criação, aprovação, rejeição e estorno de pagamentos |
| `shipping` | Criação e evolução do status de envio |
| `common` | Configurações, exceções, entidades base, utilitários e mensageria compartilhada |

```mermaid
flowchart TB
    Client[Cliente / Frontend / API Consumer]

    subgraph App[Enterprise E-commerce API - Modular Monolith]
        Auth[Auth Module]
        Category[Category Module]
        Product[Product Module]
        Inventory[Inventory Module]
        Cart[Cart Module]
        Order[Order Module]
        Payment[Payment Module]
        Shipping[Shipping Module]
        Common[Common Module]
    end

    PostgreSQL[(PostgreSQL)]
    Redis[(Redis)]
    RabbitMQ[(RabbitMQ)]

    Client --> Auth
    Client --> Category
    Client --> Product
    Client --> Inventory
    Client --> Cart
    Client --> Order
    Client --> Payment
    Client --> Shipping

    Auth --> PostgreSQL
    Category --> PostgreSQL
    Product --> PostgreSQL
    Inventory --> PostgreSQL
    Cart --> PostgreSQL
    Order --> PostgreSQL
    Payment --> PostgreSQL
    Shipping --> PostgreSQL

    Auth --> Redis
    Order --> RabbitMQ
    Payment --> RabbitMQ
    RabbitMQ --> Payment
    RabbitMQ --> Shipping
    RabbitMQ --> Inventory

    Common -. Configurações e contratos compartilhados .-> Auth
    Common -. Configurações e contratos compartilhados .-> Order
    Common -. Configurações e contratos compartilhados .-> Payment
```

---

## Fluxo REST

O fluxo principal da aplicação é REST e síncrono. Ele continua sendo a fonte de execução das regras de negócio e não foi substituído pela mensageria.

Responsabilidades por camada:

| Camada | Responsabilidade |
| --- | --- |
| `Controller` | Recebe requisições HTTP, valida entrada e retorna respostas REST |
| `DTO` | Define contratos de entrada e saída da API |
| `Service` | Orquestra casos de uso e aplica regras de negócio |
| `Repository` | Encapsula acesso a dados com Spring Data JPA |
| `Banco` | Armazena o estado persistente da aplicação |

```mermaid
sequenceDiagram
    participant Client as Cliente
    participant Controller as Controller
    participant Service as Service
    participant Repository as Repository
    participant DB as PostgreSQL

    Client->>Controller: HTTP Request
    Controller->>Service: DTO validado
    Service->>Service: Regras de negócio
    Service->>Repository: Operação de persistência
    Repository->>DB: SQL/JPA
    DB-->>Repository: Resultado
    Repository-->>Service: Entidade
    Service-->>Controller: Response DTO
    Controller-->>Client: HTTP Response
```

> A mensageria complementa esse fluxo com eventos, mas não altera os contratos REST nem muda a execução principal dos casos de uso.

---

## Fluxo RabbitMQ

RabbitMQ foi incorporado como uma base inicial de **Event-Driven Architecture**. O objetivo é permitir que módulos reajam a eventos de domínio sem acoplar diretamente novas responsabilidades ao fluxo síncrono.

No estado atual, os consumers são observacionais: eles recebem eventos e registram logs. Isso evita duplicidade de processamento e mantém a previsibilidade do comportamento existente.

Conceitos utilizados:

| Conceito | Papel no projeto |
| --- | --- |
| Publisher | Componente que publica eventos após uma ação de negócio relevante |
| Exchange | Ponto central de roteamento das mensagens |
| Topic Exchange | Exchange que roteia mensagens por routing key |
| Routing Key | Chave que identifica o tipo do evento publicado |
| Binding | Regra que liga uma fila a uma exchange por routing key |
| Queue | Fila que armazena mensagens para um consumidor |
| Consumer/Listener | Componente que consome mensagens com `@RabbitListener` |
| Spring AMQP | Integração Spring usada para `RabbitTemplate`, conversores JSON e listeners |

RabbitMQ **não substitui chamadas REST** neste momento porque o fluxo síncrono ainda concentra regras importantes como criação de pedido, criação de pagamento, confirmação de pedido, restauração de estoque e criação de envio. Migrar isso prematuramente para assíncrono aumentaria risco de duplicidade, inconsistência e complexidade operacional.

Com essa base, o sistema fica preparado para evoluções futuras como DLQ, retries, Outbox Pattern, idempotência, auditoria de eventos e integração com serviços externos.

**Exchange**

| Nome | Tipo |
| --- | --- |
| `ecommerce.topic` | Topic Exchange |

**Routing keys**

| Routing key | Origem | Consumidores atuais |
| --- | --- | --- |
| `order.created` | `OrderEventPublisher` | `PaymentEventListener`, `InventoryEventListener` |
| `payment.approved` | `PaymentEventPublisher` | `ShippingEventListener` |
| `payment.rejected` | `PaymentEventPublisher` | `InventoryEventListener` |
| `order.cancelled` | `OrderEventPublisher` | `InventoryEventListener` |

**Filas**

| Fila | Objetivo |
| --- | --- |
| `payment.order-created.queue` | Receber pedidos criados no módulo de pagamento |
| `order.payment-result.queue` | Reservada para observação futura de resultados de pagamento |
| `shipping.payment-approved.queue` | Receber pagamentos aprovados no módulo de envio |
| `inventory.events.queue` | Receber eventos relevantes para estoque |

```mermaid
flowchart LR
    Order[Order Module]
    Payment[Payment Module]
    Shipping[Shipping Module]
    Inventory[Inventory Module]

    Exchange{{ecommerce.topic}}

    Order -->|order.created| Exchange
    Order -->|order.cancelled| Exchange
    Exchange -->|order.created| Payment
    Exchange -->|order.created / order.cancelled| Inventory

    Payment -->|payment.approved| Exchange
    Payment -->|payment.rejected| Exchange
    Exchange -->|payment.approved| Shipping
    Exchange -->|payment.rejected| Inventory
```

> No MVP, os listeners apenas registram logs. Eles não substituem chamadas diretas entre serviços e não executam processamento automático de pagamento, envio ou estoque.

---

## Estrutura do projeto

```text
.
├── docker-compose.yml
├── pom.xml
├── mvnw
├── mvnw.cmd
└── src
    ├── main
    │   ├── java
    │   │   └── com/joaopablo/ecommerce
    │   │       ├── auth
    │   │       ├── cart
    │   │       ├── category
    │   │       ├── common
    │   │       │   └── messaging/rabbitmq
    │   │       ├── inventory
    │   │       ├── order
    │   │       ├── payment
    │   │       ├── product
    │   │       ├── shipping
    │   │       └── BackendApplication.java
    │   └── resources
    │       ├── application.yaml
    │       └── db/migration
    └── test
        └── java/com/joaopablo/ecommerce
```

---

## Configuração

A aplicação usa variáveis de ambiente para dados sensíveis e parâmetros externos. Essa separação evita hardcode de credenciais e facilita a execução em ambientes diferentes.

| Variável | Obrigatória | Valor padrão | Descrição |
| --- | --- | --- | --- |
| `JWT_SECRET` | Sim | - | Chave secreta usada para assinatura e validação dos tokens JWT |
| `GOOGLE_CLIENT_ID` | Sim para OAuth2 | - | Client ID da aplicação configurada no Google Cloud Console |
| `GOOGLE_CLIENT_SECRET` | Sim para OAuth2 | - | Client Secret da aplicação configurada no Google Cloud Console |
| `RABBITMQ_HOST` | Não | `localhost` | Host do broker RabbitMQ |
| `RABBITMQ_PORT` | Não | `5672` | Porta AMQP usada pela aplicação |
| `RABBITMQ_USERNAME` | Não | `guest` | Usuário de conexão com RabbitMQ |
| `RABBITMQ_PASSWORD` | Não | `guest` | Senha de conexão com RabbitMQ |

Exemplo de `.env`:

```env
JWT_SECRET=change-me-with-a-strong-secret-change-me-with-a-strong-secret
GOOGLE_CLIENT_ID=your-google-client-id
GOOGLE_CLIENT_SECRET=your-google-client-secret

RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

Configurações locais padrão:

| Serviço | Host | Porta | Observação |
| --- | --- | --- | --- |
| API | `localhost` | `8080` | Aplicação Spring Boot |
| PostgreSQL | `localhost` | `5432` | Banco principal |
| PgAdmin | `localhost` | `5050` | Administração do PostgreSQL |
| RabbitMQ AMQP | `localhost` | `5672` | Conexão da aplicação |
| RabbitMQ Management | `localhost` | `15672` | Interface web do RabbitMQ |
| Redis | `localhost` | `6379` | Necessário para tokens/cache |

---

## Docker

O `docker-compose.yml` provisiona os serviços de infraestrutura necessários para desenvolvimento local, exceto Redis, que deve estar disponível separadamente em `localhost:6379`.

| Container | Imagem | Finalidade |
| --- | --- | --- |
| `ecommerce-postgres` | `postgres:17` | Banco relacional da aplicação |
| `ecommerce-pgadmin` | `dpage/pgadmin4` | Interface para administração do PostgreSQL |
| `ecommerce-rabbitmq` | `rabbitmq:3.13-management` | Broker AMQP e painel de gerenciamento |

Subir toda a infraestrutura definida no compose:

```bash
docker compose up -d
```

Subir apenas banco e mensageria:

```bash
docker compose up -d postgres rabbitmq
```

Acompanhar logs dos containers:

```bash
docker compose logs -f
```

Parar os containers:

```bash
docker compose down
```

Credenciais locais:

| Serviço | URL | Usuário | Senha |
| --- | --- | --- | --- |
| PgAdmin | `http://localhost:5050` | `admin@admin.com` | `admin` |
| RabbitMQ Management | `http://localhost:15672` | `guest` | `guest` |
| PostgreSQL | `localhost:5432/ecommerce` | `postgres` | `postgres` |

---

## Como executar

### 1. Clone

```bash
git clone <repository-url>
cd enterprise-ecommerce/backend
```

### 2. Configuração

Crie um arquivo `.env` na raiz do projeto:

```bash
touch .env
```

Preencha as variáveis com base na seção [Configuração](#configuração).

### 3. Docker

Suba os containers de infraestrutura:

```bash
docker compose up -d
```

### 4. Banco

O PostgreSQL será exposto em:

```text
localhost:5432
```

As migrações Flyway são executadas automaticamente na inicialização da aplicação.

### 5. Redis

Garanta que o Redis esteja rodando localmente:

```text
localhost:6379
```

### 6. Build

Compile e execute os testes:

```bash
./mvnw clean install
```

Para apenas empacotar sem rodar testes:

```bash
./mvnw -DskipTests package
```

### 7. Execução

Inicie a aplicação:

```bash
./mvnw spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

---

## Screenshots

### Swagger UI

![Swagger UI](../docs/images/swagger.png)

Interface interativa gerada pelo Springdoc OpenAPI para explorar endpoints, visualizar schemas e testar requisições autenticadas.

### RabbitMQ Management

![RabbitMQ Management](../docs/images/rabbitmq-queues.png)

Painel web do RabbitMQ para inspecionar exchanges, filas, bindings, mensagens e taxa de consumo/publicação.

---

## Swagger

A documentação OpenAPI está disponível via Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

O contrato OpenAPI em JSON fica disponível em:

```text
http://localhost:8080/v3/api-docs
```

No Swagger é possível:

- Visualizar todos os endpoints documentados.
- Conferir exemplos de request e response.
- Validar modelos de entrada e saída.
- Testar endpoints protegidos usando autenticação Bearer.

Para endpoints protegidos, clique em **Authorize** e informe:

```text
Bearer <access-token>
```

---

## RabbitMQ Management

Com o Docker Compose em execução, acesse:

```text
http://localhost:15672
```

Credenciais:

```text
Usuário: guest
Senha: guest
```

No painel é possível inspecionar:

- Exchange `ecommerce.topic`
- Filas criadas pela aplicação
- Bindings por routing key
- Mensagens publicadas e consumidas
- Taxa de publicação/consumo

---

## Exemplos de autenticação

### Login JWT

```http
POST /api/v1/auth/login HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{
  "email": "joao.pablo@email.com",
  "password": "Senha@123"
}
```

Resposta esperada:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "550e8400-e29b-41d4-a716-446655440000",
  "type": "Bearer",
  "expiresIn": 86400000
}
```

### Bearer Token

Use o token retornado no login em endpoints protegidos:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
```

Exemplo com `curl`:

```bash
curl -H "Authorization: Bearer <access-token>" \
  http://localhost:8080/api/v1/products
```

### Google OAuth2

Para iniciar o login com Google:

```text
GET http://localhost:8080/api/v1/auth/google
```

Também é possível usar o fluxo padrão do Spring Security:

```text
GET http://localhost:8080/oauth2/authorization/google
```

---

## Exemplos de endpoints

Os endpoints abaixo refletem os controllers existentes e podem ser explorados com mais detalhes no Swagger.

### Auth

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/v1/auth/register` | Registra um novo usuário |
| `POST` | `/api/v1/auth/login` | Autentica usuário e retorna tokens |
| `POST` | `/api/v1/auth/refresh` | Renova access token com refresh token |
| `POST` | `/api/v1/auth/logout` | Revoga refresh token |
| `GET` | `/api/v1/auth/google` | Inicia autenticação com Google |

### Products

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/v1/products` | Cria produto |
| `GET` | `/api/v1/products` | Lista produtos com paginação/filtros |
| `GET` | `/api/v1/products/{id}` | Busca produto por ID |
| `PUT` | `/api/v1/products/{id}` | Atualiza produto |
| `DELETE` | `/api/v1/products/{id}` | Remove produto |

### Orders

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/v1/orders` | Cria pedido |
| `GET` | `/api/v1/orders` | Lista pedidos |
| `GET` | `/api/v1/orders/{id}` | Busca pedido por ID |
| `GET` | `/api/v1/orders/user/{userId}` | Lista pedidos de um usuário |
| `PATCH` | `/api/v1/orders/{id}/status` | Atualiza status do pedido |
| `PATCH` | `/api/v1/orders/{id}/cancel` | Cancela pedido |

### Payments

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/v1/payments` | Cria pagamento |
| `GET` | `/api/v1/payments/{id}` | Busca pagamento por ID |
| `PATCH` | `/api/v1/payments/{id}/approve` | Aprova pagamento |
| `PATCH` | `/api/v1/payments/{id}/reject` | Rejeita pagamento |
| `PATCH` | `/api/v1/payments/{id}/refund` | Estorna pagamento |

### Shipping

| Método | Endpoint | Descrição |
| --- | --- | --- |
| `POST` | `/api/v1/shippings` | Cria envio |
| `GET` | `/api/v1/shippings` | Lista envios |
| `GET` | `/api/v1/shippings/{id}` | Busca envio por ID |
| `GET` | `/api/v1/shippings/order/{orderId}` | Busca envio por pedido |
| `PATCH` | `/api/v1/shippings/{id}/ship` | Marca como enviado |
| `PATCH` | `/api/v1/shippings/{id}/out-for-delivery` | Marca como saiu para entrega |
| `PATCH` | `/api/v1/shippings/{id}/deliver` | Marca como entregue |

---

## Testes

| Comando | Quando usar |
| --- | --- |
| `./mvnw test` | Executa a suíte de testes sem empacotar a aplicação |
| `./mvnw clean install` | Limpa, compila, testa e instala o artefato no repositório local Maven |
| `./mvnw -DskipTests package` | Gera o pacote da aplicação sem executar testes |

```bash
./mvnw test
```

```bash
./mvnw clean install
```

```bash
./mvnw -DskipTests package
```

> Alguns testes de integração dependem de serviços locais, como PostgreSQL e Redis, conforme configuração do ambiente.

---

## Roadmap

### Mensageria

- [ ] Dead Letter Queues para eventos não processados
- [ ] Retry com backoff para consumidores RabbitMQ
- [ ] Outbox Pattern para publicação transacional confiável
- [ ] Idempotência em consumers
- [ ] Estratégia de versionamento de eventos

### Observabilidade

- [ ] Correlation ID
- [ ] Logs estruturados
- [ ] Métricas de aplicação
- [ ] Dashboards operacionais
- [ ] Tracing distribuído

### Infraestrutura

- [ ] Containerização da aplicação
- [ ] Health checks para serviços externos
- [ ] Profiles específicos por ambiente
- [ ] Configuração centralizada por ambiente

### Arquitetura

- [ ] Refinar contratos de eventos
- [ ] Evoluir consumidores observacionais para casos de uso reais
- [ ] Avaliar extração futura de módulos para microsserviços
- [ ] Definir limites transacionais para fluxos assíncronos

### Deploy

- [ ] Pipeline CI/CD
- [ ] Deploy em Kubernetes
- [ ] Deploy em cloud provider
- [ ] Estratégia de rollback

### Testes

- [ ] Testcontainers para PostgreSQL, Redis e RabbitMQ
- [ ] Testes de contrato para API REST
- [ ] Testes de integração para eventos RabbitMQ
- [ ] Testes end-to-end dos fluxos principais

---

## Autor

Desenvolvido por **João Pablo**.

Backend developer com foco em Java, Spring Boot, APIs REST, segurança, persistência relacional e arquitetura de sistemas corporativos. Este projeto foi criado para demonstrar domínio técnico em construção de APIs, modularização, documentação, mensageria e evolução incremental de arquitetura.

Conecte-se:

- [GitHub](https://github.com/J040Pablo)
- [LinkedIn](https://linkedin.com/in/joaopablodelgadogomes)

