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
├── followup-service/      # Seguimiento post-adopción (puerto 8086, /followup-app)
├── staff-service/         # Gestión de personal (puerto 8091, /staff-app)
├── donation-service/      # Donaciones (puerto 8090, /donation-app)
├── supply-service/        # Insumos de refugio (puerto 8092, /supply-app)
├── shelter-service/       # Gestión de refugios (puerto 8095, /shelter-app)
└── pom.xml                # POM Padre
```

## Stack Tecnológico

| Tecnología | Uso |
|---|---|
| **Java 21** | Runtime |
| **Spring Boot 3.4.5** | Framework |
| **Spring Cloud 2024.0.0** | Feign clients entre servicios |
| **Spring Data JPA** | Acceso a datos con relaciones |
| **PostgreSQL 18 / H2** | Bases de datos (prod / dev) |
| **OpenFeign** | Comunicación sincrónica entre servicios |
| **Resilience4j** | Circuit breaker para tolerancia a fallos |
| **Flyway 11.7.2** | Migraciones de base de datos |
| **Lombok** | Reducción de código repetitivo |
| **Bean Validation** | Validación de DTOs de entrada |
| **SLF4J + Logback** | Logging con archivos por servicio |
| **spring-dotenv** | Carga de variables de entorno desde `.env` |
| **Maven** | Gestión de dependencias |
| **GitHub Actions** | CI/CD pipeline (build + test) |

## Roles del Sistema

| Rol | Permisos |
|---|---|
| **ADOPTER** | Editar su propio perfil. Crear solicitudes de adopcion. |
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
| `GET` | `/users/by-email/{email}/auth` | Auth por email | Público |
| `GET` | `/users/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/users` | Crear usuario | Público (registro) |
| `PUT` | `/users/by-id/{id}` | Actualizar usuario | Propio perfil + SHELTER_ADMIN edita VOLUNTEER |
| `DELETE` | `/users/by-id/{id}` | Eliminar usuario | ADMIN |

**Comunicación saliente:**
- → notification-service (`POST /notifications`): `USER_CREATED`, `USER_UPDATED`, `USER_DELETED`

### pet-service (Puerto 8082, `/pet-app`) — COMPLETO

API CRUD para gestionar mascotas. La informacion clinica no se almacena en pet-service; se consulta y administra desde health-service.

**Base de datos**: PostgreSQL `pet_db` (o H2 con perfil `h2`)

**Endpoints** (`/pets`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/pets` | Listar todas (filtro `?status=`) | Público |
| `GET` | `/pets/by-id/{id}` | Obtener por ID | Público |
| `GET` | `/pets/by-id/{id}/health` | Info de salud | Público |
| `GET` | `/pets/by-id/{id}/history` | Historial de cambios | ADMIN |
| `GET` | `/pets/internal/shelter/{shelterId}/active` | Mascotas activas por refugio para validaciones internas | ADMIN |
| `POST` | `/pets` | Crear mascota | SHELTER_ADMIN, VOLUNTEER, ADMIN |
| `PUT` | `/pets/by-id/{id}` | Actualizar mascota | SHELTER_ADMIN, VOLUNTEER, ADMIN |
| `PATCH` | `/pets/by-id/{id}/status` | Actualizar estado de mascota | SHELTER_ADMIN, VOLUNTEER, ADMIN |
| `DELETE` | `/pets/by-id/{id}` | Eliminar mascota | ADMIN |

**Comunicación saliente:**
- → health-service (`GET /health/by-pet/{petId}`, `DELETE /health/by-pet/{petId}`): consulta y elimina ficha clinica asociada
- → user-service (`GET /users/by-id/{id}`): obtiene email para notificaciones
- → notification-service (`POST /notifications`): `PET_CREATED`, `PET_UPDATED`, `PET_DELETED`

### adoption-service (Puerto 8083, `/adoption-app`) — COMPLETO

API para gestionar adopciones. Crea solicitudes en estado `PENDING`, valida usuario, mascota, refugio y disponibilidad, y sincroniza el estado de la mascota con pet-service al aprobar o cancelar.

**Base de datos**: PostgreSQL `adoption_db` (o H2 con perfil `h2`)

**Endpoints** (`/adoptions`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/adoptions` | Listar adopciones visibles segun rol | ADOPTER, SHELTER_ADMIN, ADMIN |
| `GET` | `/adoptions/by-id/{id}` | Obtener por ID si pertenece al usuario/refugio | ADOPTER, SHELTER_ADMIN, ADMIN |
| `GET` | `/adoptions/admin` | Listar adopciones por estado, incluyendo canceladas si se filtra | SHELTER_ADMIN, ADMIN |
| `GET` | `/adoptions/admin/by-id/{id}` | Obtener adopcion incluyendo canceladas | SHELTER_ADMIN, ADMIN |
| `GET` | `/adoptions/by-id/{id}/history` | Historial de cambios | ADOPTER, SHELTER_ADMIN, ADMIN |
| `POST` | `/adoptions` | Crear solicitud de adopcion en estado PENDING | ADOPTER, SHELTER_ADMIN, ADMIN |
| `PUT` | `/adoptions/by-id/{id}` | Actualizar adopción | SHELTER_ADMIN, ADMIN |
| `DELETE` | `/adoptions/by-id/{id}` | Cancelar adopcion | SHELTER_ADMIN, ADMIN |

**Comunicación saliente:**
- → user-service (`GET /users/by-id/{id}`): verifica usuario
- → pet-service (`GET /pets/by-id/{id}`): verifica mascota
- → pet-service (`PATCH /pets/by-id/{id}/status`): sincroniza disponibilidad de mascota al aprobar/cancelar adopcion
- → notification-service (`POST /notifications`): `ADOPTION_CREATED`, `ADOPTION_UPDATED`, `ADOPTION_DELETED`, `PET_CREATED`, `PET_UPDATED`, `PET_DELETED`

### notification-service (Puerto 8084, `/notification-app`) — COMPLETO

API para gestionar notificaciones. Almacena notificaciones con tipos categorizados vía `@ManyToOne(NotificationType)` y usa soft delete con estado `ARCHIVED`.

**Base de datos**: PostgreSQL `notif_db` (o H2 con perfil `h2`)

**Endpoints** (`/notifications`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/notifications` | Listar notificaciones visibles segun rol (filtro `?status=`) | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| `GET` | `/notifications/by-id/{id}` | Obtener por ID si pertenece al usuario/refugio | ADOPTER, VOLUNTEER, VET, SHELTER_ADMIN, ADMIN |
| `POST` | `/notifications` | Crear notificación | SHELTER_ADMIN, ADMIN |
| `PUT` | `/notifications/by-id/{id}` | Actualizar notificación | SHELTER_ADMIN, ADMIN |
| `DELETE` | `/notifications/by-id/{id}` | Archivar notificacion (`ARCHIVED`) | ADMIN |

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
| `GET` | `/health` | Listar todos (filtro `?vaccinationStatus=`, `?sterilizationStatus=`) | VET, SHELTER_ADMIN, ADMIN |
| `GET` | `/health/by-id/{id}` | Obtener por ID | VET, SHELTER_ADMIN, ADMIN |
| `GET` | `/health/by-pet/{petId}` | Obtener ficha por mascota | VET, SHELTER_ADMIN, ADMIN |
| `GET` | `/health/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/health` | Crear registro clínico | VET, SHELTER_ADMIN, ADMIN |
| `PUT` | `/health/by-id/{id}` | Actualizar registro clínico | VET, SHELTER_ADMIN, ADMIN |
| `DELETE` | `/health/by-id/{id}` | Marcar registro clinico como DELETED | VET, SHELTER_ADMIN, ADMIN |
| `DELETE` | `/health/by-pet/{petId}` | Marcar registro clinico como DELETED por mascota | VET, SHELTER_ADMIN, ADMIN |

**Comunicación saliente:**
- → user-service (`GET /users/by-id/{id}`): obtiene userId del creador
- → pet-service (`GET /pets/by-id/{id}`): verifica mascota
- → notification-service (`POST /notifications`): `HEALTH_CHECK_CREATED`, `HEALTH_CHECK_UPDATED`, `HEALTH_ALERT`

### followup-service (Puerto 8086, `/followup-app`) — COMPLETO

API para gestionar seguimientos post-adopción.

**Base de datos**: PostgreSQL `followup_db` (o H2 con perfil `h2`)

**Endpoints** (`/followups`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/followups` | Listar todos (filtro `?status=`) | Público |
| `GET` | `/followups/by-id/{id}` | Obtener por ID | Público |
| `GET` | `/followups/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/followups` | Crear seguimiento | SHELTER_ADMIN, ADMIN |
| `PUT` | `/followups/by-id/{id}` | Actualizar seguimiento | SHELTER_ADMIN, ADMIN |
| `DELETE` | `/followups/by-id/{id}` | Eliminar seguimiento | SHELTER_ADMIN, ADMIN |

### donation-service (Puerto 8090, `/donation-app`) — COMPLETO

API para gestionar donaciones. Verifica existencia de usuario y refugio vía Feign antes de crear.

**Base de datos**: PostgreSQL `donation_db` (o H2 con perfil `h2`)

**Endpoints** (`/donations`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/donations` | Listar todas (filtro `?status=`) | ADMIN |
| `GET` | `/donations/by-id/{id}` | Obtener por ID | ADMIN |
| `GET` | `/donations/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/donations` | Crear donación | ADMIN |
| `PUT` | `/donations/by-id/{id}` | Actualizar donación | ADMIN |
| `DELETE` | `/donations/by-id/{id}` | Eliminar donación | ADMIN |

**Comunicación saliente:**
- → user-service (`GET /users/by-id/{id}`): verifica usuario
- → notification-service (`POST /notifications`): `DONATION_RECEIVED`, `DONATION_UPDATED`, `DONATION_CANCELLED`
- → shelter-service (`GET /shelters/by-id/{id}`): verifica refugio

### staff-service (Puerto 8091, `/staff-app`) — COMPLETO

API para gestionar personal de refugios.

**Base de datos**: PostgreSQL `staff_db` (o H2 con perfil `h2`)

**Endpoints** (`/staff`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/staff` | Listar todo (filtro `?status=`) | ADMIN |
| `GET` | `/staff/by-id/{id}` | Obtener por ID | ADMIN |
| `GET` | `/staff/by-user/{userId}` | Obtener staff activo por usuario | Autenticado |
| `GET` | `/staff/internal/shelter/{shelterId}/active` | Staff activo por refugio para validaciones internas | ADMIN |
| `GET` | `/staff/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/staff` | Crear personal | SHELTER_ADMIN, ADMIN |
| `PUT` | `/staff/by-id/{id}` | Actualizar personal | SHELTER_ADMIN, ADMIN |
| `DELETE` | `/staff/by-id/{id}` | Eliminar personal | SHELTER_ADMIN, ADMIN |

**Comunicación saliente:**
- → user-service (`GET /users/by-id/{id}`): verifica usuario
- → notification-service (`POST /notifications`): `STAFF_ADDED`, `STAFF_REMOVED`
- → shelter-service (`GET /shelters/by-id/{id}`): verifica refugio

### supply-service (Puerto 8092, `/supply-app`) — COMPLETO

API para gestionar insumos de refugios.

**Base de datos**: PostgreSQL `supply_db` (o H2 con perfil `h2`)

**Endpoints** (`/supplies`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/supplies` | Listar todos (filtro `?status=`) | Público |
| `GET` | `/supplies/by-id/{id}` | Obtener por ID | Público |
| `GET` | `/supplies/shelter/{shelterId}` | Insumos por refugio | Público |
| `GET` | `/supplies/internal/shelter/{shelterId}/active` | Insumos activos por refugio para validaciones internas | ADMIN |
| `GET` | `/supplies/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/supplies` | Crear insumo | ADMIN, SHELTER_ADMIN |
| `PUT` | `/supplies/by-id/{id}` | Actualizar insumo | ADMIN, SHELTER_ADMIN |
| `DELETE` | `/supplies/by-id/{id}` | Eliminar insumo | ADMIN, SHELTER_ADMIN |

**Comunicación saliente:**
- → user-service (`GET /users/by-id/{id}`): verifica usuario
- → notification-service (`POST /notifications`): `SUPPLY_LOW_STOCK`, `SUPPLY_ORDERED`, `SUPPLY_RECEIVED`
- → shelter-service (`GET /shelters/by-id/{id}`): verifica refugio

### shelter-service (Puerto 8095, `/shelter-app`) — COMPLETO

API para gestionar refugios.

**Base de datos**: PostgreSQL `shelter_db` (o H2 con perfil `h2`)

**Endpoints** (`/shelters`):

| Método | Ruta | Descripción | Acceso |
|---|---|---|---|
| `GET` | `/shelters` | Listar todos (filtro `?status=`) | Público |
| `GET` | `/shelters/by-id/{id}` | Obtener por ID | Público |
| `GET` | `/shelters/by-id/{id}/history` | Historial de cambios | ADMIN |
| `POST` | `/shelters` | Crear refugio usando usuario autenticado | SHELTER_ADMIN, ADMIN |
| `PUT` | `/shelters/by-id/{id}` | Actualizar refugio | SHELTER_ADMIN, ADMIN |
| `DELETE` | `/shelters/by-id/{id}` | Marcar refugio como DELETED si no tiene dependientes activos | SHELTER_ADMIN, ADMIN |

**Comunicación saliente:**
- → user-service (`GET /users/by-id/{id}`): verifica usuario
- → notification-service (`POST /notifications`): `SHELTER_CREATED`, `SHELTER_UPDATED`, `SHELTER_DELETED`
- → pet-service (`GET /pets/internal/shelter/{shelterId}/active`): valida mascotas activas antes de borrar refugio
- → staff-service (`GET /staff/internal/shelter/{shelterId}/active`): valida staff activo antes de borrar refugio
- → supply-service (`GET /supplies/internal/shelter/{shelterId}/active`): valida insumos activos antes de borrar refugio

## Comunicación entre servicios

```
user-service ──(Feign)──→ notification-service (POST /notifications)

pet-service  ──(Feign)──→ user-service (GET /users/by-id/{id})
pet-service  ──(Feign)──→ health-service (GET /health/by-pet/{petId}, DELETE /health/by-pet/{petId})
pet-service  ──(Feign)──→ notification-service (POST /notifications)
pet-service  ──(Feign)──→ shelter-service (GET /shelters/by-id/{id})

adoption-service ──(Feign)──→ user-service (GET /users/by-id/{id})
adoption-service ──(Feign)──→ pet-service (GET /pets/by-id/{id}, PATCH /pets/by-id/{id}/status)
adoption-service ──(Feign)──→ notification-service (POST /notifications)
adoption-service ──(Feign)──→ shelter-service (GET /shelters/by-id/{id})
adoption-service ──(Feign)──→ followup-service (POST /followups)

health-service ──(Feign)──→ user-service (GET /users/by-id/{id})
health-service ──(Feign)──→ pet-service (GET /pets/by-id/{id})
health-service ──(Feign)──→ notification-service (POST /notifications)

donation-service ──(Feign)──→ user-service (GET /users/by-id/{id})
donation-service ──(Feign)──→ notification-service (POST /notifications)
donation-service ──(Feign)──→ shelter-service (GET /shelters/by-id/{id})

followup-service ──(Feign)──→ user-service (GET /users/by-id/{id})
followup-service ──(Feign)──→ pet-service (GET /pets/by-id/{id})
followup-service ──(Feign)──→ notification-service (POST /notifications)

notification-service ──(Feign)──→ user-service (GET /users/by-id/{id})

shelter-service ──(Feign)──→ user-service (GET /users/by-id/{id})
shelter-service ──(Feign)──→ notification-service (POST /notifications)
shelter-service ──(Feign)──→ pet-service (GET /pets/internal/shelter/{shelterId}/active)
shelter-service ──(Feign)──→ staff-service (GET /staff/internal/shelter/{shelterId}/active)
shelter-service ──(Feign)──→ supply-service (GET /supplies/internal/shelter/{shelterId}/active)

staff-service ──(Feign)──→ user-service (GET /users/by-id/{id})
staff-service ──(Feign)──→ notification-service (POST /notifications)
staff-service ──(Feign)──→ shelter-service (GET /shelters/by-id/{id})

supply-service ──(Feign)──→ user-service (GET /users/by-id/{id})
supply-service ──(Feign)──→ notification-service (POST /notifications)
supply-service ──(Feign)──→ shelter-service (GET /shelters/by-id/{id})
```

## Cómo ejecutar

### Build del proyecto completo

```bash
# Desde la raiz del proyecto
.\mvnw.cmd clean install -DskipTests
```

### Ejecutar con Maven Wrapper

Cada servicio necesita su propia terminal:

```bash
# Perfil PostgreSQL (default)
cd donation-service
.\mvnw.cmd spring-boot:run

cd user-service
.\mvnw.cmd spring-boot:run

# Perfil H2 (desarrollo rapido, sin PostgreSQL)
cd donation-service
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
```

### Ejecutar todos los servicios con H2 (un solo comando)

```powershell
# Desde la raiz del proyecto, abre una terminal por cada servicio
.\start-all-h2.ps1
```

### Ejecutar desde IntelliJ IDEA

1. Abrir el proyecto como proyecto Maven
2. Hacer clic derecho en `pom.xml` → **Maven** → **Reload project**
3. Ir a **Run** → **Edit Configurations** → agregar configuracion para cada servicio
4. En **VM options** agregar `--spring.profiles.active=h2` para usar H2

**Orden recomendado:** notification → user → shelter → health → pet → adoption → donation → followup → staff → supply

## Perfiles de base de datos

| Perfil | Base de datos | Flyway | Uso |
|---|---|---|---|
| `default` | PostgreSQL 18 | habilitado | Produccion |
| `h2` | H2 en memoria | deshabilitado | Desarrollo |
| `postgres` | PostgreSQL (override) | habilitado | Desarrollo con PostgreSQL |

Variables de entorno requeridas (via `.env`):
```env
DB_HOST=localhost
DB_PORT=5432
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
- Endpoints internos `/internal/**` restringidos a rol `ADMIN`
- `UserSecurity.canEdit()`: ADMIN edita cualquiera; SHELTER_ADMIN edita VOLUNTEER; cada rol edita su propio perfil
- **FeignAuthInterceptor**: Propaga automáticamente el header `Authorization` entre servicios via Feign
- **GlobalExceptionHandler**: Manejo centralizado de excepciones en los 10 servicios con respuestas seguras (no expone stack traces)
- **Structured logging**: Todos los métodos de notification-service incluyen logging estructurado con requestId
- **Enum validation**: Todos los `Enum.valueOf()` protegidos con try/catch para evitar 500 por valores inválidos
- **HikariCP pool size**: 5 conexiones por servicio (50 total) para evitar agotamiento de PostgreSQL `max_connections=100`
- **Shared DTO**: `UserAuthResponse` centralizado en `shared-kernel` para eliminar duplicación entre 9 servicios

## Patrones del Proyecto

### Capas de DTO

```
Request (validacion) → Command (servicio) → Result (servicio) → Response (API)
```

### Logging
- Archivos de log por servicio: `logs/{service}.log`
- Perfiles `dev` (DEBUG) y `prod` (INFO)
- Logging estructurado en notification-service con campos: `method`, `userId`, `recipient`, `status`

### Manejo Global de Excepciones
- `GlobalExceptionHandler` con `@RestControllerAdvice` en cada servicio
- `IllegalArgumentException` → 400 Bad Request
- `MethodArgumentNotValidException` → 400 Bad Request
- `UnauthorizedException` → 401 Unauthorized
- `ForbiddenException` → 403 Forbidden
- `ValidationException` → 400 Bad Request
- `BusinessException` → 400 Bad Request
- `Exception` (catch-all) → 500 Internal Server Error (mensaje genérico, log interno con stack trace)

### Resiliencia
- **Resilience4j Circuit Breaker**: Habilitado en 9 servicios con Feign clients
  - `slidingWindowSize`: 10
  - `failureRateThreshold`: 50%
  - `waitDurationInOpenState`: 30s
- **Feign Fallbacks**: Retornan `ResponseEntity.status(503)` cuando servicios remotos están caídos
- **Try/Catch en Feign calls**: Envoltura de todas las llamadas remotas en 6 servicios críticos

### Reglas de Negocio Transversales
- **Soft delete**: registros eliminados se marcan con estados logicos (`CANCELLED`, `DELETED`, `INACTIVE`, `ARCHIVED`) en lugar de borrarse fisicamente.
- **Bloqueo de operaciones sobre eliminados**: no se permite actualizar ni eliminar nuevamente registros con soft delete.
- **Filtros invalidos**: Valores invalidos en filtros por estado retornan 400 Bad Request en vez de listas vacias.
- **Adopciones**: nacen en `PENDING`; solo `APPROVED` cambia la mascota a `NOT_AVAILABLE`; cancelar una adopcion aprobada vuelve la mascota a `AVAILABLE`.
- **Mascotas**: al eliminar una mascota primero se elimina su ficha clinica en health-service; si falla, la mascota no se marca como `DELETED`.
- **Refugios**: no se pueden marcar como `DELETED` si tienen mascotas, staff o supplies activos.
- **Sin reintentos automaticos**: adoption-service y pet-service fallan la operacion si no pueden sincronizar estados dependientes.

### Base de Datos
- **Foreign Keys**: `donation_history` tiene FK explicitas via `V3__add_foreign_keys_to_donations.sql`
- **Unique Constraints**: `health` tabla tiene constraint unico en `pet_id` via `V3__add_unique_constraint_pet_id.sql`
- **Entity-Migration Alignment**: Correcciones en `User.email`, `UserPhone.number`, `UserAddress.homeNumber`, `Donation` fields
- **Flyway Repair**: `flyway.repair-on-migrate=true` habilitado para reparar checksums automaticamente
- **Schema Isolation**: `flyway.create-schemas=true` para crear schemas automaticamente en cada base de datos
- **Neon Postgres**: Soporte configurado para Neon Serverless Postgres con aislamiento de schemas

### CI/CD
- Pipeline GitHub Actions en `.github/workflows/build.yml`
- Trigger: push/PR a `main`/`master`
- Jobs: `mvn clean compile -B` + `mvn test -B` (H2 in-memory para tests)
- Maven dependency caching habilitado para builds mas rapidos

## Historial de Cambios

### Roles y Permisos
- `SHELTER` renombrado a `SHELTER_ADMIN`
- SHELTER_ADMIN mantiene permisos anteriores + puede editar perfiles de VOLUNTEER
- Agregado rol `VOLUNTEER`: edita su perfil y mascotas (excepto ficha medica)
- Agregado rol `VET`: edita su perfil y fichas medicas en health-service
- `ADOPTER` y `ADMIN` se mantienen sin cambios
- Flyway V10 migra datos existentes: `SHELTER` → `SHELTER_ADMIN`

### Notificaciones
- Sistema de tipos de notificacion via `notification_types` (entidad `NotificationType`)
- Tipos para todos los microservicios (Flyway V2 + V3)
- Notification-service en puerto 8084, context-path `/notification-app`
- User-service envia notificaciones al crear/actualizar/eliminar usuarios
- DTOs alineados entre clientes y servidor (`userId`, `recipient`, `message`, `typeName`, `status`)

### Correcciones y Mejoras
- Flyway actualizado a 11.7.2 con soporte PostgreSQL 18 (`flyway-database-postgresql`)
- `@EnableFeignClients` y OpenFeign agregados a pet-service y user-service
- Endpoint `DELETE /pets/by-id/{id}` en pet-service
- URLs de datasource estandarizadas con `${DB_HOST:localhost}:${DB_PORT:5432}`
- Eliminados DTOs y Feign clients no utilizados en adoption-service
- Eliminados archivos que anulaban auto-configuracion Spring Boot
- Agregados campos: `updatedAt` en Adoption, `shelterId` en Pet
- Migracion de `NotificationResponce.java` → `NotificationResponse.java`
- donation-service: campos monetarios migrados de `Double` a `BigDecimal`
- user-service: corregido `findByStatusIgnoreCase` en repositorio (enum no soporta IgnoreCase)
- user-service: agregada migracion V3 para columna `updated_at`
- health-service: agregado driver PostgreSQL al pom.xml
- Todos los servicios: agregado `flyway-database-postgresql` dependency

### Auditoria Tecnica y Mejoras Criticas (2026-05)
- **Resilience4j**: Circuit breaker agregado a 9 servicios (config: slidingWindowSize=10, failureRateThreshold=50%, waitDuration=30s)
- **Feign Fallbacks**: supply-service y notification-service ahora retornan `ResponseEntity.status(503)` en lugar de `null`
- **Feign Auth Propagation**: `FeignAuthInterceptor` como `@Component` en 9 servicios para propagar `Authorization` headers
- **Exception Handling**: `UnauthorizedException`, `ForbiddenException`, `ValidationException`, `BusinessException` manejados en los 10 servicios
- **Secure Catch-All**: Exception handler genérico retorna mensaje sin stack trace, log interno con detalles completos
- **Structured Logging**: Todos los métodos de notification-service incluyen logging con campos estructurados
- **Enum Validation**: filtros con valores de enum invalidos responden 400 Bad Request en todos los servicios principales
- **Shared DTO**: `UserAuthResponse` movido a shared-kernel; 9 servicios actualizados para usar DTO centralizado
- **HikariCP Pool**: `maximum-pool-size: 5` en todos los servicios para evitar agotamiento de conexiones PostgreSQL
- **Database Fixes**:
  - `V3__add_foreign_keys_to_donations.sql` en donation-service
  - `V3__add_unique_constraint_pet_id.sql` en health-service
  - Corrección de entity↔migration mismatches (`User.email`, `UserPhone.number`, `UserAddress.homeNumber`, `Donation` fields)
  - Corrección de DB name en notification-service: `notif_db` (era `notification_db`)
- **Security**: GET endpoints restringidos por roles en health, staff, donation, followup services
- **Supply Service**: Refactorizado mapeo DTO con helper `applyCommandToEntity()`
- **CI/CD**: Pipeline GitHub Actions `.github/workflows/build.yml` para build automático y tests
- **Dead Code**: Eliminados 4 archivos `UserRole.java` no utilizados (adoption, donation, health, pet)
- **Role Mapping**: user-service retorna `user.getRole().name()` para compatibilidad con shared-kernel `UserAuthResponse`

### Reglas de Negocio y Soft Delete (2026-06)
- **Soft delete generalizado**: donation (`CANCELLED`), followup (`CANCELLED`), pet (`DELETED`), health (`DELETED`), staff (`INACTIVE`), supply (`INACTIVE`), user (`INACTIVE`), shelter (`DELETED`) y notification (`ARCHIVED`).
- **Bloqueo de operaciones sobre registros eliminados**: update/delete repetido se rechaza para estados logicos eliminados.
- **Adoption-service**: las adopciones nacen siempre en `PENDING`; `APPROVED` sincroniza mascota a `NOT_AVAILABLE`; `CANCELLED` puede volver mascota a `AVAILABLE`.
- **Sin reintentos en adoption-service**: si pet-service no puede sincronizar el estado de la mascota, falla la operacion.
- **Pet-service**: si health-service no puede eliminar la ficha clinica asociada, la mascota no se marca como `DELETED`.
- **Shelter-service**: no permite eliminar refugios con mascotas, staff o supplies activos.
- **Notification-service**: `DELETE` archiva notificaciones como `ARCHIVED`; solo ADMIN puede ver archivadas.
- **Filtros invalidos**: filtros por estado invalidos devuelven 400 Bad Request.
- **Endpoints internos**: rutas `/internal/**` usadas para validaciones entre servicios protegidas con rol `ADMIN`.

## Notas y Limitaciones Conocidas

### Problemas identificados (pendientes de correccion)
- **health-service**: La tabla se llama `health` (palabra reservada en algunos motores SQL). Funciona en PostgreSQL pero puede fallar en MySQL.
- **followup-service**: Notificaciones enviadas a email hardcodeado `sistema@adoptapp.com`.
- **Sin paginacion**: Los endpoints `GET /resource` retornan listas completas sin paginacion.
- **Rutas no RESTful**: Se usa `/resource/by-id/{id}` en lugar del estandar REST `/resource/{id}`.
- **HTTP Basic Auth**: Sin JWT/OAuth2. Credenciales enviadas en Base64 en cada request.

### Decisiones de diseno
- **Database-per-service**: Cada microservicio tiene su propia base de datos PostgreSQL.
- **Feign sincrono**: Comunicacion entre servicios via OpenFeign con fallbacks y circuit breakers.
- **Sin API Gateway**: Cada servicio se expone directamente en su propio puerto.
- **Shared Kernel**: `UserAuthResponse` centralizado para evitar duplicación de DTOs de autenticación. Excepciones compartidas (`BusinessException`, `ForbiddenException`, `UnauthorizedException`, `ValidationException`, `ResourceNotFoundException`, `RemoteServiceException`) y `ErrorResponseFactory` para respuestas consistentes.
- **HikariCP pool**: 5 conexiones por servicio para mantener 50 conexiones totales < PostgreSQL `max_connections=100`.

## Testing

### Coleccion Postman
Archivo `postman-collection.json` en la raiz con todos los endpoints de los 10 servicios. Usa Basic Auth con variables `username` y `password`. URLs base configuradas como variables de coleccion.

### Comandos cURL
Ver `test-commands.txt` en la raiz del proyecto para comandos curl de prueba de todos los servicios, organizados por perfil (PostgreSQL y H2).

### Tests Unitarios
Todos los servicios incluyen tests de integracion con H2 in-memory (`*ApplicationTests.java`). Ejecutar con:
```bash
.\mvnw.cmd test
```

### Schema SQL Completo
Archivo `schema.sql` en la raiz con el esquema completo de las 10 bases de datos PostgreSQL. Util para inicializacion manual o referencia.
