FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY shelter-service/target/shelter-service-*.jar app.jar

USER appuser

EXPOSE 8095

ENTRYPOINT ["java", "-jar", "app.jar"]