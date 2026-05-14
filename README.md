# AdoptApp - Plataforma de Adopción de Mascotas

Plataforma de adopción de mascotas basada en microservicios con **Spring Boot 4.0.0** y **Java 21**, diseñada para gestionar el ciclo de vida completo de adopciones en refugios.

## Arquitectura

Proyecto multi-módulo Maven con 10 microservicios desplegables de forma independiente:

```
adoptapp/
├── user-service/          # Gestión de usuarios
├── pet-service/           # Catálogo de mascotas
├── adoption-service/      # Proceso de adopción
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
| **Spring Data JPA** | Acceso a datos con relaciones |
| **PostgreSQL** | Base de datos relacional (una BD por servicio) |
| **Lombok** | Reducción de código repetitivo |
| **Bean Validation** | Validación de DTOs de entrada |
| **Maven** | Gestión de dependencias |

## Avances Realizados

### user-service (Puerto 8081) - COMPLETO CON RELACIONES JPA

API CRUD para gestionar usuarios (adoptantes). Refactorizado con relaciones JPA reales. Migraciones Flyway aplicadas automáticamente al iniciar.

**Base de datos**: PostgreSQL `users_db`

**Endpoints** (`/users`):

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/users` | Listar todos (filtro `?status=`) |
| `GET` | `/users/by-id/{id}` | Obtener por ID |
| `POST` | `/users` | Crear usuario |
| `PUT` | `/users/by-id/{id}` | Actualizar usuario |
| `DELETE` | `/users/by-id/{id}` | Eliminar usuario |

**Modelo de datos normalizado**:
- `User` → `@OneToOne` → `UserPhone` (tabla `phone_numbers`)
- `User` → `@OneToMany` → `UserAddress` (tabla `user_addresses`)
- `User.status` → `@Enumerated(EnumType.STRING)` → `UserStatus {ACTIVE, INACTIVE, SUSPENDED}`

**DTOs** con campos de dirección: `country`, `city`, `street`, `homeNumber`, `postalCode`, `type`

**Validaciones** en `UserRequest`: `@NotBlank`, `@Email`, `@Size`, `@NotNull`

**Patrón**: Request → Command → Result → Response. Java records en todos los DTOs.

**Siembra de datos**: 3 usuarios de ejemplo con teléfono y dirección.

### pet-service (Puerto 8082) - COMPLETO CON RELACIONES JPA

API CRUD para gestionar mascotas. Refactorizado con entidades relacionadas.

**Base de datos**: PostgreSQL `pet_db`

**Endpoints** (`/pets`):

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/pets` | Listar todas (filtro `?status=`) |
| `GET` | `/pets/by-id/{id}` | Obtener por ID |
| `POST` | `/pets` | Crear mascota |
| `PUT` | `/pets/by-id/{id}` | Actualizar mascota |
| `DELETE` | `/pets/by-id/{id}` | Eliminar mascota |

**Modelo de datos normalizado**:
- `Pet` → `@ManyToOne` → `PetStatus` (tabla `pet_status` - catálogo de estados)
- `Pet` → `@OneToOne(cascade=ALL)` → `PetHealth` (tabla `pet_health` - vacunado, esterilizado, enfermedades)
- Nuevos repositorios: `StatusRepository`, `HealthRepository`

**DTOs** incluyen datos de salud: `vaccinated`, `sterilized`, `diseases`, `status` (String)

**Validaciones** en `PetRequest`: `@NotBlank`, `@Size`, `@Min`, `@NotNull`

**Siembra de datos**: 3 mascotas ("Yoni", "Loki", "Oso") con su estado y salud.

### adoption-service - EN DESARROLLO (CON BUGS)

Estructura base creada pero no compila. Problemas conocidos:
- `AdoptionService` referencia `Pet` y campos que no existen en `Adoption`
- `AdoptionController` con errores de sintaxis, rutas incompletas, código inalcanzable
- `AdoptionRepository` con `existsByNameIgnoreCase()` pero `Adoption` no tiene campo `name`
- `DataInitializer` con variable `command` no definida
- Falta configuración de BD y driver PostgreSQL en `pom.xml`

**Entidad Adoption**: `id`, `userId`, `petId`, `status`, `createdAt`

### Servicios Restantes - SOLO ESQUELETO

| Servicio | Propósito |
|---|---|
| `donation-service` | Gestionar donaciones monetarias y en especie |
| `shelter-service` | Gestionar ubicaciones y capacidad de refugios |
| `staff-service` | Gestionar personal y voluntarios |
| `health-service` | Seguimiento de vacunas, tratamientos y registros médicos |
| `notification-service` | Notificaciones por email/SMS |
| `supply-service` | Control de insumos (comida, juguetes, medicinas) |
| `followup-service` | Seguimiento post-adopción |

## Patrones del Proyecto

### Capas de DTO (4 capas)

```
Request (validación) → Command (servicio) → Result (servicio) → Response (API)
```

### Relaciones JPA implementadas

- `@OneToOne` para teléfono de usuario y salud de mascota
- `@OneToMany` para direcciones de usuario
- `@ManyToOne` para estado de mascota (catálogo)
- `@Enumerated(EnumType.STRING)` para estado de usuario
- `CascadeType.ALL` para operaciones en cascada

### Repositorios Spring Data JPA

```java
findByStatus_NameIgnoreCase(String name);
findByStatusIgnoreCase(String status);
existsByUsernameIgnoreCase(String username);
existsByEmailIgnoreCase(String email);
```

### Inicialización de Datos

Cada servicio incluye un `CommandLineRunner` que siembra datos si la tabla está vacía.

## Empezando

### Prerrequisitos

- Java 21
- Maven 3.8+
- PostgreSQL corriendo localmente

### Configuración de Bases de Datos

**user-service** (puerto 8081, BD: `users_db`):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/user_db
    username: postgres
    password: 1234
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
```

**pet-service** (puerto 8082, BD: `pet_db`):
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pet_db
    username: postgres
    password: 1234
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
```

### Ejecutar un Servicio

```bash
cd user-service
./mvnw spring-boot:run
```

### Compilar Todos

```bash
mvn clean install
```

> Nota: `adoption-service` fallará al compilar.

## Ejemplos de Uso

### Crear Usuario

```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "name": "John",
    "surname": "Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "country": "Chile",
    "city": "Santiago",
    "street": "Av. Siempre Viva",
    "homeNumber": "742",
    "postalCode": "8320000",
    "type": "HOME"
  }'
```

### Crear Mascota

```bash
curl -X POST http://localhost:8082/pets \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rex",
    "species": "Perro",
    "race": "Pastor Alemán",
    "age": 3,
    "size": "Grande",
    "color": "Negro",
    "personality": "Amigable",
    "fosterId": 1,
    "vaccinated": true,
    "sterilized": false,
    "diseases": "Ninguna",
    "status": "AVAILABLE"
  }'
```

### Listar por Estado

```bash
curl "http://localhost:8081/users?status=ACTIVE"
curl "http://localhost:8082/pets?status=AVAILABLE"
```

## Qué Falta / Trabajo Futuro

- **adoption-service**: Reparar bugs y completar implementación
- **7 servicios restantes**: Implementar CRUD y lógica de negocio
- **Comunicación entre servicios**: Feign, REST template o message broker
- **API Gateway**: Spring Cloud Gateway para ruteo
- **Descubrimiento**: Eureka o Consul
- **Seguridad**: Spring Security con autenticación JWT
- **Manejo global de excepciones**: `@RestControllerAdvice`
- **Paginación**: Endpoints con paginación Spring Data
- **Contenedores**: Dockerfiles y docker-compose
- **CI/CD**: Pipeline de integración y despliegue
## Cambios Realizados

### POM Padre - Gestión Centralizada de Dependencias

Se agregó `<dependencyManagement>` al `pom.xml` raíz con versiones centralizadas para facilitar la configuración en todos los microservicios:

| Dependencia | Versión |
|---|---|
| `flyway-core` | 10.21.0 |
| `flyway-database-postgresql` | 10.21.0 |
| `postgresql` | 42.7.5 |
| `h2` | 2.3.232 |

### user-service - Correcciones y Flyway

| Cambio | Detalle |
|---|---|
| `spring-boot-starter-webmvc` | Corregido a `spring-boot-starter-web` |
| `spring-boot-starter-webmvc-test` | Corregido a `spring-boot-starter-test` |
| `flyway-core` con version hardcodeada 9.22.3 | Eliminada la versión, ahora la gestiona el POM padre |
| `flyway-core` → `spring-boot-starter-flyway` | Cambio requerido porque Spring Boot 4.0.0 movió la auto-configuración de Flyway fuera de `spring-boot-autoconfigure` |
| Migraciones Flyway | V1 (crear tablas) y V2 (insertar datos iniciales) se ejecutan automáticamente al iniciar |

## Problemas Conocidos

1. `adoption-service` no compila
2. `Pet.fosterId` es `Long` simple sin relación JPA
3. Credenciales PostgreSQL por defecto (`postgres`/`1234`)
4. No hay `.gitignore` en raíz
5. Los 7 servicios esqueleto no tienen lógica de negocio
6. `application-h2.yml` no configura Flyway correctamente (usa propiedad inválida `flyway.console.enabled`)
