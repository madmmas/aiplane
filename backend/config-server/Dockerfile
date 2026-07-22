# syntax=docker/dockerfile:1

FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

COPY pom.xml .
COPY api-server/pom.xml api-server/pom.xml
COPY config-server/pom.xml config-server/pom.xml
COPY api-server api-server
COPY config-server config-server

RUN mvn -pl config-server -am -B -DskipTests package

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN apk add --no-cache curl

COPY --from=build /build/config-server/target/config-server-*-exec.jar /app/app.jar

EXPOSE 8888

HEALTHCHECK --interval=10s --timeout=5s --start-period=30s --retries=5 \
  CMD curl -fsS http://127.0.0.1:8888/actuator/health | grep -q '"status":"UP"'

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
