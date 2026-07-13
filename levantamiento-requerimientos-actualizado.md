# Levantamiento De Requerimientos Actualizado - AdoptAPP

## 1. Proposito Del Documento

Este documento actualiza el levantamiento de requerimientos original del proyecto AdoptAPP y lo contrasta con el sistema codificado al cierre del semestre.

El objetivo es mostrar:

- Requerimientos originales definidos al inicio.
- Cambios realizados durante el desarrollo.
- Requerimientos eliminados, reemplazados o postergados.
- Diferencias entre la idea inicial y el producto final codificado.

Este documento no reemplaza la documentacion tecnica ni la documentacion funcional. Su foco es explicar la evolucion de los requerimientos.

## 2. Problema Original Identificado

El problema original del proyecto fue la falta de trazabilidad en el ciclo de vida de las mascotas dentro de un refugio.

La situacion inicial consideraba las siguientes dificultades:

- No existia un control claro del estado de cada mascota.
- No habia validacion automatica de adoptantes.
- La comunicacion entre areas medica, logistica y administrativa era lenta.
- Las adopciones podian retrasarse por falta de informacion centralizada.
- El bienestar animal podia verse afectado por falta de seguimiento.
- No habia suficiente trazabilidad sobre salud, adopciones, stock, donaciones y visitas posteriores.

AdoptAPP se propuso como una solucion basada en microservicios para organizar el proceso completo de adopcion y gestion de refugios.

## 3. Actores Originales Del Sistema

Los actores identificados inicialmente fueron:

- Adoptante: persona que busca adoptar una mascota y necesita transparencia en el proceso.
- Administrador del refugio: persona que coordina operaciones, personal y recursos del refugio.
- Veterinario: persona encargada de la salud y fichas clinicas de las mascotas.
- Voluntario: persona que apoya en logistica, visitas de seguimiento y tareas del refugio.

## 4. Actores Codificados Al Cierre

Durante el desarrollo, los actores fueron representados mediante roles del sistema:

- `ADMIN`: administrador general del sistema.
- `SHELTER_ADMIN`: administrador asociado a un refugio.
- `ADOPTER`: usuario adoptante.
- `VOLUNTEER`: voluntario.
- `VET`: veterinario.

### Cambio aplicado

El actor "Administrador del refugio" se formalizo como `SHELTER_ADMIN`.

Tambien se agrego el rol `ADMIN`, que no aparecia explicitamente como actor original, pero fue necesario para administrar el sistema completo.

## 5. Requerimientos Funcionales Originales

Los requerimientos funcionales originales fueron:

- RF01: Registro y gestion de perfiles de mascotas.
- RF02: Registro de adoptantes con validacion de datos personales.
- RF03: Creacion y seguimiento de solicitudes de adopcion.
- RF04: Registro de historial medico.
- RF05: Gestion de stock de alimentos y medicinas por sede.
- RF06: Programacion de visitas de seguimiento post-adopcion.
- RF07: Asignacion de turnos y tareas a voluntarios.
- RF08: Registro y procesamiento de donaciones monetarias.
- RF09: Emision de alertas automaticas sobre citas medicas.
- RF10: Generacion de reportes de tasa de adopcion mensual.
- RF11: Gestion de sedes fisicas.
- RF12: Validacion de antecedentes del adoptante.
- RF13: Carga de evidencias fotograficas para seguimiento.
- RF14: Buscador filtrado de mascotas.
- RF15: Cancelacion motivada de solicitudes de adopcion.

## 6. Requerimientos No Funcionales Originales

Los requerimientos no funcionales originales fueron:

- RNF01: Arquitectura basada en microservicios independientes.
- RNF02: Persistencia de datos individual por servicio usando JPA/Hibernate.
- RNF03: Comunicacion inter-servicio mediante WebClient o Feign Client.
- RNF04: Registro de auditoria y trazabilidad mediante logs con SLF4J.
- RNF05: Manejo centralizado de excepciones y validaciones JSR 380.

## 7. Resultado Codificado Al Cierre Del Semestre

Al cierre del semestre, el proyecto quedo implementado como un sistema de microservicios con los siguientes modulos principales:

- `user-service`
- `shelter-service`
- `staff-service`
- `pet-service`
- `health-service`
- `adoption-service`
- `followup-service`
- `donation-service`
- `supply-service`
- `notification-service`
- `shared-kernel`
- `api-gateway`
- `eureka-server`

El sistema tambien incorpora:

- Base de datos PostgreSQL en Neon.
- Schemas separados por microservicio.
- Migraciones automaticas con Flyway.
- Docker y Docker Compose.
- Despliegue en Render.
- API Gateway.
- Eureka Server.
- OpenAPI y Swagger.
- Basic Auth.
- HATEOAS.
- Pruebas con JUnit y Mockito.
- Perfil H2 para testing.
- JaCoCo para cobertura.

## 8. Cambios Principales Respecto Del Levantamiento Original

### 8.1 Se agrego un administrador general

El levantamiento original mencionaba principalmente al administrador del refugio. Durante el desarrollo se agrego el rol `ADMIN` para administrar el sistema completo.

Motivo:

Era necesario tener un rol superior para consultar informacion global, revisar registros eliminados y administrar operaciones generales.

### 8.2 Se separo la salud en un microservicio propio

La salud de la mascota no quedo dentro de `pet-service`. Se implemento como `health-service`.

Motivo:

Permite separar la informacion general de la mascota de su ficha clinica.

Resultado:

- `pet-service` gestiona datos generales de mascotas.
- `health-service` gestiona vacunacion, esterilizacion, enfermedades y estado de ficha clinica.

### 8.3 Las adopciones nacen automaticamente como PENDING

El requerimiento original hablaba de solicitudes de adopcion. Durante el desarrollo se definio una regla mas clara:

Toda adopcion creada nace en estado `PENDING`.

Solo despues puede pasar a:

- `APPROVED`
- `REJECTED`
- `CANCELLED`

### 8.4 Se agrego control para evitar adopciones duplicadas

Se agrego una regla de negocio que impide tener mas de una adopcion activa para la misma mascota.

Motivo:

Evitar que una mascota sea solicitada o aprobada varias veces al mismo tiempo.

### 8.5 Se aplico soft delete

En vez de borrar registros fisicamente, varios microservicios usan estados logicos:

- Mascotas: `DELETED`
- Usuarios: `INACTIVE`
- Staff: `INACTIVE`
- Supplies: `INACTIVE`
- Adopciones: `CANCELLED`
- Donaciones: `CANCELLED`
- Fichas de salud: `DELETED`
- Notificaciones: `ARCHIVED`

Motivo:

Mantener trazabilidad y evitar perdida de informacion historica.

### 8.6 Se agrego ownership por usuario, refugio y rol

El sistema codificado incluye reglas para que los usuarios vean o modifiquen solo la informacion que les corresponde.

Ejemplos:

- Un adoptante ve sus propias adopciones.
- Un administrador de refugio ve informacion asociada a su refugio.
- Un administrador general puede tener una vision mas amplia.

### 8.7 Se reemplazo "sedes fisicas" por refugios

El informe original hablaba de sedes fisicas. En el codigo se implemento como `shelter-service`.

Resultado:

La entidad principal usada en el sistema es "refugio".

### 8.8 Se reemplazo "stock por sede" por insumos por refugio

El requerimiento original hablaba de stock de alimentos y medicinas por sede.

En el codigo se implemento como `supply-service`, asociado a refugios.

### 8.9 Se implementaron notificaciones generales

El requerimiento original mencionaba alertas automaticas sobre citas medicas.

El sistema final implementa un microservicio de notificaciones mas general, utilizado para distintos eventos del sistema.

Cambio:

El alcance se amplio desde "citas medicas" hacia "notificaciones del sistema".

### 8.10 Se postergaron reportes mensuales

El requerimiento RF10 sobre reportes de tasa de adopcion mensual no quedo implementado como modulo final.

Estado:

Postergado como mejora futura.

### 8.11 Se postergaron evidencias fotograficas

El requerimiento RF13 sobre carga de evidencias fotograficas no quedo implementado.

Estado:

Postergado como mejora futura.

### 8.12 Se postergaron antecedentes tipo lista negra

El requerimiento RF12 sobre antecedentes del adoptante no quedo implementado como lista negra o aprobacion formal.

En su lugar, se implementaron validaciones de usuario, roles y permisos.

Estado:

Reemplazado parcialmente por validaciones de usuario y ownership.

## 9. Requerimientos Eliminados, Reemplazados O Postergados

### 9.1 Eliminados o no implementados al cierre

- Reportes mensuales de adopcion.
- Carga de evidencias fotograficas.
- Validacion formal de antecedentes con lista negra.
- Asignacion detallada de turnos a voluntarios.

### 9.2 Reemplazados

- "Sedes fisicas" fue reemplazado por refugios.
- "Stock por sede" fue reemplazado por insumos por refugio.
- "Alertas de citas medicas" fue reemplazado por notificaciones generales.
- "Administrador del refugio" fue formalizado como `SHELTER_ADMIN`.
- "Gestion medica dentro de mascota" fue reemplazada por `health-service`.

### 9.3 Agregados durante el desarrollo

- `shared-kernel`.
- API Gateway.
- Eureka Server.
- OpenAPI.
- HATEOAS.
- Docker.
- Render.
- Neon PostgreSQL.
- Flyway.
- Soft delete.
- Ownership por rol.
- Perfil H2 para pruebas.
- JaCoCo.

## 10. Comparacion General Entre Idea Inicial Y Sistema Final

La idea inicial se enfocaba en resolver la falta de trazabilidad y comunicacion entre areas de un refugio.

El sistema final mantiene ese objetivo, pero lo aterriza en una arquitectura mas concreta:

- Cada area importante quedo separada en un microservicio.
- Las adopciones tienen estados claros.
- Las mascotas cambian de estado segun la adopcion.
- Las fichas de salud se gestionan separadamente.
- Los registros eliminados quedan trazables.
- Los permisos dependen del rol del usuario.
- Los servicios se documentan y pueden probarse por separado.

## 11. Estado Final Del Proyecto

Al cierre del semestre, el proyecto implementa la mayor parte de los procesos principales:

- Gestion de usuarios.
- Gestion de roles.
- Gestion de refugios.
- Gestion de staff.
- Gestion de mascotas.
- Gestion de fichas de salud.
- Gestion de adopciones.
- Seguimiento post-adopcion.
- Gestion de donaciones.
- Gestion de insumos.
- Gestion de notificaciones.

Los requerimientos mas importantes del flujo de adopcion quedaron implementados. Algunos requerimientos secundarios quedaron fuera del alcance final y se consideran mejoras futuras.

## 12. Conclusion

El levantamiento original fue ajustado durante el desarrollo para construir una solucion mas realista y consistente con una arquitectura de microservicios.

El sistema final conserva el objetivo principal del proyecto: mejorar la trazabilidad del ciclo de vida de una mascota y ordenar el proceso de adopcion.

Los cambios realizados permitieron reforzar la seguridad, separar responsabilidades, mantener historiales, evitar duplicidades y entregar un proyecto mas viable para el cierre del semestre.
