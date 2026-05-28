# API REST — GreenHouse Manager

## Información General

- **URL Base:** `http://localhost:8080`
- **Formato:** JSON
- **Autenticación:** JWT Bearer Token (header `Authorization`)
- **Idioma:** Header `Accept-Language: es|en`
- **Documentación Swagger:** `/swagger-ui.html`

## Endpoints de Autenticación

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/api/auth/login` | Público | Iniciar sesión con email y contraseña |
| POST | `/api/auth/register` | Público | Registrar nuevo usuario |
| POST | `/api/auth/refresh` | JWT | Refrescar token JWT |
| POST | `/api/auth/forgot-password` | Público | Solicitar recuperación de contraseña |
| POST | `/api/auth/reset-password` | Token | Restablecer contraseña |
| POST | `/api/auth/verify` | Público | Verificar email con token |
| GET | `/api/auth/verify-email` | Público | Verificar email (GET) |
| POST | `/api/auth/resend-verification` | JWT | Reenviar correo de verificación |
| GET | `/api/auth/me` | JWT | Obtener perfil del usuario autenticado |

## Endpoints de Invernaderos

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/greenhouses` | JWT | Listar invernaderos |
| GET | `/api/greenhouses/{id}` | JWT | Obtener invernadero por ID |
| POST | `/api/greenhouses` | ADMIN | Crear invernadero |
| PUT | `/api/greenhouses/{id}` | ADMIN | Actualizar invernadero |
| DELETE | `/api/greenhouses/{id}` | ADMIN | Eliminar invernadero |
| POST | `/{id}/crops` | ADMIN | Agregar cultivo |
| PUT | `/{id}/crops/{cropId}` | ADMIN | Actualizar cultivo |
| DELETE | `/{id}/crops/{cropId}` | ADMIN | Eliminar cultivo |
| POST | `/{id}/sensors` | ADMIN | Agregar sensor |
| PUT | `/{id}/sensors/{sensorId}` | ADMIN | Actualizar sensor |
| DELETE | `/{id}/sensors/{sensorId}` | ADMIN | Eliminar sensor |
| POST | `/{id}/irrigation-events` | ADMIN | Registrar riego |
| PUT | `/{id}/irrigation-events/{eventId}` | ADMIN | Actualizar riego |
| DELETE | `/{id}/irrigation-events/{eventId}` | ADMIN | Eliminar riego |

## Endpoints de Usuarios

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/users` | ADMIN | Listar usuarios |
| POST | `/api/users` | ADMIN | Crear usuario |
| PATCH | `/api/users/{id}/role` | ADMIN | Cambiar rol de usuario |

## Endpoints de Alertas

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/alerts/open` | JWT | Listar alertas abiertas |
| PATCH | `/api/alerts/{id}/resolve` | OPERATOR | Resolver alerta |

## Endpoints del Dashboard

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/dashboard` | JWT | Resumen general del sistema |

## Endpoints del Simulador IoT

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| POST | `/api/simulator/start` | OPERATOR | Iniciar simulación IoT |
| POST | `/api/simulator/stop` | OPERATOR | Detener simulación IoT |
| GET | `/api/simulator/status` | JWT | Estado del simulador |

## Endpoints IA

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/ia/health` | JWT | Health check del servicio IA |
| POST | `/api/ia/predict` | JWT | Predecir temperatura y humedad |
| POST | `/api/ia/recommend` | JWT | Obtener recomendación agronómica |

## Endpoints de Operaciones

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/zones` | JWT | Listar zonas |
| POST | `/api/zones` | OPERATOR | Crear zona |
| PUT | `/api/zones/{id}` | OPERATOR | Actualizar zona |
| DELETE | `/api/zones/{id}` | OPERATOR | Eliminar zona |
| GET | `/api/sensors` | JWT | Listar sensores |
| GET | `/api/readings` | JWT | Listar lecturas |
| POST | `/api/readings` | OPERATOR | Registrar lectura |
| PUT | `/api/readings/{id}` | OPERATOR | Actualizar lectura |
| DELETE | `/api/readings/{id}` | OPERATOR | Eliminar lectura |
| GET | `/api/actuators` | JWT | Listar actuadores |
| POST | `/api/actuators` | OPERATOR | Crear actuador |
| PUT | `/api/actuators/{id}` | OPERATOR | Actualizar actuador |
| DELETE | `/api/actuators/{id}` | OPERATOR | Eliminar actuador |
| GET | `/api/rules` | JWT | Listar reglas |
| POST | `/api/rules` | OPERATOR | Crear regla |
| PUT | `/api/rules/{id}` | OPERATOR | Actualizar regla |
| DELETE | `/api/rules/{id}` | OPERATOR | Eliminar regla |

## Health Check

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| GET | `/api/health` | Público | Health check del sistema |

## Códigos de Error

| Código | Significado | Causa |
|--------|-------------|-------|
| 400 | Bad Request | Validación de campos fallida |
| 401 | Unauthorized | Token JWT faltante, inválido o expirado |
| 403 | Forbidden | Usuario no tiene el rol requerido |
| 404 | Not Found | Recurso no existe |
| 409 | Conflict | Email duplicado en registro |
| 429 | Too Many Requests | Rate limiting excedido |
| 500 | Internal Server Error | Error no manejado |

## Ejemplos

### Login exitoso

```json
POST /api/auth/login
{
  "email": "admin@greenhouse.local",
  "password": "admin1234"
}

Response 200:
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "admin@greenhouse.local",
  "fullName": "Administrador",
  "roles": ["ROLE_ADMIN"],
  "expiresIn": 86400,
  "verified": true
}
```

### Error de validación

```json
Response 400:
{
  "timestamp": "2026-05-27T12:00:00.000Z",
  "status": 400,
  "error": "Error de validacion",
  "message": "Error de validacion en los campos enviados",
  "path": "/api/auth/login",
  "fieldErrors": {
    "email": "El correo es obligatorio",
    "password": "La contrasena es obligatoria"
  }
}
```

### Error 401

```json
Response 401:
{
  "timestamp": "2026-05-27T12:00:00.000Z",
  "status": 401,
  "error": "Unauthorized",
  "message": "Credenciales invalidas",
  "path": "/api/auth/login"
}
```
