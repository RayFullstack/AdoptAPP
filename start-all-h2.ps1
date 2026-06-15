$services = @(
  "user-service",
  "pet-service",
  "adoption-service",
  "notification-service",
  "health-service",
  "followup-service",
  "shelter-service",
  "staff-service",
  "supply-service",
  "donation-service"
)

foreach ($service in $services) {
    $cmd = "& .\mvnw -pl $service spring-boot:run '-Dspring-boot.run.profiles=h2'"
    Start-Process powershell -ArgumentList "-NoExit", "-Command", $cmd
}
