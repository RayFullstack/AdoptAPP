#!/bin/bash

# ==============================================================================
# ADOPTAPP - DIAGNOSTICO AUTOMATICO DE MICROSERVICIOS
# ==============================================================================
# Objetivo:
# - NO modifica nada
# - SOLO prueba endpoints
# - Genera informe detallado
# - Detecta errores comunes
# - Sugiere posibles soluciones
#
# Compatible con:
# - Git Bash
# - WSL
# - Linux
# - Terminal integrada IntelliJ
#
# Uso:
# chmod +x diagnose.sh
# ./diagnose.sh
# ==============================================================================

REPORT_FILE="adoptapp_diagnostic_report.txt"
DATE=$(date)

echo "=============================================" > $REPORT_FILE
echo " ADOPTAPP - INFORME DE DIAGNOSTICO" >> $REPORT_FILE
echo "=============================================" >> $REPORT_FILE
echo "Fecha: $DATE" >> $REPORT_FILE
echo "" >> $REPORT_FILE

# ==============================================================================
# CONFIG
# ==============================================================================

AUTH="admin@adoptapp.com:admin123"

ENDPOINTS=(
"http://localhost:8081/user-app/users"
"http://localhost:8082/pet-app/pets"
"http://localhost:8083/adoption-app/adoptions"
"http://localhost:8084/notification-app/notifications"
"http://localhost:8085/health-app/health"
"http://localhost:8086/followup-app/followups"
"http://localhost:8090/donation-app/donations"
"http://localhost:8091/staff-app/staff"
"http://localhost:8092/supply-app/supplies"
"http://localhost:8095/shelter-app/shelters"
)

# ==============================================================================
# FUNCIONES
# ==============================================================================

check_port() {
  PORT=$1

  if command -v nc >/dev/null 2>&1; then
    nc -z localhost $PORT >/dev/null 2>&1
    return $?
  else
    timeout 1 bash -c "</dev/tcp/localhost/$PORT" >/dev/null 2>&1
    return $?
  fi
}

diagnose_http_code() {
  CODE=$1

  case $CODE in
    200)
      echo "OK - Endpoint funcionando"
      ;;
    201)
      echo "OK - Recurso creado correctamente"
      ;;
    400)
      echo "ERROR 400 - Bad Request"
      echo "Posibles causas:"
      echo "- JSON invalido"
      echo "- Campos faltantes"
      echo "- Enum incorrecto"
      echo "- DTO mal mapeado"
      ;;
    401)
      echo "ERROR 401 - Unauthorized"
      echo "Posibles causas:"
      echo "- Credenciales incorrectas"
      echo "- Spring Security mal configurado"
      echo "- Basic Auth no propagado"
      ;;
    403)
      echo "ERROR 403 - Forbidden"
      echo "Posibles causas:"
      echo "- Rol insuficiente"
      echo "- @PreAuthorize incorrecto"
      echo "- SecurityConfig bloqueando endpoint"
      ;;
    404)
      echo "ERROR 404 - Endpoint no encontrado"
      echo "Posibles causas:"
      echo "- Context path incorrecto"
      echo "- Puerto incorrecto"
      echo "- Controller no registrado"
      echo "- Mapping incorrecto"
      ;;
    405)
      echo "ERROR 405 - Method Not Allowed"
      echo "Posibles causas:"
      echo "- Metodo HTTP incorrecto"
      echo "- Falta @PostMapping/@GetMapping"
      ;;
    500)
      echo "ERROR 500 - Internal Server Error"
      echo "Posibles causas:"
      echo "- NullPointerException"
      echo "- Error JPA/Hibernate"
      echo "- Error Feign Client"
      echo "- Error DTO/entity"
      echo "- Base de datos desconectada"
      ;;
    503)
      echo "ERROR 503 - Service Unavailable"
      echo "Posibles causas:"
      echo "- Microservicio caido"
      echo "- Circuit Breaker abierto"
      echo "- Feign Client fallo"
      echo "- Dependencia no iniciada"
      ;;
    *)
      echo "Codigo HTTP recibido: $CODE"
      ;;
  esac
}

# ==============================================================================
# VALIDACION CURL
# ==============================================================================

echo "Validando curl..." | tee -a $REPORT_FILE

if ! command -v curl >/dev/null 2>&1; then
  echo "ERROR: curl no instalado" | tee -a $REPORT_FILE
  exit 1
fi

echo "curl OK" | tee -a $REPORT_FILE
echo "" >> $REPORT_FILE

# ==============================================================================
# VALIDACION JAVA
# ==============================================================================

echo "Validando Java..." | tee -a $REPORT_FILE

if command -v java >/dev/null 2>&1; then
  java -version >> $REPORT_FILE 2>&1
else
  echo "ERROR: Java no encontrado" | tee -a $REPORT_FILE
fi

echo "" >> $REPORT_FILE

# ==============================================================================
# VALIDACION POSTGRES
# ==============================================================================

echo "Validando PostgreSQL..." | tee -a $REPORT_FILE

if check_port 5432; then
  echo "PostgreSQL puerto 5432 OK" | tee -a $REPORT_FILE
else
  echo "ERROR: PostgreSQL no disponible en puerto 5432" | tee -a $REPORT_FILE
  echo "Posibles soluciones:" | tee -a $REPORT_FILE
  echo "- Iniciar PostgreSQL" | tee -a $REPORT_FILE
  echo "- Verificar application.yml" | tee -a $REPORT_FILE
  echo "- Verificar docker compose" | tee -a $REPORT_FILE
fi

echo "" >> $REPORT_FILE

# ==============================================================================
# VALIDACION VARIABLES ENTORNO
# ==============================================================================

echo "Validando variables de entorno..." | tee -a $REPORT_FILE

if [ -z "$DB_USER" ]; then
  echo "ERROR: DB_USER no definida" | tee -a $REPORT_FILE
fi

if [ -z "$DB_PASSWORD" ]; then
  echo "ERROR: DB_PASSWORD no definida" | tee -a $REPORT_FILE
fi

if [ ! -z "$DB_USER" ] && [ ! -z "$DB_PASSWORD" ]; then
  echo "Variables entorno OK" | tee -a $REPORT_FILE
fi

echo "" >> $REPORT_FILE

# ==============================================================================
# TEST PUERTOS
# ==============================================================================

echo "=============================================" | tee -a $REPORT_FILE
echo " VALIDACION DE PUERTOS" | tee -a $REPORT_FILE
echo "=============================================" | tee -a $REPORT_FILE

PORTS=(8081 8082 8083 8084 8085 8086 8090 8091 8092 8095)

for PORT in "${PORTS[@]}"
do
  echo "" | tee -a $REPORT_FILE
  echo "Puerto $PORT" | tee -a $REPORT_FILE

  if check_port $PORT; then
    echo "STATUS: ABIERTO" | tee -a $REPORT_FILE
  else
    echo "STATUS: CERRADO" | tee -a $REPORT_FILE
    echo "Posibles soluciones:" | tee -a $REPORT_FILE
    echo "- Servicio no iniciado" | tee -a $REPORT_FILE
    echo "- Error Spring Boot" | tee -a $REPORT_FILE
    echo "- Puerto ocupado" | tee -a $REPORT_FILE
    echo "- Crash al iniciar" | tee -a $REPORT_FILE
  fi
done

echo "" >> $REPORT_FILE

# ==============================================================================
# TEST ENDPOINTS
# ==============================================================================

echo "=============================================" | tee -a $REPORT_FILE
echo " TEST ENDPOINTS" | tee -a $REPORT_FILE
echo "=============================================" | tee -a $REPORT_FILE

for URL in "${ENDPOINTS[@]}"
do
  echo "" | tee -a $REPORT_FILE
  echo "Testing: $URL" | tee -a $REPORT_FILE

  HTTP_CODE=$(curl -u $AUTH \
    -o /tmp/adoptapp_response.txt \
    -s \
    -w "%{http_code}" \
    "$URL")

  echo "HTTP CODE: $HTTP_CODE" | tee -a $REPORT_FILE

  diagnose_http_code $HTTP_CODE >> $REPORT_FILE

  echo "" >> $REPORT_FILE
  echo "RESPONSE:" >> $REPORT_FILE
  cat /tmp/adoptapp_response.txt >> $REPORT_FILE 2>/dev/null

  echo "" >> $REPORT_FILE
  echo "---------------------------------------------" >> $REPORT_FILE
done

# ==============================================================================
# HEALTHCHECK ACTUATOR
# ==============================================================================

echo "" >> $REPORT_FILE
echo "=============================================" >> $REPORT_FILE
echo " ACTUATOR HEALTHCHECK" >> $REPORT_FILE
echo "=============================================" >> $REPORT_FILE

ACTUATORS=(
"http://localhost:8081/actuator/health"
"http://localhost:8082/actuator/health"
"http://localhost:8083/actuator/health"
"http://localhost:8084/actuator/health"
"http://localhost:8085/actuator/health"
"http://localhost:8086/actuator/health"
"http://localhost:8090/actuator/health"
"http://localhost:8091/actuator/health"
"http://localhost:8092/actuator/health"
"http://localhost:8095/actuator/health"
)

for URL in "${ACTUATORS[@]}"
do
  echo "" >> $REPORT_FILE
  echo "Testing actuator: $URL" >> $REPORT_FILE

  HTTP_CODE=$(curl \
    -o /tmp/actuator_response.txt \
    -s \
    -w "%{http_code}" \
    "$URL")

  echo "HTTP CODE: $HTTP_CODE" >> $REPORT_FILE

  if [ "$HTTP_CODE" = "200" ]; then
    echo "Actuator OK" >> $REPORT_FILE
  else
    echo "Actuator FAIL" >> $REPORT_FILE
    echo "Posibles soluciones:" >> $REPORT_FILE
    echo "- Agregar spring-boot-starter-actuator" >> $REPORT_FILE
    echo "- Exponer endpoint health" >> $REPORT_FILE
    echo "- management.endpoints.web.exposure.include=*" >> $REPORT_FILE
  fi

  cat /tmp/actuator_response.txt >> $REPORT_FILE 2>/dev/null
done

# ==============================================================================
# BUSQUEDA DE LOGS
# ==============================================================================

echo "" >> $REPORT_FILE
echo "=============================================" >> $REPORT_FILE
echo " BUSQUEDA DE ERRORES EN LOGS" >> $REPORT_FILE
echo "=============================================" >> $REPORT_FILE

ERROR_PATTERNS=(
"Exception"
"ERROR"
"Caused by"
"Connection refused"
"FeignException"
"HikariPool"
"PSQLException"
"BeanCreationException"
"NullPointerException"
)

for PATTERN in "${ERROR_PATTERNS[@]}"
do
  echo "" >> $REPORT_FILE
  echo "Buscando: $PATTERN" >> $REPORT_FILE

  grep -Ri "$PATTERN" . \
    --include="*.log" \
    --include="*.txt" \
    --include="*.out" \
    2>/dev/null \
    | head -20 >> $REPORT_FILE
done

# ==============================================================================
# RESUMEN FINAL
# ==============================================================================

echo "" >> $REPORT_FILE
echo "=============================================" >> $REPORT_FILE
echo " RESUMEN FINAL" >> $REPORT_FILE
echo "=============================================" >> $REPORT_FILE

echo "Diagnostico completado." >> $REPORT_FILE
echo "" >> $REPORT_FILE

echo "Recomendaciones generales:" >> $REPORT_FILE
echo "1. Revisar puertos duplicados" >> $REPORT_FILE
echo "2. Revisar variables entorno" >> $REPORT_FILE
echo "3. Revisar PostgreSQL" >> $REPORT_FILE
echo "4. Revisar Feign Clients" >> $REPORT_FILE
echo "5. Revisar Spring Security" >> $REPORT_FILE
echo "6. Revisar application.yml" >> $REPORT_FILE
echo "7. Revisar logs de IntelliJ" >> $REPORT_FILE
echo "8. Revisar perfiles activos (postgres/h2)" >> $REPORT_FILE

echo ""
echo "============================================="
echo " DIAGNOSTICO COMPLETADO"
echo "============================================="
echo ""
echo "Informe generado:"
echo "$REPORT_FILE"
echo ""
