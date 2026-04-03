FROM eclipse-temurin:17-jre-alpine

# Устанавливаем curl для healthcheck
RUN apk add --no-cache curl

COPY target/*.jar app.jar
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "/app.jar"]
