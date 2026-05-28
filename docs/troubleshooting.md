# Troubleshooting — GreenHouse Manager

## Problemas de Docker

### "Port already allocated"

```bash
Error: Port 5432 is already in use
```
**Solución:** Detener otros contenedores PostgreSQL o cambiar el puerto en `docker-compose.yml`.

### Container exits immediately

```bash
docker logs greenhouse-backend
```
Verificar que PostgreSQL está accesible. El backend espera conexión a `localhost:5432`.

## Problemas de Base de Datos

### "Connection refused" a PostgreSQL

**Causa:** PostgreSQL no está corriendo o los datos de conexión son incorrectos.

**Solución:**
```bash
docker compose up -d db
# Verificar con:
docker compose ps
```

### Flyway migration failed

**Causa:** Esquema inconsistente o migraciones antiguas.

**Solución:**
```bash
# Resetear base de datos y volver a crear
docker compose down -v
docker compose up --build
```

## Problemas JWT

### "Invalid or expired token"

**Causa:** Token JWT expirado (24h por defecto) o malformado.

**Solución:**
- Refrescar token: `POST /api/auth/refresh`
- Re-autenticar: `POST /api/auth/login`

### "Missing Authorization header"

**Causa:** El frontend no está enviando el token.

**Solución:** Verificar que `localStorage` contiene `greenhouse-session` con un token válido. Limpiar localStorage y volver a iniciar sesión.

## Problemas OAuth2

### Google redirect "redirect_uri_mismatch"

**Causa:** La URI de redirección no está registrada en Google Cloud Console.

**Solución:** Agregar `http://localhost:5173/login/oauth2/code/google` en las URIs autorizadas de Google Cloud Console.

### OAuth callback retorna error

**Causa:** Fallo en la sincronización del usuario.

**Solución:** Verificar logs del backend con `docker logs greenhouse-backend`.

## Problemas CORS

### "Blocked by CORS policy"

**Causa:** El backend no acepta el origen del frontend.

**Solución:** Verificar `cors.allowed-origins` en `application.properties`.

## Problemas del Servicio IA

### "Connection refused" al microservicio Flask

**Causa:** Flask no está corriendo en el puerto 5000.

**Solución:**
```bash
docker compose up ia
# O manualmente:
cd ia && pip install -r requirements.txt && python app.py
```

## Problemas del Simulador IoT

### "Simulator already running"

**Causa:** El simulador ya está en ejecución.

**Solución:** Detenerlo primero o refrescar la página.

## Problemas del Frontend

### Pantalla en blanco (build)

**Causa:** Error de compilación o dependencias.

**Solución:**
```bash
cd frontend
npm install
npm run dev
# Verificar consola del navegador para errores
```

### "API not connected"

**Causa:** El frontend no puede alcanzar el backend.

**Solución:** Verificar que `VITE_API_URL` apunta al backend correcto. En Docker debe ser `http://backend:8080`.

## Problemas de Internacionalización

### "El idioma no cambia"

**Causa:** El idioma no se persiste correctamente en localStorage.

**Solución:** Limpiar localStorage (`localStorage.removeItem('greenhouse-language')`) y recargar.

### Mensajes sin traducir (muestran la key)

**Causa:** La key no existe en el diccionario del frontend o en los archivos `.properties` del backend.

**Solución:** Agregar la key faltante en ambos idiomas.

## Errores HTTP Comunes

| Código | Significado | Causa común |
|--------|-------------|-------------|
| 400 | Bad Request | Validación de campos fallida |
| 401 | Unauthorized | Token JWT faltante o inválido |
| 403 | Forbidden | Usuario no tiene el rol requerido |
| 404 | Not Found | Recurso no existe |
| 409 | Conflict | Email duplicado en registro |
| 429 | Too Many Requests | Rate limiting excedido |
| 500 | Internal Server Error | Error no manejado en backend |

## Logs

```bash
# Backend
docker logs greenhouse-backend

# Frontend
# Consola del navegador (F12 → Console)

# Todos los servicios
docker compose logs
```
