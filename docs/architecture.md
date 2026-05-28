# Arquitectura del Sistema — GreenHouse Manager

## Visión General

GreenHouse Manager es un sistema académico para la gestión inteligente de invernaderos agrícolas. Combina un backend Spring Boot 3 con PostgreSQL, un frontend React + Vite, un microservicio Flask de IA predictiva y un simulador IoT.

```
┌─────────────────────────────────────────────────────────────┐
│                    FRONTEND (React + Vite)                    │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────────┐ │
│  │Dashboard │  │Auth UI   │  │ERD Viewer│  │ Manual User  │ │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └──────┬──────┘ │
│       └──────────────┴─────────────┴───────────────┘        │
│                        │                                     │
│               i18n ES/EN │ localStorage                     │
└─────────────────────────┼───────────────────────────────────┘
                          │ HTTP (JWT Bearer + Accept-Language)
┌─────────────────────────┼───────────────────────────────────┐
│              BACKEND (Spring Boot 3)                         │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌─────────────┐ │
│  │Controllers│→│ Services │→│Repos     │→│ PostgreSQL   │ │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └─────────────┘ │
│       │             │              │                         │
│  ┌────┴─────┐  ┌────┴─────┐  ┌────┴─────┐                  │
│  │Security  │  │ RuleEngine│  │ JWT/OAuth│                  │
│  └──────────┘  └──────────┘  └──────────┘                  │
│       │                                                     │
│       └──────────────┬──────────────────────────────────────┘
│                      │ HTTP
└──────────────────────┼──────────────────────────────────────┘
          ┌────────────┼────────────┐
          ▼            ▼            ▼
   ┌──────────┐ ┌──────────┐ ┌──────────┐
   │Flask IA  │ │IoT Sim   │ │Swagger UI│
   └──────────┘ └──────────┘ └──────────┘
```

## Arquitectura Backend (Spring Boot 3)

### Capas

| Capa | Tecnología | Propósito |
|------|-----------|-----------|
| Controller | Spring MVC REST | Endpoints HTTP, validación, Swagger |
| Service | Spring @Service | Lógica de negocio, transacciones, auditoría |
| Repository | Spring Data JPA | Acceso a datos PostgreSQL |
| Domain | JPA Entities | Modelo de datos ORM |
| Config | Spring @Configuration | Seguridad, JWT, CORS, i18n, OpenAPI |

### Flujo de Autenticación (JWT + OAuth2)

```
Login:        POST /api/auth/login → AuthService.login() → JWT Token
OAuth2 Google: /oauth2/authorization/google → redirect → callback → cookie
Refresh:      POST /api/auth/refresh → nuevo JWT
Verify:       GET /verify-email?token=... → AuthService.verifyEmailWithToken()
```

### Flujo de Internacionalización

```
Frontend guarda idioma → localStorage
  → API calls incluyen Accept-Language: es|en
    → Backend resuelve Locale via AcceptHeaderLocaleResolver
      → MessageSource.getMessage() retorna texto traducido
```

### Flujo IoT Simulación

```
SimulationService.tick() cada 5s
  → SensorState.nextValue() genera valor gradual
  → Reading persistido
  → RuleEngineService.evaluateReading()
    → Threshold check → Alertas
    → Rule evaluation → Actuadores/Riego
```

### Flujo IA Predictiva

```
Frontend envía lecturas históricas
  → IaController → IaService → Flask microservicio HTTP
  → Flask ejecuta modelo scikit-learn
  → Retorna predicción + recomendación + nivel de riesgo
```

## Arquitectura Frontend (React + Vite)

### Componentes Principales

| Componente | Propósito |
|-----------|-----------|
| App.jsx | Estado global, enrutamiento, polling |
| LoginScreen.jsx | Autenticación email/password y OAuth |
| DashboardSection.jsx | Métricas generales, simulador, gráficos |
| OperationsSection.jsx | Centro operativo rápido |
| CrudSection.jsx | CRUD genérico reutilizable |
| ERDViewer.jsx | Diagrama entidad-relación dinámico |
| DataDictionarySection.jsx | Diccionario de datos automático |

### Gestión de Estado

- Estado centralizado en App.jsx (props drilling)
- Sesión JWT en localStorage
- Idioma en localStorage
- Tema (dark/light) en localStorage
- Polling cada 5 segundos para datos en tiempo real

## Seguridad

- JWT Bearer tokens con expiración configurable
- OAuth2 Google con httpOnly cookie
- BCrypt para contraseñas
- Rate limiting por IP
- CORS configurado para frontend
- CSRF deshabilitado para /api/** (REST stateless)
- Roles: ADMIN, OPERATOR, VIEWER

## Despliegue (Docker)

```
docker compose up --build
  → Frontend :5173
  → Backend  :8080
  → Postgres :5432
  → Flask IA :5000
```
