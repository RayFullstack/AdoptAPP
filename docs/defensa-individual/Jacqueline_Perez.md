# Defensa Técnica Individual

## Proyecto AdoptAPP

### Estudiante
**Jacqueline Pérez**

---------------------------------------------------------------------------------------------------------

# 1. Intro

El presente documento corresponde a mi defensa técnica individual del proyecto AdoptAPP, desarrollado durante la asignatura Full Stack I.

Durante el desarrollo del proyecto participé principalmente en la implementación de la lógica de negocio de distintos microservicios, colaborando además en la integración del sistema, documentación técnica, configuración de componentes y corrección de errores durante las distintas etapas del desarrollo.

Mi trabajo se concentró principalmente en los microservicios Adoption Service, FollowUp Service, Donation Service y Health Service, donde participé implementando funcionalidades, validaciones, integración con otros microservicios, documentación mediante Swagger, configuración de base de datos y pruebas funcionales.

El objetivo de esta defensa es describir detalladamente las actividades realizadas durante el desarrollo del proyecto y explicar el aporte realizado dentro del equipo.

-------------------------------------------------------------------------------------------------------------

# 2. Mi participación en el proyecto

Durante el desarrollo de AdoptAPP participé activamente en la implementación de diversos componentes del sistema.

Mis principales responsabilidades fueron:

- Configurar parte de los microservicios asignados para el proyecto.
- Implementar la lógica de negocio de Adoption Service.
- Implementar la lógica de negocio de FollowUp Service.
- Desarrollar Donation Service.
- Participar en el desarrollo de Health Service.
- Implementar validaciones del negocio.
- Integrar microservicios utilizando OpenFeign.
- Configurar documentación mediante Swagger/OpenAPI.
- Participar en la configuración de PostgreSQL y Flyway.
- Realizar correcciones detectadas durante las pruebas.
- Actualizar parte de la documentación técnica del proyecto.
- Participar en la integración final mediante Git.

Estas actividades permitieron consolidar una solución backend basada completamente en microservicios.

--------------------------------------------------------------------

# 3. Arquitectura utilizada

El proyecto fue desarrollado utilizando una arquitectura basada en microservicios.

Cada microservicio fue construido utilizando Spring Boot y organizado mediante una arquitectura por capas compuesta por:

- Controller
- Service
- Repository
- Model
- DTO
- Client
- Config
- Exception

Esta estructura facilita la separación de responsabilidades, mejora el mantenimiento del código y permite que cada servicio evolucione de manera independiente.

La comunicación entre microservicios fue implementada mediante Spring Cloud OpenFeign, mientras que API Gateway centraliza el acceso a los servicios y Eureka Server permite el descubrimiento automático de cada uno de ellos.

También participé en la configuración de varios de los microservicios asignados, verificando su correcta integración con el resto de la arquitectura antes de iniciar el desarrollo de la lógica de negocio.

------------------------------------------------------------------------------------------------------

# 4. Adoption Service

Uno de los principales microservicios en los que trabajé fue Adoption Service.

Mi participación consistió en desarrollar la lógica encargada de administrar todo el proceso de adopción de mascotas.

Durante esta implementación realicé las siguientes actividades:

- Implementé la capa Service donde se concentra toda la lógica de negocio.
- Desarrollé los DTO utilizados para recibir y responder información mediante la API.
- Implementé las validaciones necesarias para verificar la existencia del usuario y de la mascota antes de crear una adopción.
- Implementé las reglas para controlar los distintos estados de la adopción.
- Participé en la sincronización del estado de la mascota cuando una adopción era aprobada o cancelada.
- Implementé validaciones para evitar solicitudes de adopción duplicadas.
- Integré el microservicio con User Service, Pet Service, Notification Service y FollowUp Service mediante OpenFeign.
- Participé en la implementación del historial de adopciones.
- Realicé correcciones de errores detectados durante las pruebas de integración.
- Documenté los endpoints mediante Swagger/OpenAPI.

Además participé en la configuración de PostgreSQL y Flyway para asegurar el correcto funcionamiento del servicio utilizando migraciones automáticas.

Gracias a estas implementaciones el proceso completo de adopción pudo ejecutarse de forma segura, manteniendo la integridad de los datos y permitiendo la comunicación con el resto de los microservicios del proyecto.

# 5. FollowUp Service

Otro de los microservicios en los que participé activamente fue **FollowUp Service**, encargado de administrar el seguimiento posterior a la adopción de las mascotas.

Mi trabajo consistió en desarrollar e integrar la lógica necesaria para registrar el seguimiento realizado a cada adopción, permitiendo mantener un historial del estado de la mascota una vez entregada al adoptante.

Durante esta implementación realicé las siguientes actividades:

- Desarrollé la clase **FollowUpService**, donde implementé la lógica principal del microservicio.
- Configuré los repositorios encargados del acceso a la base de datos.
- Implementé los métodos para registrar nuevos seguimientos.
- Desarrollé los métodos para consultar seguimientos existentes.
- Implementé la actualización de seguimientos y cambios de estado.
- Integré el servicio con **User Service** para obtener información del adoptante.
- Integré el servicio con **Pet Service** para validar la mascota asociada al seguimiento.
- Integré el servicio con **Notification Service** para enviar notificaciones automáticas cuando existían cambios importantes.
- Implementé validaciones para asegurar la consistencia de los datos registrados.
- Participé en la corrección de errores detectados durante las pruebas funcionales.
- Documenté los endpoints utilizando Swagger/OpenAPI.

Con estas implementaciones fue posible mantener un registro actualizado del estado de las mascotas después de la adopción y facilitar el seguimiento realizado por los refugios.

--------------------------------------------------------------------------------------

# 6. Donation Service

También participé en el desarrollo del microservicio **Donation Service**, encargado de administrar las donaciones registradas dentro de la plataforma.

Mi trabajo consistió en desarrollar la lógica necesaria para registrar y administrar correctamente las donaciones realizadas por los usuarios.

Durante esta etapa desarrollé las siguientes funcionalidades:

- Implementé la entidad **Donation** utilizando anotaciones JPA.
- Desarrollé la clase **DonationService**, donde se concentra la lógica principal del microservicio.
- Implementé los métodos para crear nuevas donaciones.
- Implementé consultas para obtener el listado de donaciones registradas.
- Implementé la actualización de información.
- Implementé la cancelación lógica de donaciones.
- Configuré la comunicación con User Service para validar los usuarios.
- Configuré la comunicación con Shelter Service para validar los refugios.
- Integré Notification Service para generar notificaciones relacionadas con las donaciones.
- Implementé validaciones antes de registrar una nueva donación.
- Configuré PostgreSQL y Flyway para mantener sincronizada la estructura de la base de datos.
- Documenté completamente los endpoints mediante Swagger/OpenAPI.

Durante el desarrollo también participé en la resolución de errores detectados durante las pruebas de integración y en la optimización del funcionamiento del microservicio.

--------------------------------------------------------------------------------------

# 7. Health Service

Además participé en el desarrollo del microservicio **Health Service**, encargado de administrar la ficha clínica de cada mascota registrada en AdoptAPP.

Mi participación estuvo enfocada principalmente en implementar la lógica de negocio y la estructura necesaria para administrar correctamente la información médica de las mascotas.

Entre las actividades desarrolladas se encuentran:

- Implementé la entidad correspondiente a la ficha clínica.
- Participé en el desarrollo de la lógica de negocio del servicio.
- Implementé métodos para registrar información clínica.
- Implementé consultas para obtener antecedentes médicos.
- Participé en la actualización de vacunas, enfermedades y tratamientos.
- Configuré la comunicación con Pet Service para validar la mascota antes de registrar información.
- Integré Notification Service para informar cambios importantes en la ficha clínica.
- Implementé validaciones sobre la información ingresada.
- Participé en la corrección de errores encontrados durante la integración del sistema.
- Documenté los endpoints mediante Swagger/OpenAPI.

Estas implementaciones permitieron mantener organizada la información médica de las mascotas y facilitar futuras consultas clínicas dentro de la plataforma.

---------------------------------------------------------------------------------

# 8. Comunicación entre microservicios

Una parte importante de mi trabajo consistió en implementar la comunicación entre microservicios utilizando **Spring Cloud OpenFeign**.

Durante el desarrollo configuré distintos clientes Feign para permitir el intercambio de información entre los servicios sin acceder directamente a sus bases de datos.

Entre las integraciones realizadas se encuentran:

- Adoption Service con User Service.
- Adoption Service con Pet Service.
- Adoption Service con Notification Service.
- Adoption Service con FollowUp Service.
- FollowUp Service con User Service.
- FollowUp Service con Pet Service.
- FollowUp Service con Notification Service.
- Donation Service con User Service.
- Donation Service con Shelter Service.
- Donation Service con Notification Service.
- Health Service con Pet Service.
- Health Service con Notification Service.

Esta integración permitió mantener una arquitectura desacoplada, facilitando el mantenimiento del proyecto y permitiendo que cada microservicio cumpliera una responsabilidad específica sin depender directamente de la base de datos de otro servicio.

# 9. Seguridad del sistema

Durante el desarrollo del proyecto participé en la implementación y configuración de la seguridad de los microservicios utilizando **Spring Security**.

Mi trabajo consistió en proteger los endpoints más importantes para evitar accesos no autorizados y garantizar que solamente los usuarios con los permisos correspondientes pudieran ejecutar determinadas operaciones.

Entre las actividades realizadas se encuentran:

- Configuré Spring Security en los microservicios asignados.
- Implementé autenticación mediante Basic Authentication.
- Participé en la definición de roles como ADMIN, SHELTER_ADMIN, ADOPTER, VOLUNTEER y VET.
- Configuré la protección de los endpoints sensibles utilizando anotaciones como @PreAuthorize.
- Participé en la implementación de validaciones relacionadas con permisos de acceso.
- Verifiqué el correcto funcionamiento de la autenticación mediante Swagger y Postman.

Estas configuraciones permitieron mejorar la seguridad general del sistema y proteger la información administrada por cada microservicio.

---

# 10. Documentación mediante Swagger

Como parte del desarrollo también participé en la documentación de las APIs utilizando **Swagger/OpenAPI**.

Mi trabajo consistió en documentar correctamente los endpoints desarrollados para facilitar las pruebas y el consumo de la API por parte de otros desarrolladores.

Entre las tareas realizadas se encuentran:

- Documenté los controladores REST.
- Agregué descripciones a los endpoints utilizando @Operation.
- Configuré respuestas mediante @ApiResponse.
- Documenté los modelos utilizando @Schema.
- Configuré la autenticación BasicAuth dentro de Swagger.
- Verifiqué que cada endpoint pudiera ejecutarse correctamente desde Swagger UI.

Esta documentación permitió disponer de una interfaz gráfica para probar cada uno de los servicios desarrollados.

----------------------------------------------------------

# 11. PostgreSQL y Flyway

Durante el desarrollo participé en la configuración de la persistencia del proyecto utilizando PostgreSQL como base de datos principal.

Además colaboré en la configuración de Flyway para administrar automáticamente las migraciones de la base de datos.

Entre las actividades realizadas se encuentran:

- Configuración de PostgreSQL dentro de los microservicios.
- Configuración de los archivos application.yml y perfiles de ejecución.
- Configuración de las conexiones mediante variables de entorno.
- Configuración de Flyway para ejecutar las migraciones automáticamente.
- Verificación del correcto funcionamiento de las migraciones antes de iniciar cada microservicio.
- Corrección de errores relacionados con la conexión a la base de datos durante las pruebas.

La utilización de Flyway permitió mantener sincronizada la estructura de la base de datos durante todo el desarrollo.

--------------------------------------------------------------------------

# 12. Manejo de errores

También participé en la implementación del manejo de errores dentro de los microservicios.

El objetivo fue entregar respuestas uniformes cuando ocurría alguna excepción durante la ejecución del sistema.

Durante esta implementación participé en:

- Configuración de excepciones globales.
- Respuestas HTTP consistentes.
- Validaciones de datos obligatorios.
- Manejo de recursos inexistentes.
- Corrección de errores detectados durante las pruebas.

Las respuestas implementadas consideran códigos HTTP como:

- 400 Bad Request
- 401 Unauthorized
- 403 Forbidden
- 404 Not Found
- 409 Conflict
- 500 Internal Server Error
- 503 Service Unavailable

Esto permitió mejorar considerablemente la experiencia al consumir las APIs.

------------------------------------------------------------------------

# 13. Pruebas realizadas

Durante el desarrollo participé en la ejecución de distintas pruebas funcionales para verificar el correcto funcionamiento del sistema.

Las pruebas fueron realizadas utilizando principalmente:

- Swagger UI.
- Postman.
- Base de datos PostgreSQL.
- Perfil H2 para pruebas locales.

Las verificaciones realizadas permitieron comprobar:

- Registro de información.
- Consultas.
- Actualizaciones.
- Eliminaciones lógicas.
- Comunicación entre microservicios.
- Validaciones de negocio.
- Manejo de errores.

Gracias a estas pruebas fue posible detectar distintos problemas durante el desarrollo y corregirlos antes de integrar los microservicios con el resto del proyecto.

---------------------------------------------------------------------------------

# 14. Documentación técnica

Además del desarrollo del código participé en la actualización de la documentación técnica del proyecto.

Entre las actividades realizadas se encuentran:

- Actualización del README principal.
- Incorporación de información sobre Swagger.
- Documentación de reglas de negocio.
- Actualización de instrucciones de ejecución.
- Documentación relacionada con PostgreSQL.
- Documentación relacionada con Docker.
- Documentación de perfiles de ejecución.
- Actualización de la arquitectura general del proyecto.

Esta documentación permite que cualquier desarrollador pueda comprender el funcionamiento general del sistema y ejecutar el proyecto correctamente.

# 15. Control de versiones

Durante todo el desarrollo del proyecto utilicé Git como sistema de control de versiones y GitHub como plataforma para mantener organizado el trabajo realizado.

En una primera etapa trabajé sobre mi rama de desarrollo **jacqueline-dev**, donde fui registrando avances mediante distintos commits relacionados con los microservicios que desarrollé y las correcciones realizadas durante las pruebas.

Posteriormente, debido a que varias funcionalidades requerían integración directa con el trabajo de mi compañera, continué desarrollando parte de las implementaciones trabajando desde la rama **camila-dev** en su computador. Esta modalidad permitió avanzar de manera conjunta en la integración de los microservicios y resolver conflictos directamente durante el desarrollo.

Una vez finalizada la segunda entrega del proyecto se realizó la fusión (**merge**) entre ambas ramas, integrando el trabajo desarrollado de manera individual y conjunta en una única versión del sistema.

Entre los principales trabajos registrados durante el desarrollo se encuentran:

- Desarrollo de Adoption Service.
- Desarrollo de FollowUp Service.
- Desarrollo de Donation Service.
- Participación en Health Service.
- Corrección de errores detectados durante las pruebas.
- Integración entre microservicios.
- Configuración de PostgreSQL.
- Actualización de documentación.
- Integración final del proyecto.

El uso de Git permitió mantener un historial completo de cambios, facilitando el trabajo colaborativo y permitiendo recuperar versiones anteriores cuando fue necesario.

-----------------------------------------------------------------------------------------------

# 16. Integración del proyecto

Una vez terminado el desarrollo individual de cada integrante, participé en la integración final del proyecto.

Durante esta etapa colaboré en:

- Corrección de conflictos de integración.
- Verificación de compatibilidad entre microservicios.
- Ajustes de configuración.
- Corrección de errores de compilación.
- Verificación de comunicación mediante OpenFeign.
- Validación de conexiones con PostgreSQL.
- Revisión de migraciones Flyway.
- Revisión de la documentación técnica.
- Actualización del README.
- Revisión del funcionamiento general del sistema.

Gracias a esta etapa fue posible consolidar todos los microservicios en una única solución completamente funcional.

-----------------------------------------------------------

# 17. Principales dificultades encontradas

Durante el desarrollo del proyecto surgieron distintos desafíos técnicos que debieron resolverse para lograr una correcta integración del sistema.

Entre las principales dificultades se encontraban:

- Conflictos durante la integración de ramas mediante Git.
- Errores de configuración de PostgreSQL.
- Problemas de conexión entre microservicios.
- Ajustes de configuración en Spring Security.
- Corrección de errores relacionados con Flyway.
- Compatibilidad entre perfiles de ejecución.
- Integración de OpenFeign.
- Validación de dependencias Maven.

Cada uno de estos problemas fue solucionado mediante pruebas, revisión del código y trabajo colaborativo con el equipo.

-------------------------------------------------------------------------------------------

# 18. Aprendizajes obtenidos

Este proyecto representó una experiencia muy importante para mi formación como estudiante de Ingeniería en Informática.

Gracias al desarrollo de AdoptAPP pude fortalecer conocimientos relacionados con:

- Arquitectura basada en microservicios.
- Spring Boot.
- Spring Data JPA.
- Spring Security.
- PostgreSQL.
- Flyway.
- Swagger.
- OpenFeign.
- Git y GitHub.
- Arquitectura por capas.
- Desarrollo de APIs REST.
- Integración entre microservicios.
- Trabajo colaborativo utilizando ramas de desarrollo.

Además comprendí la importancia de mantener una buena organización del código, documentar correctamente cada componente y trabajar utilizando buenas prácticas de desarrollo.

----------------------------------------------------------------------------

# 19. Conclusión

Mi participación en el proyecto AdoptAPP estuvo enfocada principalmente en el desarrollo de los microservicios **Adoption Service**, **FollowUp Service**, **Donation Service** y **Health Service**, además de colaborar en la configuración de distintos componentes del sistema, la documentación técnica, las pruebas funcionales y la integración final del proyecto.

Durante el desarrollo implementé lógica de negocio, configuré la comunicación entre microservicios mediante OpenFeign, participé en la configuración de PostgreSQL y Flyway, documenté los endpoints utilizando Swagger y colaboré en la resolución de errores detectados durante las distintas etapas del proyecto.

Asimismo participé en el control de versiones utilizando Git y GitHub, trabajando inicialmente sobre mi rama de desarrollo y posteriormente colaborando directamente en la rama compartida durante la integración final del proyecto.

Este proyecto me permitió adquirir experiencia práctica utilizando tecnologías ampliamente utilizadas en el desarrollo de software moderno y comprender la importancia del trabajo colaborativo dentro de un equipo de desarrollo.

Considero que esta experiencia fortaleció significativamente mis conocimientos en desarrollo Backend y me permitió comprender el funcionamiento de una arquitectura distribuida basada en microservicios, aplicando herramientas y metodologías que serán de gran utilidad en futuros proyectos profesionales.
