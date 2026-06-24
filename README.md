# AdoptApp - Plataforma de Adopción de Mascotas

Plataforma de adopción de mascotas basada en microservicios con **Spring Boot 3.4.5** y **Java 21**, diseñada para gestionar el ciclo de vida completo de adopciones en refugios.

## Arquitectura

Proyecto multi-módulo Maven con 10 microservicios, un API Gateway, un servidor Eureka y un shared kernel:

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
├── api-gateway/           # Punto de entrada y enrutamiento (puerto 8080)
├── eureka-server/         # Registro y descubrimiento (puerto 8761)
├── shared-kernel/         # DTOs, excepciones y utilidades compartidas
├── compose.yml            # Orquestación de los 12 contenedores
└── pom.xml                # POM padre
```

## Stack Tecnológico

| Tecnología | Uso |
|---|---|
| **Java 21** | Runtime |
| **Spring Boot 3.4.5** | Framework |
| **Spring Cloud 2024.0.0** | OpenFeign, Gateway, LoadBalancer y Eureka |
| **Spring Data JPA** | Acceso a datos con relaciones |
| **Supabase PostgreSQL / H2** | Base de datos online por schemas / tests y desarrollo |
| **Spring Cloud Gateway** | Punto de entrada único con rutas `lb://` |
| **Netflix Eureka** | Registro y descubrimiento de servicios |
| **Docker Compose** | Construcción y ejecución de los 12 componentes |
| **Springdoc OpenAPI** | Documentación Swagger de las APIs |
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

**Base de datos**: Supabase PostgreSQL, schema `users_service` (o H2 con perfil `h2`)

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

**Base de datos**: Supabase PostgreSQL, schema `pet_service` (o H2 con perfil `h2`)

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

**Base de datos**: Supabase PostgreSQL, schema `adoption_service` (o H2 con perfil `h2`)

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

**Base de datos**: Supabase PostgreSQL, schema `notification_service` (o H2 con perfil `h2`)

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

**Base de datos**: Supabase PostgreSQL, schema `health_service` (o H2 con perfil `h2`)

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

**Base de datos**: Supabase PostgreSQL, schema `followup_service` (o H2 con perfil `h2`)

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

**Base de datos**: Supabase PostgreSQL, schema `donation_service` (o H2 con perfil `h2`)

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

**Base de datos**: Supabase PostgreSQL, schema `staff_service` (o H2 con perfil `h2`)

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

**Base de datos**: Supabase PostgreSQL, schema `supply_service` (o H2 con perfil `h2`)

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

**Base de datos**: Supabase PostgreSQL, schema `shelter_service` (o H2 con perfil `h2`)

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

## Infraestructura y descubrimiento

### API Gateway

El Gateway escucha en `http://localhost:8080` y publica los mismos context paths de los microservicios. Sus 10 rutas usan `lb://<spring.application.name>` para resolver instancias registradas en Eureka.

| Servicio | URL directa | URL mediante Gateway |
|---|---|---|
| user-service | `http://localhost:8081/user-app/users` | `http://localhost:8080/user-app/users` |
| pet-service | `http://localhost:8082/pet-app/pets` | `http://localhost:8080/pet-app/pets` |
| adoption-service | `http://localhost:8083/adoption-app/adoptions` | `http://localhost:8080/adoption-app/adoptions` |

El Gateway reenvía el encabezado `Authorization`; cada microservicio continúa aplicando HTTP Basic y sus reglas de autorización.

### Eureka Server

- Panel: `http://localhost:8761`
- Registro REST: `http://localhost:8761/eureka/apps`
- Todos los clientes usan `prefer-ip-address: true` dentro de Docker.
- Eureka no se registra a sí mismo ni consulta otro registro.
- El estado esperado es 11 aplicaciones `UP`: los 10 microservicios y `API-GATEWAY`.

### Docker Compose

`compose.yml` levanta 12 contenedores. Eureka incluye un healthcheck contra `/actuator/health`; los demás componentes esperan `service_healthy` antes de comenzar. Las dependencias adicionales usan `service_started`.

Los servicios pueden tardar cerca de dos minutos en iniciar mientras Flyway, JPA y Supabase se estabilizan. Durante ese periodo el Gateway puede responder temporalmente `503`; una vez actualizado el registro, una ruta protegida sin credenciales debe responder `401`.

## Cómo ejecutar

### Build del proyecto completo

```bash
# Desde la raiz del proyecto
.\mvnw.cmd clean package -DskipTests
```

Este paso es obligatorio antes de construir las imágenes porque los Dockerfile copian los JAR desde `target/`.

### Variables para Supabase

Crear un archivo `.env` en la raíz. Está ignorado por Git y no debe incluirse en commits:

```env
DB_URL=jdbc:postgresql://aws-1-us-east-1.pooler.supabase.com:6543/postgres?sslmode=require&prepareThreshold=0
DB_USER=postgres.<PROJECT_REF>
DB_PASSWORD=<SUPABASE_PASSWORD>
```

Cada contenedor recibe su propio `DB_SCHEMA`. Flyway crea y migra automáticamente los schemas `users_service`, `pet_service`, `adoption_service`, `notification_service`, `health_service`, `followup_service`, `donation_service`, `staff_service`, `supply_service` y `shelter_service`.

### Ejecutar con Docker Compose

```powershell
.\mvnw.cmd clean package -DskipTests
docker compose build
docker compose up -d

docker compose ps
docker compose logs -f eureka-server api-gateway
```

Eureka debe mostrarse como `Up (healthy)`. Para detener los contenedores:

```powershell
docker compose down
```

Después de modificar código Java:

```powershell
.\mvnw.cmd package -DskipTests
docker compose up -d --build --force-recreate
```

### Ejecutar con Maven Wrapper

Cada servicio necesita su propia terminal:

```bash
# Perfil Supabase (default)
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

Para ejecución manual, iniciar primero `eureka-server`, después los microservicios y finalmente `api-gateway`.

## Perfiles de base de datos

| Perfil | Base de datos | Flyway | Uso |
|---|---|---|---|
| `supabase` (default) | Supabase PostgreSQL | habilitado | Ejecución principal |
| `h2` | H2 en memoria | deshabilitado | Desarrollo |
| `postgres` | PostgreSQL local | habilitado | Desarrollo local opcional |

Variables de entorno requeridas (via `.env`):
```env
DB_URL=jdbc:postgresql://<SUPABASE_POOLER>:6543/postgres?sslmode=require&prepareThreshold=0
DB_USER=postgres.<PROJECT_REF>
DB_PASSWORD=<SUPABASE_PASSWORD>
```

El perfil Supabase configura HikariCP con `maximum-pool-size: 2` y `minimum-idle: 0` por servicio para evitar agotar las conexiones del pooler.

## OpenAPI, Swagger y HATEOAS

Los 10 microservicios incluyen Springdoc OpenAPI. La documentacion se consulta directamente en cada puerto:

```text
http://localhost:<PUERTO>/<CONTEXT_PATH>/doc/swagger-ui/index.html
```

Ejemplos:

- user-service: `http://localhost:8081/user-app/doc/swagger-ui/index.html`
- pet-service: `http://localhost:8082/pet-app/doc/swagger-ui/index.html`
- adoption-service: `http://localhost:8083/adoption-app/doc/swagger-ui/index.html`

Tambien se pueden acceder mediante API Gateway:

```text
http://localhost:8080/<CONTEXT_PATH>/doc/swagger-ui/index.html
```

Todos los microservicios tienen configuracion OpenAPI con `basicAuth`, por lo que Swagger UI muestra el boton **Authorize** para probar endpoints protegidos con HTTP Basic.

La documentacion incluye:

- `@Tag` para agrupar endpoints por microservicio.
- `@Operation` con resumen y descripcion.
- `@ApiResponses` para documentar respuestas exitosas y errores.
- DTOs documentados con `@Schema`.
- `ErrorResponse` documentado desde shared-kernel.
- Descripcion de respuestas HATEOAS indicando que incluyen enlaces en `_links`.

Los endpoints principales de consulta retornan respuestas con HATEOAS usando `EntityModel` o `CollectionModel`, agregando enlaces como `self`, listados relacionados y acciones disponibles segun el recurso.

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
- **HikariCP pool size**: 2 conexiones máximas y 0 conexiones mínimas por servicio en el perfil Supabase
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
- **Schema Isolation**: `flyway.create-schemas=true` para crear automáticamente un schema por microservicio
- **Supabase PostgreSQL**: conexión mediante transaction pooler en puerto 6543, SSL y `prepareThreshold=0`

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
- **HikariCP Pool**: `maximum-pool-size: 2` y `minimum-idle: 0` en todos los perfiles Supabase
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

### Supabase, Docker y Service Discovery (2026-06)
- **Supabase**: migración desde PostgreSQL local a un proyecto online con 10 schemas aislados.
- **Transaction pooler**: conexión por puerto 6543 con SSL y `prepareThreshold=0`.
- **Docker**: Dockerfile con Java 21 y usuario sin privilegios para cada componente; orquestación central en `compose.yml`.
- **Eureka Server**: registro en puerto 8761, healthcheck de Actuator y espera mediante `service_healthy`.
- **Eureka Client**: agregado a los 10 microservicios y al API Gateway con registro por IP.
- **API Gateway**: agregado en puerto 8080 con 10 rutas `lb://` y Spring Cloud LoadBalancer.
- **Verificación**: los 12 contenedores levantan, las 11 aplicaciones cliente se registran como `UP` y las rutas protegidas responden `401` sin credenciales.
- **OpenAPI**: Springdoc habilitado en los 10 microservicios con Swagger UI bajo `/doc/swagger-ui/index.html`.

### Documentacion, HATEOAS y Testing (2026-06)
- **OpenAPI Security**: agregado `basicAuth` en la configuracion OpenAPI de todos los microservicios.
- **Swagger UI**: endpoints agrupados con `@Tag`, operaciones documentadas con `@Operation` y errores documentados con `ErrorResponse`.
- **DTOs documentados**: requests y responses principales documentados con `@Schema`.
- **HATEOAS**: agregado soporte en controllers principales para devolver enlaces `_links` en respuestas de consulta.
- **Postman por Gateway**: coleccion Postman actualizada para enrutar todas las llamadas mediante `http://localhost:8080`.
- **Pruebas unitarias**: agregadas pruebas con JUnit y Mockito para services, controllers y client fallbacks.
- **JaCoCo**: configurado para medir cobertura de pruebas por microservicio.

## Notas y Limitaciones Conocidas

### Problemas identificados (pendientes de correccion)
- **health-service**: La tabla se llama `health` (palabra reservada en algunos motores SQL). Funciona en PostgreSQL pero puede fallar en MySQL.
- **followup-service**: Notificaciones enviadas a email hardcodeado `sistema@adoptapp.com`.
- **Sin paginacion**: Los endpoints `GET /resource` retornan listas completas sin paginacion.
- **Rutas no RESTful**: Se usa `/resource/by-id/{id}` en lugar del estandar REST `/resource/{id}`.
- **HTTP Basic Auth**: Sin JWT/OAuth2. Credenciales enviadas en Base64 en cada request.
- **Arranque inicial**: JPA, Flyway y Supabase pueden tardar cerca de dos minutos; el Gateway puede responder `503` hasta el siguiente refresco de Eureka.
- **Build Docker en dos pasos**: los Dockerfile copian JAR desde `target`, por lo que Maven debe ejecutarse antes de construir imágenes.
- **Feign con URL fija**: Eureka resuelve las rutas del Gateway; las llamadas Feign internas todavía usan URLs configuradas por entorno.
- **Codigo muerto pendiente de limpieza**: `DonationRequest` ya no se usa porque donation-service separa `DonationCreateRequest` y `DonationUpdateRequest`.
- **DTOs remotos con campos antiguos**: algunos `PetResponse` en followup-service y health-service aun conservan campos de salud (`vaccinated`, `sterilized`, `diseases`) aunque la salud se maneja en health-service.
- **Tests duplicados en adoption-service**: existen pruebas de fallbacks especificas y una prueba general `ClientFallbackTest` que cubren parte del mismo comportamiento.

### Decisiones de diseno
- **Schema-per-service**: Los microservicios comparten el proyecto PostgreSQL de Supabase, pero cada uno usa un schema aislado.
- **Feign sincrono**: Comunicacion entre servicios via OpenFeign con fallbacks y circuit breakers.
- **API Gateway**: Punto de entrada en el puerto 8080 con rutas `lb://` resueltas mediante Eureka. Los puertos directos se mantienen expuestos para desarrollo.
- **Eureka**: Los 10 microservicios y el Gateway se registran por IP dentro de la red de Docker.
- **Shared Kernel**: `UserAuthResponse` centralizado para evitar duplicación de DTOs de autenticación. Excepciones compartidas (`BusinessException`, `ForbiddenException`, `UnauthorizedException`, `ValidationException`, `ResourceNotFoundException`, `RemoteServiceException`) y `ErrorResponseFactory` para respuestas consistentes.
- **HikariCP pool**: 2 conexiones máximas por servicio para respetar los límites del pooler de Supabase.

## Testing

### Coleccion Postman
Archivo `postman-collection.json` en la raiz con todos los endpoints de los 10 servicios. Usa Basic Auth con variables `username` y `password`. URLs base configuradas como variables de coleccion.

La coleccion esta configurada para usar API Gateway como punto de entrada:

```text
http://localhost:8080
```

Variables principales:

```text
username=admin@empresa.com
password=admin123
userBaseUrl=http://localhost:8080/user-app
petBaseUrl=http://localhost:8080/pet-app
adoptionBaseUrl=http://localhost:8080/adoption-app
notificationBaseUrl=http://localhost:8080/notification-app
healthBaseUrl=http://localhost:8080/health-app
followupBaseUrl=http://localhost:8080/followup-app
donationBaseUrl=http://localhost:8080/donation-app
staffBaseUrl=http://localhost:8080/staff-app
supplyBaseUrl=http://localhost:8080/supply-app
shelterBaseUrl=http://localhost:8080/shelter-app
```

Las llamadas fueron verificadas mediante API Gateway con credenciales de administrador y respondieron `200 OK` en los endpoints principales de los 10 microservicios.

### Comandos cURL
Ver `test-commands.txt` en la raiz del proyecto para comandos curl de prueba de todos los servicios, organizados por perfil (PostgreSQL y H2).

### Tests Unitarios
Los microservicios incluyen pruebas unitarias organizadas por capa:

```text
src/test/java/
|-- service/      # Reglas de negocio con JUnit + Mockito
|-- controller/   # Pruebas simples de controller con Mockito directo
+-- client/       # Pruebas de fallbacks de Feign clients
```

Estrategia usada:

- `ServiceTest`: valida reglas de negocio esenciales con repositorios y clients mockeados.
- `ControllerTest`: valida respuestas del controller usando services mockeados.
- `ClientFallbackTest`: valida comportamiento de fallbacks cuando un servicio remoto no responde.
- `ApplicationTests`: valida carga basica del contexto con perfil `h2`.

Ejecutar todos los tests:

```bash
.\mvnw.cmd test
```

Ejecutar tests de un microservicio especifico:

```bash
.\mvnw.cmd -pl adoption-service test
```

Ejecutar JaCoCo:

```bash
.\mvnw.cmd clean test jacoco:report
```

Los reportes se generan en:

```text
<microservicio>/target/site/jacoco/index.html
```

### Schema SQL Completo
Archivo `schema.sql` en la raiz con el esquema completo de las 10 bases de datos PostgreSQL. Util para inicializacion manual o referencia.
