# API Docs

La documentacion interactiva queda disponible en `/swagger-ui.html` cuando el backend esta en ejecucion.

## Endpoints Principales

| Metodo | Ruta | Autorizacion | Uso |
| --- | --- | --- | --- |
| GET | `/api/greenhouses` | Autenticado | Lista invernaderos |
| POST | `/api/greenhouses` | ADMIN | Crea un invernadero |
| GET | `/api/greenhouses/{id}` | Autenticado | Consulta detalle |
| GET | `/api/alerts/open` | Autenticado | Lista alertas abiertas |
| PATCH | `/api/alerts/{id}/resolve` | OPERATOR o ADMIN | Marca alerta como resuelta |
| GET | `/api/me` | Autenticado | Devuelve el usuario OAuth2 |

## Cabeceras

| Cabecera | Valor | Descripcion |
| --- | --- | --- |
| `Accept-Language` | `es` o `en` | Idioma de respuesta |
| `Authorization` | `Bearer <token>` | Token OAuth2/OIDC |
| `Content-Type` | `application/json` | Cuerpo JSON |

## Criterios De Aceptacion

- El usuario puede autenticarse con Google OAuth2.
- Un ADMIN puede crear y consultar invernaderos.
- Un OPERATOR puede resolver alertas.
- El frontend consume `/api/greenhouses` y muestra datos localizados en espanol e ingles.
- La suite de pruebas se ejecuta desde GitHub Actions.
