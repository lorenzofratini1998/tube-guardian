FROM maven:3.9.12-eclipse-temurin-25-alpine AS builder

WORKDIR /app

COPY pom.xml .
COPY tg-common/pom.xml tg-common/
COPY tg-video-orchestrator/pom.xml tg-video-orchestrator/

COPY . .

RUN mvn clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

ARG JAR_FILE=/app/tg-video-orchestrator/target/*-exec.jar
COPY --from=builder ${JAR_FILE} app.jar

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]