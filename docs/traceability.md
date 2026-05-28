# Trazabilidad del Proyecto

## Matriz de Trazabilidad

La matriz relaciona cada funcionalidad implementada con su historia de usuario,
endpoints, componentes frontend y tests asociados.

| Funcionalidad | Historia de Usuario | Endpoints | Frontend | Tests |
|---------------|--------------------|-----------|----------|-------|
| Autenticacion (JWT + OAuth2) | US#1 | `/api/auth/*` | LoginScreen.jsx | AuthServiceTest, SecurityConfigTest |
| CRUD Invernaderos | US#2 | `/api/greenhouses/*` | GreenhousesSection.jsx, GreenhouseTable.jsx | GreenhouseControllerTest |
| Gestion de Alertas | US#3 | `/api/alerts/*` | AlertsSection.jsx | AlertServiceTest |
| Simulacion IoT | US#4 | `/api/simulator/*` | DashboardSection.jsx | SimulatorControllerTest |
| Prediccion IA | US#5 | `/api/ia/*` | IaSection.jsx | IaControllerTest |
| Control de Acceso | US#6 | (global via @PreAuthorize) | LoginScreen.jsx | SecurityConfigTest |
| Internacionalizacion | US#7 | (Accept-Language header) | i18n.js, App.jsx | I18nIntegrationTest, i18n.test.jsx |
| Dashboard | US#8 | `/api/simulator/status`, `/api/devops/*` | DashboardSection.jsx, DevOpsSection.jsx | — |

## Convencion de Commits

Formato: `tipo(alcance): US#N mensaje descriptivo`

### Tipos
- `feat`: Nueva funcionalidad
- `fix`: Correccion de bug
- `docs`: Documentacion
- `test`: Tests
- `refactor`: Refactorizacion
- `ci`: CI/CD
- `style`: Formato, estilos
- `chore`: Mantenimiento

### Ejemplos
```
feat(auth): US#1 login con Google OAuth2
fix(alerts): US#3 correccion en resolucion de alertas
docs(api): US#7 swagger endpoints internacionalizados
ci(devops): implementar workflows CI/CD enterprise
test(platform): US#2 tests de integracion invernaderos
```

## Historias de Usuario

Ver `docs/taiga-historias.md` y `docs/taiga-integration.md` para la lista completa.

Las historias se generan automaticamente desde el codigo via
`StoryGenerationService` y se sincronizan con Taiga via `TaigaService`.

## Estados de Historia

- `NEW`: Creada, no iniciada
- `IN_PROGRESS`: En desarrollo
- `READY_FOR_TEST`: Lista para QA
- `DONE`: Completada y verificada
