#
# Multi-stage Docker build for Spring Boot (Render friendly)
#

FROM eclipse-temurin:24-jdk AS build
WORKDIR /app

# Cache dependencies first
COPY pom.xml ./
COPY .mvn .mvn
COPY mvnw mvnw
RUN chmod +x mvnw && ./mvnw -q -DskipTests dependency:go-offline

# Build
COPY src src
RUN ./mvnw -q -DskipTests package

FROM eclipse-temurin:24-jre
WORKDIR /app

# Render sets PORT; we also use SPRING_PROFILES_ACTIVE=prod in Render env vars
ENV JAVA_OPTS=""

COPY --from=build /app/target/*.jar app.jar

EXPOSE 8080
CMD ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]

