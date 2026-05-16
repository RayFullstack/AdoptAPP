# AdoptApp - Plataforma de Adopción de Mascotas

Plataforma de adopción de mascotas basada en microservicios con **Spring Boot 4.0.0** y **Java 21**, diseñada para gestionar el ciclo de vida completo de adopciones en refugios.

## Arquitectura

Proyecto multi-módulo Maven con 10 microservicios desplegables de forma independiente:

```
adoptapp/
├── user-service/          # Gestión de usuarios (puerto 8081)
├── pet-service/           # Catálogo de mascotas (puerto 8082)
├── adoption-service/      # Proceso de adopción (puerto 8083)
├── donation-service/      # Donaciones (esqueleto)
├── shelter-service/       # Gestión de refugios (esqueleto)
├── staff-service/         # Gestión de personal (esqueleto)
├── health-service/        # Registros de salud (esqueleto)
├── notification-service/  # Notificaciones (esqueleto)
├── supply-service/        # Insumos de refugio (esqueleto)
├── followup-service/      # Seguimiento post-adopción (esqueleto)
└── pom.xml                # POM Padre
```

## Stack Tecnológico

| Tecnología | Uso |
|---|---|
| **Java 21** | Runtime |
| **Spring Boot 4.0.0** | Framework |
| **Spring Cloud 2025.1.0 (Oakwood)** | Feign clients entre servicios |
| **Spring Data JPA** | Acceso a datos con relaciones |
| **PostgreSQL / H2** | Bases de datos (prod / dev) |
| **OpenFeign** | Comunicación sincrónica entre servicios |
| **Lombok** | Reducción de código repetitivo |
| **Bean Validation** | Validación de DTOs de entrada |
| **SLF4J + Logback** | Logging con archivos por servicio |
| **Maven** | Gestión de dependencias |

## Servicios Activos

### user-service (Puerto 8081) — COMPLETO

API CRUD para gestionar usuarios (adoptantes).

**Base de datos**: PostgreSQL `users_db` (o H2 con perfil `h2`)

**Endpoints** (`/users`):

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/users` | Listar todos (filtro `?status=`) |
| `GET` | `/users/by-id/{id}` | Obtener por ID |
| `POST` | `/users` | Crear usuario |
| `PUT` | `/users/by-id/{id}` | Actualizar usuario |
| `DELETE` | `/users/by-id/{id}` | Eliminar usuario |

### pet-service (Puerto 8082) — COMPLETO

API CRUD para gestionar mascotas.

**Base de datos**: PostgreSQL `pet_db` (o H2 con perfil `h2`)

**Endpoints** (`/pets`):

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/pets` | Listar todas (filtro `?status=`) |
| `GET` | `/pets/by-id/{id}` | Obtener por ID |
| `POST` | `/pets` | Crear mascota |
| `PUT` | `/pets/by-id/{id}` | Actualizar mascota |
| `DELETE` | `/pets/by-id/{id}` | Eliminar mascota |

### adoption-service (Puerto 8083) — COMPLETO

API para gestionar adopciones. Verifica existencia de usuario y mascota vía Feign antes de crear.

**Base de datos**: PostgreSQL `adoption_db` (o H2 con perfil `h2`)

**Flyway migrations**: `V1__create_adoptions_table.sql`, `V2__create_adoption_history.sql`, `V3__insert_initial_data.sql`

**Endpoints** (`/adoptions`):

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/adoptions` | Listar todas (filtro `?status=`) |
| `GET` | `/adoptions/by-id/{id}` | Obtener por ID |
| `GET` | `/adoptions/{id}/history` | Historial de cambios |
| `POST` | `/adoptions` | Crear adopción (valida user + pet) |
| `PUT` | `/adoptions/{id}` | Actualizar adopción |
| `DELETE` | `/adoptions/{id}` | Eliminar adopción |

## Cambios Realizados

### Comunicación entre servicios con Feign

- **Spring Cloud 2025.1.0 (Oakwood)** — versión compatible con Spring Boot 4.0.0
- `UserServiceClient`, `PetServiceClient`, `PetNotificationClient`, `UserNotificationClient` como interfaces Feign
- Fallbacks implementados para tolerancia a fallos
- URLs configuradas via `application.yml` (`services.user-service.url`, `services.pet-service.url`)

### Logging

- **SLF4J + `@Slf4j`** agregado en servicios que no lo tenían (`PetService`)
- `log.info()`, `log.warn()`, `log.error()` en métodos `create()`, `updateById()`, `deleteById()` de los 3 servicios
- Archivos de log por servicio:
  - `logs/adoptions.log`
  - `logs/users.log`
  - `logs/pets.log`
- Perfiles `dev` (DEBUG) y `prod` (INFO) para logging

### Manejo Global de Excepciones

- `GlobalExceptionHandler` con `@RestControllerAdvice` en cada servicio
- Maneja: `IllegalArgumentException` → 409, `MethodArgumentNotValidException` → 400, `Exception` → 500
- Controladores simplificados: eliminados `try-catch` y `@ExceptionHandler` locales

## Patrones del Proyecto

### Capas de DTO

```
Request (validación) → Command (servicio) → Result (servicio) → Response (API)
```

### Relaciones JPA implementadas

- `@OneToOne` para teléfono de usuario y salud de mascota
- `@OneToMany` para direcciones de usuario
- `@ManyToOne` para estado de mascota (catálogo)
- `@Enumerated(EnumType.STRING)` para estado de usuario
- `CascadeType.ALL` para operaciones en cascada


