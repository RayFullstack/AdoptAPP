# AdoptApp - Plataforma de Adopción de Mascotas

Plataforma de adopción de mascotas basada en microservicios con **Spring Boot 3.4.5** y **Java 21**, diseñada para gestionar el ciclo de vida completo de adopciones en refugios.

## Arquitectura

Proyecto multi-módulo Maven con 10 microservicios desplegables de forma independiente:

```
adoptapp/
├── user-service/          # Gestión de usuarios (puerto 8081, /user-app)
├── pet-service/           # Catálogo de mascotas (puerto 8082, /pet-app)
├── adoption-service/      # Proceso de adopción (puerto 8083, /adoption-app)
├── notification-service/  # Notificaciones (puerto 8084, /notification-app)
├── health-service/        # Registros de salud (puerto 8085, /health-app)
├── donation-service/      # Donaciones (puerto 8090, /donation-app)
├── followup-service/      # Seguimiento post-adopción (puerto 8086, /followup-app)
├── shelter-service/       # Gestión de refugios (puerto 8095, /shelter-app)
├── staff-service/         # Gestión de personal (puerto 8091, /staff-app)
├── supply-service/        # Insumos de refugio (puerto 8092, /supply-app)
└── pom.xml                # POM Padre
```

## Stack Tecnológico

| Tecnología | Uso |
|---|---|
| **Java 21** | Runtime |
| **Spring Boot 3.4.5** | Framework |
| **Spring Cloud 2025.1.0 (Oakwood)** | Feign clients entre servicios |
| **Spring Data JPA** | Acceso a datos con relaciones |
| **PostgreSQL / H2** | Bases de datos (prod / dev) |
| **OpenFeign** | Comunicación sincrónica entre servicios |
| **Flyway** | Migraciones de base de datos |
| **Lombok** | Reducción de código repetitivo |
| **Bean Validation** | Validación de DTOs de entrada |
| **SLF4J + Logback** | Logging con archivos por servicio |
| **spring-dotenv** | Carga de variables de entorno desde `.env` |
| **Maven** | Gestión de dependencias |

## Roles del Sistema

| Rol | Permisos |
|---|---|
| **ADOPTER** | Editar su propio perfil. Crear mascotas y adopciones. |
| **SHELTER_ADMIN** | Hereda todos los permisos de SHELTER. Editar su propio perfil y los perfiles de VOLUNTEER. Gestionar mascotas y adopciones. |
| **VOLUNTEER** | Editar su propio perfil. Crear y editar perfiles de mascotas (excepto ficha médica). |
| **VET** | Editar su propio perfil y la ficha médica de animales (health-service). |
| **ADMIN** | Acceso completo a todas las operaciones. |

## Servicios Activos

### user-service (Puerto 8081, `/user-app`) — COMPLETO

API CRUD para gestionar usuarios con 5 roles: ADOPTER, SHELTER_ADMIN, VOLUNTEER, VET, ADMIN.

**Base de datos**: PostgreSQL `users_db` (o H2 con perfil `h2`)

**Endpoints** (`/users`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/users` | Listar todos (filtro `?status=`) | Público |
| `GET` | `/users/by-id/{id}` | Obtener por ID | Público |
| `GET` | `/users/by-email/{email}` | Obtener por email | Público |
| `GET` | `/users/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/users` | Crear usuario | Cualquier rol autenticado |
| `PUT` | `/users/by-id/{id}` | Actualizar usuario | Propio perfil + SHELTER_ADMIN edita VOLUNTEER |
| `DELETE` | `/users/by-id/{id}` | Eliminar usuario | ADMIN |

**Comunicación saliente:**
- → notification-service (`POST /notifications`): `USER_CREATED`, `USER_UPDATED`, `USER_DELETED`

### pet-service (Puerto 8082, `/pet-app`) — COMPLETO

API CRUD para gestionar mascotas. La información de salud (vacunación, esterilización, enfermedades) se almacena localmente y se replica en health-service para trazabilidad histórica.

**Base de datos**: PostgreSQL `pet_db` (o H2 con perfil `h2`)

**Endpoints** (`/pets`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/pets` | Listar todas (filtro `?status=`) | Público |
| `GET` | `/pets/by-id/{id}` | Obtener por ID | Público |
| `GET` | `/pets/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/pets` | Crear mascota | SHELTER_ADMIN, VOLUNTEER, ADMIN |
| `PUT` | `/pets/by-id/{id}` | Actualizar mascota | SHELTER_ADMIN, VOLUNTEER, ADMIN |
| `DELETE` | `/pets/by-id/{id}` | Eliminar mascota | ADMIN |

**Comunicación saliente:**
- → health-service (`POST /health`, `PUT /health/by-id/{id}`, `DELETE /health/by-id/{id}`): delega registros clínicos
- → user-service (`GET /users/by-id/{id}`): obtiene email para notificaciones
- → notification-service (`POST /notifications`): `PET_CREATED`, `PET_UPDATED`, `PET_DELETED`

### adoption-service (Puerto 8083, `/adoption-app`) — COMPLETO

API para gestionar adopciones. Verifica existencia de usuario y mascota vía Feign antes de crear.

**Base de datos**: PostgreSQL `adoption_db` (o H2 con perfil `h2`)

**Endpoints** (`/adoptions`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/adoptions` | Listar todas (filtro `?status=`) | Público |
| `GET` | `/adoptions/by-id/{id}` | Obtener por ID | Público |
| `GET` | `/adoptions/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/adoptions` | Crear adopción (valida user + pet) | SHELTER_ADMIN, ADMIN |
| `PUT` | `/adoptions/by-id/{id}` | Actualizar adopción | SHELTER_ADMIN, ADMIN |
| `DELETE` | `/adoptions/by-id/{id}` | Eliminar adopción | ADMIN |

**Comunicación saliente:**
- → user-service (`GET /users/by-id/{id}`): verifica usuario
- → pet-service (`GET /pets/by-id/{id}`): verifica mascota
- → notification-service (`POST /notifications`): `ADOPTION_CREATED`, `ADOPTION_UPDATED`, `ADOPTION_DELETED`, `PET_CREATED`, `PET_UPDATED`, `PET_DELETED`

### notification-service (Puerto 8084, `/notification-app`) — COMPLETO

API para gestionar notificaciones. Almacena notificaciones con tipos categorizados vía `@ManyToOne(NotificationType)`.

**Base de datos**: PostgreSQL `notif_db` (o H2 con perfil `h2`)

**Endpoints** (`/notifications`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/notifications` | Listar todas (filtro `?status=`) | Público |
| `GET` | `/notifications/by-id/{id}` | Obtener por ID | Público |
| `POST` | `/notifications` | Crear notificación | Autenticado |
| `PUT` | `/notifications/by-id/{id}` | Actualizar notificación | Autenticado |
| `DELETE` | `/notifications/by-id/{id}` | Eliminar notificación | ADMIN |

**Tipos de notificación disponibles (vía Flyway V2 + V3):**

| Servicio | Tipos |
|---|---|
| adoption | `ADOPTION_CREATED`, `ADOPTION_UPDATED`, `ADOPTION_DELETED` |
| pet | `PET_CREATED`, `PET_UPDATED`, `PET_DELETED`, `PET_STATUS_CHANGED` |
| user | `USER_CREATED`, `USER_UPDATED`, `USER_DELETED` |
| donation | `DONATION_RECEIVED`, `DONATION_UPDATED`, `DONATION_CANCELLED` |
| followup | `FOLLOWUP_SCHEDULED`, `FOLLOWUP_COMPLETED`, `FOLLOWUP_CANCELLED` |
| health | `HEALTH_CHECK_CREATED`, `HEALTH_CHECK_UPDATED`, `HEALTH_ALERT` |
| shelter | `SHELTER_CREATED`, `SHELTER_UPDATED`, `SHELTER_DELETED` |
| staff | `STAFF_ADDED`, `STAFF_REMOVED` |
| supply | `SUPPLY_LOW_STOCK`, `SUPPLY_ORDERED`, `SUPPLY_RECEIVED` |

### health-service (Puerto 8085, `/health-app`) — COMPLETO

API para gestionar registros clínicos de mascotas con historial de cambios. VET, SHELTER_ADMIN, ADMIN tienen acceso de escritura.

**Base de datos**: PostgreSQL `health_db` (o H2 con perfil `h2`)

**Endpoints** (`/health`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/health` | Listar todos (filtro `?vaccinationStatus=`, `?sterilizationStatus=`) | Público |
| `GET` | `/health/by-id/{id}` | Obtener por ID | Público |
| `GET` | `/health/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/health` | Crear registro clínico | VET, SHELTER_ADMIN, ADMIN |
| `PUT` | `/health/by-id/{id}` | Actualizar registro clínico | VET, SHELTER_ADMIN, ADMIN |
| `DELETE` | `/health/by-id/{id}` | Eliminar registro clínico | ADMIN |

**Comunicación saliente:**
- → user-service (`GET /users/by-id/{id}`): obtiene userId del creador
- → pet-service (`GET /pets/by-id/{id}`): verifica mascota
- → notification-service (`POST /notifications`): `HEALTH_CHECK_CREATED`, `HEALTH_CHECK_UPDATED`, `HEALTH_ALERT`

### donation-service (Puerto 8090, `/donation-app`)

API para gestionar donaciones. Verifica existencia de usuario y refugio vía Feign antes de crear.

**Base de datos**: PostgreSQL `donation_db` (o H2 con perfil `h2`)

**Endpoints** (`/donations`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/donations` | Listar todas (filtro `?status=`) | Público |
| `GET` | `/donations/by-id/{id}` | Obtener por ID | Público |
| `GET` | `/donations/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/donations` | Crear donación | ADMIN |
| `PUT` | `/donations/by-id/{id}` | Actualizar donación | ADMIN |
| `DELETE` | `/donations/by-id/{id}` | Eliminar donación | ADMIN |

**Comunicación saliente:**
- → user-service (`GET /users/by-id/{id}`): verifica usuario
- → notification-service (`POST /notifications`): `CREATED`, `DONATION_UPDATED`, `DELETED`
- → shelter-service (`GET /shelters/by-id/{id}`): verifica refugio

### followup-service (Puerto 8086, `/followup-app`)

API para gestionar seguimientos post-adopción.

**Base de datos**: PostgreSQL `followup_db` (o H2 con perfil `h2`)

**Endpoints** (`/followups`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/followups` | Listar todos (filtro `?status=`) | Público |
| `GET` | `/followups/by-id/{id}` | Obtener por ID | Público |
| `GET` | `/followups/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/followups` | Crear seguimiento | ADMIN |
| `PUT` | `/followups/by-id/{id}` | Actualizar seguimiento | ADMIN |
| `DELETE` | `/followups/by-id/{id}` | Eliminar seguimiento | ADMIN |

## Comunicación entre servicios

```
user-service ──(Feign)──→ notification-service (POST /notifications)

pet-service  ──(Feign)──→ user-service (GET /users/by-email/{email}/auth)
pet-service  ──(Feign)──→ health-service (CRUD /health)
pet-service  ──(Feign)──→ notification-service (POST /notifications)
pet-service  ──(Feign)──→ shelter-service (GET /shelters/by-id/{id})

adoption-service ──(Feign)──→ user-service (GET /users/by-id/{id})
adoption-service ──(Feign)──→ pet-service (GET /pets/by-id/{id})
adoption-service ──(Feign)──→ notification-service (POST /notifications)
adoption-service ──(Feign)──→ shelter-service (GET /shelters/by-id/{id})
adoption-service ──(Feign)──→ followup-service (POST /followups)

health-service ──(Feign)──→ user-service (GET /users/by-email/{email}/auth)
health-service ──(Feign)──→ pet-service (GET /pets/by-id/{id})
health-service ──(Feign)──→ notification-service (POST /notifications)

donation-service ──(Feign)──→ user-service (GET /users/by-email/{email}/auth)
donation-service ──(Feign)──→ notification-service (POST /notifications)
donation-service ──(Feign)──→ shelter-service (GET /shelters/by-id/{id})

followup-service ──(Feign)──→ user-service (GET /users/by-email/{email}/auth)
followup-service ──(Feign)──→ pet-service (GET /pets/by-id/{id})
followup-service ──(Feign)──→ notification-service (POST /notifications)

notification-service ──(Feign)──→ user-service (GET /users/by-email/{email}/auth)

shelter-service ──(Feign)──→ user-service (GET /users/by-email/{email}/auth)
shelter-service ──(Feign)──→ notification-service (POST /notifications)

staff-service ──(Feign)──→ user-service (GET /users/by-email/{email}/auth)
staff-service ──(Feign)──→ notification-service (POST /notifications)
staff-service ──(Feign)──→ shelter-service (GET /shelters/by-id/{id})

supply-service ──(Feign)──→ user-service (GET /users/by-email/{email}/auth)
supply-service ──(Feign)──→ notification-service (POST /notifications)
supply-service ──(Feign)──→ shelter-service (GET /shelters/by-id/{id})
```

## Cómo ejecutar

Cada servicio necesita su propia terminal:

```bash
# Perfil H2 (desarrollo rápido, sin PostgreSQL)
cd notification-service
mvn spring-boot:run -Dspring-boot.run.profiles=h2

cd user-service
mvn spring-boot:run -Dspring-boot.run.profiles=h2

cd pet-service
mvn spring-boot:run -Dspring-boot.run.profiles=h2

cd adoption-service
mvn spring-boot:run
```

**Orden recomendado:** notification → user → health → pet → adoption → donation → followup

## Perfiles de base de datos

| Perfil | Base de datos | Flyway | Uso |
|---|---|---|---|
| `default` | PostgreSQL | habilitado | Producción |
| `h2` | H2 en memoria | deshabilitado | Desarrollo (solo user, pet, notification) |
| `postgres` | PostgreSQL (override) | habilitado | Desarrollo con PostgreSQL |

Variables de entorno requeridas (vía `.env`):
```env
DB_USER=postgres
DB_PASSWORD=1234
```

## Seguridad

- HTTP Basic delegado a **user-service** via `CustomUserDetailsService` + Feign en todos los servicios
- user-service: `CustomUserDetailsService` desde BD
- adoption-service: `CustomUserDetailsService` → user-service
- pet-service: `CustomUserDetailsService` → user-service
- health-service: `CustomUserDetailsService` → user-service
- Roles: `ADOPTER`, `SHELTER_ADMIN`, `VOLUNTEER`, `VET`, `ADMIN`
- `@PreAuthorize` en endpoints sensibles
- `UserSecurity.canEdit()`: ADMIN edita cualquiera; SHELTER_ADMIN edita VOLUNTEER; cada rol edita su propio perfil


## Patrones del Proyecto

### Capas de DTO

```
Request (validación) → Command (servicio) → Result (servicio) → Response (API)
```

### Logging
- Archivos de log por servicio: `logs/{service}.log`
- Perfiles `dev` (DEBUG) y `prod` (INFO)

### Manejo Global de Excepciones
- `GlobalExceptionHandler` con `@RestControllerAdvice` en cada servicio
- `IllegalArgumentException` → 400 Bad Request (409 es para conflictos de concurrencia; corregir pendiente)
- `MethodArgumentNotValidException` → 400 Bad Request
- `Exception` → 500 Internal Server Error

## Historial de Cambios

### Roles y Permisos
- `SHELTER` renombrado a `SHELTER_ADMIN`
- SHELTER_ADMIN mantiene permisos anteriores + puede editar perfiles de VOLUNTEER
- Agregado rol `VOLUNTEER`: edita su perfil y mascotas (excepto ficha médica)
- Agregado rol `VET`: edita su perfil y fichas médicas en health-service
- `ADOPTER` y `ADMIN` se mantienen sin cambios
- Flyway V10 migra datos existentes: `SHELTER` → `SHELTER_ADMIN`

### Notificaciones
- Sistema de tipos de notificación vía `notification_types` (entidad `NotificationType`)
- Tipos para todos los microservicios (Flyway V2 + V3)
- Notification-service en puerto 8084, context-path `/notification-app`
- User-service envía notificaciones al crear/actualizar/eliminar usuarios
- DTOs alineados entre clientes y servidor (`userId`, `recipient`, `message`, `typeName`, `status`)

### Correcciones y Mejoras
- `@EnableFeignClients` y OpenFeign agregados a pet-service y user-service
- Endpoint `DELETE /pets/by-id/{id}` en pet-service
- URLs de datasource estandarizadas con `${DB_HOST:localhost}:${DB_PORT:5432}`
- Eliminados DTOs y Feign clients no utilizados en adoption-service
- Eliminados archivos que anulaban auto-configuración Spring Boot
- Agregados campos: `updatedAt` en Adoption, `shelterId` en Pet
- Migración de `NotificationResponce.java` → `NotificationResponse.java`

## Notas y Limitaciones Conocidas

### Problemas identificados (pendientes de corrección)
- **pet-service**: `DataInitializer.java` referencia métodos que no existen en `Pet.java` (`setVaccinated`, `setSterilized`, `setDiseases`). El servicio puede no arrancar con perfil `h2`.
- **health-service**: La tabla se llama `health` (palabra reservada en algunos motores SQL). Funciona en PostgreSQL pero puede fallar en MySQL.
- **donation-service**: Usa `Double` para montos monetarios en lugar de `BigDecimal` (precisión floating-point).
- **shelter-service**: Soft delete implementado pero `GET /shelters` retorna registros eliminados.
- **followup-service**: Notificaciones enviadas a email hardcodeado `sistema@adoptapp.com`.
- **UserAuthResponse**: 3 firmas diferentes entre servicios (inconsistencia de DTOs de autenticación).
- **Sin paginación**: Los endpoints `GET /resource` retornan listas completas sin paginación.
- **Rutas no RESTful**: Se usa `/resource/by-id/{id}` en lugar del estándar REST `/resource/{id}`.
- **HTTP Basic Auth**: Sin JWT/OAuth2. Credenciales enviadas en Base64 en cada request.

### Decisiones de diseño
- **Database-per-service**: Cada microservicio tiene su propia base de datos PostgreSQL.
- **Feign síncrono**: Comunicación entre servicios vía OpenFeign con fallbacks.
- **Sin API Gateway**: Cada servicio se expone directamente en su propio puerto.
- **Sin CI/CD**: No hay pipelines de GitHub Actions configurados.
