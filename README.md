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
- `/budgets?year=YYYY&month=M`: presupuestos generales o por categoría.
- `/recurring-movements`: definiciones recurrentes.
- `/recurring-movements/generate?through=YYYY-MM-DD`: materialización transaccional e idempotente.
- `/financial-summary/monthly?year=YYYY&month=M&currency=MXN`: resumen mensual por moneda.

Gastos, ingresos y pagos de deuda aceptan relaciones opcionales con cuentas y
medios de pago para mantener compatibilidad con los contratos anteriores.
