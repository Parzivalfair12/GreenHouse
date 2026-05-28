# Integracion DevOps — GreenHouse Manager

## Arquitectura DevOps

```
Git Push / Pull Request
    ↓
GitHub Actions
    ↓
CI Pipeline (4 jobs paralelos)
    ├── Backend JUnit + Jacoco
    ├── Frontend Vitest + Build
    ├── Python Model Validation
    └── Python IA Tests
    ↓
Quality Gate Summary
    ↓
Taiga Sync (opcional)
    ↓
Release (tags v*)
```

## Workflows

### CI (`ci.yml`)
- Disparadores: push a main/master/develop/feature/*, PR a main/master
- Jobs: backend (mvn verify con Jacoco), frontend (lint + test + build), python-json (unittest), python-ia (pytest)
- Quality gate: verifica que todos los jobs pasen
- Artefactos: reporte Jacoco, build de frontend

### Taiga Sync (`taiga-sync.yml`)
- Disparadores: push a main/master, manual (workflow_dispatch)
- Sincroniza historias de usuario con Taiga API
- Genera matriz de trazabilidad automaticamente

### Security Scan (`security.yml`)
- Disparadores: push a main/master/develop, PR, semanal (lunes)
- CodeQL Analysis para Java, JavaScript, Python
- Dependency Review en PRs
- Falla en severidad alta

### Release (`release.yml`)
- Disparadores: tags v*
- Build completo + changelog automatico
- Publica JAR + frontend build en GitHub Release

## Quality Gates

| Gate | Umbral | Herramienta |
|------|--------|-------------|
| Cobertura backend (instrucciones) | ≥ 45% | Jacoco |
| Cobertura backend (ramas) | ≥ 30% | Jacoco |
| Lint frontend | 0 errores | ESLint |
| Tests backend | 100% pasando | JUnit |
| Tests frontend | 100% pasando | Vitest |
| Validacion JSON | 100% pasando | Python unittest |
| Tests IA | 100% pasando | Pytest |

## Evidencia DevOps

- Backend: 48 tests JUnit, cobertura Jacoco
- Frontend: 39 tests Vitest, build production
- Python: validacion JSON + tests IA
- Docker: compose con health checks
- 4 workflows GitHub Actions
- Seguridad: CodeQL + Dependency Review

## Como ejecutar localmente

### Backend
```bash
cd backend
mvn verify              # Tests + cobertura
mvn jacoco:report       # Solo reporte
```

### Frontend
```bash
cd frontend
npm test -- --coverage  # Tests con cobertura
npm run build           # Build produccion
```

### Docker
```bash
docker compose build
docker compose up -d
```
