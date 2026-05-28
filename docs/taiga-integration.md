# Integracion con Taiga — Guia tecnica

## Arquitectura

```
Taiga.io (API REST v1)
    ↑ ↓ HTTPS
TaigaService (Spring Boot)
    ↑ ↓
TaigaController (/api/taiga/*)
    ↑ ↓
TaigaSection.jsx (Frontend React)
```

## Configuracion

### Backend

Variables de entorno requeridas:

```bash
TAIGA_API_URL=https://api.taiga.io/api/v1
TAIGA_TOKEN=<tu-token-de-autenticacion>
TAIGA_PROJECT_ID=<id-del-proyecto-en-taiga>
```

### Como obtener el token de Taiga

1. Iniciar sesion en https://tree.taiga.io
2. Ir a Settings → API Tokens
3. Crear un nuevo token con alcance de proyecto
4. Copiar el token a `TAIGA_TOKEN`

### Como obtener el Project ID

1. Abrir el proyecto en Taiga
2. La URL tiene el formato: `https://tree.taiga.io/project/<username>-<project-name>-<id>`
3. El numero al final es el Project ID

## Endpoints

| Metodo | Ruta | Descripcion |
|--------|------|-------------|
| GET | `/api/taiga/stories` | Lista historias del proyecto |
| GET | `/api/taiga/epics` | Lista epics del proyecto |
| GET | `/api/taiga/stories/{id}` | Historia por ID |
| GET | `/api/taiga/stories/{id}/tasks` | Tareas de una historia |
| PATCH | `/api/taiga/stories/{id}/status` | Actualiza estado |
| POST | `/api/taiga/stories/{id}/comment` | Agrega comentario |
| GET | `/api/taiga/summary` | Resumen del proyecto |
| GET | `/api/taiga/generated-stories` | Historias generadas desde codigo |
| POST | `/api/taiga/sync` | Sincroniza historias con Taiga |
| GET | `/api/taiga/traceability` | Matriz de trazabilidad |
| GET | `/api/taiga/commits` | Historial de commits |

## Modo degradado

Si `TAIGA_TOKEN` no esta configurado, el servicio funciona en modo degradado:
- Los endpoints retornan listas vacias
- El summary indica `taigaEnabled: false`
- El frontend muestra indicador "No configurado"
- No se requiere Taiga para operar el sistema

## Webhooks

Taiga soporta webhooks para eventos. Configurar en Taiga:
Settings → Webhooks → Add webhook

URL sugerida: `https://tu-dominio/api/taiga/webhook/github`

Eventos recomendados: userstory.create, userstory.change

## Sincronizacion automatica

El workflow `taiga-sync.yml` en GitHub Actions puede sincronizar
automaticamente las historias generadas desde el codigo con Taiga.

Configurar secrets en GitHub:
- `TAIGA_TOKEN`
- `TAIGA_API_URL`
- `TAIGA_PROJECT_ID`
