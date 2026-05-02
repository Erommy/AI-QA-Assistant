# Stage 1: Build the Spring Boot jar with the React frontend embedded
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app

# Copy Maven descriptor first to cache dependency downloads separately from source changes
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy full source and run the prod build (installs Node, builds React, packages jar)
COPY . .
RUN mvn clean package -Pprod -DskipTests

# Stage 2: Minimal runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

# Activate the production Spring profile
ENV SPRING_PROFILES_ACTIVE=prod

EXPOSE 10000

ENTRYPOINT ["java", "-jar", "app.jar"]
