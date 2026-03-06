# 🏦 Digital Bank API

> Projeto de portfólio de banco digital com **Java 21**, **Spring Boot 3.3**, **Apache Kafka**, **Redis**, **AWS S3** e **Testcontainers**. Desenvolvido para candidatura a vagas de Dev Java Júnior.

[![Java](https://img.shields.io/badge/Java-21-orange)](https://www.java.com)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen)](https://spring.io/projects/spring-boot)
[![Kafka](https://img.shields.io/badge/Apache%20Kafka-7.6-black)](https://kafka.apache.org)
[![Redis](https://img.shields.io/badge/Redis-7-red)](https://redis.io)
[![AWS](https://img.shields.io/badge/AWS-S3-yellow)](https://aws.amazon.com/s3)
[![Docker](https://img.shields.io/badge/Docker-Ready-blue)](https://www.docker.com)
[![License](https://img.shields.io/badge/License-MIT-lightgrey)](LICENSE)

---

## 📋 Sobre o Projeto

API REST completa de banco digital com autenticação JWT, gerenciamento de contas, transações financeiras, mensageria assíncrona com Kafka e armazenamento de comprovantes no AWS S3.

---

## 🚀 Stack Completa

| Tecnologia | Versão | Uso no Projeto |
|---|---|---|
| Java | 21 | Linguagem principal, Records, Pattern Matching |
| Spring Boot | 3.3.4 | Framework principal |
| Spring Security + JWT | 6.x | Autenticação e autorização stateless |
| Spring Data JPA | 3.x | Persistência com PostgreSQL |
| PostgreSQL | 16 | Banco de dados principal |
| Flyway | 10.x | Migrations versionadas |
| **Apache Kafka** | 7.6 | **Eventos de transação assíncronos** |
| **Redis** | 7 | **Cache de contas (TTL 10min)** |
| **AWS S3** | SDK v2 | **Armazenamento de comprovantes** |
| Spring Actuator | 3.x | Health check e métricas |
| **Testcontainers** | 1.19 | **Testes com PostgreSQL e Kafka reais** |
| Swagger/OpenAPI | 3.x | Documentação interativa |
| Docker + Compose | Latest | Todos os serviços containerizados |
| Lombok | Latest | Redução de boilerplate |
| JUnit 5 + Mockito | 5.x | Testes unitários |

---

## 🏗️ Arquitetura

```
src/
├── config/
│   ├── AwsS3Config.java          # Config AWS S3 (LocalStack ou AWS real)
│   ├── RedisConfig.java          # Config cache Redis (TTL, serialização)
│   ├── SecurityConfig.java       # Spring Security + JWT
│   ├── UserDetailsServiceConfig.java
│   └── OpenApiConfig.java
├── controller/                   # Endpoints REST
├── dto/
│   ├── request/                  # Records Java 21 de entrada
│   └── response/                 # Records Java 21 de saída
├── entity/                       # Entidades JPA
├── enums/                        # Tipos enumerados
├── exception/handler/            # Handler global de erros
├── kafka/
│   ├── event/TransactionEvent.java    # Evento publicado no Kafka
│   ├── producer/TransactionEventProducer.java  # Publica eventos
│   └── consumer/NotificationConsumer.java      # Consome e processa
├── repository/                   # Interfaces JPA
├── security/filter/              # JWT Filter
├── service/impl/
│   ├── AuthService.java
│   ├── AccountService.java       # @Cacheable com Redis
│   ├── TransactionService.java   # Publica Kafka + salva S3
│   └── S3Service.java            # Comprovantes no AWS S3
└── resources/
    ├── application.yml           # Config base
    ├── application-dev.yml       # Perfil dev (LocalStack)
    ├── application-prod.yml      # Perfil prod (AWS real)
    └── logback-spring.xml        # Logs estruturados por perfil
```

### Fluxo de uma Transação

```
POST /api/accounts/{id}/transactions
         │
         ▼
  TransactionService
    ├── Valida conta e saldo
    ├── Executa operação no banco
    ├── S3Service → salva comprovante JSON no S3
    └── Kafka Producer → publica TransactionEvent
                              │
                              ▼
                    NotificationConsumer
                    └── processa notificação (log/email/SMS)
```

---

## ⚡ Funcionalidades

### 🔐 Autenticação
- Registro com validação de CPF e email únicos
- Login com JWT (24h de validade)

### 🏦 Contas
- Criar conta corrente ou poupança
- Listar contas, consultar saldo (**cacheado no Redis**)
- Bloquear / desbloquear conta

### 💰 Transações
- **Depósito, Saque, Transferência, PIX**
- Cada transação gera um **evento no Kafka**
- **Comprovante JSON salvo automaticamente no S3**
- Extrato paginado

### 📊 Monitoramento (Actuator)
- `GET /actuator/health` — status da aplicação
- `GET /actuator/info` — informações do app
- `GET /actuator/metrics` — métricas JVM e HTTP

---

## ▶️ Como Executar

### Pré-requisitos
- Docker e Docker Compose instalados

### Subir tudo com Docker
```bash
git clone https://github.com/seu-usuario/digital-bank.git
cd digital-bank

docker-compose up -d

# Acompanhar logs
docker-compose logs -f app
```

Serviços disponíveis:
| Serviço | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Actuator Health | http://localhost:8080/actuator/health |
| LocalStack S3 | http://localhost:4566 |
| Redis | localhost:6379 |
| Kafka | localhost:9092 |

---

## 📖 Exemplos de Uso

**1. Registrar usuário:**
```json
POST /api/auth/register
{
  "name": "João Silva",
  "cpf": "123.456.789-09",
  "email": "joao@email.com",
  "password": "senha123!"
}
```

**2. Login:**
```json
POST /api/auth/login
{
  "email": "joao@email.com",
  "password": "senha123!"
}
```

**3. Criar conta:**
```json
POST /api/accounts
Authorization: Bearer {token}
{ "accountType": "CHECKING" }
```

**4. PIX (gera evento Kafka + salva no S3):**
```json
POST /api/accounts/{accountId}/transactions
Authorization: Bearer {token}
{
  "amount": 250.00,
  "type": "PIX",
  "targetAccountNumber": "12345678-9",
  "description": "PIX para amigo"
}
```

---

## 🧪 Testes

```bash
# Roda todos os testes (Testcontainers sobe PostgreSQL + Kafka reais no Docker)
./mvnw test
```

**Estrutura de testes:**
- **Testes unitários** — `AuthServiceTest`, `TransactionServiceTest` com Mockito
- **Testes de integração** — `AuthControllerTest` com Testcontainers (PostgreSQL + Kafka reais)

---

## ☁️ Deploy na AWS (Produção)

```bash
# Variáveis de ambiente necessárias na EC2/ECS
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://{RDS_ENDPOINT}:5432/digitalbank
DB_USERNAME={seu_usuario}
DB_PASSWORD={sua_senha}
REDIS_HOST={ELASTICACHE_ENDPOINT}
KAFKA_BOOTSTRAP_SERVERS={MSK_ENDPOINT}
JWT_SECRET={secret_seguro}
AWS_S3_BUCKET=digitalbank-receipts
AWS_REGION=us-east-1
# Em produção, sem AWS_ENDPOINT → usa credenciais reais da AWS (IAM Role)
```

---

## 🔒 Segurança

- Senhas criptografadas com **BCrypt**
- Autenticação **stateless** com JWT
- Validação de ownership (usuário só opera em suas próprias contas)
- Secrets via variáveis de ambiente (nunca no código)
