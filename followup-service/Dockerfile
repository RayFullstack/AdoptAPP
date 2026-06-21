FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY followup-service/target/followup-service-*.jar app.jar

USER appuser

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]