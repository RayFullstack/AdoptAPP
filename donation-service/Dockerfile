FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY . .

RUN mvn -pl donation-service -am package -DskipTests -B

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY --from=build /workspace/donation-service/target/donation-service-*.jar app.jar

USER appuser

EXPOSE 8090

ENTRYPOINT ["java", "-jar", "app.jar"]