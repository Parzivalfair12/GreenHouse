# Despliegue del Servicio de IA (Flask)

Este documento describe cómo desplegar el microservicio de IA (`ia/`) que expone los endpoints de predicción y recomendación.

## Opción 1: Docker Compose (Recomendado)

El servicio de IA ya está integrado en `docker-compose.yml`.

```bash
cd GreenHouse
docker compose up --build
```

Esto levanta:
- PostgreSQL (`greenhouse-postgres`)
- Backend Spring Boot (`greenhouse-backend`)
- Frontend React (`greenhouse-frontend`)
- **IA Flask** (`greenhouse-ia`) ← NUEVO

### Variables de entorno del servicio IA

| Variable | Descripción | Default |
|----------|-------------|---------|
| `DB_HOST` | Host de PostgreSQL | `postgres` (Docker) / `localhost` (local) |
| `DB_PORT` | Puerto de PostgreSQL | `5432` |
| `DB_NAME` | Nombre de la base de datos | `greenhouse` |
| `DB_USER` | Usuario de PostgreSQL | `greenhouse_user` |
| `DB_PASS` | Contraseña de PostgreSQL | `greenhouse_pass` |

### Health check

```bash
curl http://localhost:5000/ia/health
```

Respuesta esperada:
```json
{"status": "UP", "service": "greenhouse-ia", "version": "2.0.0"}
```

### Puertos expuestos

| Puerto | Servicio |
|--------|----------|
| `5000` | IA Flask (Gunicorn) |

---

## Opción 2: Render o Railway

### Build command
```bash
pip install -r requirements.txt
```

### Start command
```bash
gunicorn -b 0.0.0.0:$PORT --timeout 30 --workers 1 --log-level info app:app
```

### Variables de entorno requeridas

```bash
DB_HOST=your-db-host.render.com
DB_PORT=5432
DB_NAME=greenhouse
DB_USER=greenhouse_user
DB_PASS=your-secure-password
```

> Nota: En Render/Railway, `$PORT` se inyecta automáticamente.

### Conexión desde el backend

En `application.yml` o variables de entorno:
```bash
IA_URL=https://tu-ia-service.onrender.com
```

---

## Opción 3: Local (sin Docker)

```bash
cd ia
cp .env.example .env
# Editar .env con tus credenciales
pip install -r requirements.txt
python app.py
```

El servidor arranca en `http://localhost:5000`.

---

## Endpoints de la IA

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/ia/health` | Health check |
| POST | `/ia/predict` | Predicción de temperatura/humedad |
| POST | `/ia/recommend` | Recomendación basada en riesgo |
| POST | `/ia/anomaly` | Detección de anomalías |
| GET | `/ia/history` | Historial de lecturas |

---

## Logs

La IA usa `logging` de Python con nivel INFO. En Docker:

```bash
docker compose logs -f ia
```
