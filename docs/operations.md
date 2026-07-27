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

`POST /api/v1/users/{userId}/alerts/refresh` detecta presupuestos consumidos,
deudas próximas o vencidas y recurrencias próximas.

Los recordatorios por correo están desactivados por defecto. Solo deben
activarse con `REMINDERS_ENABLED=true` después de configurar y probar SMTP.

## Importación bancaria

El endpoint de importación inicia con `dryRun=true`. El cliente debe presentar
el resumen al usuario y repetir la solicitud con `dryRun=false` para confirmar.
Un mismo archivo confirmado no puede importarse dos veces para el mismo usuario.

Columnas CSV:

```text
date,description,amount,type,currency
2026-07-01,Salary,25000.00,INCOME,MXN
2026-07-02,Groceries,850.25,EXPENSE,MXN
```

## Preparación de un despliegue futuro

Cuando exista un servidor se deberán definir secretos reales, TLS, dominio,
almacenamiento de respaldo, SMTP y monitoreo externo. El despliegue deberá
consumir el JAR o la imagen validada por CI, nunca compilar directamente en el
servidor.
