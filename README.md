# Greenhouse Manager

Software academico para administrar un invernadero con backend Spring Boot, frontend React, autenticacion OAuth2, internacionalizacion, pruebas y documentacion.

## Modulos

- `backend`: API REST Spring Boot para sensores, cultivos, riegos, alertas y usuarios.
- `frontend`: aplicacion React para consultar el estado del invernadero.
- `.github/workflows`: pipeline de pruebas para backend, frontend y pruebas Python.
- `docs`: modelo JSON, entidad relacion, diccionario de datos y documentacion de la API.
- `tests/python`: pruebas auxiliares para validar el modelo JSON.

## Requisitos

- JDK 21
- Node.js 20 o superior
- Python 3.11 o superior
- Docker Desktop para levantar PostgreSQL localmente

## Ejecucion

### 1. Base de datos

Opcion recomendada con Docker:

```bash
docker compose up -d
```

Esto crea automaticamente:

```text
Database: greenhouse
User: greenhouse_user
Password: greenhouse_pass
Port: 5432
```

Opcion manual con pgAdmin o psql:

1. Conectate como usuario administrador de PostgreSQL, por ejemplo `postgres`.
2. Ejecuta el script:

```text
docs/create-database.sql
```

El backend crea/actualiza las tablas automaticamente con Hibernate usando `spring.jpa.hibernate.ddl-auto=update`.

### 2. Backend

PowerShell:

```powershell
cd backend
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-21.0.11.10-hotspot'
$env:Path="$env:JAVA_HOME\bin;C:\Users\andre\Tools\apache-maven-3.9.15\bin;$env:Path"
$env:GOOGLE_CLIENT_ID='demo-client'
$env:GOOGLE_CLIENT_SECRET='demo-secret'
mvn spring-boot:run
```

Bash:

```bash
cd backend
export GOOGLE_CLIENT_ID=demo-client
export GOOGLE_CLIENT_SECRET=demo-secret
mvn spring-boot:run
```

El backend queda disponible en:

```text
http://localhost:8080
```

Swagger:

```text
http://localhost:8080/swagger-ui.html
```

### 3. Frontend

```bash
cd frontend
npm install
npm run dev -- --port 5173
```

El frontend queda disponible en:

```text
http://localhost:5173
```

## OAuth2

El backend incluye OAuth2 real con Google. En Google Cloud Console crea un OAuth Client de tipo `Web application` y agrega este redirect URI autorizado:

```text
http://localhost:8080/login/oauth2/code/google
```

Luego define estas variables antes de ejecutar el backend:

```bash
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
FRONTEND_URL=http://localhost:5173
```

El boton `Continuar con Google` envia al usuario a Google, Spring Boot guarda o actualiza el usuario autenticado en `app_user` con `provider = google`, y despues redirige al frontend.

## Pruebas

```bash
cd backend && mvn test
cd frontend && npm test
python -m unittest discover -s tests/python
```

## Base De Datos PostgreSQL

El backend usa PostgreSQL por defecto:

```text
Host: localhost
Port: 5432
Database: greenhouse
User: greenhouse_user
Password: greenhouse_pass
JDBC URL: jdbc:postgresql://localhost:5432/greenhouse
```

Para levantar la base:

```bash
docker compose up -d
```

Tambien existe el script manual:

```text
docs/create-database.sql
```

Las pruebas JUnit usan H2 con el perfil `test`, asi no dependen de Docker.

## Usuarios Y Login

La autenticacion guarda usuarios en la tabla `app_user` de PostgreSQL. Al iniciar el backend se crea este usuario administrador:

```text
Email: admin@greenhouse.local
Password: admin1234
Role: ADMIN
```

El login con correo solo permite entrar si el usuario existe en `app_user` y la contrasena coincide con `password_hash`. Para revisar los usuarios en pgAdmin, abre la base `greenhouse`, entra a `Schemas > public > Tables > app_user` y usa `View/Edit Data`.
