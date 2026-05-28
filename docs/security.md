# Seguridad — GreenHouse Manager

## Arquitectura de Autenticación

### JWT (JSON Web Token)

- Generado en login exitoso
- Incluye: email, roles, expiración
- Firma HMAC-SHA256 con clave secreta configurable
- Enviado en header `Authorization: Bearer <token>`
- Válido por `app.jwt-expiration-ms` (configurable, default 86400000ms = 24h)

```
Headers: { "alg": "HS256" }
Payload: { "sub": email, "roles": ["ROLE_ADMIN"], "iat": ..., "exp": ... }
```

### OAuth2 Google

- Usa `spring-security-oauth2-client`
- Redirect URI: `{baseUrl}/login/oauth2/code/google`
- Usuario se sincroniza automáticamente en PostgreSQL
- JWT adicional se genera en callback y se almacena en localStorage
- Cookie httpOnly para sesión OAuth2 (JS no accesible)

### Política de Contraseñas

- Almacenadas con BCrypt (Spring Security `BCryptPasswordEncoder`)
- Mínimo 4 caracteres (configurable)
- Usuarios OAuth2 tienen hash placeholder (`oauth2-google`)

## Control de Acceso (RBAC)

| Rol | Permisos |
|-----|----------|
| ADMIN | Acceso completo: CRUD usuarios, invernaderos, configuración |
| OPERATOR | Lectura + escritura: sensores, alertas, riegos, lecturas |
| VIEWER | Solo lectura: dashboard, sensores, alertas, IA |

Implementado mediante `@PreAuthorize` en controllers:
```java
@PreAuthorize("hasRole('ADMIN')")
@PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
@PreAuthorize("isAuthenticated()")
```

## Rate Limiting

- Filtro `RateLimitingFilter` antes de autenticación
- Límite por dirección IP
- Respuesta 429 Too Many Requests cuando se excede
- Configurable en `application.properties`

## Headers de Seguridad

### CORS

- Origen permitido: frontend (puerto 5173 en desarrollo)
- Métodos: GET, POST, PUT, PATCH, DELETE, OPTIONS
- Headers: Authorization, Content-Type, Accept-Language

### Security Headers (SecurityHeadersFilter)

- `X-Content-Type-Options: nosniff`
- `X-Frame-Options: SAMEORIGIN`
- `X-XSS-Protection: 1; mode=block`

### CSRF

- Deshabilitado para `/api/**` (API REST stateless con JWT)
- Habilitado para endpoints no-API (OAuth2 callbacks)

## Flujo End-to-End

```
Request → RateLimitingFilter → SecurityHeadersFilter
  → JwtAuthenticationFilter (extrae JWT del header)
    → SecurityFilterChain (valida rutas públicas/protegidas)
      → Controller (verifica @PreAuthorize)
        → Service (lógica de negocio)
          → Repository (PostgreSQL)
```

## Accept-Language (i18n)

- Header enviado por el frontend automáticamente
- Spring `AcceptHeaderLocaleResolver` lo procesa
- Mensajes resueltos mediante `ResourceBundleMessageSource`
- Idiomas soportados: español (es), inglés (en)
- Fallback al español si no se especifica idioma
