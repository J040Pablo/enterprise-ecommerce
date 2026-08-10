# Enterprise E-commerce

[![Backend CI](https://github.com/J040Pablo/enterprise-ecommerce/actions/workflows/ci.yml/badge.svg)](https://github.com/J040Pablo/enterprise-ecommerce/actions/workflows/ci.yml)
[![Frontend CI](https://github.com/J040Pablo/enterprise-ecommerce/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/J040Pablo/enterprise-ecommerce/actions/workflows/frontend-ci.yml)

Monorepo de e-commerce full-stack: API REST com **Java 21 / Spring Boot** e SPA com **Angular 19**.

## Documentação

| Parte | Descrição | README |
| --- | --- | --- |
| **Backend** | API modular (auth, produtos, carrinho, pedidos, pagamentos, shipping), PostgreSQL, Redis, RabbitMQ, Docker, deploy AWS | backend/README.md |
| **Frontend** | Angular Standalone + Material: catálogo, carrinho, checkout, pedidos e painel admin | frontend/README.md |
| **Screenshots** | Telas da loja, admin e infraestrutura | docs/images/ |

## Objetivos

- Arquitetura limpa e organizada (modular monolith no backend; features no frontend)
- Código escalável e de fácil manutenção
- API RESTful com Spring Boot
- SPA Angular integrada à API
- Segurança utilizando JWT e OAuth2
- Persistência de dados com PostgreSQL
- Testes automatizados (backend e frontend)
- Containerização com Docker
- Documentação da API com OpenAPI/Swagger
- Deploy em ambiente AWS
- Pipelines de CI utilizando GitHub Actions para backend e frontend

## Estrutura do repositório

```text
enterprise-ecommerce/
├── backend/              # Spring Boot API + docker-compose
├── frontend/             # Angular SPA + Dockerfile/nginx
├── docs/images/           # Screenshots compartilhados
└── .github/workflows/
    ├── ci.yml             # CI do backend
    └── frontend-ci.yml    # CI do frontend