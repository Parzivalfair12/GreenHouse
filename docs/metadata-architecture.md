# Metadata-Driven Architecture — GreenHouse

## Overview

GreenHouse uses a **metadata-driven architecture** where `modelo.json` is the single source of truth for the entire system. This file drives:

- Frontend ERD visualizations
- Data Dictionary
- Backend metadata API
- SQL schema generation
- Java code generation
- Model validation
- Documentation

## Architecture Flow

```
modelo.json
    │
    ├── Frontend (Vite/React)
    │     ├── modelParser.js → ERD Viewer, Data Dictionary
    │     ├── javaGenerator.js → Code Generator UI
    │     └── Metadata Pages → Dashboard, Validation
    │
    └── Backend (Spring Boot)
          ├── ModelParser.java → Loads JSON
          ├── ModelValidator.java → Enterprise validation
          ├── MetadataService.java → Business logic
          ├── MetadataController.java → REST API
          ├── SqlGenerator.java → Schema generation
          └── JavaGenerator.java → Code generation
```

## File Locations

| File | Purpose |
|------|---------|
| `backend/src/main/resources/modelo.json` | Backend copy (single source of truth) |
| `frontend/src/config/modelo.json` | Frontend copy (synced) |
| `backend/src/main/java/.../metadata/` | Backend metadata module |
| `frontend/src/pages/ERD/` | ERD Viewer + Data Dictionary |
| `frontend/src/pages/Metadata/` | Metadata Dashboard + Validation + Generator |

## Backend Module Structure

```
metadata/
├── config/MetadataConfig.java      — Loads modelo.json at startup
├── parser/ModelParser.java         — Parses JSON into typed records
├── dto/
│   ├── ModelDefinition.java        — Top-level model
│   ├── EntityDefinition.java       — Entity with audit, indexes, permissions
│   ├── FieldDefinition.java        — Field with PK, FK, enum, validation
│   ├── RelationshipDefinition.java — Relationship with cardinality
│   ├── EnumDefinition.java         — Enum with values
│   └── ValidationResult.java       — Validation issues + summary
├── validator/ModelValidator.java   — Enterprise validator (12+ checks)
├── service/MetadataService.java    — Business logic layer
├── controller/MetadataController.java — REST API (14 endpoints)
└── generator/
    ├── SqlGenerator.java           — PostgreSQL schema generator
    └── JavaGenerator.java          — Entity/DTO/Repo/Service/Controller generator
```

## REST API

| Endpoint | Description |
|----------|-------------|
| `GET /api/metadata/model` | Full model definition |
| `GET /api/metadata/entities` | All entities |
| `GET /api/metadata/entities/{name}` | Single entity |
| `GET /api/metadata/relationships` | All relationships |
| `GET /api/metadata/enums` | All enums |
| `GET /api/metadata/validate` | Validate model |
| `GET /api/metadata/sql` | Full SQL schema |
| `GET /api/metadata/sql/{entity}` | SQL for one entity |
| `GET /api/metadata/generate/entities` | Generated Java entities |
| `GET /api/metadata/generate/dtos` | Generated Java DTOs |
| `GET /api/metadata/generate/repositories` | Generated repositories |
| `GET /api/metadata/generate/services` | Generated services |
| `GET /api/metadata/generate/controllers` | Generated controllers |
| `GET /api/metadata/generate/all` | All generated Java |

## Model Validator Checks

| Code | Description |
|------|-------------|
| DUPLICATE_ENTITY | Duplicate entity name |
| DUPLICATE_TABLE | Duplicate table name |
| DUPLICATE_FIELD | Duplicate field in entity |
| NULLABLE_PK | PK cannot be nullable |
| FK_NO_REFERENCE | FK missing reference definition |
| FK_BROKEN_REFERENCE | FK references unknown entity |
| INVALID_CARDINALITY | Invalid cardinality format |
| INVALID_REL_TYPE | Invalid relationship type |
| ENUM_NOT_FOUND | Enum type not found |
| FIELD_NO_TYPE | Field missing type |
| RESERVED_TABLE_NAME | Table name is SQL reserved word |
| RESERVED_FIELD_NAME | Field name is SQL reserved word |
| EMPTY_ENUM | Enum has no values |
| CYCLE_DETECTED | Potential relationship cycle |
| NO_PK | Entity has no primary key |
| DUPLICATE_ENUM | Duplicate enum name |

## Frontend Routes

| Route | Page |
|-------|------|
| `/erd` | ERD Viewer (React Flow) |
| `/erd/dictionary` | Data Dictionary |
| `/metadata` | Metadata Dashboard |
| `/metadata/validation` | Model Validation |
| `/metadata/generator` | Code Generator (SQL + Java) |

## Extending the Model

To add a new entity, simply add it to `modelo.json`:

```json
{
  "name": "NewEntity",
  "table": "new_entity",
  "description": "...",
  "audit": true,
  "softDelete": false,
  "timestamps": true,
  "indexes": [],
  "permissions": ["ADMIN:CREATE", "VIEWER:READ"],
  "fields": [
    { "name": "id", "type": "Long", "pk": true, "nullable": false },
    { "name": "name", "type": "String", "nullable": false, "validation": "@NotBlank" }
  ]
}
```

The entity automatically appears in:
- ERD Viewer
- Data Dictionary
- Metadata Dashboard
- Validation
- SQL generation
- Java code generation
