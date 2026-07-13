# Documentacion Tecnica - AdoptAPP

## 1. Proposito Del Documento

Este documento describe la estructura tecnica del proyecto AdoptAPP. Su objetivo es explicar la arquitectura, los modulos principales, los perfiles de ejecucion, las variables de entorno necesarias, la forma de levantar el sistema desde cero y la ejecucion de pruebas.

Este documento complementa la documentacion funcional. La documentacion funcional explica el problema, actores, reglas y flujos de negocio. Esta documentacion tecnica explica como esta organizado y configurado el proyecto.

## 2. Arquitectura General

AdoptAPP esta construido como un sistema de microservicios usando Spring Boot y Maven multi-modulo.

La arquitectura general contiene:

- Un proyecto padre Maven.
- Un modulo compartido llamado `shared-kernel`.
- Diez microservicios principales.
- Un API Gateway.
- Un Eureka Server para descubrimiento de servicios.
- Base de datos PostgreSQL en Neon.
- Migraciones automaticas con Flyway.
- Documentacion OpenAPI por microservicio.
- Pruebas unitarias con JUnit y Mockito.
- Perfil H2 para pruebas.
- Docker y Docker Compose para ejecucion local.
- Configuracion para despliegue en Render.

## 3. Modulos Del Proyecto

### 3.1 Proyecto Padre

El proyecto padre centraliza configuraciones comunes de Maven, versiones de dependencias y modulos hijos.

Desde la raiz del repositorio se pueden compilar y probar los microservicios usando el wrapper de Maven:

```bash
.\mvnw.cmd clean install
```

### 3.2 shared-kernel

El modulo `shared-kernel` contiene clases compartidas entre microservicios.

Incluye principalmente:

- DTO comun de errores.
- Excepciones compartidas.
- Fabrica de respuestas de error.
- Utilidades reutilizables.

Su objetivo es evitar duplicacion de codigo comun y mantener respuestas de error consistentes.

### 3.3 user-service

Microservicio encargado de usuarios, roles, permisos y autenticacion basica.

Responsabilidades principales:

- Crear usuarios.
- Registrar adoptantes.
- Actualizar usuarios.
- Bloquear escalamiento indebido de roles.
- Manejar estados como `ACTIVE`, `INACTIVE` y `SUSPENDED`.
- Entregar informacion de usuarios a otros microservicios.

### 3.4 shelter-service

Microservicio encargado de refugios.

Responsabilidades principales:

- Crear refugios.
- Actualizar informacion de refugios.
- Aplicar soft delete con estado `DELETED`.
- Validar reglas antes de eliminar refugios.
- Entregar informacion de refugios a otros microservicios.

### 3.5 staff-service

Microservicio encargado del personal asociado a refugios.

Responsabilidades principales:

- Crear staff.
- Asociar usuarios a refugios.
- Controlar staff activo e inactivo.
- Aplicar permisos por refugio.

### 3.6 pet-service

Microservicio encargado de mascotas.

Responsabilidades principales:

- Registrar mascotas.
- Listar mascotas disponibles.
- Actualizar informacion de mascotas.
- Cambiar estado de mascotas.
- Aplicar soft delete con estado `DELETED`.
- Validar ownership por refugio.
- Comunicarse con health-service para borrar ficha de salud asociada cuando corresponde.

### 3.7 health-service

Microservicio encargado de fichas de salud de mascotas.

Responsabilidades principales:

- Crear ficha clinica.
- Actualizar estado de vacunacion, esterilizacion y enfermedades.
- Bloquear cambios sobre fichas eliminadas.
- Evitar cambio de `petId` cuando la ficha ya existe.
- Aplicar soft delete con estado `DELETED`.

### 3.8 adoption-service

Microservicio encargado de solicitudes de adopcion.

Responsabilidades principales:

- Crear adopciones en estado `PENDING`.
- Aprobar o rechazar adopciones.
- Cancelar adopciones.
- Evitar adopciones duplicadas para una misma mascota.
- Cambiar estado de mascota al aprobar o cancelar.
- Crear seguimiento post-adopcion cuando corresponde.
- Validar permisos por usuario, refugio y rol.

### 3.9 followup-service

Microservicio encargado del seguimiento post-adopcion.

Responsabilidades principales:

- Registrar seguimientos.
- Consultar seguimientos.
- Cancelar seguimientos.
- Validar que usuario y mascota existan.

### 3.10 donation-service

Microservicio encargado de donaciones.

Responsabilidades principales:

- Registrar donaciones.
- Asociar donaciones a usuarios y refugios.
- Aplicar estado automatico al crear.
- Bloquear actualizaciones sobre donaciones canceladas.
- Aplicar soft delete con estado `CANCELLED`.

### 3.11 supply-service

Microservicio encargado de insumos.

Responsabilidades principales:

- Registrar insumos para refugios.
- Actualizar stock.
- Notificar eventos relacionados a insumos.
- Aplicar soft delete con estado `INACTIVE`.

### 3.12 notification-service

Microservicio encargado de notificaciones.

Responsabilidades principales:

- Crear notificaciones.
- Listar notificaciones segun permisos.
- Filtrar por usuario, refugio o administrador.
- Aplicar soft delete con estado `ARCHIVED`.

### 3.13 api-gateway

Modulo encargado de exponer una entrada comun al sistema.

Responsabilidades principales:

- Enrutar solicitudes hacia los microservicios.
- Usar nombres de servicios registrados en Eureka.
- Mantener rutas como `/user-app/**`, `/pet-app/**`, `/adoption-app/**`, entre otras.

### 3.14 eureka-server

Modulo encargado del descubrimiento de servicios.

Responsabilidades principales:

- Registrar microservicios activos.
- Permitir que el API Gateway encuentre servicios por nombre.
- Mostrar estado de instancias registradas.

## 4. Perfiles De Ejecucion

El proyecto usa perfiles para separar ambientes.

### 4.1 Perfil dev

Perfil usado para desarrollo local.

Puede apuntar a una base de datos local o configuracion de desarrollo.

### 4.2 Perfil postgres

Perfil orientado a PostgreSQL local.

Se usa cuando se trabaja con una instancia local de PostgreSQL.

### 4.3 Perfil neon

Perfil usado para conectar los microservicios a Neon PostgreSQL.

Es el perfil principal para despliegue en Render.

Ejemplo:

```bash
SPRING_PROFILES_ACTIVE=neon
```

### 4.4 Perfil h2

Perfil usado para pruebas.

Permite ejecutar tests con base de datos en memoria sin depender de Neon ni PostgreSQL local.

Ejemplo:

```bash
SPRING_PROFILES_ACTIVE=h2
```

### 4.5 Perfil prod

Perfil pensado para ejecucion productiva.

Puede usarse junto con variables de entorno externas para evitar credenciales escritas en archivos del repositorio.

## 5. Variables De Entorno

Las variables principales usadas por los microservicios son:

```bash
SPRING_PROFILES_ACTIVE=neon
DB_URL=jdbc:postgresql://HOST/DB_NAME?sslmode=require
DB_USER=USUARIO
DB_PASSWORD=PASSWORD
DB_SCHEMA=nombre_schema
EUREKA_URL=https://adoptapp-eureka-server.onrender.com/eureka
PORT=8080
```

### 5.1 SPRING_PROFILES_ACTIVE

Define que perfil debe usar Spring Boot.

Valores habituales:

- `dev`
- `postgres`
- `neon`
- `h2`
- `prod`

### 5.2 DB_URL

Cadena JDBC de conexion a PostgreSQL.

En Neon debe incluir SSL:

```bash
jdbc:postgresql://host/neondb?sslmode=require
```

### 5.3 DB_USER

Usuario de base de datos entregado por Neon.

### 5.4 DB_PASSWORD

Contrasena de base de datos entregada por Neon.

No debe subirse al repositorio.

### 5.5 DB_SCHEMA

Schema usado por cada microservicio.

Ejemplos:

- `user_service`
- `pet_service`
- `adoption_service`
- `notification_service`
- `health_service`
- `followup_service`
- `donation_service`
- `staff_service`
- `supply_service`
- `shelter_service`

### 5.6 EUREKA_URL

URL del servidor Eureka.

En local:

```bash
http://localhost:8761/eureka
```

En Render:

```bash
https://adoptapp-eureka-server.onrender.com/eureka
```

### 5.7 PORT

Puerto usado por Render para exponer cada servicio.

Cada microservicio debe leer el puerto desde la variable `PORT` para que Render detecte correctamente el servicio.

## 6. Base De Datos Y Flyway

El proyecto usa PostgreSQL y Flyway.

Cada microservicio tiene su propio schema en la misma base de datos Neon.

Flyway se encarga de:

- Crear o actualizar tablas.
- Mantener historial de migraciones.
- Aplicar cambios de base de datos automaticamente al iniciar el microservicio.

Cada microservicio tiene migraciones en:

```text
src/main/resources/db/migration
```

Ejemplo de nombre de migracion:

```text
V1__create_tables.sql
```

## 7. Ejecucion Desde Cero

### 7.1 Requisitos Previos

Antes de ejecutar el proyecto se necesita:

- Java JDK 21.
- Maven Wrapper incluido en el proyecto.
- Docker Desktop, si se ejecuta con Docker.
- Cuenta y base de datos Neon, si se ejecuta contra Neon.
- Variables de entorno configuradas.

### 7.2 Clonar El Proyecto

```bash
git clone https://github.com/RayFullstack/AdoptAPP.git
cd AdoptAPP
```

### 7.3 Configurar Variables De Entorno Locales

En PowerShell:

```powershell
$env:SPRING_PROFILES_ACTIVE="neon"
$env:DB_URL="jdbc:postgresql://HOST/neondb?sslmode=require"
$env:DB_USER="USUARIO_NEON"
$env:DB_PASSWORD="PASSWORD_NEON"
$env:EUREKA_URL="http://localhost:8761/eureka"
```

Si se levanta un microservicio individual, tambien se debe configurar:

```powershell
$env:DB_SCHEMA="user_service"
```

El valor de `DB_SCHEMA` cambia segun el microservicio.

### 7.4 Compilar El Proyecto

Desde la raiz:

```bash
.\mvnw.cmd clean package -DskipTests
```

Para compilar un solo microservicio:

```bash
.\mvnw.cmd -pl user-service clean package -DskipTests
```

### 7.5 Levantar Eureka

```bash
.\mvnw.cmd -pl eureka-server spring-boot:run
```

Eureka queda disponible en:

```text
http://localhost:8761
```

### 7.6 Levantar Microservicios

En terminales separadas:

```bash
.\mvnw.cmd -pl user-service spring-boot:run
.\mvnw.cmd -pl shelter-service spring-boot:run
.\mvnw.cmd -pl pet-service spring-boot:run
.\mvnw.cmd -pl health-service spring-boot:run
.\mvnw.cmd -pl adoption-service spring-boot:run
```

Se repite el mismo patron para los demas microservicios.

### 7.7 Levantar API Gateway

```bash
.\mvnw.cmd -pl api-gateway spring-boot:run
```

El gateway queda disponible en:

```text
http://localhost:8080
```

### 7.8 Ejecutar Con Docker Compose

Desde la raiz del proyecto:

```bash
docker compose build
docker compose up -d
docker compose ps
```

Para ver logs:

```bash
docker compose logs -f
```

Para apagar:

```bash
docker compose down
```

## 8. Rutas Principales

Cuando se usa API Gateway local, las rutas principales son:

```text
http://localhost:8080/user-app/users
http://localhost:8080/shelter-app/shelters
http://localhost:8080/pet-app/pets
http://localhost:8080/health-app/health
http://localhost:8080/adoption-app/adoptions
http://localhost:8080/followup-app/followups
http://localhost:8080/donation-app/donations
http://localhost:8080/staff-app/staff
http://localhost:8080/supply-app/supplies
http://localhost:8080/notification-app/notifications
```

## 9. OpenAPI Y Swagger

Cada microservicio tiene documentacion OpenAPI.

La documentacion Swagger normalmente se consulta en:

```text
http://localhost:PUERTO/context-path/doc/swagger-ui/index.html
```

Ejemplo para user-service local:

```text
http://localhost:8081/user-app/doc/swagger-ui/index.html
```

Swagger usa autenticacion Basic cuando el endpoint esta protegido.

## 10. Seguridad

El proyecto usa Spring Security con Basic Auth.

Los permisos se controlan por roles:

- `ADMIN`
- `SHELTER_ADMIN`
- `ADOPTER`
- `VOLUNTEER`
- `VET`

Las reglas de seguridad se aplican en los controladores y servicios segun el caso.

## 11. Pruebas

El proyecto usa:

- JUnit 5.
- Mockito.
- AssertJ.
- Spring Boot Test.
- Spring Security Test.
- JaCoCo.

Las pruebas se organizan principalmente en:

```text
src/test/java
```

Estructura recomendada:

```text
src/test/java/.../service
src/test/java/.../controller
src/test/java/.../client
```

### 11.1 Ejecutar Todas Las Pruebas

Desde la raiz:

```bash
.\mvnw.cmd test
```

### 11.2 Ejecutar Pruebas De Un Microservicio

```bash
.\mvnw.cmd -pl adoption-service test
```

### 11.3 Ejecutar Una Clase De Test

```bash
.\mvnw.cmd -pl adoption-service test -Dtest=AdoptionServiceTest
```

### 11.4 Perfil H2 Para Pruebas

Las pruebas deben ejecutarse con perfil `h2` cuando se necesita base de datos en memoria.

Ejemplo en clases de test con Spring:

```java
@ActiveProfiles("h2")
```

### 11.5 JaCoCo

JaCoCo se usa para medir cobertura.

Comando general:

```bash
.\mvnw.cmd test
```

Cuando el reporte esta configurado, se genera en:

```text
target/site/jacoco/index.html
```

Cada microservicio puede tener su propio reporte.

## 12. Docker

El proyecto tiene Dockerfiles para construir imagenes de los microservicios.

Los Dockerfiles usan build multi-stage, por lo que pueden compilar el JAR dentro del proceso de construccion de imagen.

Comandos principales:

```bash
docker compose build
docker compose up -d
docker compose ps
docker compose down
```

## 13. Render

En Render cada microservicio se configura como servicio independiente.

Variables importantes por servicio:

```bash
SPRING_PROFILES_ACTIVE=neon
DB_URL=jdbc:postgresql://HOST/neondb?sslmode=require
DB_USER=USUARIO
DB_PASSWORD=PASSWORD
DB_SCHEMA=schema_del_servicio
EUREKA_URL=https://adoptapp-eureka-server.onrender.com/eureka
PORT=puerto_asignado_por_render
```

El orden recomendado de despliegue es:

1. Eureka Server.
2. Microservicios base: user-service, shelter-service, notification-service.
3. Microservicios dependientes: staff-service, pet-service, health-service.
4. Microservicios de flujo: adoption-service, followup-service, donation-service, supply-service.
5. API Gateway.

## 14. Archivos Importantes

```text
pom.xml
compose.yml
.env.example
.dockerignore
README.md
documentacion-funcional.md
documentacion-tecnica.md
shared-kernel/
api-gateway/
eureka-server/
*-service/
```

## 15. Consideraciones Tecnicas

- No subir credenciales reales al repositorio.
- Usar variables de entorno para base de datos.
- Mantener cada microservicio con su propio schema.
- Ejecutar pruebas antes de subir cambios.
- Verificar que los servicios aparezcan registrados en Eureka.
- Probar endpoints desde API Gateway.
- Mantener Swagger actualizado cuando cambian endpoints o DTOs.
- Mantener migraciones Flyway consistentes.

## 16. Resumen Final

AdoptAPP esta organizado como una arquitectura de microservicios con Spring Boot, Maven multi-modulo, Eureka, API Gateway, Neon PostgreSQL, Flyway, OpenAPI, Docker y pruebas automatizadas.

La configuracion por perfiles permite ejecutar el proyecto en desarrollo local, pruebas con H2, PostgreSQL local, Neon y ambientes productivos. Las variables de entorno permiten separar la configuracion sensible del codigo fuente.

La ejecucion desde cero requiere configurar variables, compilar el proyecto, levantar Eureka, iniciar los microservicios y finalmente usar API Gateway como punto de entrada principal.
