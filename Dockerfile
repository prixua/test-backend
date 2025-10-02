# Multi-stage build para otimizar o tamanho da imagem
FROM gradle:8.5-jdk21-alpine AS builder

WORKDIR /app
COPY . .

# Build da aplicação
RUN gradle clean bootJar --no-daemon

# Imagem final otimizada
FROM openjdk:21-jdk-slim

# Instalar ferramentas necessárias
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# Criar usuário não-root para segurança
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Criar diretórios
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

WORKDIR /app

# Copiar JAR da imagem de build
COPY --from=builder /app/build/libs/*.jar app.jar

# Mudar para usuário não-root
USER appuser

# Expor porta
EXPOSE 8080

# Configurações JVM otimizadas para containers
ENV JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC -XX:+UseContainerSupport -Djava.security.egd=file:/dev/./urandom"

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/health || exit 1

# Comando de inicialização
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
