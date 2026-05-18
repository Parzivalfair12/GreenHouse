# Taiga Historias De Usuario

Este archivo resume las historias que se pueden registrar en Taiga y sus criterios de aceptacion.

## HU-01 Autenticacion Con Google

Como usuario del sistema quiero iniciar sesion con mi correo de Google para acceder sin crear una clave nueva.

Criterios de aceptacion:

- El boton "Entrar con Google" redirige a `/oauth2/authorization/google`.
- El backend usa OAuth2 Client de Spring Security.
- El endpoint `/api/me` devuelve nombre, correo y estado de autenticacion.

## HU-02 Consulta De Invernaderos

Como operador quiero consultar los invernaderos activos para conocer su estado general.

Criterios de aceptacion:

- El endpoint `GET /api/greenhouses` devuelve id, nombre, ubicacion, area, estado, cantidad de cultivos y sensores.
- El frontend muestra los invernaderos en una tabla responsive.
- La pantalla funciona en espanol e ingles.

## HU-03 Gestion De Alertas

Como operador quiero revisar y resolver alertas para dejar trazabilidad de incidentes ambientales.

Criterios de aceptacion:

- El endpoint `GET /api/alerts/open` lista alertas no resueltas.
- El endpoint `PATCH /api/alerts/{id}/resolve` cambia `resolved` a `true`.
- La respuesta respeta la cabecera `Accept-Language`.

## HU-04 Validacion Del Modelo JSON

Como docente quiero validar que el modelo JSON tenga datos completos para verificar la coherencia del diseno.

Criterios de aceptacion:

- El archivo `docs/modelo-invernadero.json` contiene invernaderos, alertas y usuarios.
- Las pruebas Python validan secciones obligatorias, datos anidados y umbrales.
- GitHub Actions ejecuta la validacion automaticamente.
