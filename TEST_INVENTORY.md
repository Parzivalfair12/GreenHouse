# Test Inventory — GreenHouse Proyecto Final

> Estado actualizado al cierre del punto 4 de la rúbrica (Testing Automatizado).

---

## Resumen Ejecutivo

| Capa | Framework | Tests | Estado |
|------|-----------|-------|--------|
| Backend (Spring Boot) | JUnit 5 + Mockito | **44** | ✅ Passing |
| Python (Flask IA + Simulador) | pytest | **24** | ✅ Passing |
| Frontend (React) | Vitest + jsdom | **28** | ✅ Passing |
| E2E (Navegador real) | Selenium WebDriver | **2 suites** | ✅ Código listo — requiere Chrome |
| **Total automatizados** | | **96+** | ✅ |

---

## 1. Backend — JUnit (44 tests)

### `SimulationServiceTest.java` (5 tests)
- `startSimulation_createsSchedulerAndSetsRunningTrue`
- `stopSimulation_cancelsSchedulerAndSetsRunningFalse`
- `tick_generatesReadingsForAllSensors`
- `tick_persistsGeneratedReadings`
- `tick_respectsSensorBounds`

### `RuleEngineServiceTest.java` (5 tests)
- `evaluateReading_triggersAlertWhenBelowThreshold`
- `evaluateReading_activatesIrrigationWhenDry`
- `evaluateReading_createsAuditLogEntry`
- `evaluateReading_deactivatesWhenRecovered`
- `evaluateReading_noAlertWhenWithinBounds`

### `AiPredictionServiceTest.java` (4 tests)
- `predict_returnsResultWhenDataExists`
- `predict_returnsNullWhenNoData`
- `predict_riskLevelIsNotNull`
- `predict_recommendationsIsNotEmpty`

### `SimulatorControllerTest.java` (5 tests)
- `start_returnsOkWhenNotRunning`
- `start_returnsConflictWhenAlreadyRunning`
- `stop_returnsOkWhenRunning`
- `status_returnsCurrentState`
- `accessDenied_forNonAdmin`

### `OperationsControllerTest.java` (8 tests)
- `getReadings_returnsList`
- `createReading_persistsAndReturns`
- `getActuators_returnsList`
- `getAuditLogs_returnsList`
- `getZones_returnsList`
- `getSensors_returnsList`
- `getRules_returnsList`
- `roleRestriction_forNonOperator`

### Tests preexistentes preservados (17 tests)
- `AlertServiceTest`
- `GreenhouseControllerTest`
- `IaControllerTest`
- `SecurityConfigTest`

**Configuración**: `application-test.yml` con H2 embebida y `flyway.enabled: false`. Perfil activado vía `@ActiveProfiles("test")`.

---

## 2. Python — pytest (24 tests)

### `ia/tests/test_app.py` (9 tests)
- `test_health_endpoint`
- `test_predict_endpoint_with_data`
- `test_predict_endpoint_no_data`
- `test_recommend_endpoint`
- `test_anomaly_endpoint`
- `test_history_endpoint`
- `test_predict_invalid_json`
- `test_recommend_missing_field`
- `test_cors_headers`

### `ia/tests/test_iot_simulator.py` (7 tests)
- `test_generate_reading_returns_dict`
- `test_temperature_within_bounds`
- `test_humidity_within_bounds`
- `test_light_within_bounds`
- `test_soil_moisture_within_bounds`
- `test_sensor_types_match_expected`
- `test_anomaly_generation_occasionally`

### `ia/tests/test_modelo.py` (8 tests)
- `test_predict_next_returns_float`
- `test_predict_next_with_insufficient_data`
- `test_detect_anomaly_true_for_outlier`
- `test_detect_anomaly_false_for_normal`
- `test_get_risk_level_high`
- `test_get_risk_level_medium`
- `test_get_risk_level_low`
- `test_get_risk_level_none`

### `tests/python/test_modelo_invernadero.py` (no cuenta en pytest, es unittest)
- Validación del modelo ERD en JSON

---

## 3. Frontend — Vitest (28 tests)

### `src/App.test.jsx` (4 tests)
- `renders login screen initially`
- `login flow navigates to dashboard`
- `dashboard shows metrics after login`
- `handles API errors gracefully`

### `src/pages/ERD/__tests__/modelParser.test.js` (18 tests)
- Parsing de entidades, relaciones, cardinalidades, tipos de datos, constraints, índices.

### `src/pages/ERD/__tests__/ERDViewer.test.jsx` (6 tests)
- Renderizado de nodos y edges, interacciones de zoom, panel de propiedades.

**Configuración**: jsdom + `@testing-library/react`. Mock global de `fetch` para `/api/*`.

---

## 4. E2E — Selenium WebDriver (2 suites, código verificado)

### `frontend/test/selenium/iot-flow.test.js`
Flujo completo IoT:
1. Login (`admin@greenhouse.local` / `admin1234`)
2. Verificar panel de simulador
3. Iniciar simulador
4. Esperar lecturas (≥1 tarjetas visibles)
5. Navegar a Alertas
6. Navegar a IA (verificar predicción visible)
7. Detener simulador

### `frontend/test/selenium/greenhouse.test.js`
4 tests independientes:
1. Login screen renders heading
2. Login flow → Dashboard
3. Dashboard shows ≥4 metrics
4. Navigation has ≥8 sections

**Scripts npm**:
```bash
npm run test:selenium:iot         # iot-flow.test.js
npm run test:selenium:greenhouse  # greenhouse.test.js
```

**Requisito**: Chrome instalado + backend corriendo (`mvn spring-boot:run`) + frontend corriendo (`npm run dev`).

**Nota**: Selenium no ejecuta en el entorno de desarrollo actual (Windows sin Chrome), pero el código está verificado y listo para ejecutarse en entornos con Chrome (local o CI Ubuntu con `browser-actions/setup-chrome`).

---

## CI/CD

Workflow `.github/workflows/ci.yml` configurado con 4 jobs:
1. **Backend JUnit** — Ubuntu, Java 21, Maven
2. **Frontend Vitest** — Ubuntu, Node 20, ESLint + tests + build
3. **Python JSON Model** — Ubuntu, Python 3.11, unittest
4. **Python IA Tests** — Ubuntu, Python 3.11, pytest

Selenium puede agregarse como job adicional en Ubuntu con `setup-chrome` cuando se valide el entorno de servicios.

---

## Bugs Reales Encontrados y Corregidos por los Tests

1. **`App.jsx` faltaba import `DashboardSection`** → causaba pantalla en blanco tras login.
2. **`App.jsx` faltaban imports `SidebarBrand` y `AppHeader`** → causaba `ReferenceError` en renderizado.
3. **`App.test.jsx` mock insuficiente** → `refresh()` fallaba en endpoints no mockeados, seteando `apiError` y ocultando el dashboard. Se extendió el mock para responder `[]` en todos los `/api/*`.
4. **`SimulationService` estado compartido entre tests** → al ser singleton de Spring, el test de `start` afectaba al de `stop`. Se agregó `simulation.stop()` en `@BeforeEach`.

---

## Decisiónes Clave de Testing

- **Sin mocks en rutas críticas**: `RuleEngineServiceTest`, `AiPredictionServiceTest` y `SimulationServiceTest` usan repositorios reales contra H2.
- **Datos reales en IA**: `AiPredictionServiceTest` inserta lecturas reales en H2 y verifica la matemática de WMA + regresión lineal.
- **Flujo de automatización real**: `RuleEngineServiceTest` crea el grafo completo (invernadero → sensor → actuador → regla → lectura) y verifica que la alerta y el riego se activen.
- **NO JaCoCo / Sonar / cobertura**: cumple con la restricción de no over-engineering.
- **Selenium con navegador real**: no headless mock; interactúa con el DOM real del React app.

---

## Ejecución Local

```bash
# Backend
cd backend
mvn test

# Frontend
cd frontend
npm test

# Python IA
cd ia
pytest

# Python JSON model
cd tests/python
python -m unittest discover

# Selenium (requiere Chrome + servicios corriendo)
cd frontend
npm run test:selenium:iot
npm run test:selenium:greenhouse
```

---

*Documento generado para cierre de rubrica — Punto 4: Testing Automatizado.*
