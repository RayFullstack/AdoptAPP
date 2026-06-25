FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY . .

RUN mvn -pl user-service -am package -DskipTests -B

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN useradd --system --no-create-home appuser

COPY --from=build /workspace/user-service/target/user-service-*.jar app.jar

USER appuser

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]