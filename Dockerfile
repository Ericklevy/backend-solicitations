# Usando Eclipse Temurin (sucessor do OpenJDK)
FROM eclipse-temurin:21-jdk-alpine AS build

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