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
- **Docker** (Containerização)

## 📋 Funcionalidades

### Autenticação
- Login com JWT
- Proteção de endpoints
- Validação de tokens

### Agências
- Cadastro de agências com coordenadas (X, Y)
- Consulta de todas as agências
- Cálculo da agência mais próxima com base em coordenadas
- Cache Redis para otimização de consultas

## 🏗️ Arquitetura

```
src/main/java/com/example/desafio_santander/
├── DesafioSantanderApplication.java    # Classe principal Spring Boot
├── config/                             # Configurações da aplicação
│   ├── DataLoader.java                 # Carregamento inicial de dados
│   └── RedisConfig.java                # Configuração do Redis
├── controller/                         # Controllers REST
│   ├── agencia/
│   │   └── AgenciaController.java      # Endpoints de agências
│   └── auth/
│       └── AuthController.java         # Endpoints de autenticação
├── dto/                                # Data Transfer Objects
│   ├── agencia/
│   │   ├── AgenciaDTO.java             # DTO base da agência
│   │   ├── AgenciaRequestDTO.java      # Request para cadastro
│   │   ├── AgenciaResponseDTO.java     # Response de agência
│   │   ├── DistanciaRequestDTO.java    # Request para cálculo distância
│   │   └── DistanciaResponseDTO.java   # Response com distância
│   └── auth/
│       ├── AuthRequestDTO.java         # Request de login
│       └── AuthResponseDTO.java        # Response com token JWT
├── exception/                          # Tratamento de exceções
│   ├── AgenciaCadastroException.java   # Exceção de cadastro
│   ├── AgenciaNotFoundException.java   # Exceção de agência não encontrada
│   ├── DistanciaException.java         # Exceção de cálculo de distância
│   └── GlobalExceptionHandler.java     # Handler global de exceções
├── model/                              # Entidades JPA
│   └── Agencia.java                    # Entidade Agência
├── repository/                         # Repositórios Spring Data
│   └── AgenciaRepository.java          # Repository de agências
├── security/                           # Configurações de segurança JWT
│   ├── CustomUserDetailsService.java  # Service de detalhes do usuário
│   ├── JwtAuthenticationFilter.java    # Filtro de autenticação JWT
│   ├── JwtUtil.java                    # Utilitários JWT
│   └── SecurityConfig.java             # Configuração Spring Security
└── service/                            # Lógica de negócio
    ├── AgenciaService.java             # Serviço de agências
    ├── AuthService.java                # Serviço de autenticação
    ├── CachedDistanciaDecorator.java   # Decorator com cache Redis
    ├── DistanciaCoreService.java       # Core service de distância
    └── DistanciaUseCase.java           # Interface do caso de uso

src/test/java/com/example/desafio_santander/
├── DesafioSantanderApplicationTests.java
├── controller/
│   ├── AgenciaControllerTest.java      # Testes do controller de agências
│   └── AuthControllerTest.java         # Testes do controller de auth
├── security/                           # Testes de segurança
└── service/                            # Testes dos services
```

## 🐳 Executar com Docker

### Pré-requisitos
- Docker
- Docker Compose

### Execução Rápida
```bash
# Tornar o script executável (primeira vez)
chmod +x docker-run.sh

# Executar a aplicação completa (PostgreSQL + Redis + App)
./docker-run.sh
```

### Comandos Docker Alternativos
```bash
# Parar todos os serviços
docker-compose down

# Subir todos os serviços
docker-compose up -d

# Ver logs da aplicação
docker-compose logs -f app

# Rebuildar e subir
docker-compose up -d --build
```

### Portas Expostas
- **API**: http://localhost:8080
- **PostgreSQL**: localhost:5432
- **Redis**: localhost:6379

## 🛠️ Configuração Local (Desenvolvimento)

### Pré-requisitos
- Java 17+
- Gradle 7+

### Executar Aplicação
```bash
# Executar com H2 (desenvolvimento)
./gradlew bootRun

# A aplicação estará disponível em http://localhost:8080
```

### Banco de Dados H2 (Desenvolvimento)
- URL: `jdbc:h2:mem:agencia_db`
- Console: http://localhost:8080/h2-console
- Usuário: `sa`
- Senha: (vazio)

## 📚 API Endpoints

### Autenticação
- **POST** `/autenticacao` - Fazer login e obter token JWT

### Agências
- **POST** `/desafio/cadastrar` - Cadastrar nova agência (requer autenticação)
- **GET** `/desafio/consultar` - Listar todas as agências
- **GET** `/desafio/distancia?posX={x}&posY={y}` - Encontrar agência mais próxima (requer autenticação)

## 🧪 Exemplos de Uso (cURL)

### 1. Autenticação
```bash
curl --location 'http://localhost:8080/autenticacao' \
--header 'Content-Type: application/json' \
--data '{
    "usuario": "admin",
    "senha": "admin123"
}'
```

**Resposta:**
```json
{
    "token": "Bearer eyJhbGciOiJIUzI1NiJ9..."
}
```

### 2. Cadastrar Agência
```bash
curl --location 'http://localhost:8080/desafio/cadastrar' \
--header 'Content-Type: application/json' \
--header 'Authorization: Bearer {SEU_TOKEN_AQUI}' \
--data '{
    "posX": 10,
    "posY": -5
}'
```

### 3. Consultar Todas as Agências
```bash
curl --location 'http://localhost:8080/desafio/consultar' \
--header 'Accept: application/json'
```

### 4. Encontrar Agência Mais Próxima
```bash
curl --location 'http://localhost:8080/desafio/distancia?posX=-10&posY=5' \
--header 'Accept: application/json' \
--header 'Authorization: Bearer {SEU_TOKEN_AQUI}'
```

## ⚙️ Configuração

### Credenciais Padrão
- **Usuário**: admin
- **Senha**: admin123

### Variáveis de Ambiente (Docker)
```env
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/agencia_db
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=password
SPRING_REDIS_HOST=redis
SPRING_REDIS_PORT=6379
```

### Configurações Importantes
- JWT Token válido por 1 hora
- Cache Redis ativo em produção
- CORS configurado para desenvolvimento
- Endpoints públicos: `/autenticacao`, `/desafio/consultar`
- Endpoints protegidos: `/desafio/cadastrar`, `/desafio/distancia`

## 🧪 Testes

```bash
# Executar todos os testes
./gradlew test

# Executar testes com relatório
./gradlew test --info
```

## 📝 Estrutura do Projeto

```
├── docker-compose.yml      # Configuração Docker
├── docker-run.sh          # Script de execução
├── Dockerfile             # Imagem da aplicação
├── init-db.sql           # Script inicial do banco
└── src/
    ├── main/
    │   ├── java/         # Código fonte
    │   └── resources/
    │       └── application.properties
    └── test/             # Testes unitários e integração
```
