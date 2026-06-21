FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY staff-service/target/staff-service-*.jar app.jar

USER appuser

EXPOSE 8091

ENTRYPOINT ["java", "-jar", "app.jar"]