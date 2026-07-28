# Enterprise E-commerce

Sistema de e-commerce desenvolvido para demonstrar conhecimentos em Java e Spring Boot.

![Java CI](https://github.com/J040Pablo/enterprise-ecommerce/actions/workflows/ci.yml/badge.svg)

## Objetivos

- Arquitetura limpa e organizada
- Código escalável e de fácil manutenção
- API RESTful com Spring Boot
- Segurança utilizando JWT e OAuth2
- Persistência de dados com PostgreSQL
- Testes automatizados
- Containerização com Docker
- Documentação da API com OpenAPI/Swagger
- Deploy em ambiente AWS
- Pipeline CI/CD utilizando GitHub Actions

## Continuous Integration

O projeto utiliza GitHub Actions para automação do processo de integração contínua.

A cada alteração enviada ao repositório:

- O ambiente Java 21 é configurado automaticamente
- As dependências Maven são carregadas
- A aplicação é compilada
- Todos os testes automatizados são executados

Pipeline:

Git Push
↓
GitHub Actions
↓
Java 21
↓
Maven Verify
↓
Testes Automatizados
↓
Build aprovado