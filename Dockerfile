# -------- Stage 1: Build --------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# -------- Stage 2: Run --------
FROM eclipse-temurin:17-jdk-slim
WORKDIR /app
COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 80
ENTRYPOINT ["java","-jar","/app/app.jar"]