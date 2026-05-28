# CI/CD Pipeline — GreenHouse Manager

## Workflows

El proyecto utiliza **GitHub Actions** con los siguientes workflows:

### 1. CI (`ci.yml`)

**Trigger:** push a main/master/develop/feature/*, PR a main/master

| Job | Descripcion | Comando |
|-----|-------------|---------|
| Backend JUnit + Jacoco | Tests unitarios + cobertura | `mvn verify` |
| Frontend Vitest + Build | Lint, tests, build produccion | `npm test && npm run build` |
| Python Model Validation | Validacion JSON modelo | `python -m unittest discover` |
| Python IA Tests | Tests microservicio IA | `pytest` |
| Quality Gate Summary | Verifica resultados globales | — |

### 2. Taiga Sync (`taiga-sync.yml`)

**Trigger:** push a main/master, manual

Sincroniza historias de usuario con Taiga.io y genera matriz de trazabilidad.

### 3. Security Scan (`security.yml`)

**Trigger:** push/PR a main/master/develop, semanal

- CodeQL Analysis (Java, JavaScript, Python)
- Dependency Review (falla en severidad alta)

### 4. Release (`release.yml`)

**Trigger:** tags v*

- Build completo
- Changelog automatico
- Publicacion de artefactos (JAR + frontend build)

## Pipeline Local

Para ejecutar el pipeline completo localmente:

```bash
# Backend
cd backend && mvn verify

# Frontend
cd frontend && npm test && npm run build

# Python model
cd tests/python && python -m unittest discover

# Python IA
cd ia && pip install -r requirements.txt && pytest

# Docker
cd .. && docker compose build
```

## Resultados Actuales

- **Backend:** 48 tests, BUILD SUCCESS
- **Frontend:** 39 tests, BUILD SUCCESS
- **Python:** 2 suites, PASS
- **Docker:** 4 servicios, HEALTHY
- **Cobertura (backend):** configurada via Jacoco

## Mejoras Planeadas

1. Coverage threshold ≥ 45% backend
2. Coverage threshold ≥ 70% frontend (via Vitest --coverage)
3. Notificaciones Slack en fallos
4. Deploy automatico a staging
5. Pruebas de integracion con base de datos real
