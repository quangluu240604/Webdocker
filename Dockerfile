# -------- Stage 1: Build --------
FROM maven:3.9.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy source code
COPY . .

# Build jar
RUN mvn clean package -DskipTests

# -------- Stage 2: Run --------
FROM eclipse-temurin:25-jre
WORKDIR /app

# Copy jar từ stage build
COPY --from=builder /app/target/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]