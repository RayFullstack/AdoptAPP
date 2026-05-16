# Testing the Adoption Service

## Prerequisites

Start all 3 services **in order**, each in its own terminal.

### Terminal 1 — User Service (port 8081)

```bash
cd user-service
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### Terminal 2 — Pet Service (port 8082)

```bash
cd pet-service
mvn spring-boot:run -Dspring-boot.run.profiles=h2
```

### Terminal 3 — Adoption Service (port 8083)

```bash
cd adoption-service
mvn spring-boot:run
```

The `h2` profile lets user and pet services run with an in-memory database (no Postgres needed).

---

## 1. Seed a User

```bash
curl -s -X POST http://localhost:8081/user-app/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "jdoe",
    "name": "John",
    "surname": "Doe",
    "email": "john@example.com",
    "password": "pass123",
    "phone": "123456789",
    "country": "US",
    "city": "NY",
    "street": "Main St",
    "homeNumber": "123",
    "postalCode": "10001",
    "type": "PERSON",
    "status": "ACTIVE",
    "role": "ADOPTER",
    "active": true
  }'
```

Save the returned `id` (e.g. `1`).

---

## 2. Seed a Pet

```bash
curl -s -X POST http://localhost:8082/pet-app/pets \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rex",
    "species": "DOG",
    "race": "Labrador",
    "age": 3,
    "size": "LARGE",
    "color": "Brown",
    "personality": "Friendly",
    "fosterId": null,
    "vaccinated": true,
    "sterilized": true,
    "diseases": "",
    "status": "AVAILABLE"
  }'
```

Save the returned `id` (e.g. `1`).

---

## 3. Create an Adoption (connects user + pet)

```bash
curl -s -X POST http://localhost:8083/adoption-app/adoptions \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "petId": 1, "status": "PENDING"}'
```

- **`201 Created`** — adoption created successfully (both user and pet exist)
- **`409 Conflict`** — user or pet was not found (Feign client received 404 from the downstream service)

---

## 4. Verify

```bash
# List all adoptions
curl http://localhost:8083/adoption-app/adoptions

# Get by ID
curl http://localhost:8083/adoption-app/adoptions/by-id/1

# Get history
curl http://localhost:8083/adoption-app/adoptions/1/history

# Update
curl -s -X PUT http://localhost:8083/adoption-app/adoptions/1 \
  -H "Content-Type: application/json" \
  -d '{"userId": 1, "petId": 1, "status": "APPROVED"}'

# Delete
curl -s -X DELETE http://localhost:8083/adoption-app/adoptions/1
```

---

## How It Works

When `POST /adoptions` is called, `AdoptionService.create()`:

1. Calls **`UserServiceClient.getUserById()`** (Feign → `user-service:8081/user-app/users/by-id/{id}`)
2. Calls **`PetServiceClient.getPetById()`** (Feign → `pet-service:8082/pet-app/pets/by-id/{id}`)
3. If both return `2xx`, the adoption is created and saved
4. Notifications are sent to both user and pet notification endpoints

If either service is down, the **Feign fallback** returns `404 Not Found`, and the adoption is rejected with `IllegalArgumentException`.
