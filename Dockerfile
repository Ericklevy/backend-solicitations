# Usando imagem oficial do Maven com Eclipse Temurin (sucessor do OpenJDK)
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app

# Copia arquivos Maven
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia código fonte
COPY src ./src

# Build da aplicação
RUN mvn clean package -DskipTests

# Estágio de execução
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia JAR do build
COPY --from=build /app/target/*.jar app.jar

# Cria usuário não-root
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

USER appuser

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]