# Plan De Cierre De Feedback - AdoptAPP

## 1. Proposito Del Documento

Este documento registra las observaciones recibidas en la evaluacion anterior del proyecto AdoptAPP y explica que acciones se tomaron para cerrar esos puntos.

El objetivo es dejar evidencia clara de:

- Observaciones recibidas.
- Correcciones aplicadas.
- Decisiones tomadas cuando no correspondia modificar codigo.
- Evidencia concreta dentro del repositorio.
- Estado final de cada punto al cierre del semestre.

## 2. Resumen Del Feedback Recibido

El feedback recibido destaco que AdoptAPP fue uno de los proyectos mas solidos de la evaluacion.

### Fortalezas observadas

- Reglas de negocio ricas y bien pensadas.
- Soft delete consistente en varias entidades.
- Validaciones cruzadas entre microservicios.
- Uso de Feign Client con fallback.
- Configuracion de circuit breaker con Resilience4j.
- Documentacion Swagger completa.
- Manejo prolijo de perfiles YAML.
- Integracion de JaCoCo.
- Defensa tecnica precisa y alineada con el codigo real.
- Buena correspondencia entre reglas descritas y reglas codificadas.

### Aspectos a mejorar observados

- El despliegue en Render se mencionaba como operativo, pero faltaba dejar evidencia verificable en el repositorio.
- El API Gateway enruta correctamente, pero no incorpora filtros adicionales mas alla de resolver rutas.
- Era recomendable agregar una seccion de responsabilidades individuales, ya que el historial de commits estaba concentrado en una integrante.

## 3. Observacion 1: Evidencia Del Despliegue En Render

### Observacion recibida

El proyecto menciona despliegue en Render como si estuviera operativo, pero no existia suficiente evidencia verificable dentro del repositorio.

### Correccion aplicada

Se actualizo la documentacion del proyecto para dejar constancia explicita de las rutas publicas utilizadas en Render.

Se agregaron secciones con:

- URLs publicas de cada microservicio.
- URL publica del API Gateway.
- URL publica de Eureka Server.
- Rutas recomendadas para consumir servicios mediante API Gateway.
- Variables de entorno necesarias para Render.
- Explicacion de la variable `PORT`.
- Consideracion sobre arranque en frio de Render.
- Posible respuesta `503` cuando los servicios estan despertando o aun no aparecen en Eureka.

### Decision tomada

No se agrego `render.yaml`, porque el despliegue fue configurado manualmente desde el panel de Render.

La decision fue documentar las URLs y variables necesarias en el README, en lugar de declarar infraestructura como codigo con `render.yaml`.

### Evidencia concreta

- `README.md`
  - Seccion `Variables Para Render`.
  - Seccion `URLs De Render`.
  - Seccion `Rutas Por API Gateway En Render`.
  - Seccion de consideraciones sobre Render y arranque en frio.

- `documentacion-tecnica.md`
  - Seccion `Render`.
  - Variables necesarias por servicio.
  - Orden recomendado de despliegue.

### Estado final

Corregido mediante documentacion.

El repositorio ahora deja evidencia de las URLs publicas y de como se configura Render, aunque no incluye `render.yaml`.

## 4. Observacion 2: API Gateway Sin Filtros Adicionales

### Observacion recibida

El API Gateway enruta correctamente los 10 servicios, pero no incorpora filtros adicionales mas alla de la resolucion de rutas.

### Correccion o decision aplicada

Se decidio mantener el API Gateway como componente de enrutamiento simple.

La autenticacion y autorizacion siguen siendo responsabilidad de cada microservicio. El Gateway reenvia el header `Authorization`, y cada servicio aplica sus propias reglas de seguridad con Basic Auth y roles.

### Justificacion

Para el alcance del proyecto, el Gateway cumple su funcion principal:

- Centralizar el punto de entrada.
- Resolver rutas por nombre de servicio.
- Integrarse con Eureka.
- Reenviar solicitudes a los microservicios.

Agregar filtros globales de autenticacion, logging avanzado o rate limiting habria aumentado la complejidad y duplicado responsabilidades que ya estan cubiertas en cada microservicio.

### Evidencia concreta

- `api-gateway/`
  - Modulo dedicado al Gateway.
  - Configuracion de rutas hacia los microservicios.

- `README.md`
  - Seccion `API Gateway`.
  - Se explica que el Gateway reenvia el header `Authorization`.
  - Se indica que cada microservicio conserva sus propias reglas de seguridad.

- `documentacion-tecnica.md`
  - Seccion `api-gateway`.
  - Explica que su responsabilidad principal es enrutar solicitudes.

### Estado final

Decision documentada.

No se modifica el Gateway porque, para el alcance del semestre, el enrutamiento simple es suficiente y consistente con la arquitectura implementada.

## 5. Observacion 3: Responsabilidades Individuales

### Observacion recibida

Se recomendo incluir una seccion de responsabilidades individuales, ya que el historial de commits estaba concentrado en una integrante.

### Correccion aplicada

Se agrego documentacion de defensa individual para evidenciar participacion, responsabilidades y archivos trabajados.

El documento individual explica:

- Aportes realizados.
- Microservicios trabajados.
- Archivos y modulos intervenidos.
- Requerimientos corregidos.
- Commits relevantes.
- Pruebas unitarias o REST asociadas.
- Evidencia del repositorio que respalda la participacion.

### Evidencia concreta

- `docs/defensa-individual/CAMILA_OSORIO.md`
  - Describe aportes individuales.
  - Lista responsabilidades trabajadas.
  - Menciona commits relevantes.
  - Relaciona trabajo con reglas de negocio, testing, documentacion y configuracion.

### Estado final

Corregido.

Existe evidencia documental de responsabilidades individuales dentro del repositorio.

## 6. Observacion 4: Mantener Precision Entre Documentacion Y Codigo

### Observacion recibida

El feedback destaco que una de las fortalezas del proyecto fue que la documentacion coincidia con el codigo real.

### Accion aplicada

Se mantuvo esa linea creando documentos separados segun su proposito:

- Documentacion funcional.
- Documentacion tecnica.
- Levantamiento actualizado de requerimientos.
- Matriz de requerimientos.
- Defensa individual.
- README actualizado.

### Evidencia concreta

- `documentacion-funcional.md`
  - Explica problema, actores, reglas, flujos y ejemplos sin mezclar detalles tecnicos.

- `documentacion-tecnica.md`
  - Explica arquitectura, modulos, perfiles, variables, ejecucion desde cero y pruebas.

- `levantamiento-requerimientos-actualizado.md`
  - Contrasta requerimientos originales con cambios aplicados.

- `matriz-requerimientos.md`
  - Muestra requerimientos originales, estado final y observaciones.

- `README.md`
  - Resume instalacion, arquitectura, Render, Docker, Swagger, testing y reglas principales.

### Estado final

Corregido y reforzado.

La documentacion queda organizada por tipo, evitando mezclar defensa, tecnica, funcionalidad y requerimientos.

## 7. Observacion 5: Evidencia De Circuit Breaker Y Fallback

### Observacion recibida

El feedback menciono como fortaleza el uso de Feign con fallback y circuit breaker real con Resilience4j.

### Accion aplicada

Se mantuvo la configuracion y se dejo evidencia en los archivos YAML de los microservicios.

### Evidencia concreta

Configuracion Resilience4j y circuit breaker en archivos `application.yml`, por ejemplo:

- `adoption-service/src/main/resources/application.yml`
- `pet-service/src/main/resources/application.yml`
- `health-service/src/main/resources/application.yml`
- `followup-service/src/main/resources/application.yml`
- `donation-service/src/main/resources/application.yml`
- `notification-service/src/main/resources/application.yml`
- `shelter-service/src/main/resources/application.yml`
- `staff-service/src/main/resources/application.yml`
- `supply-service/src/main/resources/application.yml`

Tambien existen clases de clients y fallbacks en los microservicios que se comunican entre si.

### Estado final

Se mantiene como fortaleza del proyecto.

No requiere correccion adicional.

## 8. Observacion 6: JaCoCo Y Cobertura

### Observacion recibida

El feedback destaco la integracion de JaCoCo con umbral real de cobertura.

### Accion aplicada

Se mantuvo JaCoCo como herramienta de medicion de cobertura y se documento su uso.

### Evidencia concreta

- `README.md`
  - Seccion de pruebas.
  - Comando para generar reporte JaCoCo.
  - Ruta esperada del reporte.

- `documentacion-tecnica.md`
  - Seccion `Pruebas`.
  - Seccion `JaCoCo`.

- Directorios `src/test/java` en los microservicios.
  - Pruebas organizadas por service, controller y client.

### Estado final

Se mantiene como fortaleza del proyecto.

## 9. Resumen De Cierre

| Observacion | Accion tomada | Estado |
| --- | --- | --- |
| Falta evidencia verificable de Render | Se documentaron URLs, variables y rutas por Gateway | Corregido |
| No existe `render.yaml` | Se decide no agregarlo porque Render fue configurado manualmente | Decision documentada |
| Gateway sin filtros adicionales | Se mantiene Gateway simple y seguridad por microservicio | Decision documentada |
| Falta trazabilidad individual | Se agrego defensa individual en `docs/defensa-individual` | Corregido |
| Mantener precision documentacion-codigo | Se separaron documentos funcionales, tecnicos y de requerimientos | Corregido |
| Fallback y circuit breaker | Se mantiene configuracion existente | Sin cambio requerido |
| JaCoCo | Se mantiene configuracion y documentacion | Sin cambio requerido |

## 10. Conclusion

Las observaciones de mejora fueron abordadas principalmente mediante documentacion y decisiones explicitas de alcance.

El punto de Render fue reforzado con URLs y variables concretas. El punto del Gateway fue documentado como una decision de arquitectura: mantener el enrutamiento simple y dejar seguridad en cada microservicio. La trazabilidad individual fue cubierta mediante el documento de defensa individual.

Con esto, el cierre del proyecto queda mejor respaldado, porque el repositorio no solo contiene el codigo, sino tambien evidencia clara de decisiones, correcciones y alcance final.
