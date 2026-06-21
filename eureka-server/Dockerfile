FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

RUN useradd --system --no-create-home appuser

COPY eureka-server/target/eureka-server-*.jar app.jar

USER appuser

EXPOSE 8761

ENTRYPOINT ["java", "-jar", "app.jar"]
