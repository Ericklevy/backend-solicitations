# 🚀 Sistema de Solicitações de Atendimento

API REST para gerenciamento de solicitações de atendimento com fluxo multi-step, autenticação JWT, e busca avançada com Elasticsearch.

## 📋 Índice

- [Tecnologias](#-tecnologias)
- [Arquitetura](#-arquitetura)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e Execução](#-instalação-e-execução)
- [Endpoints](#-endpoints)
- [Fluxo de Trabalho](#-fluxo-de-trabalho)
- [Testes](#-testes)
- [Padrões Implementados](#-padrões-implementados)
- [Documentação API](#-documentação-api)

---

## 🛠️ Tecnologias

- **Java 21** + **Spring Boot 3.2**
- **PostgreSQL 15** - Persistência transacional
- **Elasticsearch 8.11** - Busca e indexação
- **JWT** - Autenticação e autorização
- **Docker** + **Docker Compose** - Containerização
- **Resilience4j** - Circuit Breaker e Retry
- **Caffeine** - Cache local
- **AOP** - Auditoria transversal
- **OpenAPI/Swagger** - Documentação
- **Testcontainers** - Testes de integração
- **Micrometer/Prometheus** - Métricas avançadas

---

## 🏗️ Arquitetura

O sistema foi desenhado com foco em isolamento de regras de negócio, facilidade de manutenção e alta performance.

### Hexagonal Architecture (Ports & Adapters)

```text
┌─────────────────────────────────────────────────────────────┐
│                     INTERFACES                              │
│           (Controllers, DTOs, Exception Handlers)          │
│                     ▲          ▼                            │
│                     │          │                            │
│                     │   PORTS  │                            │
│                     │    IN    │                            │
│                     │          │                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                 APPLICATION                          │  │
│  │           (Use Cases, Services, DTOs)               │  │
│  └──────────────────────────────────────────────────────┘  │
│                     ▲          ▼                            │
│                     │   PORTS  │                            │
│                     │    OUT   │                            │
│                     │          │                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │                   DOMAIN                             │  │
│  │         (Entities, Enums, Domain Services)           │  │
│  └──────────────────────────────────────────────────────┘  │
│                     ▲          ▼                            │
│                     │          │                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │               INFRASTRUCTURE                         │  │
│  │   (Persistence, Search, External Services, Security) │  │
│  └──────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
```

### Por que cada camada?

- **Domain**: Contém regras de negócio puras, sem dependências de infraestrutura (ex: a entidade valida se o `priority=HIGH` possui valor maior que 100).
- **Application**: Coordena as entidades do domínio para executar operações via Casos de Uso.
- **Infrastructure**: Implementa os *Ports* de acesso a banco (JPA), Elasticsearch, ViaCEP e JWT.
- **Interfaces**: Expõe os endpoints REST e lida com o *Bean Validation*.

---

## 📦 Pré-requisitos

- **Docker** e **Docker Compose** instalados (Essencial para subir tudo facilmente).
- *Java 21 e Maven (Apenas se quiser rodar localmente fora do Docker).*

---

## 🚀 Instalação e Execução (Passo a Passo)

A forma mais simples de validar a entrega é através do Docker.

### 1. Subir os Containers
Estando na raiz do repositório, rode:
```bash
docker-compose up -d
```
> **Nota**: O banco de dados é criado e as migrações (criação de tabelas) são executadas automaticamente na inicialização da aplicação (via Hibernate hbm2ddl). Além disso, um **seeder injeta automaticamente o usuário admin inicial (`admin@system.com`)** no banco, garantindo que você não precise rodar nenhum script `.sql` manualmente.

### 2. Aguarde a inicialização (~30 segundos)
Você pode acompanhar os logs usando:
```bash
docker-compose logs -f app
```

### 3. Acessar a Aplicação
- **API**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui.html

### 4. Teste Rápido via Swagger UI (Passo a Passo)

Siga este fluxo no Swagger (`http://localhost:8081/swagger-ui.html`) para testar o ciclo de vida completo de uma solicitação.

**A. Popular e Configurar o Sistema (ADMIN)**
1. **Login:** Abra `POST /auth/login`. Body: `{ "email": "admin@system.com", "password": "Admin@123" }`. Copie o token retornado.
2. **Autorizar:** Suba a página, clique no botão 🔓 **Authorize**, cole o token e clique em **Authorize**.
3. **Popular Dados:** Abra `POST /api/populate` e clique em Execute. (Gera 20 solicitações, clientes e analistas).
4. **Criar Analista:** Abra `POST /api/admin/users`. Body:
   `{ "name": "Ana Analista", "email": "ana@analista.com", "password": "Senha@123", "role": "ANALYST", "coverageStates": ["SP", "RJ", "MG"] }`
   ⚠️ **Anote o `id` retornado (ID DO USUÁRIO)**.
5. **Atualizar Cobertura:** Abra `PUT /api/admin/users/{id}/coverage`. Use o `id` anotado acima e Body `["SP", "RJ", "MG", "RS"]`.

**B. Criar Solicitação (CLIENTE)**
6. **Logout e Login:** Clique em 🔓 **Authorize** -> Logout. Depois em `POST /auth/register` crie um cliente:
   `{ "name": "João Cliente", "email": "joao2@cliente.com", "password": "Senha@123" }`. Autorize o Swagger com o novo token retornado.
7. **Criar Rascunho:** Abra `POST /api/solicitations` (01 - Create). Execute com body vazio `{}`.
   ⚠️ **Anote o `id` retornado (ID DA SOLICITAÇÃO)**. Use este ID nos próximos passos.
8. **Step 1:** Abra `PATCH /api/solicitations/{id}/step1`. Body:
   `{ "serviceType": "INSTALLATION", "title": "Instalar Ar Condicionado", "description": "Instalação na sala de estar, split 12000 BTUs" }`
9. **Step 2:** Abra `PATCH /api/solicitations/{id}/step2`. O ViaCEP preencherá os outros campos automaticamente! Body:
   `{ "cep": "01001000", "number": "100", "complement": "Apto 42" }`
10. **Step 3:** Abra `PATCH /api/solicitations/{id}/step3`. Body:
    `{ "priority": "HIGH", "preferredDate": "2026-12-01", "estimatedValue": 1500.00, "termsAccepted": true }`
11. **Submit:** Abra `POST /api/solicitations/{id}/submit` e execute com body vazio. A solicitação agora foi indexada no Elasticsearch.

**C. Analisar Solicitação (ANALISTA)**
12. **Login:** Logout no botão 🔓 **Authorize**. Em `POST /auth/login`, faça login com o email do analista criado no passo 4 (`ana@analista.com` / `Senha@123`). Autorize com o novo token.
13. **Buscar:** Abra `GET /api/analyst/solicitations/search` e clique em Execute. O Elasticsearch retornará as solicitações (apenas dos estados que a Ana cobre: SP, RJ, MG, RS).
14. **Iniciar Análise:** Abra `POST /api/analyst/solicitations/{id}/start` passando o ID DA SOLICITAÇÃO.
15. **Decidir:** Abra `POST /api/analyst/solicitations/{id}/decide`. Body:
    `{ "decision": "APPROVED", "comment": "Documentação OK. Aprovado." }`

---

## 🔑 Endpoints Principais

| Método | Endpoint | Descrição | Permissão |
|--------|----------|-----------|-----------|
| `POST` | `/auth/register` | Criar cliente | Público |
| `POST` | `/auth/login` | Login | Público |
| `POST` | `/api/solicitations` | Iniciar solicitação | **CLIENT** |
| `PATCH` | `/api/solicitations/{id}/step1` | Salvar Step 1 | **CLIENT** |
| `PATCH` | `/api/solicitations/{id}/step2` | Salvar Step 2 (Integra ViaCEP) | **CLIENT** |
| `PATCH` | `/api/solicitations/{id}/step3` | Salvar Step 3 | **CLIENT** |
| `POST` | `/api/solicitations/{id}/submit` | Enviar para análise | **CLIENT** |
| `GET` | `/api/analyst/solicitations/search` | Buscar com Elasticsearch | **ANALYST/ADMIN** |
| `POST` | `/api/analyst/solicitations/{id}/start` | Iniciar análise | **ANALYST/ADMIN** |
| `POST` | `/api/analyst/solicitations/{id}/decide` | Aprovar ou Rejeitar | **ANALYST/ADMIN** |
| `POST` | `/api/admin/users` | Criar usuário interno (Analista/Admin) | **ADMIN** |

---

## 📝 Fluxo de Trabalho (Workflow)

1. **Cliente:**
   - Cria a conta, gera um token JWT.
   - Cria uma Solicitação (nasce como `DRAFT`).
   - Preenche o *Step 1* (serviço e descrição).
   - Preenche o *Step 2* (ViaCEP traz endereço completo).
   - Preenche o *Step 3* (Confirmação e regras de valor cruzado).
   - Faz o *Submit* (O sistema muda status para `SUBMITTED`, indexa no Elastic e bloqueia edições).

2. **Analista:**
   - Possui cobertura restrita de *UFs* configurada pelo Admin.
   - Usa o Elasticsearch para procurar solicitações na sua fila. **O sistema barra (filtra) buscas que tentem buscar estados que ele não cobre.**
   - Inicia a análise (`IN_REVIEW`) e toma uma decisão (`APPROVED` / `REJECTED`).

---

## 🧪 Testes

Foi construída uma robusta suíte com **158 testes unitários e de integração** cobrindo 100% dos cenários felizes e casos de falha.

Rodar testes via Maven:
```bash
./mvnw test
```
**Cobertura:**
- Domain Models: 95%+
- Application Services: 85%+
- Controllers: 80%+

---

## 🎯 Padrões Implementados em Detalhe

1. **CQRS (Segregação de Leituras)**: A API salva as solicitações no PostgreSQL e, através de eventos, as indexa assincronamente no Elasticsearch. O Analista consome do Elasticsearch, desonerando o banco relacional de query strings custosas.
2. **Event-Driven**: Emissão de eventos assíncronos garantem que operações não bloqueiem a thread principal do cliente.
3. **Strategy Pattern**: A lógica de avanço de steps do cliente e as validações pesadas foram isoladas em estratégias.
4. **Circuit Breaker + Retry**: O acesso à API do ViaCEP está protegido pelo Resilience4j, evitando interrupções em cascata caso o correio falhe.
5. **AOP para Auditoria (Requisito Extra)**: Um `AuditAspect` intercepta os principais métodos do sistema e grava na tabela `audit_logs` todas as interações sensíveis de forma transparente, sem poluir a lógica de negócios.

---

## 📈 Extras Implementados

1. **Tabela de Auditoria**: Usando AOP como requisitado, gerando trilha na tabela.
2. **Métricas (Micrometer + Prometheus)**: 
   - Health: http://localhost:8080/actuator/health
   - Prometheus: http://localhost:8080/actuator/prometheus
3. **Documentação Automática (Swagger)**: http://localhost:8080/swagger-ui.html

---
Feito com dedicação para o desafio de Backend. ☕
