FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY donation-service/target/donation-service-*.jar app.jar

USER appuser

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]