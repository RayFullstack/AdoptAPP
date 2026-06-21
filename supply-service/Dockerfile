FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY supply-service/target/supply-service-*.jar app.jar

USER appuser

EXPOSE 8092

ENTRYPOINT ["java", "-jar", "app.jar"]