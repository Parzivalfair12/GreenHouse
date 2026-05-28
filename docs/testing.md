# Guía de Pruebas — GreenHouse Manager

## Stack de Pruebas

| Tipo | Framework | Cobertura |
|------|-----------|-----------|
| Backend unitario | JUnit 5 + MockMvc | 48 tests |
| Frontend unitario | Vitest + React Testing Library | 39 tests |
| Frontend E2E | Selenium WebDriver | 6 tests |
| IA | pytest | 8 tests |
| CI/CD | GitHub Actions | 4 jobs |

## Backend (JUnit 5)

### Cómo ejecutar

```bash
cd backend
mvn test                 # Todos los tests
mvn test -Dtest=AuthServiceTest  # Test específico
mvn clean package        # Build completo con tests
```

### Categorías de tests

| Test | Archivo | Propósito |
|------|---------|-----------|
| RuleEngineServiceTest | `service/RuleEngineServiceTest.java` | Motor de reglas, umbrales, alertas |
| AlertServiceTest | `service/AlertServiceTest.java` | CRUD de alertas |
| SimulationServiceTest | `service/SimulationServiceTest.java` | Simulación IoT |
| AiPredictionServiceTest | `service/AiPredictionServiceTest.java` | Predicción IA |
| AuthControllerTest | `web/AuthControllerTest.java` | Login, registro, JWT |
| GreenhouseControllerTest | `web/GreenhouseControllerTest.java` | CRUD invernaderos |
| OperationsControllerTest | `web/OperationsControllerTest.java` | Operaciones |
| SecurityConfigTest | `web/SecurityConfigTest.java` | Roles y permisos |
| SimulatorControllerTest | `web/SimulatorControllerTest.java` | Endpoints simulador |
| IaControllerTest | `web/IaControllerTest.java` | Endpoints IA |
| I18nIntegrationTest | `web/I18nIntegrationTest.java` | Accept-Language, traducciones |

### Base de datos de prueba

- H2 en memoria (perfil `test`)
- Esquema generado por Hibernate (no Flyway)
- DataSeeder inserta datos demo automáticamente

## Frontend (Vitest)

### Cómo ejecutar

```bash
cd frontend
npm test                 # Todos los tests (modo run)
npm run test:ui          # Tests con UI Vitest
npm run test:selenium    # Tests E2E Selenium
```

### Categorías de tests

| Test | Archivo | Propósito |
|------|---------|-----------|
| App.test | `App.test.jsx` | Login, dashboard, errores, OAuth |
| ERDViewer.test | `pages/ERD/__tests__/ERDViewer.test.jsx` | ERD rendering |
| i18n.test | `__tests__/i18n.test.jsx` | Traducciones, persistencia, cambio dinámico |

### Lo que cubren los tests i18n

- `getSavedLanguage()` retorna 'es' por defecto
- `saveLanguage()` persiste en localStorage
- `translate()` resuelve claves planas y con fallback
- Parity de keys entre español e inglés
- Cambio dinámico de idioma en UI
- Persistencia de selección en localStorage

## Tests E2E (Selenium)

```bash
cd frontend
node test/selenium/erd.test.js  # Tests ERD Viewer
```

## Tests IA (pytest)

```bash
cd ia
pytest                         # Tests modelo Flask
```

## CI/CD (GitHub Actions)

El pipeline ejecuta automáticamente en push a main/master:

1. **Backend**: `mvn test` + `mvn package`
2. **Frontend**: `npm test` + `npm run build` + ESLint
3. **Python Model**: `python -m unittest discover -s tests/python`
4. **IA**: `pytest` en directorio ia/

## Cobertura esperada

- Backend: 70%+ líneas de negocio cubiertas
- Frontend: Componentes principales cubiertos
- i18n: 100% de keys verificadas entre idiomas
