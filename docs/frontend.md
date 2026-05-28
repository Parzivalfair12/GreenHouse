# Frontend — GreenHouse Manager

## Stack Tecnológico

- **Framework:** React 18
- **Build:** Vite 5
- **Ruteo:** React Router DOM v6
- **Iconos:** Lucide React
- **ERD:** React Flow (XYFlow)
- **Testing:** Vitest + React Testing Library
- **E2E:** Selenium WebDriver

## Estructura de Archivos

```
frontend/src/
├── App.jsx                    # Estado global, polling, enrutamiento
├── App.test.jsx               # Tests de integración
├── main.jsx                   # Punto de entrada
├── api.js                     # Capa de abstracción HTTP
├── i18n.js                    # Diccionario ES/EN + helpers
├── styles.css                 # Estilos (dark/light mode)
├── components/
│   ├── shared.jsx             # Panel, Metric, Section, PaginationControls
│   ├── AppHeader.jsx          # Encabezado con selector idioma/tema
│   ├── Navbar.jsx             # Navegación lateral
│   ├── LoginScreen.jsx        # Pantalla de inicio de sesión
│   ├── DashboardSection.jsx   # Panel principal con métricas
│   ├── OperationsSection.jsx  # Centro operativo rápido
│   ├── CrudSection.jsx        # CRUD genérico reutilizable
│   ├── AlertsSection.jsx      # Gestión de alertas
│   ├── GreenhousesSection.jsx # CRUD de invernaderos
│   ├── UsersSection.jsx       # Administración de usuarios
│   ├── ManualSection.jsx      # Manual de usuario interactivo
│   ├── DataSection.jsx        # Exportación de datos
│   ├── DataDictionarySection.jsx # Diccionario de datos
│   ├── ArchitectureSection.jsx # Diagrama de arquitectura
│   ├── IaSection.jsx          # Panel IA predictiva
│   ├── TaigaSection.jsx       # Integración Taiga
│   ├── LogsSection.jsx        # Bitácora de auditoría
│   ├── Toast.jsx              # Notificaciones toast
│   ├── ErrorBoundary.jsx      # Manejo de errores React
│   └── LoadingSpinner.jsx     # Indicador de carga
├── pages/ERD/
│   ├── ERDViewer.jsx          # Visor ERD con React Flow
│   ├── ERDSidebar.jsx         # Barra lateral ERD
│   ├── EntityNode.jsx         # Nodo personalizado
│   ├── erdStyles.css          # Estilos ERD
│   └── __tests__/
│       └── ERDViewer.test.jsx # Tests ERD
├── config/
│   ├── modelo.json            # Single source of truth del modelo
│   └── modelParser.js         # Parser de modelo a nodos/edges
└── __tests__/
    └── i18n.test.jsx          # Tests de internacionalización
```

## Gestión de Estado

El estado global se maneja mediante props drilling desde App.jsx hacia los componentes hijos. No se usa Redux ni Context API para mantener la arquitectura simple y predecible.

**Estados persistidos en localStorage:**
- `greenhouse-session`: token JWT, email, roles
- `greenhouse-language`: idioma seleccionado (es/en)
- `greenhouse-theme`: tema visual (dark/light)
- `greenhouse-erd-positions`: posiciones de nodos en ERD

## Flujo de Autenticación

1. LoginScreen.jsx captura email/password
2. api.js envía POST /api/auth/login
3. Backend retorna JWT token
4. App.jsx guarda sesión en localStorage
5. Navbar.jsx se renderiza con datos autenticados
6. Cada request API incluye Authorization: Bearer <token>
7. Si 401, api.js intenta refresh automático
8. Si refresh falla, se redirige al login

## Internacionalización

- i18n.js contiene diccionario plano ES/EN
- getSavedLanguage() / saveLanguage() para persistencia
- Header Accept-Language enviado automáticamente
- Helper translate() para migración gradual a namespaces
- 150+ claves traducidas en ambos idiomas

## Pruebas

```bash
npm test                    # Vitest (39 tests)
npm run test:ui             # UI Vitest
npm run test:selenium       # Selenium E2E
npm run build               # Build producción
```
