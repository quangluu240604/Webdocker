#
# Runtime image for Spring Boot app.
# This Dockerfile assumes you already built the JAR locally (./mvnw package),
# then it copies `target/*.jar` into the image.
#
# Usage:
#   ./mvnw -DskipTests package
#   docker build -t spring-app .
#   docker run --rm -p 8080:8080 spring-app
#

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY target/*.jar /app/app.jar

EXPOSE 8080
ENV JAVA_OPTS=""

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
