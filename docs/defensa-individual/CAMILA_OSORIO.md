# Defensa Tecnica Individual - AdoptAPP

## 1. Identificacion

| Campo | Detalle |
|---|---|
| Proyecto | AdoptAPP |
| Tipo de proyecto | Backend con arquitectura de microservicios |
| Tecnologia principal | Java 21, Spring Boot 3.4.5 y Maven |
| Base de datos | Neon PostgreSQL y H2 para pruebas |
| Estudiante | Camila |

El presente documento tiene como propósito presentar la defensa técnica individual del trabajo realizado durante el desarrollo de AdoptAPP. En él se describen las principales contribuciones efectuadas, las decisiones técnicas adoptadas, los microservicios en los que participé, las reglas de negocio implementadas o corregidas y la evidencia disponible en el repositorio que respalda mi participación dentro del proyecto.

Además de desarrollar funcionalidades específicas, participé en la mejora de la arquitectura, la configuración del proyecto, la documentación técnica y la implementación de buenas prácticas relacionadas con seguridad, pruebas unitarias y mantenibilidad del código, buscando que el sistema fuera más consistente y fácil de evolucionar.

## 2. Alcance Del Trabajo Individual

Mi participación se concentró principalmente en el desarrollo y mejora de los módulos shared-kernel, user-service, adoption-service, notification-service, shelter-service y staff-service. En estos microservicios trabajé tanto en la implementación de nuevas funcionalidades como en la corrección de reglas de negocio, validaciones y mejoras relacionadas con la seguridad y la consistencia de la información.

De forma complementaria, también realicé cambios que impactan a todo el proyecto, especialmente en la configuración de perfiles de ejecución, migraciones con Flyway, documentación mediante Swagger/OpenAPI, organización de pruebas unitarias, configuración de la base de datos y actualización de la documentación principal del repositorio.

El objetivo de estos cambios fue mantener una arquitectura consistente entre los distintos microservicios, reducir la duplicación de código, mejorar la comunicación entre servicios y asegurar que cada componente cumpliera con las responsabilidades definidas dentro de la arquitectura del sistema.

## 3. Estructura Multi-Modulo Maven

AdoptAPP fue desarrollado utilizando una arquitectura multi-módulo con Maven, decisión que permitió organizar todos los microservicios dentro de un único repositorio sin perder la independencia de cada uno de ellos. Cada servicio mantiene su propia configuración, dependencias y ciclo de vida, mientras que el proyecto padre centraliza las versiones de las librerías, plugins y configuraciones compartidas.

La estructura principal del proyecto quedó organizada de la siguiente manera:

adoptapp/
├── user-service
├── pet-service
├── adoption-service
├── notification-service
├── health-service
├── followup-service
├── donation-service
├── staff-service
├── supply-service
├── shelter-service
├── api-gateway
├── eureka-server
├── shared-kernel
├── compose.yml
└── pom.xml

Esta organización facilitó el desarrollo independiente de cada microservicio, permitiendo realizar cambios sin afectar directamente al resto del sistema. Asimismo, el proyecto padre permitió mantener una configuración uniforme para todos los módulos, simplificando la administración de dependencias y reduciendo posibles problemas de compatibilidad entre versiones.

Otra ventaja importante de esta estructura fue la posibilidad de compilar el proyecto completo desde la raíz o ejecutar únicamente el módulo que se estuviera desarrollando o probando. Esto agilizó considerablemente el proceso de desarrollo y facilitó la integración continua durante las distintas etapas del proyecto.

## 4. Configuracion De Microservicios Principales

Dentro de mi participación trabajé principalmente sobre los microservicios user-service, adoption-service, notification-service, shelter-service, staff-service y el módulo shared-kernel, cada uno de ellos responsable de una parte específica de la lógica de negocio del sistema.

El user-service concentra toda la administración de usuarios, autenticación, roles y permisos, convirtiéndose en uno de los servicios más importantes para controlar el acceso a la aplicación.

El adoption-service administra el flujo completo del proceso de adopción, validando la información del adoptante, la disponibilidad de las mascotas y las distintas reglas de negocio asociadas al ciclo de vida de una adopción.

Por su parte, el notification-service centraliza el envío y administración de notificaciones generadas por los distintos microservicios, permitiendo mantener informados tanto a los usuarios como a los administradores del sistema cuando ocurre un evento relevante.

En el caso del shelter-service, este se encarga de la administración de los refugios registrados en la plataforma, mientras que el staff-service gestiona el personal asociado a cada uno de ellos y la relación existente entre usuarios y refugios para aplicar correctamente las reglas de ownership y permisos.

Finalmente, el módulo shared-kernel fue utilizado para centralizar componentes reutilizables entre los distintos microservicios, evitando duplicar clases comunes y manteniendo una estructura uniforme para el manejo de respuestas, excepciones y objetos compartidos.

Todos estos servicios fueron organizados siguiendo una arquitectura por capas, separando claramente las responsabilidades entre controladores, servicios, repositorios, modelos, DTOs, clientes Feign, configuraciones y manejo de excepciones. Esta organización permitió mantener un código más limpio, facilitar las pruebas unitarias y mejorar el mantenimiento del proyecto a medida que fueron incorporándose nuevas funcionalidades.

## 5. Shared Kernel

Con el objetivo de evitar la duplicación de código entre los distintos microservicios, se incorporó el módulo shared-kernel, el cual centraliza las clases y componentes que son utilizados de manera transversal por varios servicios del sistema. Esta decisión permitió mantener una estructura más ordenada y asegurar que todos los microservicios compartieran los mismos contratos y mecanismos de manejo de errores.

Dentro de este módulo se agruparon clases como DTOs reutilizables, excepciones personalizadas, respuestas estandarizadas para errores y utilidades comunes utilizadas durante la comunicación entre servicios. De esta forma, cuando un cambio debía realizarse sobre alguno de estos componentes, era suficiente modificar una única implementación, evitando inconsistencias entre los distintos módulos del proyecto.

Además de reducir la cantidad de código repetido, el shared-kernel facilitó el mantenimiento del sistema y permitió que la comunicación entre microservicios se mantuviera consistente a medida que el proyecto fue creciendo.

## 6. Perfiles De Ejecucion

Con el fin de facilitar el desarrollo, las pruebas y el despliegue del sistema, se configuraron distintos perfiles de ejecución utilizando archivos application.yml específicos para cada entorno. Gracias a esta configuración fue posible adaptar el comportamiento de los microservicios sin necesidad de modificar el código fuente, simplemente seleccionando el perfil correspondiente al momento de ejecutar la aplicación.

El perfil neon fue utilizado como entorno principal de desarrollo, permitiendo conectar todos los microservicios a una base de datos PostgreSQL alojada en la nube mediante Neon. Por otra parte, el perfil h2 fue configurado para ejecutar pruebas utilizando una base de datos en memoria, lo que permitió realizar validaciones unitarias de forma rápida y sin depender de servicios externos.

También se configuraron los perfiles dev, prod y postgres, destinados al desarrollo local, al entorno productivo y a una instalación opcional utilizando PostgreSQL local. Esta separación permitió trabajar con distintos ambientes sin modificar la configuración principal del proyecto y facilitó las pruebas durante las distintas etapas de desarrollo.

La utilización de perfiles independientes mejoró considerablemente la flexibilidad del sistema y permitió mantener una configuración más limpia, organizada y fácil de administrar.

Archivos relacionados:

```text
user-service/src/main/resources/application-*.yml
adoption-service/src/main/resources/application-*.yml
notification-service/src/main/resources/application-*.yml
shelter-service/src/main/resources/application-*.yml
staff-service/src/main/resources/application-*.yml
```

Uso de cada perfil:

| Perfil | Uso |
|---|---|
| `neon` | Ejecucion principal usando Neon PostgreSQL |
| `h2` | Pruebas unitarias y ejecucion sin base externa |
| `dev` | Desarrollo local |
| `prod` | Configuracion productiva |
| `postgres` | PostgreSQL local opcional |

El perfil `h2` es importante porque permite ejecutar pruebas sin depender de Neon ni de una instalacion local de PostgreSQL.

## 7. Flyway Para Migraciones Automaticas

Para administrar la estructura de la base de datos se incorporó Flyway como herramienta de migraciones automáticas. Su utilización permitió mantener un control de versiones sobre los cambios realizados en cada esquema, evitando modificaciones manuales y asegurando que todos los desarrolladores trabajaran sobre la misma estructura de base de datos.

Cada microservicio mantiene sus propias migraciones dentro de la carpeta correspondiente, respetando el principio de independencia entre servicios. De esta forma, cada módulo administra únicamente las tablas y estructuras que forman parte de su dominio, evitando dependencias innecesarias con el resto de la aplicación.

Esta estrategia también facilitó el despliegue del proyecto utilizando Neon PostgreSQL, ya que las migraciones se ejecutan automáticamente al iniciar la aplicación cuando corresponde. Gracias a esto fue posible mantener sincronizados los distintos esquemas del sistema y reducir significativamente los errores relacionados con diferencias entre ambientes de desarrollo y producción.

Archivos relacionados:

```text
*/src/main/resources/db/migration/
*/src/main/resources/application-neon.yml
```

Schemas usados:

```text
users_service
adoption_service
notification_service
shelter_service
staff_service
```

## 8. Logica De Usuarios, Roles Y Permisos

Una parte importante de mi trabajo estuvo enfocada en el desarrollo y mejora del user-service, encargado de centralizar toda la administración de usuarios dentro del sistema. Este microservicio no solo permite registrar, consultar y actualizar usuarios, sino que también concentra la lógica relacionada con la autenticación, los roles y las validaciones necesarias para proteger las operaciones más sensibles de la aplicación.

Durante el desarrollo se incorporaron distintas reglas de negocio orientadas a garantizar la integridad de la información. Entre ellas destacan las validaciones para impedir registros duplicados utilizando el mismo correo electrónico o nombre de usuario, el control de usuarios inactivos mediante soft delete, el registro del historial de cambios y la prevención de modificaciones indebidas sobre los roles asignados a cada usuario.

Además, se definió una estructura de roles compuesta por ADMIN, SHELTER_ADMIN, ADOPTER, VOLUNTEER y VET, donde cada uno posee permisos específicos según las responsabilidades que desempeña dentro del sistema. Esta organización permitió aplicar restricciones directamente sobre los endpoints y reforzar la seguridad del proyecto mediante reglas de autorización acordes a la lógica de negocio.

Centralizar esta información en un único microservicio facilitó la administración de usuarios y permitió que el resto de los servicios consumieran la información necesaria para validar permisos y controlar el acceso a las distintas funcionalidades del sistema.

Partes trabajadas:

```text
user-service/src/main/java/com/adoptapp/userservice/
user-service/src/test/java/com/adoptapp/userservice/
```

## 9. Gestion De Refugios

Dentro de los microservicios en los que participé, uno de los principales fue shelter-service, encargado de administrar toda la información relacionada con los refugios registrados en la plataforma. Este servicio concentra la lógica necesaria para crear, consultar y actualizar refugios, manteniendo la información organizada y asegurando que cada operación cumpla con las reglas de negocio definidas para el sistema.

Durante el desarrollo se incorporaron validaciones orientadas a preservar la integridad de los datos, especialmente en los procesos de eliminación lógica. Antes de marcar un refugio como eliminado, el sistema verifica que no existan mascotas, miembros del personal o insumos activos asociados a dicho refugio. De esta forma se evita dejar información huérfana o generar inconsistencias entre los distintos microservicios.

Asimismo, se aplicaron controles de ownership para garantizar que un administrador de refugio únicamente pudiera gestionar la información correspondiente a la organización con la que se encuentra asociado. Estas validaciones complementan la seguridad implementada mediante roles y permiten proteger operaciones sensibles que no pueden resolverse únicamente con autenticación.

Partes trabajadas:

```text
shelter-service/src/main/java/com/adoptapp/shelterservice/
shelter-service/src/test/java/com/adoptapp/shelterservice/
```


## 10. Gestion De Staff

El staff-service fue desarrollado para administrar el personal que trabaja en cada refugio y mantener la relación entre usuarios y organizaciones dentro del sistema. Este microservicio cumple un rol importante, ya que la información almacenada en él es utilizada posteriormente para validar permisos, ownership y distintas reglas de acceso implementadas en otros servicios.

Dentro de este módulo se trabajó en la creación, consulta y actualización de los registros de personal, además de las validaciones necesarias para comprobar la existencia tanto del usuario como del refugio antes de establecer la relación entre ambos. También se incorporó la eliminación lógica mediante el estado INACTIVE, permitiendo conservar el historial sin eliminar físicamente los registros de la base de datos.

Gracias a estas validaciones fue posible asegurar que cada miembro del personal permaneciera correctamente asociado a un refugio y que solo pudiera realizar acciones dentro del contexto que le corresponde según su rol y responsabilidades.

Partes trabajadas:

```text
staff-service/src/main/java/com/adoptapp/staffservice/
staff-service/src/test/java/com/adoptapp/staffservice/
```


## 11. Gestion De Notificaciones

El notification-service fue diseñado para centralizar la administración de las notificaciones generadas por los distintos procesos del sistema. En lugar de que cada microservicio administrara sus propios mensajes, se optó por concentrar esta responsabilidad en un servicio independiente, facilitando el mantenimiento y la reutilización de la lógica relacionada con las notificaciones.

Durante su desarrollo se implementaron funcionalidades para crear, consultar y filtrar notificaciones según distintos criterios, como el usuario destinatario, el refugio asociado o el tipo de evento que las originó. También se incorporó un mecanismo de eliminación lógica mediante el estado ARCHIVED, permitiendo conservar el historial sin afectar la trazabilidad de los eventos registrados por la aplicación.

Adicionalmente, se estandarizó el formato de las respuestas y el manejo de errores para mantener un comportamiento uniforme con el resto de los microservicios. Esto permitió simplificar la integración con otros servicios y mejorar la consistencia general de la arquitectura.

Partes trabajadas:

```text
notification-service/src/main/java/com/adoptapp/notificationservice/
notification-service/src/test/java/com/adoptapp/notificationservice/
```

## 12. Logica De Adopciones

Uno de los componentes más relevantes del proyecto corresponde al adoption-service, encargado de administrar el flujo completo del proceso de adopción. Este microservicio coordina información proveniente de distintos servicios, como usuarios, mascotas, refugios y notificaciones, por lo que gran parte de la lógica de negocio del sistema se concentra en este módulo.

Durante su desarrollo se implementaron y corrigieron diversas reglas orientadas a garantizar la consistencia del proceso de adopción. Entre ellas se encuentran las validaciones del usuario adoptante, la verificación de la disponibilidad de la mascota, la comprobación de la existencia del refugio asociado y el control de permisos según el rol del usuario autenticado.

Además de administrar la creación, aprobación, rechazo y cancelación de solicitudes, este servicio incorpora validaciones que permiten evitar estados inconsistentes dentro del sistema, asegurando que cada adopción siga el flujo definido por las reglas de negocio y que las distintas operaciones se ejecuten únicamente cuando se cumplen todas las condiciones necesarias.

Partes trabajadas:

```text
adoption-service/src/main/java/com/adoptapp/adoptionservice/
adoption-service/src/test/java/com/adoptapp/adoptionservice/
```

## 13. Adopciones En Estado PENDING

Como parte de la lógica del proceso de adopción, se definió que toda solicitud creada por un usuario debe iniciar automáticamente en estado PENDING. Esta decisión busca asegurar que ninguna adopción pueda ser aprobada o rechazada directamente al momento de su creación, manteniendo un flujo de trabajo controlado y acorde con las reglas del negocio.

De esta manera, cada solicitud debe ser revisada posteriormente por un usuario con los permisos correspondientes, como un SHELTER_ADMIN o un ADMIN, quienes son los responsables de aprobar o rechazar la adopción según la información disponible. Esta validación evita que existan cambios de estado indebidos y permite mantener un proceso de adopción más seguro y transparente para todas las partes involucradas.

## 14. Sincronizacion Entre Adopcion Y Mascota

Una vez definido el flujo de adopción, también fue necesario mantener sincronizada la información entre adoption-service y pet-service. El objetivo fue asegurar que el estado de una mascota reflejara correctamente la situación real del proceso de adopción y evitar inconsistencias entre ambos servicios.

Para ello se implementó una comunicación entre microservicios que actualiza automáticamente la disponibilidad de la mascota cuando una solicitud cambia de estado. Si una adopción es aprobada, la mascota pasa a estar NOT_AVAILABLE, impidiendo que otros usuarios puedan iniciar nuevas solicitudes sobre ella. Del mismo modo, si una adopción previamente aprobada es cancelada, la mascota puede volver a quedar disponible para futuras adopciones.

Esta sincronización permitió mantener la información consistente entre ambos servicios y reducir el riesgo de que una mascota apareciera disponible cuando ya existía una adopción confirmada.

## 15. Validaciones Para Evitar Adopciones Duplicadas

Otra de las reglas de negocio implementadas fue impedir que una misma mascota pudiera tener más de una adopción activa al mismo tiempo. Antes de registrar una nueva solicitud, el sistema verifica si ya existe una adopción en estado PENDING o APPROVED para esa mascota.

Si alguna de estas condiciones se cumple, la operación es rechazada y el usuario recibe un mensaje indicando que la mascota ya posee un proceso de adopción en curso.

Esta validación fue incorporada para proteger la consistencia del sistema y evitar situaciones donde distintos usuarios intentaran adoptar simultáneamente a la misma mascota, lo que podría generar conflictos durante la gestión del proceso.

Estados considerados activos:

```text
PENDING
APPROVED
```

## 16. Gestion De Mascotas Y Separacion De Salud

Aunque mi participación principal estuvo enfocada en otros microservicios, también colaboré en la reorganización de la información relacionada con la salud de las mascotas. Inicialmente, estos datos formaban parte de pet-service, sin embargo, se decidió separar esta responsabilidad creando un microservicio independiente denominado health-service.

Esta modificación permitió que pet-service se concentrara únicamente en la administración de la información general de cada mascota, mientras que health-service quedó encargado de gestionar las fichas clínicas, el historial médico, las vacunas, la esterilización y las enfermedades registradas.

La separación de estas responsabilidades permitió mantener una arquitectura más alineada con los principios de los microservicios, reduciendo el acoplamiento entre componentes y facilitando el mantenimiento de cada dominio de negocio de forma independiente.

## 17. Seguimiento Post-Adopcion

Como parte del flujo completo de adopción, también se incorporó la lógica necesaria para realizar el seguimiento posterior a la entrega de una mascota. Este proceso permite registrar controles posteriores a la adopción con el objetivo de verificar que la mascota continúe en buenas condiciones y que el proceso se haya desarrollado de manera satisfactoria.

El seguimiento contempla el registro de observaciones, visitas y comentarios asociados a una adopción específica, manteniendo la relación entre el adoptante, la mascota y el seguimiento realizado. Asimismo, se incorporó la posibilidad de cancelar un seguimiento cuando corresponda, manteniendo siempre el historial mediante el uso de estados lógicos en lugar de eliminar físicamente la información.

La incorporación de esta funcionalidad permitió completar el ciclo de adopción dentro del sistema, extendiendo la gestión más allá de la aprobación de la solicitud y aportando un mayor control sobre el bienestar de las mascotas después de ser adoptadas.

## 18. Donaciones, Insumos, Staff Y Notificaciones

Además de los microservicios principales, también se realizaron revisiones y mejoras sobre distintos servicios que complementan el funcionamiento general de AdoptAPP. Entre ellos se encuentran donation-service, supply-service, staff-service y notification-service, los cuales permiten administrar procesos que apoyan la gestión diaria de los refugios y mejoran la interacción entre los diferentes usuarios del sistema.

Durante el desarrollo se revisaron distintas reglas de negocio relacionadas con la administración de donaciones, el control de insumos, la gestión del personal y el envío de notificaciones. Asimismo, se incorporaron validaciones para comprobar la existencia de usuarios y refugios antes de realizar determinadas operaciones, además de corregir situaciones donde algunas notificaciones podían ser enviadas al destinatario incorrecto.

Estas mejoras permitieron mantener una mayor consistencia entre los distintos microservicios y asegurar que la información intercambiada durante cada proceso correspondiera realmente a la entidad involucrada.

## 19. Soft Delete

Con el objetivo de preservar la información histórica del sistema, se decidió utilizar una estrategia de soft delete en lugar de eliminar físicamente los registros de la base de datos. Esta decisión permite mantener la trazabilidad de las operaciones realizadas y facilita futuras auditorías o consultas históricas.

Cada microservicio administra la eliminación lógica utilizando estados específicos según el tipo de entidad. Por ejemplo, los usuarios pasan al estado INACTIVE, las mascotas y refugios utilizan el estado DELETED, mientras que las adopciones, donaciones y seguimientos utilizan estados acordes a su ciclo de vida, como CANCELLED o ARCHIVED en el caso de las notificaciones.

Gracias a esta estrategia fue posible conservar el historial de la información sin afectar la integridad de la base de datos ni perder referencias importantes entre los distintos microservicios.

Se aplicó en los microservicios de la siguiente forma:

| Entidad | Estado |
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

Esto permite mantener trazabilidad y evitar perdida fisica de informacion.

## 20. Bloqueo De Operaciones Sobre Registros Eliminados

La implementación del soft delete hizo necesario incorporar validaciones adicionales para impedir que un registro eliminado lógicamente continuara siendo modificado por el sistema. Por esta razón, se agregaron controles que verifican el estado de cada entidad antes de permitir cualquier operación de actualización o eliminación.

Estas validaciones impiden, por ejemplo, modificar usuarios inactivos, actualizar refugios eliminados, editar notificaciones archivadas o realizar cambios sobre adopciones canceladas. Del mismo modo, se evita ejecutar nuevamente una eliminación sobre registros que ya se encuentran deshabilitados.

La incorporación de estas restricciones permitió reforzar la lógica de negocio y prevenir estados inconsistentes que podrían afectar el funcionamiento general de la aplicación.

Ejemplos:

- No actualizar usuarios `INACTIVE`.
- No actualizar refugios `DELETED`.
- No actualizar staff `INACTIVE`.
- No actualizar notificaciones `ARCHIVED`.
- No actualizar adopciones `CANCELLED`.
- No eliminar dos veces el mismo recurso.

## 21. Validacion De Ownership

Como complemento a la autenticación basada en roles, se implementaron reglas de ownership con el propósito de controlar que cada usuario únicamente pudiera acceder a la información que realmente le corresponde.

Estas validaciones consideran distintos factores, como el rol autenticado, el identificador del usuario y la relación existente entre el personal y el refugio al que pertenece. Gracias a ello, un usuario con rol ADOPTER únicamente puede consultar sus propias adopciones, mientras que un SHELTER_ADMIN solo puede administrar los recursos asociados a su refugio. Por su parte, el rol ADMIN mantiene acceso completo al sistema debido a sus funciones administrativas.

Este mecanismo permitió fortalecer la seguridad de la aplicación, agregando una capa adicional de control que complementa la autorización implementada mediante Spring Security.

Ejemplos:

- `ADOPTER` solo puede ver sus propias adopciones.
- `SHELTER_ADMIN` solo puede gestionar recursos de su refugio.
- `ADMIN` puede acceder a todos los recursos.
- Staff se usa para relacionar usuarios con refugios.

## 22. BasicAuth Para Swagger

Con el propósito de facilitar las pruebas y la documentación de los distintos microservicios, se incorporó Swagger/OpenAPI como herramienta para generar la documentación de las APIs de forma automática.

Durante esta configuración se agregó soporte para autenticación mediante Basic Auth, permitiendo que los endpoints protegidos pudieran probarse directamente desde la interfaz de Swagger utilizando el botón Authorize. Además, se documentaron los principales endpoints, los DTO utilizados en las solicitudes y respuestas, los posibles códigos de estado HTTP y la estructura de los errores devueltos por la aplicación.

Esta documentación facilita tanto el desarrollo como las pruebas del sistema, ya que permite visualizar el funcionamiento de cada endpoint sin necesidad de utilizar herramientas externas para comprender la estructura de las solicitudes.

Archivos relacionados:

```text
*/src/main/java/**/config/OpenApiConfig.java
*/src/main/java/**/controller/*Controller.java
```

## 23. Pruebas Unitarias Con JUnit Y Mockito

Con el objetivo de asegurar el correcto funcionamiento de la lógica de negocio, se desarrollaron pruebas unitarias utilizando JUnit 5, Mockito, AssertJ y Spring Security Test. Estas pruebas fueron diseñadas para validar el comportamiento de los servicios de manera aislada, simulando las dependencias externas mediante el uso de mocks.

Gracias a esta estrategia fue posible comprobar el cumplimiento de las principales reglas de negocio sin necesidad de levantar todos los microservicios ni establecer conexiones reales con la base de datos u otros servicios del sistema.

Las pruebas permitieron detectar errores de forma temprana y verificar que las distintas funcionalidades continuaran operando correctamente a medida que el proyecto evolucionaba, aportando una mayor estabilidad y facilitando el mantenimiento del código.

## 24. Organizacion De Pruebas

Con el crecimiento del proyecto, fue necesario mantener una organización clara de las pruebas unitarias para facilitar su mantenimiento y permitir que cada microservicio pudiera validarse de forma independiente. Para ello, las pruebas fueron separadas según la responsabilidad de cada componente, diferenciando aquellas enfocadas en la lógica de negocio, los controladores y los clientes utilizados para la comunicación entre microservicios.

Las pruebas de la capa service se orientaron principalmente a validar las reglas de negocio implementadas en cada módulo, mientras que las pruebas de controller permitieron verificar las respuestas HTTP y el comportamiento de los distintos endpoints expuestos por la API. Por otra parte, los Feign Clients también fueron considerados dentro de la estrategia de pruebas, incorporando validaciones para los mecanismos de fallback cuando algún servicio externo no se encontraba disponible.

Esta organización permitió mantener una estructura más ordenada, facilitó la incorporación de nuevas pruebas durante el desarrollo y contribuyó a mejorar la mantenibilidad del proyecto.

Ejemplos de archivos de prueba asociados al trabajo:

```text
user-service/src/test/java/com/adoptapp/userservice/service/UserServiceTest.java
user-service/src/test/java/com/adoptapp/userservice/controller/UserControllerTest.java
adoption-service/src/test/java/com/adoptapp/adoptionservice/service/AdoptionServiceTest.java
adoption-service/src/test/java/com/adoptapp/adoptionservice/controller/AdoptionControllerTest.java
notification-service/src/test/java/com/adoptapp/notificationservice/service/NotificationServiceTest.java
notification-service/src/test/java/com/adoptapp/notificationservice/controller/NotificationControllerTest.java
shelter-service/src/test/java/com/adoptapp/shelterservice/service/ShelterServiceTest.java
shelter-service/src/test/java/com/adoptapp/shelterservice/controller/ShelterControllerTest.java
staff-service/src/test/java/com/adoptapp/staffservice/service/StaffServiceTest.java
staff-service/src/test/java/com/adoptapp/staffservice/controller/StaffControllerTest.java
```



## 25. Perfil H2 Para Pruebas

Con el objetivo de ejecutar pruebas de manera rápida y sin depender de servicios externos, se configuró un perfil específico utilizando la base de datos H2 en memoria. Gracias a esta configuración fue posible ejecutar las pruebas unitarias sin necesidad de establecer una conexión con la base de datos principal alojada en Neon PostgreSQL.

El uso de H2 permitió aislar completamente el entorno de pruebas, reduciendo los tiempos de ejecución y evitando problemas derivados de la disponibilidad de la base de datos externa. Además, esta configuración facilita la integración continua, ya que las pruebas pueden ejecutarse en cualquier entorno sin requerir una configuración adicional.

La utilización de perfiles independientes para desarrollo y pruebas permitió mantener un entorno más controlado y reducir el riesgo de afectar la información almacenada en la base de datos principal durante el proceso de desarrollo.

Archivos relacionados:

```text
*/src/main/resources/application-h2.yml
```

## 26. JaCoCo Para Cobertura

Como parte de la estrategia de aseguramiento de calidad, se incorporó JaCoCo para medir la cobertura obtenida por las pruebas unitarias desarrolladas en los distintos microservicios. Esta herramienta permite generar reportes que muestran qué clases, métodos y líneas de código fueron ejecutados durante las pruebas, facilitando la identificación de componentes que aún requieren una mayor validación.

Los reportes generados fueron utilizados como apoyo para evaluar el alcance de las pruebas implementadas y detectar posibles áreas del proyecto que necesitaban incrementar su cobertura. Esto permitió orientar de mejor manera el desarrollo de nuevas pruebas y mejorar progresivamente la calidad del código.

La incorporación de JaCoCo complementó el proceso de testing y proporcionó una visión más clara del nivel de cobertura alcanzado por el proyecto.

Archivo relacionado:

```text
pom.xml
```

Comando de ejecucion:

```powershell
.\mvnw.cmd clean test jacoco:report
```

Reportes:

```text
<microservicio>/target/site/jacoco/index.html
```

## 27. Revision De Errores De Logica

Durante el desarrollo del proyecto también se realizó un proceso constante de revisión y corrección de distintas reglas de negocio que presentaban inconsistencias o podían generar comportamientos no esperados dentro de la aplicación.

Entre las mejoras realizadas se encuentran la validación para impedir adopciones duplicadas, la actualización automática del estado de las mascotas durante el proceso de adopción, el fortalecimiento de las reglas de ownership, la corrección del manejo de registros eliminados mediante soft delete y la validación de permisos asociados a los distintos roles del sistema.

Asimismo, se revisaron procesos relacionados con el envío de notificaciones, la administración de refugios y la validación de usuarios, incorporando controles adicionales para reducir la posibilidad de errores y asegurar un comportamiento consistente entre los diferentes microservicios.

Estas mejoras permitieron fortalecer la lógica del sistema y aumentar la confiabilidad de las operaciones realizadas por la aplicación.

## 28. Actualizacion Del README

Finalmente, se realizó una actualización de la documentación técnica del proyecto con el propósito de reflejar el estado real de la aplicación y facilitar su comprensión tanto para los integrantes del equipo como para futuros desarrolladores.

El archivo README fue ampliado incorporando información sobre la arquitectura basada en microservicios, los componentes principales del sistema, las reglas de negocio implementadas, la configuración de seguridad, el uso de Swagger/OpenAPI, la ejecución de pruebas unitarias, la cobertura mediante JaCoCo y los distintos perfiles de configuración disponibles.

También se documentó el proceso de despliegue utilizando Docker, API Gateway, Eureka Server y Neon PostgreSQL, además de incluir instrucciones para la ejecución local del proyecto y las variables de entorno necesarias para su funcionamiento.

Contar con una documentación actualizada permitió que el proyecto fuera más fácil de comprender, ejecutar y mantener, además de servir como apoyo durante la presentación y defensa técnica de AdoptAPP.

## 29. Commits Que Respaldan La Participacion

El trabajo realizado durante el desarrollo de AdoptAPP puede respaldarse a través del historial de commits registrados en el repositorio del proyecto. Estos cambios reflejan mi participación en distintas etapas del desarrollo, incluyendo la configuración de la arquitectura, mejoras en la lógica de negocio, documentación, pruebas unitarias, configuración de entornos y despliegue de la aplicación.

Entre los commits más relevantes se encuentran aquellos relacionados con la integración de la base de datos en Neon PostgreSQL, la incorporación de Docker y Docker Compose, la configuración del API Gateway y Eureka Server, la implementación de pruebas unitarias, la documentación mediante Swagger/OpenAPI y HATEOAS, además de distintas correcciones derivadas de revisiones técnicas realizadas durante el proyecto.

Cada uno de estos cambios representa una parte del trabajo desarrollado y permite evidenciar la evolución del sistema, mostrando cómo fueron incorporándose nuevas funcionalidades y corrigiéndose distintos aspectos de la aplicación hasta alcanzar una versión más estable y consistente.

Commits relevantes:

```text
878b917 fix: permitir configuracion de puerto en Render
695ccd4 fix: compilar microservicios dentro de Docker
049d7a1 config: migrar base de datos a Neon
dfa13de test: corregir PetResponse en FollowUpServiceTest
aecd0af feat: documentar APIs, agregar HATEOAS y pruebas unitarias
d35f53b feat: documentar APIs, agregar HATEOAS y pruebas unitarias
03477cd feat: integrar Supabase, Docker Compose, Eureka Server y API Gateway
2fd7f1f feat: integrar Supabase, Docker Compose, Eureka Server y API Gateway
284f914 feat: technical audit fixes - resilience, security, logical and architecture improvements
3772f84 feat: technical audit fixes - resilience, security, logical and architecture improvements
37f77fd test: configure all services to use H2 in-memory DB for tests
```

## 30. Evidencia De Archivos Trabajados

La participación realizada también puede comprobarse mediante los distintos archivos y módulos modificados durante el desarrollo del proyecto. Gran parte del trabajo se concentró en los microservicios asignados, además de componentes compartidos que impactan directamente el funcionamiento general de la aplicación.

Entre los archivos principales se encuentran el README, el archivo pom.xml del proyecto, el módulo shared-kernel y los microservicios user-service, adoption-service, notification-service, shelter-service y staff-service.

De forma complementaria, también se realizaron modificaciones en archivos de configuración correspondientes a los distintos perfiles de ejecución, migraciones de Flyway, pruebas unitarias, Dockerfiles, configuración de Docker Compose y documentación de las APIs mediante Swagger/OpenAPI.

Toda esta evidencia puede ser revisada directamente en el repositorio, permitiendo identificar los cambios realizados y los componentes involucrados durante mi participación en el proyecto.
Evidencia principal:

```text
README.md
pom.xml
shared-kernel/
user-service/
adoption-service/
notification-service/
shelter-service/
staff-service/
```

Evidencia complementaria:

```text
*/src/main/resources/application-*.yml
*/src/main/resources/db/migration/
*/src/test/java/
*/Dockerfile
compose.yml
postman-collection.json
```

## 31. Pruebas Unitarias Y REST Que Respaldan El Trabajo

Las funcionalidades desarrolladas fueron respaldadas mediante distintas estrategias de validación, utilizando tanto pruebas unitarias como pruebas sobre los servicios REST expuestos por cada microservicio.

Las pruebas unitarias permitieron verificar el correcto funcionamiento de las principales reglas de negocio de forma aislada, mientras que la documentación generada con Swagger/OpenAPI facilitó la validación manual de los distintos endpoints durante el desarrollo.

Adicionalmente, el proyecto cuenta con una colección de Postman utilizada para comprobar el funcionamiento de las APIs, junto con la integración del API Gateway y Eureka Server para validar la comunicación entre los distintos microservicios.

Esta combinación de pruebas permitió comprobar tanto la lógica interna de cada servicio como el comportamiento general del sistema cuando los distintos componentes interactúan entre sí.

Comandos utiles:

```powershell
.\mvnw.cmd test
.\mvnw.cmd -pl user-service test
.\mvnw.cmd -pl adoption-service test
.\mvnw.cmd clean test jacoco:report
```

## 32. Evidencia Del Repositorio

Toda la información presentada en esta defensa técnica puede verificarse directamente en el repositorio del proyecto mediante el historial de cambios y la estructura de archivos existente.

A través de herramientas de control de versiones como Git es posible revisar los commits realizados, comparar versiones del código, identificar las modificaciones efectuadas y analizar la evolución de los distintos microservicios durante el desarrollo de AdoptAPP.

Asimismo, el repositorio incluye la documentación técnica, las migraciones de base de datos, las pruebas unitarias, los perfiles de configuración y el resto de los archivos necesarios para comprender el funcionamiento del sistema y reproducir el entorno de desarrollo.

Esta evidencia permite respaldar objetivamente la participación realizada y demuestra el trabajo efectuado sobre los distintos componentes del proyecto.

## 33. Conclusion

Mi participación en el desarrollo de AdoptAPP estuvo enfocada principalmente en la implementación y mejora de distintos microservicios, así como en la configuración técnica necesaria para mantener una arquitectura consistente y preparada para su crecimiento.

Además del desarrollo de funcionalidades, trabajé en la definición de reglas de negocio, la implementación de validaciones, la configuración de perfiles de ejecución, las migraciones con Flyway, la documentación de las APIs mediante Swagger/OpenAPI, la incorporación de pruebas unitarias y la actualización de la documentación técnica del proyecto.

Todas estas mejoras permitieron fortalecer la calidad del sistema, aumentando su mantenibilidad, seguridad y estabilidad. Asimismo, contribuyeron a que el proyecto pudiera ejecutarse correctamente en distintos entornos, facilitaron las pruebas de las funcionalidades implementadas y mejoraron la organización general de la arquitectura basada en microservicios.

En conjunto, los aportes realizados permitieron entregar un proyecto más robusto, mejor documentado y preparado tanto para su evaluación académica como para futuras etapas de desarrollo.