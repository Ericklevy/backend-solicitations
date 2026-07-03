# 🎫 Sistema de Solicitações de Atendimento

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2-6DB33F?style=for-the-badge&logo=spring)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql)
![Elasticsearch](https://img.shields.io/badge/Elasticsearch-8.11-005571?style=for-the-badge&logo=elasticsearch)
![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker)

API REST para gerenciamento de solicitações de atendimento, com cadastro **multi-step**, autenticação **JWT** baseada em papéis, **busca avançada** com Elasticsearch e integração com o **ViaCEP**.

🔗 **Front-end em produção:** https://frontend-solicitations-4zht.vercel.app
🔗 **API em produção:** https://backend-solicitations-api.onrender.com
🔗 **Swagger (produção):** https://backend-solicitations-api.onrender.com/swagger-ui/index.html

---

## ⚡ Teste rápido em produção (recomendado para avaliação)

> A API está hospedada no plano gratuito do **Render**, que "dorme" após ~15 minutos sem uso. Por isso, **siga os 2 passos abaixo antes de testar** — leva menos de 1 minuto.

**Passo 1 — Acorde o backend**

Clique no link abaixo e aguarde a resposta (pode levar de 30 a 60 segundos na primeira chamada):

👉 **[Acordar API — Health Check](https://backend-solicitations-api.onrender.com/actuator/health)**

Quando a página retornar `{"status":"UP"}`, a API está pronta para uso.

**Passo 2 — Acesse o Front-end**

Com o backend já "acordado", acesse a interface pronta para uso, sem precisar configurar nada:

👉 **[Acessar Front-end (Vercel)](https://frontend-solicitations-4zht.vercel.app)**

**Alternativa — testar direto pelo Swagger (sem front-end):**

👉 **[Swagger UI em produção](https://backend-solicitations-api.onrender.com/swagger-ui/index.html)**

Basta seguir o mesmo [guia passo a passo do Swagger](#-guia-rápido-via-swagger) descrito mais abaixo, usando essa URL em vez do `localhost`.

> Credencial padrão de admin já criada em produção: `admin@system.com` / `Admin@123`

---

## 📋 Índice

- [Teste rápido em produção](#-teste-rápido-em-produção-recomendado-para-avaliação)
- [Visão geral do domínio](#-visão-geral-do-domínio)
- [Tecnologias](#️-tecnologias)
- [Arquitetura](#️-arquitetura)
- [Estrutura de pastas](#-estrutura-de-pastas)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação e execução (local)](#-instalação-e-execução-local)
- [Guia rápido via Swagger](#-guia-rápido-via-swagger)
- [Exemplos via cURL](#-exemplos-via-curl)
- [Endpoints principais](#-endpoints-principais)
- [Fluxo de trabalho (workflow)](#-fluxo-de-trabalho-workflow)
- [Regras de negócio-chave](#-regras-de-negócio-chave)
- [Testes](#-testes)
- [Ferramentas e scripts úteis](#-ferramentas-e-scripts-úteis-docker)
- [Padrões implementados](#-padrões-implementados-em-detalhe)
- [Observabilidade e documentação](#-observabilidade-e-documentação)
- [Extras implementados](#-extras-implementados)
- [Front-end integrado](#-front-end-integrado)

---

## 🧭 Visão geral do domínio

Uma **Solicitação** é criada por um **Cliente** e preenchida em **3 etapas** (podendo salvar e continuar depois). Um **Analista** avalia as solicitações apenas dos **estados (UF)** sob sua cobertura, e um **Admin** administra usuários e permissões.

| Perfil | Pode fazer |
|---|---|
| **CLIENT** | Se cadastrar sozinho · criar/editar apenas as próprias solicitações · salvar rascunho e continuar depois · enviar para análise |
| **ANALYST** | Listar e analisar solicitações apenas das UFs sob sua responsabilidade · não cria usuários |
| **ADMIN** | Acesso total · único que cria usuários internos (`ANALYST`/`ADMIN`) · define as UFs de cobertura de cada analista |

---

## 🛠️ Tecnologias

| Categoria | Tecnologia |
|---|---|
| Linguagem / Framework | Java 21 + Spring Boot 3.2 |
| Persistência | PostgreSQL 15 |
| Busca / indexação | Elasticsearch 8.11 |
| Autenticação | JWT |
| Resiliência | Resilience4j (Circuit Breaker + Retry) |
| Cache | Caffeine |
| Auditoria | Spring AOP |
| Documentação | OpenAPI / Swagger |
| Testes | JUnit 5 + Testcontainers |
| Observabilidade | Micrometer / Prometheus |
| Infraestrutura | Docker + Docker Compose |
| Front-end (demo) | Vercel |
| Hospedagem da API | Render |

---

## 🏗️ Arquitetura

O sistema segue **Arquitetura Hexagonal (Ports & Adapters)**, isolando as regras de negócio de detalhes de infraestrutura.

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

**Por que cada camada?**

- **Domain** — regras de negócio puras, sem dependência de infraestrutura (ex.: a entidade valida se `priority=HIGH` possui valor maior que 100).
- **Application** — coordena as entidades do domínio para executar operações via Casos de Uso.
- **Infrastructure** — implementa os *Ports* de acesso a banco (JPA), Elasticsearch, ViaCEP e JWT.
- **Interfaces** — expõe os endpoints REST e lida com o *Bean Validation*.

---

## 📁 Estrutura de pastas

```
src/main/java/com/empresa/solicitacoes
├── domain/            # Entidades, enums, regras de negócio puras
├── application/        # Casos de uso, services, DTOs de aplicação
├── infrastructure/
│   ├── persistence/    # Repositórios JPA, mapeamentos
│   ├── search/          # Integração Elasticsearch
│   ├── external/        # Cliente ViaCEP (Resilience4j)
│   └── security/        # JWT, filtros, configuração de segurança
├── interfaces/
│   ├── controller/      # Endpoints REST
│   ├── dto/              # Request/Response DTOs
│   └── exception/       # Handlers globais de exceção
└── aop/                 # Aspecto de auditoria (@Audit)

scripts/
├── run-tests.bat / .sh          # Roda a suíte de testes em container isolado
└── view-audit-logs.bat / .sh    # Exibe as últimas ações da tabela audit_logs
```

---

## 📦 Pré-requisitos

> Só necessário para rodar **localmente**. Para avaliar o projeto sem instalar nada, use a seção [Teste rápido em produção](#-teste-rápido-em-produção-recomendado-para-avaliação) acima.

- **Docker** e **Docker Compose** instalados (essencial para subir tudo facilmente).
- *Java 21 e Maven (apenas se quiser rodar localmente fora do Docker).*

---

## 🚀 Instalação e Execução (local)

### 1. Subir os containers

Estando na raiz do repositório, rode:

```bash
docker-compose up -d
```

> **Nota**: o banco de dados é criado e as migrações (criação de tabelas) são executadas automaticamente na inicialização da aplicação (via Hibernate `hbm2ddl`). Além disso, um **seeder injeta automaticamente o usuário admin inicial (`admin@system.com`)** no banco, garantindo que você não precise rodar nenhum script `.sql` manualmente.

### 2. Aguarde a inicialização (~30 segundos)

Você pode acompanhar os logs usando:

```bash
docker-compose logs -f app
```

### 3. Acessar a aplicação

| Recurso | URL local | URL produção |
|---|---|---|
| API | http://localhost:8081 | https://backend-solicitations-api.onrender.com |
| Swagger UI | http://localhost:8081/swagger-ui.html | https://backend-solicitations-api.onrender.com/swagger-ui/index.html |
| Health check | http://localhost:8081/actuator/health | https://backend-solicitations-api.onrender.com/actuator/health |
| Métricas Prometheus | http://localhost:8081/actuator/prometheus | https://backend-solicitations-api.onrender.com/actuator/prometheus |
| Front-end | — | https://frontend-solicitations-4zht.vercel.app |

---

## 🧪 Guia rápido via Swagger

Siga este fluxo (local ou em produção) para testar o ciclo de vida completo de uma solicitação. Basta trocar `http://localhost:8081` pela URL de produção, se preferir.

### A. Popular e configurar o sistema (ADMIN)

1. **Login** — `POST /auth/login`. Body: `{ "email": "admin@system.com", "password": "Admin@123" }`. Copie apenas o valor do campo `token` retornado no JSON.
2. **Autorizar** — suba a página, clique no botão 🔓 **Authorize**, cole o token e clique em **Authorize**.
3. **Popular dados** — `POST /api/populate` e clique em Execute (gera 20 solicitações, clientes e analistas).
4. **Criar analista** — `POST /api/admin/users`. Body:
   ```json
   { "name": "Ana Analista", "email": "ana@analista.com", "password": "Senha@123", "role": "ANALYST", "coverageStates": ["SP", "RJ", "MG"] }
   ```
   ⚠️ Anote o `id` retornado (**ID DO USUÁRIO**).
5. **Atualizar cobertura** — `PUT /api/admin/users/{id}/coverage`. Use o `id` anotado acima e Body `["SP", "RJ", "MG", "RS"]`.

### B. Criar solicitação (CLIENTE)

6. **Logout e login** — clique em 🔓 **Authorize** → Logout. Depois, em `POST /auth/register`, crie um cliente:
   ```json
   { "name": "João Cliente", "email": "joao2@cliente.com", "password": "Senha@123" }
   ```
   Copie apenas o valor do campo `token` e autorize o Swagger com ele.
7. **Criar rascunho** — `POST /api/solicitations` com body vazio `{}`. ⚠️ Anote o `id` retornado (**ID DA SOLICITAÇÃO**). Use este ID nos próximos passos.
8. **Step 1** — `PATCH /api/solicitations/{id}/step1`. Body:
   ```json
   { "serviceType": "INSTALLATION", "title": "Instalar Ar Condicionado", "description": "Instalação na sala de estar, split 12000 BTUs" }
   ```
9. **Step 2** — `PATCH /api/solicitations/{id}/step2` (o ViaCEP preenche o restante automaticamente). Body:
   ```json
   { "cep": "01001000", "number": "100", "complement": "Apto 42" }
   ```
10. **Step 3** — `PATCH /api/solicitations/{id}/step3`. Body:
    ```json
    { "priority": "HIGH", "preferredDate": "2026-12-01", "estimatedValue": 1500.00, "termsAccepted": true }
    ```
11. **Submit** — `POST /api/solicitations/{id}/submit` (body vazio). A solicitação é indexada no Elasticsearch.

### C. Analisar solicitação (ANALISTA)

12. **Login** — logout no 🔓 **Authorize**, depois login com o email do analista criado no passo 4 (`ana@analista.com` / `Senha@123`). Copie o valor de `token` (o JSON também retorna `id` e `name`, usados pelo front-end). Autorize com o novo token.
13. **Buscar** — `GET /api/analyst/solicitations/search` e clique em Execute (retorna apenas UFs cobertas por Ana: SP, RJ, MG, RS).
14. **Iniciar análise** — `POST /api/analyst/solicitations/{id}/start` passando o **ID DA SOLICITAÇÃO**.
15. **Decidir** — `POST /api/analyst/solicitations/{id}/decide`. Body:
    ```json
    { "decision": "APPROVED", "comment": "Documentação OK. Aprovado." }
    ```

---

## 💻 Exemplos via cURL

**Login (produção):**
```bash
curl -X POST https://backend-solicitations-api.onrender.com/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@system.com", "password": "Admin@123"}'
```

**Criar solicitação (com token):**
```bash
curl -X POST https://backend-solicitations-api.onrender.com/api/solicitations \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

**Buscar solicitações (Analista) com filtros:**
```bash
curl -X GET "https://backend-solicitations-api.onrender.com/api/analyst/solicitations/search?status=SUBMITTED&priority=HIGH&page=0&size=10&sort=submittedAt,desc" \
  -H "Authorization: Bearer $TOKEN"
```

> Para rodar localmente, basta trocar a base URL por `http://localhost:8081`.

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
| `PUT` | `/api/admin/users/{id}/coverage` | Definir UFs de cobertura do analista | **ADMIN** |

---

## 📝 Fluxo de Trabalho (Workflow)

**Cliente:**
- Cria a conta, gera um token JWT.
- Cria uma Solicitação (nasce como `DRAFT`).
- Preenche o *Step 1* (serviço e descrição).
- Preenche o *Step 2* (ViaCEP traz endereço completo).
- Preenche o *Step 3* (confirmação e regras de valor cruzado).
- Faz o *Submit* (o sistema muda status para `SUBMITTED`, indexa no Elastic e bloqueia edições).

**Analista:**
- Possui cobertura restrita de *UFs* configurada pelo Admin.
- Usa o Elasticsearch para procurar solicitações na sua fila. **O sistema barra (filtra) buscas que tentem buscar estados que ele não cobre.**
- Inicia a análise (`IN_REVIEW`) e toma uma decisão (`APPROVED` / `REJECTED`).

```
DRAFT ──submit──▶ SUBMITTED ──start──▶ IN_REVIEW ──decide──▶ APPROVED
                                                    └────────▶ REJECTED
```

---

## ⚖️ Regras de negócio-chave

- Uma solicitação só pode ser editada pelo **CLIENT dono**, e apenas enquanto `status = DRAFT`.
- `POST /auth/register` cria **apenas CLIENT**; somente ADMIN cria `ANALYST`/`ADMIN`.
- CEP inválido ou falha no ViaCEP **bloqueia a conclusão do Step 2** (mas permite salvar rascunho).
- Se `priority = HIGH`, então `estimatedValue >= 100`.
- `preferredDate` não pode ser uma data no passado.
- O `submit` só é aceito com os 3 steps **completos e coerentes**; falhas retornam quais campos/etapas estão pendentes.
- ANALYST nunca visualiza ou decide sobre UFs fora do seu `coverageStates`, mesmo que solicite explicitamente via filtro.

---

## 🧪 Testes

Foi construída uma robusta suíte com **158 testes unitários e de integração** cobrindo os cenários felizes e casos de falha.

### Testes via Docker (recomendado)

Não quer instalar o Maven ou o Java na sua máquina? Sem problemas! Há um script pronto que baixa um container isolado do Maven, roda todos os testes do projeto e se autodestrói ao final:

| Sistema | Comando |
|---|---|
| Windows | `.\scripts\run-tests.bat` |
| Linux / Mac / Git Bash | `./scripts/run-tests.sh` |

> **Nota técnica:** o setup de testes no Docker resolve nativamente os desafios de Docker-in-Docker e montagem de volumes, mapeando sockets e contornando limitações de bridge network no Windows/Mac (via `host.docker.internal` e desabilitação do Ryuk) para rodar os Testcontainers sem fricção.

### Testes via Maven local

Se preferir rodar localmente com sua própria instalação Java/Maven:

```bash
./mvnw test
```

**Cobertura:**

| Camada | Cobertura |
|---|---|
| Domain Models | 95%+ |
| Application Services | 85%+ |
| Controllers | 80%+ |

---

## 🧰 Ferramentas e scripts úteis (Docker)

Para facilitar a vida de quem vai avaliar o projeto, o repositório traz scripts prontos na pasta `scripts/` que executam comandos Docker mais complexos de forma simples:

| Script | O que faz | Windows | Linux / Mac |
|---|---|---|---|
| **Rodar testes** | Sobe um container Maven isolado, roda toda a suíte de testes e se autodestrói ao final | `.\scripts\run-tests.bat` | `./scripts/run-tests.sh` |
| **Visualizar auditoria** | Conecta no PostgreSQL do Docker e exibe as últimas 20 ações registradas na tabela `audit_logs` | `.\scripts\view-audit-logs.bat` | `./scripts/view-audit-logs.sh` |

---

## 🎯 Padrões Implementados em Detalhe

1. **CQRS (Segregação de Leituras)** — a API salva as solicitações no PostgreSQL e, através de eventos, as indexa assincronamente no Elasticsearch. O Analista consome do Elasticsearch, desonerando o banco relacional de queries custosas.
2. **Event-Driven** — a emissão de eventos assíncronos garante que operações não bloqueiem a thread principal do cliente.
3. **Strategy Pattern** — a lógica de avanço de steps do cliente e as validações pesadas foram isoladas em estratégias.
4. **Circuit Breaker + Retry** — o acesso à API do ViaCEP está protegido pelo Resilience4j, evitando interrupções em cascata caso o serviço falhe.
5. **AOP para Auditoria (requisito extra)** — um `AuditAspect` intercepta os principais métodos do sistema e grava na tabela `audit_logs` todas as interações sensíveis, de forma transparente e sem poluir a lógica de negócios.

---

## 📊 Observabilidade e documentação

| Recurso | Local | Produção |
|---|---|---|
| Health check | http://localhost:8081/actuator/health | https://backend-solicitations-api.onrender.com/actuator/health |
| Métricas Prometheus | http://localhost:8081/actuator/prometheus | https://backend-solicitations-api.onrender.com/actuator/prometheus |
| Swagger / OpenAPI | http://localhost:8081/swagger-ui.html | https://backend-solicitations-api.onrender.com/swagger-ui/index.html |

---

## 📈 Extras Implementados

1. **Tabela de auditoria** — usando AOP como requisitado, gerando trilha na tabela `audit_logs` (consultável via `scripts/view-audit-logs`).
2. **Métricas (Micrometer + Prometheus)** — endpoints de health e métricas expostos via Actuator, tanto local quanto em produção.
3. **Documentação automática (Swagger)** — especificação OpenAPI navegável e testável, disponível local e em produção.
4. **Front-end de demonstração** — interface completa hospedada na Vercel, consumindo a API em produção.

---

## 💻 Front-end integrado

Para complementar este projeto de backend e comprovar o funcionamento da API em um cenário real, foi construído um **front-end interativo** que consome diretamente esta API na nuvem.

⚠️ **Importante:** como o backend está hospedado no plano gratuito do Render, ele "dorme" após 15 minutos sem uso. Antes de usar o front-end, siga os passos da seção [Teste rápido em produção](#-teste-rápido-em-produção-recomendado-para-avaliação) no início deste README para "acordar" a API primeiro.

👉 [Acordar API (Health Check)](https://backend-solicitations-api.onrender.com/actuator/health)  
👉 [Acessar Front-end (Vercel)](https://frontend-solicitations-4zht.vercel.app)

> **Código-fonte do Front-end:** Caso queira rodar o Front-end localmente ou avaliar o código-fonte da interface, acesse o repositório oficial do Front-end em: `[COLE_O_LINK_DO_SEU_REPOSITORIO_FRONTEND_AQUI]`. Lá, basta seguir os passos detalhados (com telas) no README do Front-end para testar.

---

Feito com dedicação para o desafio de Backend. ☕
