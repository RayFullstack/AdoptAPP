# AdoptApp - Plataforma de Adopción de Mascotas

Una plataforma de adopción de mascotas basada en microservicios, construida con **Spring Boot 3** y **Java 21**, diseñada para gestionar todo el ciclo de vida de las adopciones en refugios, incluyendo personal, usuarios y más.

## Arquitectura

Este proyecto sigue una **arquitectura de microservicios** con 10 servicios desplegables de forma independiente, gestionados bajo un POM padre multi-módulo de Maven.

```
adoptapp/
├── user-service/          # Gestión de usuarios (ACTIVO + PROBADO)
├── pet-service/           # Catálogo de mascotas (ACTIVO + PROBADO)
├── adoption-service/      # Proceso de adopción (EN DESARROLLO - CON BUGS)
├── donation-service/      # Donaciones (ESQUELETO)
├── shelter-service/       # Gestión de refugios (ESQUELETO)
├── staff-service/         # Gestión de personal (ESQUELETO)
├── health-service/        # Registros de salud de mascotas (ESQUELETO)
├── notification-service/  # Notificaciones (ESQUELETO)
├── supply-service/        # Insumos de refugio (ESQUELETO)
├── followup-service/      # Seguimiento post-adopción (ESQUELETO)
└── pom.xml                # POM Padre
```

## Stack Tecnológico

| Tecnología | Uso |
|---|---|
| **Java 21** | Runtime (shelter-service usa Java 17) |
| **Spring Boot 3.x** | Framework de la aplicación |
| **Spring Data JPA** | Acceso a base de datos |
| **PostgreSQL** | Base de datos relacional (una BD por servicio) |
| **Lombok** | Reducción de código repetitivo en entidades |
| **Bean Validation** | Validación de DTOs de entrada |
| **Maven** | Gestión de dependencias y compilación |

## Qué se ha implementado hasta ahora

### user-service (Puerto 8081) - COMPLETAMENTE IMPLEMENTADO

API CRUD completa para gestionar usuarios (adoptantes, personal, etc.).

**Base de datos**: PostgreSQL `user_db`

**Endpoints** (`/users`):

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/users` | Listar todos los usuarios (filtro opcional `?status=`) |
| `GET` | `/users/by-id/{id}` | Obtener usuario por ID |
| `POST` | `/users` | Crear un nuevo usuario |
| `PUT` | `/users/by-id/{id}` | Actualizar usuario por ID |
| `DELETE` | `/users/by-id/{id}` | Eliminar usuario por ID |

**Entidad - User**:
- `id`, `name`, `surname`, `username` (único), `email` (único), `phone`, `address`, `status`, `createdAt`

**Patrones utilizados**:
- Patrón DTO de 4 capas: `Request` (validado) -> `Command` -> `Result` -> `Response`
- Inyección por constructor en todas partes
- Java records para todos los DTOs
- Siembra de datos al iniciar (3 usuarios de ejemplo de la familia Simpson)

### pet-service (Puerto 8082) - COMPLETAMENTE IMPLEMENTADO

API CRUD completa para gestionar mascotas disponibles para adopción.

**Base de datos**: PostgreSQL `pet_db`

**Endpoints** (`/pets`):

| Método | Ruta | Descripción |
|---|---|---|
| `GET` | `/pets` | Listar todas las mascotas (filtro opcional `?status=`) |
| `GET` | `/pets/by-id/{id}` | Obtener mascota por ID |
| `POST` | `/pets` | Crear una nueva mascota |
| `PUT` | `/pets/by-id/{id}` | Actualizar mascota por ID |
| `DELETE` | `/pets/by-id/{id}` | Eliminar mascota por ID |

**Entidad - Pet**:
- `id`, `name`, `species`, `race`, `color`, `age`, `size`, `health`, `personality`, `status`, `createdAt`, `fosterId`

**Datos sembrados al iniciar**:
- "Yoni" - Doberman, 7 años, grande, arisco
- "Loki" - Gato doméstico pelo largo, 4 años, negro, mediano
- "Oso" - Cocker, 2 años, crema, mediano, con problemas cardíacos

### adoption-service - EN DESARROLLO (CONTIENE BUGS)

Estructurado con el mismo patrón que los otros servicios pero **no compila**. El código fue parcialmente copiado desde pet-service y nunca se adaptó correctamente. Problemas conocidos:

- `AdoptionService` referencia la clase `Pet` y campos específicos de mascota que no existen en la entidad `Adoption`
- `AdoptionController` tiene errores de sintaxis: falta `orElse()`, código inalcanzable, firmas de métodos incompletas, faltan mapeos de variables de ruta
- `AdoptionRepository` tiene `existsByNameIgnoreCase()` pero `Adoption` no tiene un campo `name`
- `DataInitializer` referencia una variable `command` que no está definida
- Falta configuración de base de datos en `application.yaml`
- Falta el driver de PostgreSQL en el `pom.xml`

**Entidad - Adoption** (definida pero el servicio está roto):
- `id`, `userId`, `petId`, `status`, `createdAt`

### Servicios Restantes - SOLO ESQUELETO

Los siguientes 7 servicios contienen solo la clase de aplicación Spring Boot y una clase de prueba vacía:

| Servicio | Propósito |
|---|---|
| `donation-service` | Gestionar donaciones monetarias y en especie |
| `shelter-service` | Gestionar ubicaciones y capacidad de refugios |
| `staff-service` | Gestionar personal y voluntarios del refugio |
| `health-service` | Seguimiento de vacunas, tratamientos y registros médicos |
| `notification-service` | Notificaciones por email/SMS para eventos de adopción |
| `supply-service` | Control de insumos del refugio (comida, juguetes, medicinas) |
| `followup-service` | Seguimiento y retroalimentación post-adopción |

## Empezando

### Prerrequisitos

- Java 21 (Java 17 como mínimo para shelter-service)
- Maven 3.8+
- PostgreSQL corriendo localmente

### Configuración

Dos servicios están configurados para conectarse a PostgreSQL:

**user-service** (`user-service/src/main/resources/application.yaml`):
```yaml
server:
  port: 8081
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

**pet-service** (`pet-service/src/main/resources/application.yaml`):
```yaml
server:
  port: 8082
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
# Ejecutar user-service
cd user-service
./mvnw spring-boot:run

# Ejecutar pet-service
cd pet-service
./mvnw spring-boot:run
```

### Compilar Todos los Servicios

```bash
mvn clean install
```

> Nota: `adoption-service` fallará al compilar debido a los bugs conocidos.

## Ejemplos de Uso de la API

### Crear un Usuario

```bash
curl -X POST http://localhost:8081/users \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "name": "John",
    "surname": "Doe",
    "email": "john@example.com",
    "phone": "1234567890",
    "address": "123 Main St"
  }'
```

### Listar Mascotas Disponibles

```bash
curl http://localhost:8082/pets?status=AVAILABLE
```

### Obtener Mascota por ID

```bash
curl http://localhost:8082/pets/by-id/1
```

## Patrones del Proyecto

### Capas de DTO (usado en user-service y pet-service)

```
UserRequest  --entrada validada-->  UserController
UserCommand  --capa de servicio-->  UserService
UserResult   --salida de servicio-->  UserService
UserResponse --salida API-->      Cliente
```

### Patrón de Repositorio

Repositorios Spring Data JPA con métodos de consulta derivados:
```java
List<User> findByStatusIgnoreCase(String status);
List<User> findAllByOrderByCreatedAtAsc();
boolean existsByUsernameIgnoreCase(String username);
```

### Inicialización de Datos

Cada servicio implementado incluye un `CommandLineRunner` que siembra datos iniciales al arrancar si la tabla está vacía.

## Qué Falta / Trabajo Futuro

- **Comunicación entre servicios**: Sin clientes Feign, templates REST ni brokers de mensajes
- **API Gateway**: Sin Spring Cloud Gateway ni capa de ruteo
- **Descubrimiento de servicios**: Sin Eureka ni Consul
- **Autenticación/Autorización**: Sin Spring Security
- **Manejo global de excepciones**: Sin `@RestControllerAdvice`
- **Paginación**: Todos los endpoints de lista devuelven colecciones completas
- **Docker/Contenedores**: Sin Dockerfiles ni docker-compose
- **CI/CD**: Sin configuración de pipeline
- **Migraciones de base de datos**: Sin Flyway ni Liquibase
- **adoption-service**: Necesita ser arreglado y completado
- **7 servicios restantes**: Necesitan implementación completa

## Problemas Conocidos

1. `adoption-service` no compila - ver bugs listados arriba
2. `shelter-service` usa Java 17 mientras que los demás usan Java 21
3. `Pet.fosterId` es un `Long` simple sin relación JPA definida
4. `User.phone` y `User.address` tienen `@Column(length=50)` que es restrictivo
5. Las credenciales por defecto de PostgreSQL (`postgres`/`1234`) deben cambiarse para producción
6. No hay `.gitignore` en el nivel raíz
