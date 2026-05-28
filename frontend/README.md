# GreenHouse Frontend

Frontend React + Vite para el sistema de gestión inteligente de invernaderos.

## Stack

- React 18 + Vite 5
- React Router DOM v6
- Lucide React (iconos)
- XYFlow (ERD Viewer)
- Vitest + React Testing Library
- Selenium WebDriver (E2E)

## Estructura

```
src/
├── App.jsx               # Estado global, rutas, polling, auth
├── api.js                # Cliente HTTP con JWT, refresh, Accept-Language
├── i18n.js               # Diccionario español/inglés, persistencia
├── components/           # Componentes UI de la aplicación
│   ├── LoginScreen.jsx   # Pantalla de inicio de sesión
│   ├── DashboardSection.jsx  # Panel de métricas y simulador
│   ├── OperationsSection.jsx # Centro operativo rápido
│   ├── CrudSection.jsx   # CRUD genérico reutilizable
│   ├── AppHeader.jsx     # Encabezado con idioma, tema, usuario
│   ├── Navbar.jsx        # Navegación lateral
│   ├── AlertsSection.jsx # Panel de alertas abiertas
│   ├── ManualSection.jsx # Manual de usuario interactivo
│   ├── DataDictionarySection.jsx # Diccionario de datos dinámico
│   ├── IaSection.jsx     # Panel de IA predictiva
│   ├── TaigaSection.jsx  # Integración con Taiga
│   ├── LogsSection.jsx   # Bitácora de auditoría
│   ├── DataSection.jsx   # Exportación de datos
│   ├── Toast.jsx         # Notificaciones toast
│   └── shared.jsx        # Componentes compartidos (Panel, Metric, Section)
├── pages/ERD/            # Visor de diagrama ERD
│   ├── ERDViewer.jsx     # Visor principal con ReactFlow
│   ├── ERDSidebar.jsx    # Barra lateral con lista de entidades
│   └── EntityNode.jsx    # Nodo personalizado para ReactFlow
├── config/               # Modelo de datos y parser
│   ├── modelo.json       # Single source of truth del modelo
│   └── modelParser.js    # Parser: FK detection, edges, relaciones
├── styles.css            # Estilos globales (dark/light mode)
└── __tests__/            # Tests i18n
```

## Estado Global

- Estado centralizado en `App.jsx` (props drilling hacia componentes hijos)
- Sesión JWT en localStorage (`greenhouse-session`)
- Idioma en localStorage (`greenhouse-language`)
- Tema en localStorage (`greenhouse-theme`)
- Polling cada 5s para datos en tiempo real (dashboard, alertas, sensores)

## Autenticación

- Login email/password → `POST /api/auth/login` → JWT token
- OAuth2 Google → redirect → callback → cookie + JWT
- Token refresh automático en 401
- Logout → limpiar localStorage + recargar

## Internacionalización (i18n)

Ver `docs/architecture.md` para flujo completo.

- Diccionario plano en `i18n.js`
- Helper `translate(language, path, fallback)` para migración gradual
- Persistencia en localStorage via `getSavedLanguage()` / `saveLanguage()`
- Header `Accept-Language` enviado automáticamente en cada request API

## Pruebas

```bash
npm test               # Vitest (39 tests)
npm run test:ui        # Vitest UI
npm run test:selenium  # Selenium E2E
npm run build          # Build producción
```

## Build

```bash
npm run dev       # Desarrollo (hot reload)
npm run build     # Producción (dist/)
npm run preview   # Vista previa build
```
