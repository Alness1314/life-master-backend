# Operación de LifeMaster

## Despliegue de producción

El backend se despliega en `nidum-server` mediante el workflow
`.github/workflows/deploy.yml`. Un runner autoalojado ejecuta las pruebas,
construye una imagen Docker etiquetada con el SHA del commit y actualiza el
contenedor local. El workflow de despliegue solo se ejecuta para
`development` o manualmente mediante `workflow_dispatch`; los pull requests no
se ejecutan en el runner del servidor.

La base PostgreSQL es externa y no forma parte del Compose. Su URL, usuario y
contraseña se administran como secretos del ambiente `development` de GitHub.
La API no publica puertos en el host: ngrok la alcanza como
`http://lifemaster-api:8080` mediante la red Docker externa `backend-net`.

Archivos de producción en el servidor:

```text
/home/albert/apps/lifemaster-backend/compose.yml
/home/albert/apps/lifemaster-backend/.env
```

El `.env` se genera durante el despliegue con permisos `600` y nunca se
versiona. Los archivos cargados por los usuarios se conservan en el volumen
Docker `lifemaster_files_data`.

Secretos requeridos en el ambiente GitHub `development`:

- `DB_URL_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `DEFAULT_PASS`
- `EMAIL_USERNAME`
- `EMAIL_PASSWORD`

Variables recomendadas del ambiente:

- `EMAIL_HOST`
- `EMAIL_PORT`
- `SERVER_URL`
- `SERVER_DESCRIPTION`
- `CORS_ALLOWED_ORIGINS`
- `REMINDERS_ENABLED`

La cuenta de PostgreSQL debe aceptar conexiones desde el servidor, exigir TLS
cuando el proveedor lo soporte y tener permisos para ejecutar las migraciones
Flyway.

## Ejecución local

```powershell
docker compose up --build
```

La API queda en `http://localhost:8080` y PostgreSQL en `localhost:5432`.
Las credenciales de `docker-compose.yml` son exclusivamente locales.

## Observabilidad

- `GET /actuator/health` es público para verificaciones de salud.
- `GET /actuator/health/liveness` comprueba el proceso Java.
- `GET /actuator/health/readiness` comprueba aplicación, disco y PostgreSQL.
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

## Comandos en el servidor

```bash
cd /home/albert/apps/lifemaster-backend
docker compose --env-file .env --file compose.yml ps
docker compose --env-file .env --file compose.yml logs --follow --tail 200 api
docker exec lifemaster-api curl --fail --silent \
  http://127.0.0.1:8080/actuator/health/readiness
```

El workflow conserva la imagen anterior con la etiqueta
`life-master-backend:rollback`. El procedimiento definitivo de rollback y
restauración se documentará después de validar el primer despliegue real.
