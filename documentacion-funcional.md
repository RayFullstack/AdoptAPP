# Documentacion Funcional - AdoptAPP

## 1. Proposito Del Documento

Este documento describe el funcionamiento del sistema AdoptAPP desde una perspectiva funcional. Su objetivo es explicar que problema resuelve, quienes participan, que reglas de negocio aplica y como se desarrollan los principales flujos del sistema.

Este documento no corresponde a documentacion tecnica. Por lo tanto, no explica codigo, configuraciones, despliegue, base de datos, Docker ni detalles internos de implementacion.

## 2. Problema Que Resuelve

Los refugios de mascotas necesitan gestionar varios procesos al mismo tiempo:

- Registrar mascotas disponibles para adopcion.
- Mantener informacion de salud de cada mascota.
- Recibir solicitudes de adopcion.
- Aprobar o rechazar adopciones.
- Realizar seguimiento posterior a la adopcion.
- Administrar refugios, personal, insumos y donaciones.
- Notificar eventos importantes a usuarios o refugios.

Sin un sistema centralizado, estos procesos pueden quedar desordenados, duplicados o sin trazabilidad. AdoptAPP busca organizar el ciclo completo de adopcion, desde el registro de una mascota hasta el seguimiento posterior a su adopcion.

## 3. Objetivo Del Sistema

AdoptAPP tiene como objetivo facilitar la gestion de adopciones de mascotas en refugios, permitiendo que distintos usuarios participen segun su rol.

El sistema permite:

- Gestionar usuarios y sus permisos.
- Registrar refugios.
- Administrar personal de refugios.
- Registrar mascotas.
- Administrar fichas de salud.
- Crear solicitudes de adopcion.
- Aprobar, rechazar o cancelar adopciones.
- Registrar seguimientos post-adopcion.
- Gestionar donaciones.
- Gestionar insumos.
- Enviar y consultar notificaciones.

## 4. Actores Del Sistema

### 4.1 Administrador

El administrador tiene acceso general al sistema. Puede consultar y gestionar la informacion principal de los microservicios.

Responsabilidades:

- Supervisar usuarios.
- Consultar informacion de refugios.
- Revisar adopciones.
- Administrar donaciones, insumos y notificaciones.
- Ver informacion historica.
- Acceder a registros eliminados o archivados cuando corresponda.

### 4.2 Administrador De Refugio

El administrador de refugio gestiona los recursos asociados a su refugio.

Responsabilidades:

- Administrar informacion del refugio.
- Gestionar mascotas del refugio.
- Revisar solicitudes de adopcion.
- Aprobar o rechazar adopciones.
- Gestionar personal asociado al refugio.
- Revisar insumos y notificaciones del refugio.

### 4.3 Adoptante

El adoptante es el usuario que busca adoptar una mascota.

Responsabilidades:

- Consultar mascotas disponibles.
- Crear solicitudes de adopcion.
- Revisar sus propias adopciones.
- Recibir notificaciones relacionadas con su proceso.

### 4.4 Veterinario

El veterinario se relaciona principalmente con la informacion de salud de las mascotas.

Responsabilidades:

- Crear fichas clinicas.
- Actualizar informacion de salud.
- Registrar vacunacion, esterilizacion y enfermedades.
- Consultar historial clinico.

### 4.5 Voluntario

El voluntario apoya tareas operativas del refugio.

Responsabilidades:

- Colaborar con la gestion de mascotas.
- Consultar informacion permitida.
- Apoyar tareas del refugio segun permisos asignados.

## 5. Conceptos Principales

### 5.1 Mascota

Representa a un animal registrado en el sistema. Una mascota puede estar disponible, no disponible o eliminada logicamente.

Estados principales:

- `AVAILABLE`: disponible para adopcion.
- `NOT_AVAILABLE`: no disponible para adopcion.
- `DELETED`: eliminada logicamente.

### 5.2 Adopcion

Representa una solicitud realizada por un usuario para adoptar una mascota.

Estados principales:

- `PENDING`: solicitud pendiente de revision.
- `APPROVED`: solicitud aprobada.
- `REJECTED`: solicitud rechazada.
- `CANCELLED`: solicitud cancelada.

### 5.3 Ficha De Salud

Contiene informacion clinica de una mascota.

Puede incluir:

- Estado de vacunacion.
- Estado de esterilizacion.
- Enfermedades.
- Historial de cambios.

### 5.4 Refugio

Representa una organizacion que registra y administra mascotas.

Un refugio puede tener:

- Mascotas.
- Personal.
- Insumos.
- Donaciones asociadas.

### 5.5 Notificacion

Representa un aviso generado por el sistema ante eventos importantes.

Ejemplos:

- Usuario creado.
- Mascota registrada.
- Adopcion creada.
- Adopcion aprobada.
- Donacion registrada.
- Insumo actualizado.

## 6. Reglas De Negocio

### 6.1 Reglas De Usuarios

- Un usuario debe tener un rol definido.
- No deben existir usuarios con el mismo email.
- No deben existir usuarios con el mismo username.
- Un usuario inactivo no debe operar normalmente en el sistema.
- No se debe permitir escalamiento indebido de roles.

Ejemplo:

Si un usuario adoptante intenta obtener permisos de administrador sin autorizacion, el sistema debe bloquear la operacion.

### 6.2 Reglas De Refugios

- Un refugio puede registrar mascotas, staff e insumos.
- Un refugio no debe eliminarse si tiene recursos activos asociados.
- La eliminacion de un refugio debe ser logica, no fisica.

Ejemplo:

Si el refugio tiene mascotas activas, no puede marcarse como eliminado hasta resolver esas dependencias.

### 6.3 Reglas De Mascotas

- Una mascota nueva debe quedar disponible para adopcion, salvo que se indique lo contrario por una regla del negocio.
- Una mascota eliminada no debe aparecer en listados normales.
- Una mascota adoptada no debe aparecer como disponible.
- La informacion clinica no pertenece directamente a la mascota, sino a su ficha de salud.

Ejemplo:

Si una mascota tiene una adopcion aprobada, su estado debe cambiar a `NOT_AVAILABLE`.

### 6.4 Reglas De Salud

- Cada ficha de salud pertenece a una mascota.
- No se debe cambiar indebidamente la mascota asociada a una ficha de salud.
- Una ficha eliminada no debe actualizarse.
- La informacion clinica debe mantenerse separada de la informacion general de la mascota.

Ejemplo:

Si una ficha clinica fue marcada como `DELETED`, no debe permitirse actualizar sus enfermedades o estado de vacunacion.

### 6.5 Reglas De Adopcion

- Toda solicitud de adopcion debe nacer en estado `PENDING`.
- Un adoptante no debe aprobar su propia solicitud.
- La aprobacion o rechazo debe realizarla un administrador o administrador de refugio.
- Una mascota no debe tener mas de una adopcion activa.
- Una adopcion aprobada debe cambiar el estado de la mascota.
- Una adopcion cancelada debe conservar historial.

Ejemplo:

Si la mascota ya tiene una adopcion `PENDING` o `APPROVED`, el sistema no debe permitir crear otra solicitud activa para esa misma mascota.

### 6.6 Reglas De Seguimiento

- El seguimiento post-adopcion se asocia a una adopcion.
- El seguimiento permite registrar visitas, comentarios y estado.
- Un seguimiento cancelado no debe tratarse como activo.

Ejemplo:

Despues de aprobar una adopcion, el refugio puede registrar una visita de seguimiento para verificar el bienestar de la mascota.

### 6.7 Reglas De Donaciones

- Una donacion debe estar asociada a un usuario y a un refugio.
- Una donacion cancelada no debe actualizarse como si estuviera activa.
- La donacion debe conservar trazabilidad.

Ejemplo:

Si una donacion fue cancelada, el sistema no debe permitir modificar su monto como una donacion vigente.

### 6.8 Reglas De Insumos

- Un insumo pertenece a un refugio.
- Un insumo inactivo no debe mostrarse como disponible.
- Los cambios de insumos pueden generar notificaciones.

Ejemplo:

Si un refugio registra alimento o medicamentos, estos quedan asociados a ese refugio y pueden actualizarse segun disponibilidad.

### 6.9 Reglas De Staff

- Un miembro del staff debe estar asociado a un usuario y a un refugio.
- Un staff inactivo no debe operar como activo.
- El staff permite validar pertenencia a un refugio.

Ejemplo:

Un usuario asociado como staff de un refugio no deberia gestionar recursos de otro refugio sin permisos.

### 6.10 Reglas De Notificaciones

- Las notificaciones deben tener destinatario.
- Una notificacion archivada no debe mostrarse como notificacion activa.
- El administrador puede consultar informacion mas amplia que un usuario normal.

Ejemplo:

Cuando se crea una adopcion, el sistema puede registrar una notificacion para informar el evento.

## 7. Flujos Funcionales

### 7.1 Flujo De Registro De Usuario

1. El usuario entrega sus datos.
2. El sistema valida que el email no este duplicado.
3. El sistema valida que el username no este duplicado.
4. El sistema asigna el rol correspondiente.
5. El usuario queda registrado.
6. Se puede generar una notificacion de creacion.

Resultado esperado:

El usuario queda disponible para operar segun su rol.

### 7.2 Flujo De Registro De Refugio

1. Un usuario autorizado solicita crear un refugio.
2. El sistema valida los datos.
3. El refugio queda registrado.
4. El refugio puede comenzar a asociar mascotas, staff e insumos.

Resultado esperado:

El refugio queda activo y disponible para gestionar recursos.

### 7.3 Flujo De Registro De Mascota

1. Un usuario autorizado registra una mascota.
2. El sistema valida el refugio asociado.
3. La mascota queda registrada.
4. La mascota queda disponible para adopcion si corresponde.
5. Se puede registrar posteriormente su ficha de salud.

Resultado esperado:

La mascota aparece en el catalogo de mascotas disponibles.

### 7.4 Flujo De Ficha De Salud

1. Un usuario autorizado registra la ficha clinica de una mascota.
2. El sistema valida que la mascota exista.
3. Se registra vacunacion, esterilizacion y enfermedades si corresponde.
4. La ficha queda asociada a la mascota.

Resultado esperado:

La mascota mantiene informacion clinica separada de sus datos generales.

### 7.5 Flujo De Solicitud De Adopcion

1. El adoptante selecciona una mascota disponible.
2. El adoptante crea una solicitud de adopcion.
3. El sistema valida que la mascota exista.
4. El sistema valida que la mascota este disponible.
5. El sistema valida que no exista otra adopcion activa para esa mascota.
6. La solicitud se crea en estado `PENDING`.

Resultado esperado:

La solicitud queda pendiente de revision por el refugio o administrador.

### 7.6 Flujo De Aprobacion De Adopcion

1. Un administrador o administrador de refugio revisa una solicitud pendiente.
2. El sistema valida que la solicitud exista.
3. El sistema valida que la solicitud no este cancelada.
4. El estado cambia a `APPROVED`.
5. La mascota cambia a `NOT_AVAILABLE`.
6. Se puede generar una notificacion.
7. Se puede crear seguimiento post-adopcion.

Resultado esperado:

La adopcion queda aprobada y la mascota deja de estar disponible.

### 7.7 Flujo De Rechazo De Adopcion

1. Un administrador o administrador de refugio revisa una solicitud pendiente.
2. El sistema cambia el estado a `REJECTED`.
3. La mascota puede seguir disponible si no existe otra regla que indique lo contrario.
4. Se conserva historial de la decision.

Resultado esperado:

La solicitud queda rechazada y el proceso termina sin adoptar la mascota.

### 7.8 Flujo De Cancelacion De Adopcion

1. Un usuario autorizado solicita cancelar una adopcion.
2. El sistema valida el estado actual.
3. La adopcion cambia a `CANCELLED`.
4. Si estaba aprobada, la mascota puede volver a `AVAILABLE`.
5. Se conserva el historial.

Resultado esperado:

La adopcion deja de estar activa sin perder trazabilidad.

### 7.9 Flujo De Seguimiento Post-Adopcion

1. Se aprueba una adopcion.
2. Se registra un seguimiento asociado.
3. Se agenda o registra una visita.
4. Se agregan comentarios.
5. El seguimiento puede completarse o cancelarse.

Resultado esperado:

El refugio mantiene control posterior a la adopcion.

### 7.10 Flujo De Donacion

1. Se registra una donacion.
2. El sistema valida usuario y refugio.
3. La donacion queda asociada al refugio.
4. Se puede generar una notificacion.
5. Si se cancela, se conserva como `CANCELLED`.

Resultado esperado:

El refugio mantiene registro de donaciones recibidas.

### 7.11 Flujo De Insumos

1. Se registra un insumo para un refugio.
2. El sistema valida el refugio.
3. El insumo queda asociado.
4. Se puede actualizar cantidad o estado.
5. Si se elimina, queda como `INACTIVE`.

Resultado esperado:

El refugio puede controlar sus recursos disponibles.

### 7.12 Flujo De Notificaciones

1. Ocurre un evento relevante.
2. El sistema crea una notificacion.
3. La notificacion queda asociada a un usuario o refugio.
4. El destinatario puede consultarla segun permisos.
5. Si se elimina, queda como `ARCHIVED`.

Resultado esperado:

El sistema conserva avisos importantes para usuarios y administradores.

## 8. Ejemplos Funcionales

### 8.1 Ejemplo De Adopcion Exitosa

Una adoptante consulta mascotas disponibles y selecciona una mascota llamada Luna.

1. Luna esta en estado `AVAILABLE`.
2. La adoptante crea una solicitud.
3. La solicitud queda `PENDING`.
4. El administrador del refugio revisa la solicitud.
5. La solicitud se aprueba.
6. La adopcion queda `APPROVED`.
7. Luna cambia a `NOT_AVAILABLE`.
8. Se registra una notificacion.
9. Se puede crear seguimiento post-adopcion.

Resultado:

Luna ya no aparece como disponible para nuevas solicitudes activas.

### 8.2 Ejemplo De Adopcion Duplicada

Una mascota ya tiene una solicitud de adopcion `PENDING`.

Si otro usuario intenta crear una nueva solicitud para la misma mascota, el sistema debe bloquear la operacion.

Resultado:

No se permite duplicar procesos activos sobre una mascota.

### 8.3 Ejemplo De Refugio Con Dependencias

Un refugio tiene mascotas activas registradas.

Si se intenta eliminar el refugio, el sistema debe rechazar la operacion hasta que se resuelvan las dependencias.

Resultado:

El refugio no se elimina logicamente mientras tenga recursos activos.

### 8.4 Ejemplo De Ficha De Salud Eliminada

Una ficha clinica fue marcada como `DELETED`.

Si un usuario intenta actualizar las enfermedades o estado de vacunacion de esa ficha, el sistema debe bloquear el cambio.

Resultado:

La ficha eliminada conserva trazabilidad, pero no puede modificarse como activa.

### 8.5 Ejemplo De Notificacion Archivada

Una notificacion fue archivada.

El usuario normal no deberia verla como activa. El administrador puede tener mayor visibilidad segun sus permisos.

Resultado:

La notificacion no se borra fisicamente, pero queda fuera del flujo normal.

## 9. Alcance Funcional

El sistema cubre:

- Gestion de usuarios.
- Gestion de refugios.
- Gestion de staff.
- Gestion de mascotas.
- Gestion de salud.
- Gestion de adopciones.
- Seguimiento post-adopcion.
- Gestion de donaciones.
- Gestion de insumos.
- Gestion de notificaciones.
- Control de roles.
- Soft delete.
- Historial y trazabilidad.

## 10. Fuera Del Alcance Funcional Actual

Actualmente no se considera como parte principal del alcance funcional:

- Frontend web completo.
- Pasarela real de pagos.
- Envio real de correos externos.
- Sistema avanzado de agenda.
- Chat entre usuarios y refugios.
- Geolocalizacion avanzada.
- Autenticacion con redes sociales.

Estas funcionalidades podrian considerarse como mejoras futuras.

## 11. Resumen Final

AdoptAPP organiza el proceso de adopcion de mascotas desde una perspectiva funcional completa. El sistema permite registrar refugios y mascotas, administrar fichas de salud, recibir solicitudes de adopcion, aprobar o rechazar procesos, hacer seguimiento post-adopcion y mantener trazabilidad mediante estados logicos.

La separacion por roles permite que cada actor interactue con el sistema segun sus responsabilidades. Las reglas de negocio buscan evitar inconsistencias, como adopciones duplicadas, mascotas adoptadas que sigan disponibles o registros eliminados que continuen siendo modificados.

Este documento describe el comportamiento esperado del sistema desde el punto de vista del usuario y del negocio, sin entrar en detalles tecnicos de implementacion.
