# Movies API - Sistema de Análise de Prêmios Cinematográficos

Uma API REST desenvolvida em Java 21 com Spring Boot 3 para gerenciamento de filmes e análise de intervalos entre prêmios cinematográficos (Golden Raspberry Awards).

## 🚀 Tecnologias Utilizadas

- **Java 21**
- **Spring Boot 3.2.10**
- **Spring Data JPA**
- **H2 Database** (em memória)
- **Gradle 8.5**
- **Lombok**
- **OpenCSV**
- **SpringDoc OpenAPI** (Swagger)
- **Apache Commons IO**

## 📋 Funcionalidades

- ✅ Importação de arquivos CSV com dados de filmes
- ✅ Análise de intervalos entre prêmios consecutivos de produtores
- ✅ Consulta de filmes por importação específica
- ✅ API REST completa com documentação Swagger
- ✅ Tratamento global de exceções
- ✅ Validação de dados de entrada

## 🛠️ Pré-requisitos

- **Java 21** ou superior
- **Git** (para clonar o repositório)

## 📦 Como Executar o Projeto

### 1. Clone o repositório
```bash
git clone <url-do-repositorio>
cd test-backend
```

### 2. Execute o projeto
```bash
# No Windows
./gradlew.bat bootRun

# No Linux/Mac
./gradlew bootRun
```

### 3. Acesse a aplicação
A aplicação estará disponível em: `http://localhost:8080`

## 📚 Documentação da API

### Swagger UI (Interface Interativa)
```
http://localhost:8080/swagger-ui.html
```

### OpenAPI JSON (Especificação)
```
http://localhost:8080/api-docs
```

## 🔗 Endpoints Disponíveis

### 📤 Importação de Arquivo CSV
```http
POST /api/v1/movies/import
Content-Type: multipart/form-data

# Parâmetros:
# file: arquivo CSV no formato year;title;studios;producers;winner
```

**Formato esperado do CSV:**
```csv
year;title;studios;producers;winner
1981;Tarzan, the Ape Man;MGM, United Artists;John Derek;
1982;Inchon;MGM;Mitsuharu Ishii;yes
1982;Annie;Columbia Pictures;Ray Stark;
```

**Resposta:**
```json
{
  "uuidImported": "550e8400-e29b-41d4-a716-446655440000"
}
```

### 📊 Análise de Intervalos Entre Prêmios
```http
GET /api/v1/movies/import/{uuidImport}
```

**Resposta:**
```json
{
  "min": [
    {
      "producer": "Joel Silver",
      "interval": 1,
      "previousWin": 1990,
      "followingWin": 1991
    }
  ],
  "max": [
    {
      "producer": "Matthew Vaughn",
      "interval": 13,
      "previousWin": 2002,
      "followingWin": 2015
    }
  ]
}
```

### 📋 Listar UUIDs de Importação
```http
GET /api/v1/movies/import-uuids
```

**Resposta:**
```json
[
  "550e8400-e29b-41d4-a716-446655440000",
  "6ba7b810-9dad-11d1-80b4-00c04fd430c8"
]
```

### 🎬 Buscar Filmes por UUID de Importação
```http
GET /api/v1/movies/by-import/{importUuid}
```

**Resposta:**
```json
[
  {
    "id": 1,
    "year": 1981,
    "title": "Tarzan, the Ape Man",
    "studios": "MGM, United Artists",
    "producers": "John Derek",
    "winner": false,
    "importUuid": "550e8400-e29b-41d4-a716-446655440000",
    "createdAt": "2025-10-02T15:30:00"
  }
]
```
## 💾 Banco de Dados

### H2 Console (Desenvolvimento)
```
http://localhost:8080/h2-console

# Configurações de conexão:
# JDBC URL: jdbc:h2:mem:testdb
# Username: sa
# Password: password
```

## 🔧 Configurações

### Arquivo de Configuração
As configurações estão em `src/main/resources/application.yml`:

```yaml
spring:
  application:
    name: test-backend
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 10MB
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password: password
```

### Portas e URLs
- **Aplicação**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`
- **H2 Console**: `http://localhost:8080/h2-console`
- **Health Check**: `http://localhost:8080/actuator/health`

## 📝 Estrutura do Projeto

```
src/main/java/com/example/testbackend/
├── config/                 # Configurações (OpenAPI)
├── controller/             # Controllers REST
│   └── api/               # Interfaces de documentação
├── dto/                   # Data Transfer Objects
│   └── response/          # DTOs de resposta
├── exception/             # Tratamento de exceções
├── mapper/                # Mapeadores de entidades
├── model/                 # Entidades JPA
├── repository/            # Repositórios de dados
└── service/               # Lógica de negócio
```

## 🧪 Testando a API

### Exemplo usando cURL

#### 1. Importar arquivo CSV:
```bash
curl -X POST \
  http://localhost:8080/api/v1/movies/import \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@movielist.csv'
```

#### 2. Analisar intervalos entre prêmios:
```bash
curl -X GET \
  'http://localhost:8080/api/v1/movies/awards?uuid=550e8400-e29b-41d4-a716-446655440000'
```

#### 3. Listar UUIDs de importação:
```bash
curl -X GET http://localhost:8080/api/v1/movies/import-uuids
```

### Exemplo usando Interface Swagger

1. Acesse: `http://localhost:8080/swagger-ui.html`
2. Expanda o endpoint desejado
3. Clique em "Try it out"
4. Preencha os parâmetros necessários
5. Clique em "Execute"

## 🛡️ Tratamento de Erros

A API possui tratamento global de exceções com respostas padronizadas:

### Erro 400 - Bad Request
```json
{
  "timestamp": "2023-10-01T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Arquivo deve ser do tipo CSV",
  "path": "/api/v1/movies/import"
}
```

### Erro 404 - Not Found
```json
{
  "timestamp": "2023-10-01T14:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Nenhum filme encontrado para o UUID de importação: uuid-inexistente",
  "path": "/api/v1/movies/awards"
}
```

## 🆘 Suporte

Em caso de dúvidas ou problemas:

1. Verifique a documentação Swagger em `http://localhost:8080/swagger-ui.html`
2. Consulte os logs da aplicação
3. Abra uma issue no repositório do projeto
