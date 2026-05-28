# Changelog — GreenHouse Manager

## [2.1.0] — 2026-05-27

### Internacionalización (i18n) — Punto 7

- Eliminados todos los textos quemados en frontend y backend
- `Locale.getDefault()` reemplazado por `LocaleContextHolder.getLocale()`
- Header `Accept-Language` dinámico desde localStorage
- Backend `AcceptHeaderLocaleResolver` con `setFallbackToSystemLocale(false)`
- Backend MessageSource integrado en AuthService, RuleEngineService
- Validaciones i18n en todos los DTOs
- Tests de integración i18n: Accept-Language es/en
- Frontend: `translate()` helper con soporte de namespaces
- Tests frontend: persistencia localStorage, cambio dinámico, parity de keys
- `ResourceBundleMessageSource` configurado correctamente

### Documentación — Punto 8

- JavaDocs completos en español en todos los servicios principales
- JavaDocs en controllers (Auth, Greenhouse, User, Alert, Simulator, IA)
- JavaDocs en DTOs principales (LoginRequest, LoginResponse, UserCreateRequest, ApiErrorResponse)
- JavaDocs en enums (UserRole, AlertSeverity, ActionOrigin, SensorType, ActuatorType, IrrigationMode)
- JavaDocs en SimulationService y AiPredictionService
- Comentarios estratégicos en frontend (api.js, i18n.js, App.jsx)
- README principal mejorado con sección i18n
- Creado `docs/architecture.md` — arquitectura completa
- Creado `docs/testing.md` — guía de pruebas
- Creado `docs/security.md` — documentación de seguridad
- Creado `docs/troubleshooting.md` — solución de problemas comunes
- Creado `frontend/README.md` — documentación del frontend
- Este `CHANGELOG.md`

### Correcciones

- Fix: `alreadyOpen` check en RuleEngineService ahora usa severity (locale-independent)
- Fix: `RuleEngineServiceTest` actualizado para no depender de texto en español
- Fix: Eliminados handlers duplicados en App.jsx con textos quemados en español
- Fix: Controller AuthController ahora inyecta Locale en todos los endpoints
- Fix: SimulatorController inyecta MessageSource para mensajes traducidos
- Fix: GlobalExceptionHandler usa MessageSource para mensajes de error
- Fix: AuthService elimina hardcoded "Credenciales invalidas"
- Fix: RuleEngineService elimina hardcoded strings en español
