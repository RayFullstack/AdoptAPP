FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY . .

RUN mvn -pl followup-service -am package -DskipTests -B

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY --from=build /workspace/followup-service/target/followup-service-*.jar app.jar

USER appuser

EXPOSE 8086

ENTRYPOINT ["java", "-jar", "app.jar"]