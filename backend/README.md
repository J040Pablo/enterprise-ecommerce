# Enterprise E-commerce API

API REST de e-commerce desenvolvida com **Java 21** e **Spring Boot 3.5**, organizada como um **Monólito Modular** e preparada para evolução com autenticação JWT/OAuth2, persistência relacional, cache/token store com Redis, documentação OpenAPI e mensageria com RabbitMQ.

O projeto foi estruturado para demonstrar práticas de backend utilizadas em sistemas corporativos: separação por módulos de negócio, validação de regras no domínio, documentação de contratos, migrações versionadas com Flyway, segurança com Spring Security e base inicial para arquitetura orientada a eventos.

![Java](https://img.shields.io/badge/Java-21-red?style=for-the-badge&logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?style=for-the-badge&logo=springboot)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?style=for-the-badge&logo=postgresql)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-Management-orange?style=for-the-badge&logo=rabbitmq)
![Redis](https://img.shields.io/badge/Redis-Cache%20%2F%20Tokens-red?style=for-the-badge&logo=redis)
![Maven](https://img.shields.io/badge/Maven-Build-blue?style=for-the-badge&logo=apachemaven)
![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=for-the-badge&logo=docker)

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
| Framework | Spring Boot 3.5 | Bootstrap, configuração e runtime |
| Web | Spring Web | APIs REST |
| Segurança | Spring Security | Autenticação e autorização |
| Autenticação | JWT | Access token Bearer |
| OAuth2 | Google OAuth2 Login | Login social |
| Persistência | Spring Data JPA | Repositórios e acesso ao banco |
| Banco de dados | PostgreSQL | Persistência relacional |
| Migrações | Flyway | Versionamento de schema |
| Cache/Tokens | Redis | Suporte a tokens e dados temporários |
| Mensageria | RabbitMQ + Spring AMQP | Eventos assíncronos |
| Documentação | Springdoc OpenAPI | Swagger UI e contrato OpenAPI |
| Build | Maven | Build, testes e empacotamento |
| Infra local | Docker Compose | PostgreSQL, PgAdmin e RabbitMQ |

---

## Arquitetura

O projeto segue o estilo **Modular Monolith**. Em vez de separar prematuramente a aplicação em microsserviços, o código é organizado em módulos de negócio com responsabilidades bem definidas, compartilhando o mesmo processo, banco e deploy.

Essa abordagem mantém a simplicidade operacional de um monólito, mas reduz acoplamento interno e facilita uma futura extração de módulos para serviços independentes, caso exista necessidade real.

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

    Common -.configurações e contratos compartilhados.-> Auth
    Common -.configurações e contratos compartilhados.-> Order
    Common -.configurações e contratos compartilhados.-> Payment
```

---

## Fluxo da aplicação

O fluxo REST principal permanece síncrono. Controllers recebem requisições HTTP, delegam a execução para services, que aplicam regras de negócio e persistem os dados via repositories.

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

---

## Fluxo RabbitMQ

RabbitMQ foi adicionado de forma **aditiva**. O fluxo REST continua sendo a fonte principal de execução; os eventos são publicados em paralelo para observabilidade e evolução futura.

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

A aplicação usa variáveis de ambiente para configurações sensíveis, especialmente JWT e OAuth2.

| Variável | Descrição | Exemplo |
| --- | --- | --- |
| `JWT_SECRET` | Chave secreta para assinatura dos tokens JWT | `change-me-with-a-strong-secret` |
| `GOOGLE_CLIENT_ID` | Client ID do Google OAuth2 | `your-google-client-id` |
| `GOOGLE_CLIENT_SECRET` | Client Secret do Google OAuth2 | `your-google-client-secret` |
| `RABBITMQ_HOST` | Host do RabbitMQ | `localhost` |
| `RABBITMQ_PORT` | Porta AMQP do RabbitMQ | `5672` |
| `RABBITMQ_USERNAME` | Usuário do RabbitMQ | `guest` |
| `RABBITMQ_PASSWORD` | Senha do RabbitMQ | `guest` |

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

| Serviço | Host | Porta |
| --- | --- | --- |
| API | `localhost` | `8080` |
| PostgreSQL | `localhost` | `5432` |
| PgAdmin | `localhost` | `5050` |
| RabbitMQ AMQP | `localhost` | `5672` |
| RabbitMQ Management | `localhost` | `15672` |
| Redis | `localhost` | `6379` |

---

## Docker

O `docker-compose.yml` sobe a infraestrutura principal de desenvolvimento:

- PostgreSQL
- PgAdmin
- RabbitMQ com Management UI

```bash
docker compose up -d
```

Para subir apenas os serviços necessários para banco e mensageria:

```bash
docker compose up -d postgres rabbitmq
```

Para acompanhar logs:

```bash
docker compose logs -f
```

Para parar os containers:

```bash
docker compose down
```

Credenciais locais:

| Serviço | URL | Usuário | Senha |
| --- | --- | --- | --- |
| PgAdmin | `http://localhost:5050` | `admin@admin.com` | `admin` |
| RabbitMQ Management | `http://localhost:15672` | `guest` | `guest` |
| PostgreSQL | `localhost:5432/ecommerce` | `postgres` | `postgres` |

> Redis também é usado pela aplicação e deve estar disponível em `localhost:6379`.

---

## Como executar

Clone o repositório:

```bash
git clone <repository-url>
cd enterprise-ecommerce/backend
```

Configure as variáveis de ambiente criando um arquivo `.env` na raiz do projeto:

```bash
touch .env
```

Use o exemplo da seção **Configuração** como base.

Suba a infraestrutura local:

```bash
docker compose up -d
```

Garanta que o Redis esteja rodando localmente em `localhost:6379`.

Instale dependências e compile:

```bash
./mvnw clean install
```

Execute a aplicação:

```bash
./mvnw spring-boot:run
```

A API ficará disponível em:

```text
http://localhost:8080
```

---

## Swagger

![Swagger](../docs/images/swagger.png)

A documentação OpenAPI pode ser acessada em:

```text
http://localhost:8080/swagger-ui/index.html
```

O contrato OpenAPI em JSON fica disponível em:

```text
http://localhost:8080/v3/api-docs
```

O Swagger inclui documentação dos principais fluxos, exemplos de request/response e autenticação via Bearer Token.

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

Execute todos os testes:

```bash
./mvnw test
```

Execute build completo:

```bash
./mvnw clean install
```

Execute build sem testes:

```bash
./mvnw -DskipTests package
```

> Alguns testes de integração dependem de serviços locais, como PostgreSQL e Redis, conforme configuração do ambiente.

---

## Melhorias futuras

- [ ] Dead Letter Queues para eventos não processados
- [ ] Retry com backoff para consumidores RabbitMQ
- [ ] Outbox Pattern para publicação transacional confiável
- [ ] Idempotência em consumers
- [ ] Correlation ID e tracing distribuído
- [ ] Observabilidade com métricas, logs estruturados e dashboards
- [ ] Testcontainers para testes de integração
- [ ] Pipeline CI/CD
- [ ] Containerização da aplicação
- [ ] Deploy em Kubernetes
- [ ] Deploy em cloud provider
- [ ] Estratégia de versionamento de eventos
- [ ] Separação futura de módulos em microsserviços, se houver necessidade operacional

---

## Autor

Desenvolvido por **João Pablo**.

Projeto criado com foco em boas práticas de backend, arquitetura modular, segurança, documentação de APIs e evolução incremental para sistemas orientados a eventos.

Conecte-se:

- GitHub: `https://github.com/J040Pablo`
- LinkedIn: `https://www.linkedin.com/in/joaopablodelgadogomes/<seu-perfil>`
