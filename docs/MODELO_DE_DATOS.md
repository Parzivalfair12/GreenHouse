# GreenHouse Manager - Modelo de Datos

## Resumen

Sistema academico de gestion de invernaderos con modelo de datos relacional de **11 entidades** y **8 enums**, implementado en Spring Boot + PostgreSQL.

## Estructura del Modelo

El modelo de datos esta definido en:
- `docs/modelo.json` — Fuente de verdad maestra
- `backend/src/main/resources/modelo.json` — Copia sincronizada para el backend
- `frontend/src/config/modelo.json` — Copia sincronizada para el frontend

## Entidades

| # | Entidad | Tabla | Descripcion | Relaciones |
|---|---------|-------|-------------|------------|
| 1 | Greenhouse | greenhouse | Invernadero principal | OneToMany → Crop, Sensor, Zone, Actuator, AutomationRule, IrrigationEvent |
| 2 | Crop | crop | Cultivo plantado | ManyToOne → Greenhouse |
| 3 | Sensor | sensor | Dispositivo de medicion | ManyToOne → Greenhouse, Zone; OneToMany → Reading, Alert |
| 4 | Reading | reading | Lectura de sensor | ManyToOne → Sensor |
| 5 | IrrigationEvent | irrigation_event | Evento de riego | ManyToOne → Greenhouse |
| 6 | Alert | alert | Alerta operativa | ManyToOne → Sensor |
| 7 | Actuator | actuator | Actuador simulado | ManyToOne → Greenhouse |
| 8 | AutomationRule | automation_rule | Regla de automatizacion | ManyToOne → Greenhouse |
| 9 | Zone | zone | Zona funcional | ManyToOne → Greenhouse; OneToMany → Sensor |
| 10 | AuditLog | audit_log | Log de auditoria | Sin relaciones |
| 11 | AppUser | app_user | Usuario del sistema | Sin relaciones |

## Enums

| Enum | Valores | Uso |
|------|---------|-----|
| ActionOrigin | MANUAL, AUTOMATIC | Origen de acciones en AuditLog |
| ActuatorType | IRRIGATION, FAN, HEATER, LIGHT | Tipo de actuador |
| AlertSeverity | INFO, WARNING, CRITICAL | Severidad de alertas |
| CropStatus | GERMINATING, GROWING, HARVESTED, LOST | Estado del cultivo |
| IrrigationMode | MANUAL, AUTOMATIC | Modo de riego |
| RuleType | LOW_HUMIDITY_IRRIGATION | Tipo de regla |
| SensorType | TEMPERATURE, HUMIDITY, SOIL_MOISTURE, LIGHT | Tipo de sensor |
| UserRole | ADMIN, OPERATOR, VIEWER | Rol de usuario |

## Diagrama Entidad-Relacion

Ver `docs/entidad-relacion.md` para el diagrama Mermaid completo con las 11 entidades.

## Diccionario de Datos

Ver `docs/diccionario-datos.md` para el detalle completo de cada tabla, columna, tipo, restriccion y descripcion.

## Base de Datos PostgreSQL

Las tablas se crean via Flyway migrations:
- `V1__initial_schema.sql` — 11 tablas principales + constraints FK
- `V2__add_verified_column.sql` — Columna verified en app_user

## JPA / Hibernate

Las entidades JPA estan en `backend/src/main/java/com/example/greenhouse/domain/`:
- 11 clases @Entity con relaciones JPA
- 8 enums @Enumerated(EnumType.STRING)
- Validaciones con Jakarta Validation (@NotBlank, @Positive, @Email)

## Consistencia

| Capa | 11 entidades | 8 enums | FK correctas |
|------|-------------|---------|--------------|
| modelo.json | ✅ | ✅ | ✅ |
| JPA Entities | ✅ | ✅ | ✅ |
| Flyway SQL | ✅ | ✅ | ✅ |
| ERD Viewer | ✅ | ✅ | ✅ |
| Data Dictionary | ✅ | ✅ | ✅ |

## Frontend

- **ERD Viewer**: Diagrama interactivo con React Flow (`pages/ERD/`)
- **Data Dictionary**: Diccionario visual completo (`components/DataDictionarySection.jsx`)
- **Export SQL**: Generacion de schema SQL desde modelo.json

## Notas Academicas

- El modelo JSON es la unica fuente de verdad.
- El ERD se genera dinamicamente desde el modelo.
- El diccionario de datos se deriva del modelo.
- Las migraciones Flyway reflejan el modelo.
- Las entidades JPA reflejan el modelo.
- Todo esta sincronizado y consistente.
