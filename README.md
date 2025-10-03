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
- ✅ Carregamento automático de dados iniciais na inicialização
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

### 2.1. Executar Testes e Cobertura de Código

#### Executar Todos os Testes
```bash
# No Windows
./gradlew.bat test

# No Linux/Mac
./gradlew test
```

#### Executar Testes com Relatório de Cobertura (JaCoCo)
```bash
# No Windows
./gradlew.bat test jacocoTestReport

# No Linux/Mac
./gradlew test jacocoTestReport
```
#### 📊 Relatórios Disponíveis

Após a execução dos testes, os relatórios estarão disponíveis em:
- **Relatório de Testes HTML**: `build/reports/tests/test/index.html`
- **Relatório de Cobertura JaCoCo**: `build/reports/jacoco/test/html/index.html`
- **Resultados XML**: `build/test-results/test/`

> 💡 **Dica:** Use `./gradlew test --continue` para executar todos os testes mesmo se alguns falharem, útil para verificar o status geral da aplicação.

### 3. Execução Alternativa com Docker
Se preferir, você pode executar a aplicação usando Docker diretamente:

```bash
docker run -it -p 8080:8080 prixua/test-backend:latest
```

> **Nota:** Esta opção não requer ter Java instalado localmente, apenas Docker.

### 4. Acesse a aplicação
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
GET /api/v1/movies/import/{uuidImport}/awards
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

### Portas e URLs
- **Aplicação**: `http://localhost:8080`
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/api-docs`
- **Health Check**: `http://localhost:8080/actuator/health`

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
  "path": "/api/v1/movies/import/{uuid}/awards"
}
```
