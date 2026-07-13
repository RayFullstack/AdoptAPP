# Matriz De Requerimientos - AdoptAPP

## 1. Proposito

Esta matriz contrasta los requerimientos originales del proyecto AdoptAPP con lo codificado al cierre del semestre.

La matriz permite identificar:

- Requerimientos implementados.
- Requerimientos implementados parcialmente.
- Requerimientos reemplazados.
- Requerimientos postergados.
- Cambios realizados durante el desarrollo.

## 2. Criterios De Estado

| Estado | Significado |
| --- | --- |
| Implementado | El requerimiento existe en el sistema codificado. |
| Parcial | Existe una parte del requerimiento, pero no todo el alcance original. |
| Reemplazado | La idea original fue cubierta mediante otra solucion o nombre funcional. |
| Postergado | No fue codificado al cierre y queda como mejora futura. |
| Ampliado | El requerimiento original fue implementado con alcance mayor. |

## 3. Requerimientos Funcionales

| Codigo | Requerimiento original | Codificado al cierre | Estado | Cambios, eliminaciones o reemplazos |
| --- | --- | --- | --- | --- |
| RF01 | Registro y gestion de perfiles de mascotas: especie, edad y estado. | Implementado en `pet-service`. Permite registrar, actualizar, listar y cambiar estado de mascotas. | Implementado | Se agrego relacion con refugio y soft delete con estado `DELETED`. |
| RF02 | Registro de adoptantes con validacion de datos personales. | Implementado en `user-service`. Permite crear usuarios, registrar adoptantes y validar datos como email y username. | Implementado | Se agregaron roles y control de escalamiento a `ADMIN`. |
| RF03 | Creacion y seguimiento de solicitudes de adopcion. | Implementado en `adoption-service` y relacionado con `followup-service`. | Implementado | Las adopciones nacen automaticamente en estado `PENDING`. |
| RF04 | Registro de historial medico: vacunas y desparasitaciones. | Implementado en `health-service` con ficha clinica, vacunacion, esterilizacion y enfermedades. | Parcial | Desparasitaciones no quedaron como campo especifico. La salud se separo de mascota. |
| RF05 | Gestion de stock de alimentos y medicinas por sede. | Implementado en `supply-service`, asociado a refugios. | Reemplazado | "Sede" fue reemplazado por "refugio". El stock se maneja como insumos. |
| RF06 | Programacion de visitas de seguimiento post-adopcion. | Implementado en `followup-service`. | Implementado | El seguimiento se relaciona con usuario, mascota y adopcion. |
| RF07 | Asignacion de turnos y tareas a voluntarios. | Implementado parcialmente en `staff-service` como gestion de personal. | Parcial | No se implemento una agenda detallada de turnos o tareas. |
| RF08 | Registro y procesamiento de donaciones monetarias. | Implementado en `donation-service`. | Implementado | Se agrego estado automatico y soft delete con `CANCELLED`. |
| RF09 | Emision de alertas automaticas sobre citas medicas. | Implementado como notificaciones generales en `notification-service`. | Reemplazado | El alcance cambio de citas medicas especificas a notificaciones generales del sistema. |
| RF10 | Generacion de reportes de tasa de adopcion mensual. | No implementado como modulo o endpoint final. | Postergado | Queda como mejora futura. |
| RF11 | Gestion de sedes fisicas. | Implementado en `shelter-service` como gestion de refugios. | Reemplazado | "Sede fisica" fue formalizada como "refugio". |
| RF12 | Validacion de antecedentes del adoptante: lista negra o aprobado. | No implementado como lista negra. Se aplicaron validaciones de usuario, rol y ownership. | Parcial | La validacion de antecedentes fue reemplazada parcialmente por control de roles y datos. |
| RF13 | Carga de evidencias fotograficas para seguimiento. | No implementado. | Postergado | Queda como mejora futura. |
| RF14 | Buscador filtrado de mascotas por tamano, energia y zona. | Implementado parcialmente mediante listados y filtros existentes. | Parcial | No se implemento busqueda avanzada por energia o zona. |
| RF15 | Cancelacion motivada de solicitudes de adopcion. | Implementado en `adoption-service` mediante estado `CANCELLED`. | Parcial | Existe cancelacion, pero el motivo detallado no queda como requisito central codificado. |

## 4. Requerimientos No Funcionales

| Codigo | Requerimiento original | Codificado al cierre | Estado | Cambios, eliminaciones o reemplazos |
| --- | --- | --- | --- | --- |
| RNF01 | Arquitectura basada en microservicios independientes. | Proyecto construido con 10 microservicios, API Gateway y Eureka Server. | Implementado | Se agrego descubrimiento de servicios y gateway. |
| RNF02 | Persistencia de datos individual por servicio usando JPA/Hibernate. | Cada microservicio usa JPA/Hibernate y schema propio en PostgreSQL Neon. | Implementado | Se uso una base Neon con schemas separados por servicio. |
| RNF03 | Comunicacion inter-servicio mediante WebClient o Feign Client. | Implementado mediante clientes entre microservicios. | Implementado | Se agrego Eureka para descubrimiento y gateway para entrada comun. |
| RNF04 | Registro de auditoria y trazabilidad mediante logs con SLF4J. | Implementado con logs y registros historicos en varios servicios. | Implementado | Se reforzo con soft delete e historiales de cambios. |
| RNF05 | Manejo centralizado de excepciones y validaciones JSR 380. | Implementado con `GlobalExceptionHandler`, validaciones Jakarta y `shared-kernel`. | Implementado | Se agrego formato uniforme de error mediante `ErrorResponse`. |

## 5. Requerimientos Agregados Durante El Desarrollo

| Codigo | Requerimiento agregado | Codificado al cierre | Estado | Justificacion |
| --- | --- | --- | --- | --- |
| RA01 | Crear `shared-kernel`. | Modulo compartido implementado. | Implementado | Permite reutilizar errores, excepciones y utilidades comunes. |
| RA02 | Configurar API Gateway. | `api-gateway` implementado. | Implementado | Permite entrada centralizada a los microservicios. |
| RA03 | Configurar Eureka Server. | `eureka-server` implementado. | Implementado | Permite descubrimiento de servicios. |
| RA04 | Configurar Neon PostgreSQL. | Perfiles y variables para Neon implementados. | Implementado | Permite base de datos online. |
| RA05 | Configurar Flyway. | Migraciones automaticas implementadas por microservicio. | Implementado | Permite versionar cambios de base de datos. |
| RA06 | Aplicar soft delete. | Implementado con estados logicos por servicio. | Implementado | Evita perder trazabilidad historica. |
| RA07 | Bloquear operaciones sobre registros eliminados. | Implementado en varios servicios. | Implementado | Evita modificar registros inactivos, cancelados o eliminados. |
| RA08 | Validar ownership por rol. | Implementado en flujos principales. | Implementado | Evita acceso indebido a informacion de otros usuarios o refugios. |
| RA09 | Agregar OpenAPI y Swagger. | Implementado en microservicios. | Implementado | Permite documentar y probar endpoints. |
| RA10 | Agregar HATEOAS. | Implementado en controladores principales. | Implementado | Agrega enlaces de navegacion en respuestas. |
| RA11 | Agregar pruebas unitarias. | Implementadas con JUnit y Mockito. | Implementado | Valida reglas principales de negocio. |
| RA12 | Configurar H2 para pruebas. | Perfil `h2` implementado. | Implementado | Permite ejecutar pruebas sin depender de Neon. |
| RA13 | Configurar JaCoCo. | Configurado para medir cobertura. | Implementado | Permite revisar porcentaje de cobertura. |
| RA14 | Configurar Docker y Render. | Dockerfiles, Compose y variables para Render documentadas. | Implementado | Permite despliegue y ejecucion en ambiente externo. |

## 6. Requerimientos Postergados

| Codigo | Requerimiento | Motivo de postergacion | Posible mejora futura |
| --- | --- | --- | --- |
| RF10 | Reportes mensuales de tasa de adopcion. | No era parte del flujo critico de adopcion para el cierre. | Crear modulo o endpoint de reportes. |
| RF13 | Evidencias fotograficas para seguimiento. | Requiere manejo de archivos o almacenamiento externo. | Integrar carga de imagenes en followup-service. |
| RF12 | Lista negra o aprobacion formal de adoptantes. | Se priorizo validacion de usuario, roles y permisos. | Agregar estado de evaluacion del adoptante. |
| RF07 | Turnos y tareas detalladas para voluntarios. | Se implemento gestion de staff, no agenda avanzada. | Crear modulo de tareas o calendario. |

## 7. Requerimientos Reemplazados

| Requerimiento original | Reemplazo codificado | Motivo |
| --- | --- | --- |
| Sedes fisicas | Refugios en `shelter-service` | El concepto de refugio representa mejor el dominio del proyecto. |
| Stock por sede | Insumos por refugio en `supply-service` | Se unifico el stock bajo la entidad refugio. |
| Alertas de citas medicas | Notificaciones generales | El sistema necesitaba notificar mas eventos que solo citas medicas. |
| Salud dentro de mascota | `health-service` separado | Se separo la responsabilidad medica de la informacion general de mascota. |
| Administrador del refugio informal | Rol `SHELTER_ADMIN` | Se formalizo el permiso dentro de Spring Security. |

## 8. Conclusion De La Matriz

La mayoria de los requerimientos principales fueron implementados o adaptados al sistema final.

Los cambios mas importantes fueron:

- Separar responsabilidades por microservicio.
- Formalizar roles y permisos.
- Separar la salud de mascotas en un servicio propio.
- Aplicar estados logicos para mantener trazabilidad.
- Agregar componentes tecnicos necesarios para un sistema distribuido.

Los requerimientos postergados corresponden principalmente a funcionalidades complementarias, como reportes, evidencias fotograficas, lista negra de adoptantes y agenda avanzada de voluntarios.
