FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY notification-service/target/notification-service-*.jar app.jar

USER appuser

EXPOSE 8084

ENTRYPOINT ["java", "-jar", "app.jar"]