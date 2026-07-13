# AdoptAPP - Plataforma de Adopcion de Mascotas

AdoptAPP es una plataforma backend basada en microservicios con **Spring Boot 3.4.5** y **Java 21**, disenada para gestionar el ciclo completo de adopcion de mascotas en refugios.

El sistema permite administrar usuarios, refugios, personal, mascotas, fichas clinicas, adopciones, seguimientos, donaciones, insumos y notificaciones.

## Arquitectura

Proyecto multi-modulo Maven con 10 microservicios, un API Gateway, un servidor Eureka y un shared kernel.

```text
adoptapp/
|-- user-service/          # Gestion de usuarios
|-- pet-service/           # Gestion de mascotas
|-- adoption-service/      # Gestion de adopciones
|-- notification-service/  # Gestion de notificaciones
|-- health-service/        # Fichas clinicas
|-- followup-service/      # Seguimiento post-adopcion
|-- donation-service/      # Donaciones
|-- staff-service/         # Personal de refugios
|-- supply-service/        # Insumos
|-- shelter-service/       # Refugios
|-- api-gateway/           # Punto de entrada unico
|-- eureka-server/         # Registro y descubrimiento de servicios
|-- shared-kernel/         # DTOs, excepciones y utilidades compartidas
|-- compose.yml            # Orquestacion local con Docker Compose
|-- postman-collection.json
|-- schema.sql
|-- .env.example
|-- .dockerignore
+-- pom.xml
```

## Stack Tecnologico

| Tecnologia | Uso |
|---|---|
| Java 21 | Runtime principal |
| Spring Boot 3.4.5 | Framework principal |
| Spring Cloud 2024.0.0 | Gateway, OpenFeign, LoadBalancer y Eureka |
| Spring Data JPA | Persistencia |
| Spring Security | Seguridad y roles |
| OpenFeign | Comunicacion entre microservicios |
| Netflix Eureka | Registro y descubrimiento |
| Spring Cloud Gateway | Punto de entrada unico |
| Neon PostgreSQL | Base de datos online |
| H2 | Base de datos para pruebas |
| Flyway | Migraciones automaticas |
| Docker / Docker Compose | Contenedores y ejecucion local |
| Render | Despliegue cloud |
| Springdoc OpenAPI | Swagger UI |
| HATEOAS | Enlaces en respuestas principales |
| JUnit, Mockito y JaCoCo | Pruebas y cobertura |

## Microservicios

| Servicio | Puerto local | Context path | Responsabilidad |
|---|---:|---|---|
| user-service | 8081 | `/user-app` | Usuarios, roles y permisos |
| pet-service | 8082 | `/pet-app` | Mascotas |
| adoption-service | 8083 | `/adoption-app` | Adopciones |
| notification-service | 8084 | `/notification-app` | Notificaciones |
| health-service | 8085 | `/health-app` | Fichas clinicas |
| followup-service | 8086 | `/followup-app` | Seguimiento post-adopcion |
| donation-service | 8090 | `/donation-app` | Donaciones |
| staff-service | 8091 | `/staff-app` | Personal |
| supply-service | 8092 | `/supply-app` | Insumos |
| shelter-service | 8095 | `/shelter-app` | Refugios |
| api-gateway | 8080 | `/` | Enrutamiento |
| eureka-server | 8761 | `/` | Service discovery |

Los puertos estan configurados con fallback local usando la variable `PORT`, por ejemplo:

```yaml
server:
  port: ${PORT:8081}
```

Esto permite que Render use su propio puerto interno y que localmente se mantengan los puertos conocidos.

## Roles Del Sistema

| Rol | Descripcion |
|---|---|
| ADMIN | Acceso completo |
| SHELTER_ADMIN | Administra recursos de su refugio |
| ADOPTER | Crea solicitudes de adopcion |
| VOLUNTEER | Colabora en gestion de mascotas |
| VET | Gestiona fichas clinicas |

## Reglas De Negocio Principales

- Las adopciones nacen siempre en estado `PENDING`.
- Una adopcion puede pasar a `APPROVED` o `REJECTED`.
- Al aprobar una adopcion, la mascota cambia a `NOT_AVAILABLE`.
- Al cancelar una adopcion aprobada, la mascota puede volver a `AVAILABLE`.
- No se permiten adopciones duplicadas activas para la misma mascota.
- La salud de la mascota se gestiona en `health-service`, no en `pet-service`.
- Los refugios no pueden eliminarse si tienen dependientes activos.
- Los listados normales ocultan registros eliminados o inactivos.
- Se bloquean operaciones sobre registros con soft delete.

## Soft Delete

| Entidad | Estado usado |
|---|---|
| Mascota | `DELETED` |
| Salud | `DELETED` |
| Refugio | `DELETED` |
| Usuario | `INACTIVE` |
| Staff | `INACTIVE` |
| Supply | `INACTIVE` |
| Adopcion | `CANCELLED` |
| FollowUp | `CANCELLED` |
| Donacion | `CANCELLED` |
| Notificacion | `ARCHIVED` |

## Base De Datos

El proyecto usa **Neon PostgreSQL** como base de datos online. Cada microservicio usa un schema independiente.

| Servicio | Schema |
|---|---|
| user-service | `users_service` |
| pet-service | `pet_service` |
| adoption-service | `adoption_service` |
| notification-service | `notification_service` |
| health-service | `health_service` |
| followup-service | `followup_service` |
| donation-service | `donation_service` |
| staff-service | `staff_service` |
| supply-service | `supply_service` |
| shelter-service | `shelter_service` |

Flyway crea y migra los schemas automaticamente.

## Perfiles De Ejecucion

| Perfil | Uso |
|---|---|
| `neon` | Ejecucion principal con Neon PostgreSQL |
| `h2` | Pruebas con base en memoria |
| `dev` | Desarrollo local |
| `prod` | Produccion |
| `postgres` | PostgreSQL local opcional |

## Variables De Entorno Locales

El repositorio incluye `.env.example` como plantilla.

```env
DB_URL=jdbc:postgresql://<NEON_HOST>/<NEON_DATABASE>?sslmode=require&channelBinding=require
DB_USER=<NEON_USER>
DB_PASSWORD=<NEON_PASSWORD>
```

Para ejecucion local con Docker Compose, crear un archivo `.env` real en la raiz del proyecto usando esa estructura.

El archivo `.env` esta ignorado por Git.

## Variables Para Render

Cada microservicio con base de datos necesita:

```env
SPRING_PROFILES_ACTIVE=neon
DB_URL=jdbc:postgresql://<NEON_HOST>/<NEON_DATABASE>?sslmode=require&channelBinding=require
DB_USER=<NEON_USER>
DB_PASSWORD=<NEON_PASSWORD>
DB_SCHEMA=<SCHEMA_DEL_MICROSERVICIO>
EUREKA_URL=https://adoptapp-eureka-server.onrender.com/eureka
PORT=<PUERTO_DEL_SERVICIO>
```

El `api-gateway` no usa base de datos:

```env
EUREKA_URL=https://adoptapp-eureka-server.onrender.com/eureka
PORT=8080
```

El `eureka-server` tampoco usa base de datos:

```env
PORT=8761
```

## URLs De Render

| Componente | URL |
|---|---|
| API Gateway | `https://adoptapp-api-gateway.onrender.com` |
| Eureka Server | `https://adoptapp-eureka-server.onrender.com` |
| user-service | `https://adoptapp-user-service.onrender.com` |
| pet-service | `https://adoptapp-pet-service.onrender.com` |
| adoption-service | `https://adoptapp-adoption-service.onrender.com` |
| notification-service | `https://adoptapp-notification-service.onrender.com` |
| health-service | `https://adoptapp-health-service.onrender.com` |
| followup-service | `https://adoptapp-followup-service.onrender.com` |
| donation-service | `https://adoptapp-donation-service.onrender.com` |
| staff-service | `https://adoptapp-uudu.onrender.com` |
| supply-service | `https://adoptapp-supply-service.onrender.com` |
| shelter-service | `https://adoptapp-shelter-service.onrender.com` |

## Rutas Por API Gateway En Render

| Servicio | Ruta |
|---|---|
| user-service | `https://adoptapp-api-gateway.onrender.com/user-app/users` |
| pet-service | `https://adoptapp-api-gateway.onrender.com/pet-app/pets` |
| adoption-service | `https://adoptapp-api-gateway.onrender.com/adoption-app/adoptions` |
| notification-service | `https://adoptapp-api-gateway.onrender.com/notification-app/notifications` |
| health-service | `https://adoptapp-api-gateway.onrender.com/health-app/health` |
| followup-service | `https://adoptapp-api-gateway.onrender.com/followup-app/followups` |
| donation-service | `https://adoptapp-api-gateway.onrender.com/donation-app/donations` |
| staff-service | `https://adoptapp-api-gateway.onrender.com/staff-app/staff` |
| supply-service | `https://adoptapp-api-gateway.onrender.com/supply-app/supplies` |
| shelter-service | `https://adoptapp-api-gateway.onrender.com/shelter-app/shelters` |

## API Gateway

El API Gateway escucha localmente en:

```text
http://localhost:8080
```

Usa rutas `lb://` para resolver servicios registrados en Eureka.

Ejemplos:

```text
http://localhost:8080/user-app/users
http://localhost:8080/pet-app/pets
http://localhost:8080/adoption-app/adoptions
```

El Gateway reenvia el header `Authorization`, por lo que cada microservicio sigue aplicando sus propias reglas de seguridad.

## Eureka Server

Localmente:

```text
http://localhost:8761
```

En Render:

```text
https://adoptapp-eureka-server.onrender.com
```

Estado esperado:

```text
API-GATEWAY
USER-SERVICE
PET-SERVICE
ADOPTION-SERVICE
NOTIFICATION-SERVICE
HEALTH-SERVICE
FOLLOWUP-SERVICE
DONATION-SERVICE
STAFF-SERVICE
SUPPLY-SERVICE
SHELTER-SERVICE
```

Si el Gateway responde `503`, normalmente significa que el servicio aun no esta registrado, esta despertando o Eureka aun no actualiza su lista.

## Docker

Cada componente tiene Dockerfile multistage.

Los Dockerfile actuales:

1. Usan una imagen Maven para compilar el modulo.
2. Generan el JAR dentro del contenedor.
3. Copian el JAR a una imagen final con Java 21.
4. Ejecutan el servicio con un usuario sin privilegios.

Esto permite que Render y Docker Compose construyan desde un repositorio recien clonado sin depender de JARs locales en `target/`.

## Docker Compose

Levantar todo localmente:

```powershell
docker compose build
docker compose up -d
docker compose ps
```

Ver logs:

```powershell
docker compose logs -f eureka-server api-gateway
```

Detener:

```powershell
docker compose down
```

Reconstruir:

```powershell
docker compose up -d --build --force-recreate
```

## .dockerignore

El proyecto incluye `.dockerignore` para evitar enviar archivos innecesarios o sensibles al contexto Docker.

Excluye, entre otros:

```text
.git
.idea
logs
tmp
.env
*.log
**/target
```

Esto evita incluir credenciales locales, logs, configuraciones del IDE y archivos generados dentro de las imagenes.

## OpenAPI, Swagger Y HATEOAS

Cada microservicio expone Swagger UI en:

```text
http://localhost:<PUERTO>/<CONTEXT_PATH>/doc/swagger-ui/index.html
```

Ejemplos:

```text
http://localhost:8081/user-app/doc/swagger-ui/index.html
http://localhost:8082/pet-app/doc/swagger-ui/index.html
http://localhost:8083/adoption-app/doc/swagger-ui/index.html
```

En Render:

```text
https://adoptapp-user-service.onrender.com/user-app/doc/swagger-ui/index.html
https://adoptapp-pet-service.onrender.com/pet-app/doc/swagger-ui/index.html
https://adoptapp-adoption-service.onrender.com/adoption-app/doc/swagger-ui/index.html
```

Tambien puede accederse por API Gateway si los servicios estan registrados en Eureka.

La documentacion incluye:

- `@Tag`
- `@Operation`
- `@ApiResponses`
- DTOs con `@Schema`
- `ErrorResponse`
- Seguridad `basicAuth`
- Respuestas HATEOAS con `_links`

## Seguridad

- Autenticacion con HTTP Basic.
- Roles con `@PreAuthorize`.
- Swagger UI con boton `Authorize`.
- Endpoints internos `/internal/**` restringidos.
- Propagacion del header `Authorization` en llamadas Feign.
- Manejo global de errores con `GlobalExceptionHandler`.

## Manejo De Errores

Los microservicios usan un formato de error uniforme:

```json
{
  "timestamp": "2026-06-27T17:32:10.803+00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Mensaje del error",
  "path": "/ruta",
  "traceId": "uuid",
  "details": null
}
```

Codigos manejados:

| Codigo | Uso |
|---|---|
| 400 | Request invalido o filtro invalido |
| 401 | No autenticado |
| 403 | Sin permisos |
| 404 | Recurso no encontrado |
| 409 | Conflicto de negocio |
| 503 | Servicio remoto no disponible |
| 500 | Error interno |

## Testing

Las pruebas estan organizadas por capa:

```text
src/test/java/
├── service
├── controller
└── client
```

Se usan:

- JUnit 5
- Mockito
- AssertJ
- Spring Security Test
- H2
- JaCoCo

Ejecutar todos los tests:

```powershell
.\mvnw.cmd test
```

Ejecutar un microservicio:

```powershell
.\mvnw.cmd -pl adoption-service test
```

Generar cobertura JaCoCo:

```powershell
.\mvnw.cmd clean test jacoco:report
```

Reportes:

```text
<microservicio>/target/site/jacoco/index.html
```

## Postman

El archivo:

```text
postman-collection.json
```

contiene endpoints de los microservicios y usa Basic Auth con variables:

```text
username=admin@empresa.com
password=admin123
```

Actualmente la coleccion esta configurada para API Gateway local:

```text
http://localhost:8080
```

Las variables principales son:

```text
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

Para usar Render, cambiar esas variables por:

```text
https://adoptapp-api-gateway.onrender.com/<context-path>
```

## CI/CD

Existe workflow en:

```text
.github/workflows/build.yml
```

El pipeline:

- Se ejecuta en push o pull request hacia `main` o `master`.
- Configura JDK 21.
- Ejecuta compilacion Maven.
- Ejecuta tests.

## Ejecucion Local Con Maven

Ejecutar un servicio individual:

```powershell
cd user-service
.\mvnw.cmd spring-boot:run
```

Ejecutar con H2:

```powershell
.\mvnw.cmd spring-boot:run -Dspring-boot.run.profiles=h2
```

Tambien existe:

```text
start-all-h2.ps1
```

para iniciar servicios con perfil H2 en desarrollo local.

## Limitaciones Conocidas

- No usa JWT/OAuth2; usa Basic Auth.
- No hay paginacion.
- Algunas rutas usan `/by-id/{id}` en vez de estilo REST puro.
- En Render los servicios pueden tardar por arranque en frio.
- El Gateway puede responder `503` mientras Eureka actualiza servicios.
- Las llamadas Feign internas usan URLs configuradas por entorno.
- Algunos endpoints dependen de que varios servicios esten activos.
- El primer build Docker multistage puede tardar porque descarga dependencias Maven.

## Decisiones De Diseno

- Arquitectura basada en microservicios.
- Un schema por microservicio.
- Comunicacion sincrona con OpenFeign.
- API Gateway como punto de entrada.
- Eureka para descubrimiento de servicios.
- Shared Kernel para clases comunes.
- Soft delete para conservar trazabilidad.
- H2 para pruebas.
- Neon para base online.
- Docker multistage para facilitar despliegue.

## Documentacion De Cierre

El proyecto incluye documentacion complementaria para respaldar el cierre del semestre:

| Documento                                     | Descripcion |
|-----------------------------------------------| --- |
| `documentacion-funcional.md`                  | Explica el problema, actores, reglas de negocio, flujos funcionales y ejemplos de uso. |
| `documentacion-tecnica.md`                    | Describe arquitectura, modulos, perfiles, variables de entorno, ejecucion desde cero y pruebas. |
| `levantamiento-requerimientos-actualizado.md` | Contrasta los requerimientos originales con los cambios, reemplazos y alcance final codificado. |
| `matriz-requerimientos.md`                    | Presenta una matriz RF/RNF con estado final: implementado, parcial, reemplazado o postergado. |
| `plan-cierre-feedback.md`                     | Registra observaciones de evaluacion anterior, correcciones aplicadas, decisiones de alcance y evidencia concreta. |
| `docs/defensa-individual/*.md`                | Documento de defensa individual con aportes, archivos trabajados, pruebas y commits relevantes. |

## Archivos Importantes

```text
README.md
pom.xml
compose.yml
.env.example
.dockerignore
postman-collection.json
schema.sql
docs/defensa-individual/*.md
documentacion-funcional.md
documentacion-tecnica.md
levantamiento-requerimientos-actualizado.md
matriz-requerimientos.md
plan-cierre-feedback.md
```

## Estado General

AdoptAPP cuenta con:

- 10 microservicios.
- API Gateway.
- Eureka Server.
- Shared Kernel.
- Neon PostgreSQL.
- Flyway.
- Docker.
- Render.
- OpenAPI/Swagger.
- HATEOAS.
- Seguridad con roles.
- Pruebas unitarias.
- JaCoCo.
- Coleccion Postman.
