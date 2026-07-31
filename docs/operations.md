# Operación de LifeMaster

## Estado actual

No existe un servidor configurado. El flujo de CI ejecuta pruebas, genera el JAR
y valida la imagen Docker, pero no publica ni despliega a ningún ambiente.

## Ejecución local

```powershell
docker compose up --build
```

La API queda en `http://localhost:8080` y PostgreSQL en `localhost:5432`.
Las credenciales de `docker-compose.yml` son exclusivamente locales.

## Observabilidad

- `GET /actuator/health` es público para verificaciones de salud.
- `GET /actuator/info` y `/actuator/metrics` requieren rol `Administrator`.
- Cada respuesta incluye `X-Correlation-ID`.
- Las operaciones POST, PUT, PATCH y DELETE generan un evento de auditoría.
- `GET /api/v1/audit-events` requiere rol `Administrator`.

## Alertas y recordatorios

`POST /api/v1/users/{userId}/alerts/refresh` detecta deudas próximas o vencidas
y recurrencias próximas.

Los recordatorios por correo están desactivados por defecto. Solo deben
activarse con `REMINDERS_ENABLED=true` después de configurar y probar SMTP.

## Importación bancaria

El endpoint de importación inicia con `dryRun=true`. El cliente debe presentar
el resumen al usuario y repetir la solicitud con `dryRun=false` para confirmar.
Un mismo archivo confirmado no puede importarse dos veces para el mismo usuario.
Las filas válidas se importan aunque otras filas del archivo sean rechazadas.
La respuesta separa `movements` y `failures`, informa la fila original y el
motivo de cada rechazo mediante `successfulRows` y `failedRows`.

El endpoint general es `POST /bank-imports/file` y admite archivos de hasta
20 MB en formatos CSV, XLS y XLSX. `/bank-imports/csv` se conserva para
clientes anteriores. En CSV y Excel se aceptan encabezados equivalentes en
inglés o español para fecha, descripción, importe, tipo y moneda.

Plantillas disponibles para el usuario autenticado:

- `GET /users/{userId}/bank-imports/template.csv`
- `GET /users/{userId}/bank-imports/template.xlsx`

La plantilla Excel incluye una hoja de instrucciones y listas de selección
para tipo y moneda. La primera hoja se conserva lista para ser importada.

Columnas CSV o Excel:

```text
date,description,amount,type,currency
2026-07-01,Salary,25000.00,INCOME,MXN
2026-07-02,Groceries,850.25,EXPENSE,MXN
```

Variables opcionales:

- `BANK_IMPORT_MAX_SIZE_BYTES` (`20971520` por defecto).

## Preparación de un despliegue futuro

Cuando exista un servidor se deberán definir secretos reales, TLS, dominio,
almacenamiento de respaldo, SMTP y monitoreo externo. El despliegue deberá
consumir el JAR o la imagen validada por CI, nunca compilar directamente en el
servidor.
