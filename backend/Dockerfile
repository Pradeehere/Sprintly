# ─── Stage 1: Build ───────────────────────────────────────────
FROM eclipse-temurin:23-jdk-alpine AS build
WORKDIR /app

# Copy Maven wrapper and pom.xml first for layer caching
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

RUN chmod +x mvnw && ./mvnw dependency:go-offline -q

# Copy source and build
COPY src src
RUN ./mvnw package -DskipTests -q

# ─── Stage 2: Run ─────────────────────────────────────────────
FROM eclipse-temurin:23-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
