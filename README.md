# Enterprise E-commerce

Monorepo de e-commerce full-stack: API REST com **Java 21 / Spring Boot** e SPA com **Angular 19**.

![Java CI](https://github.com/J040Pablo/enterprise-ecommerce/actions/workflows/ci.yml/badge.svg)

## Documentação

| Parte | Descrição | README |
| --- | --- | --- |
| **Backend** | API modular (auth, produtos, carrinho, pedidos, pagamentos, shipping), PostgreSQL, Redis, RabbitMQ, Docker, deploy AWS | [backend/README.md](backend/README.md) |
| **Frontend** | Angular Standalone + Material: catálogo, carrinho, checkout, pedidos e painel admin | [frontend/README.md](frontend/README.md) |
| **Screenshots** | Telas da loja, admin e infraestrutura | [docs/images/](docs/images/) |

## Objetivos

- Arquitetura limpa e organizada (modular monolith no backend; features no frontend)
- Código escalável e de fácil manutenção
- API RESTful com Spring Boot
- SPA Angular integrada à API
- Segurança utilizando JWT e OAuth2
- Persistência de dados com PostgreSQL
- Testes automatizados (backend)
- Containerização com Docker
- Documentação da API com OpenAPI/Swagger
- Deploy em ambiente AWS
- Pipeline CI/CD utilizando GitHub Actions (backend)

## Estrutura do repositório

```text
enterprise-ecommerce/
├── backend/          # Spring Boot API + docker-compose
├── frontend/         # Angular SPA + Dockerfile/nginx
├── docs/images/      # Screenshots compartilhados
└── .github/workflows # CI (Maven / Java 21)
```

## Quick start

### Backend + infraestrutura

```bash
cd backend
cp .env.example .env
docker compose up -d
./mvnw spring-boot:run
```

Detalhes: [backend/README.md](backend/README.md)

### Frontend (desenvolvimento)

```bash
cd frontend
npm ci
npm start
```

Aplicação em `http://localhost:4200` (API esperada em `http://localhost:8080`).

Detalhes: [frontend/README.md](frontend/README.md)

## Continuous Integration

O projeto utiliza GitHub Actions para integração contínua do **backend**.

A cada alteração enviada ao repositório (branch `main` / PRs):

- O ambiente Java 21 é configurado automaticamente
- As dependências Maven são carregadas
- A aplicação é compilada
- Todos os testes automatizados do backend são executados

```text
Git Push
  ↓
GitHub Actions
  ↓
Java 21
  ↓
Maven Verify (diretório backend/)
  ↓
Testes Automatizados
  ↓
Build aprovado
```

Não há workflow de CI para o frontend neste repositório.
