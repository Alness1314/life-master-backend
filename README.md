# LifeMaster Backend

Backend de control financiero construido con Spring Boot 3, Java 17 y PostgreSQL.

## Desarrollo

Las pruebas usan H2 en memoria y no necesitan una base externa:

```powershell
.\mvnw.cmd test
```

Para ejecutar la aplicación se requieren las variables declaradas en
`src/main/resources/application.yml`, incluyendo conexión PostgreSQL, secreto JWT,
correo, prefijo de API y contraseña inicial del administrador.

## Núcleo financiero

Todos los recursos financieros se encuentran bajo
`${api.prefix}/users/{userId}` y validan que `userId` corresponda al JWT.

- `/accounts`: cuentas financieras y saldo calculado.
- `/payment-methods`: medios de pago, opcionalmente asociados a una cuenta.
- `/recurring-movements`: definiciones recurrentes.
- `/recurring-movements/generate?through=YYYY-MM-DD`: materialización transaccional e idempotente.
- `/financial-summary/monthly?year=YYYY&month=M&currency=MXN`: resumen mensual por moneda.
- `/reports/monthly.csv`: exportación mensual.
- `/bank-imports/file`: vista previa y confirmación de CSV o Excel; conserva `/csv` por compatibilidad.
- `/alerts`: alertas de deuda y recurrencias.
- `/reminders`: recordatorios programados.
- `/expenses/{expenseId}/receipts`: comprobantes PDF o imagen.

Gastos, ingresos y pagos de deuda aceptan relaciones opcionales con cuentas y
medios de pago para mantener compatibilidad con los contratos anteriores.

La guía de ejecución, observabilidad, importación y preparación para un futuro
despliegue está en [docs/operations.md](docs/operations.md).
