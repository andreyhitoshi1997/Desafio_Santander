# Desafio Santander - API de Agências

API REST para gerenciamento de agências bancárias com autenticação JWT e cache Redis.

## 🚀 Tecnologias

- **Java 17**
- **Spring Boot 3.2.5**
- **Spring Security** (Autenticação JWT)
- **Spring Data JPA** (Persistência)
- **H2 Database** (Desenvolvimento)
- **PostgreSQL** (Produção)
- **Redis** (Cache)
- **Lombok** (Redução de boilerplate)
- **Hibernate Validator** (Validação)

## 📋 Funcionalidades

### Autenticação
- Login com JWT
- Proteção de endpoints
- Validação de tokens

### Agências
- Cadastro de agências com coordenadas (X, Y)
- Consulta de todas as agências
- Busca da agência mais próxima
- Cache Redis para otimização

## 🏗️ Arquitetura

```
src/main/java/com/example/desafio_santander/
├── controller/          # Controllers REST
│   ├── auth/           # Autenticação
│   └── agencia/        # Gerenciamento de agências
├── dto/                # Data Transfer Objects
├── model/              # Entidades JPA
├── repository/         # Repositórios Spring Data
├── service/            # Lógica de negócio
├── security/           # Configurações de segurança
└── config/             # Configurações gerais
```

## 🛠️ Configuração

### Pré-requisitos
- Java 17+
- Gradle 7+
- Redis (opcional, para cache)

### Executar Aplicação

```bash
# Executar com H2 (desenvolvimento)
./gradlew bootRun

# A aplicação estará disponível em http://localhost:8080
```

### Banco de Dados

**Desenvolvimento (H2):**
- URL: `jdbc:h2:mem:agencia_db`
- Console: http://localhost:8080/h2-console
- Usuário: `sa`
- Senha: (vazio)

**Produção (PostgreSQL):**
- Configure as variáveis no `docker-compose.yml`
- Execute: `docker-compose up -d postgres redis`

## 📚 API Endpoints

### Autenticação
```bash
POST /autenticacao
{
  "usuario": "usuario",
  "senha": "senha123"
}
```

### Agências
```bash
# Cadastrar agência (requer token)
POST /desafio/cadastrar
Authorization: Bearer {token}
{
  "posX": 10,
  "posY": -5
}

# Consultar todas as agências
GET /desafio/consultar

# Buscar agência mais próxima (requer token)
GET /desafio/consultar?posX=10&posY=-5
Authorization: Bearer {token}
```

## 🧪 Testes

Execute os testes do script disponível:

```bash
# Executar testes automatizados
./test_curls.sh
```

## 🐳 Docker

```bash
# Executar com PostgreSQL e Redis
docker-compose up -d postgres redis

# Executar aplicação completa
docker-compose up
```

## 📊 Cache Redis

- Cache automático para consultas de agências
- TTL configurável
- Invalidação inteligente

## 🔧 Configurações

### application.properties
```properties
# Banco H2 (desenvolvimento)
spring.datasource.url=jdbc:h2:mem:agencia_db
spring.h2.console.enabled=true

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# JPA
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
```

## 📝 Notas

- O projeto está configurado para H2 em desenvolvimento
- Para produção, altere as configurações para PostgreSQL
- Redis é opcional mas recomendado para performance
- JWT tokens têm validade configurável
- Validação automática de dados de entrada

## 🤝 Contribuição

1. Fork o projeto
2. Crie uma branch para sua feature
3. Commit suas mudanças
4. Push para a branch
5. Abra um Pull Request
