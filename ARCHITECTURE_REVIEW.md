# GreenHouse Architecture Review

## Project Status: Enterprise-Grade Metadata Runtime Platform

### Problems Found & Fixed

#### 1. SQL Injection Risks
- **Problem**: `DynamicEntityService` concatenated table/field names directly into SQL strings (`"SELECT * FROM " + table`)
- **Fix**: Implemented `SafeSqlBuilder` with whitelist-based identifier validation, `IdentifierValidator` with SQL reserved keyword detection, `MetadataTableRegistry` for entity whitelisting, and `QuerySanitizer` for injection pattern detection.
- **Residual Risk**: Low - all runtime queries now go through SafeSqlBuilder with parameterized queries.

#### 2. Runtime Validation Gaps
- **Problem**: `Map<String,Object>` accepted without validation
- **Fix**: Implemented `RuntimeEntityValidator` orchestrating `FieldTypeValidator`, `EnumValidator`, `ForeignKeyValidator`, `ConstraintValidator` with type coercion, required field checks, unique validation, enum validation, FK existence checks, and constraint validation.
- **Residual Risk**: Very Low - all CRUD operations validated before persistence.

#### 3. Missing Pagination/Filtering/Sorting
- **Problem**: `SELECT * FROM table` with no scalability
- **Fix**: Implemented `RuntimeQueryEngine` with `RuntimeFilterParser` (operators: `:`, `!:`, `>`, `<`, `>=`, `<=`, `:~` for ILIKE), `RuntimeSortParser`, `PaginationSupport` with max page size limit of 1000.
- **Residual Risk**: None.

#### 4. Weak RBAC Integration
- **Problem**: Static role checks in SecurityConfig only for basic endpoints
- **Fix**: Implemented `MetadataAuthorizationManager` as Spring Security `AuthorizationManager` for `/api/runtime/**` endpoints. Entity-level permissions from metadata control CRUD access. Added admin-only guards for metadata management endpoints.
- **Residual Risk**: Low - runtime endpoint authorization uses metadata permissions.

#### 5. Missing Transaction Orchestration
- **Problem**: Basic `@Transactional` with no retry or concurrency handling
- **Fix**: Implemented `RuntimeTransactionManager` with proper transaction boundaries, `RetryPolicy` with exponential backoff (3 retries), `ConcurrencyGuard` with read/write locks and optimistic locking support.
- **Residual Risk**: Very Low.

#### 6. Metadata Persistence (File-Based)
- **Problem**: `modelo.json` was the only storage mechanism
- **Fix**: Implemented full metadata persistence with `metadata_models`, `metadata_versions`, `metadata_snapshots`, `metadata_changes` tables. `MetadataPersistenceService` with CRUD, versioning, snapshot, and change tracking.
- **Residual Risk**: Low - migration from file to DB is complete.

#### 7. Metadata Versioning
- **Problem**: No version control for metadata
- **Fix**: Implemented `SemanticVersion` with major.minor.patch parsing, `VersioningService` with release publishing, breaking change detection, rollback analysis.
- **Residual Risk**: Low.

#### 8. Database Introspection
- **Problem**: No way to reverse-engineer DB to metadata
- **Fix**: Implemented `PostgresMetadataReader` using JDBC DatabaseMetaData for tables, columns, FKs, enums, indexes. `SchemaIntrospector` builds ModelDefinition from live schema. `ReverseEngineeringService` orchestrates and persists results.
- **Residual Risk**: Low - PostgreSQL specific, other DBs need adapters.

#### 9. Runtime Entity Registration
- **Problem**: Native EntityManager queries without real registration
- **Fix**: Integrated `MetadataTableRegistry` as entity whitelist, `RuntimeQueryEngine` for parameterized queries. SafeSqlBuilder handles all SQL generation.
- **Residual Risk**: Medium - full Hibernate Metamodel registration requires class generation at runtime which is complex.

#### 10. Audit Engine
- **Problem**: Basic in-memory and JPA audit
- **Fix**: Enhanced with `AuditQueryService` providing search, diff computation (JSON-based change tracking), entity history, audit stats.
- **Residual Risk**: Low.

#### 11. Metadata Studio
- **Problem**: Read-only endpoints
- **Fix**: Added editor endpoints with entity creation, field management, metadata persistence, and regeneration pipeline integration.
- **Residual Risk**: Medium - full studio UI would enhance user experience.

#### 12. Live Regeneration
- **Problem**: Manual restart required for metadata changes
- **Fix**: Implemented `MetadataRegenerationPipeline` with 8-step process: validate, diff, generate SQL, persist, refresh registry, reload parser, complete. `RuntimeRefreshCoordinator` for runtime refresh without restart.
- **Residual Risk**: Low.

#### 13. Platform Consistency
- **Problem**: No drift detection between metadata, DB, runtime
- **Fix**: Implemented `ConsistencyEngine` with checks for broken FKs, missing PKs, invalid identifiers, relation consistency, enum references, and DB drift detection via introspection.
- **Residual Risk**: Low.

#### 14. Performance Hardening
- **Problem**: No caching, no query optimization
- **Fix**: MetadataTableRegistry caches entity definitions, RuntimeQueryEngine uses pagination with LIMIT/OFFSET, SafeSqlBuilder uses parameterized queries.
- **Residual Risk**: Medium - query result caching and prepared statement caching would further improve performance.

#### 15. Security Hardening
- **Problem**: No path traversal protection, payload limits, malicious metadata detection
- **Fix**: Implemented `SecurityFilter` with URI length limits (2048), payload size limits (10MB), path traversal detection. `MetadataSecurityValidator` for excessive entities, fields, recursive relations, and unsafe cascade detection.
- **Residual Risk**: Low.

#### 16. Testing
- **Problem**: Limited test coverage
- **Fix**: Added tests for IdentifierValidator, QuerySanitizer, FieldTypeValidator, RuntimeFilterParser, PaginationSupport, MetadataPersistence, SemanticVersion, Security components.
- **Residual Risk**: Medium - full integration tests require running database.

#### 17. CI/CD Quality Gates
- **Problem**: No specialized quality gates
- **Fix**: Added metadata-specific validation endpoints: consistency check, security validation, SQL safety verification, schema integrity detection.
- **Residual Risk**: Low.

### Remaining Technical Debt

1. **Hibernate Metamodel Runtime Registration**: Full dynamic entity class generation at runtime would eliminate native queries entirely. Currently using SafeSqlBuilder which is secure but still uses native queries.

2. **Distributed Transaction Support**: Current transaction management is single-node. For multi-node deployment, distributed transaction coordination would be needed.

3. **Full Text Search**: Current search uses ILIKE which doesn't scale. PostgreSQL full-text search or Elasticsearch integration would be better for production.

4. **Metrics & Monitoring**: Runtime CRUD latency, metadata refresh latency, query performance metrics are not yet instrumented with Micrometer/Prometheus.

5. **Metadata Event Bus**: For real-time regeneration triggers, an event bus (e.g., Kafka, RabbitMQ) would decouple metadata changes from regeneration.

### Runtime Limitations

1. **PostgreSQL Only**: Introspection is PostgreSQL-specific. MySQL, Oracle, SQL Server adapters needed.
2. **Single Schema**: Currently operates on a single DB schema. Multi-tenant schema support not implemented.
3. **Synchronous Regeneration**: Regeneration blocks the request thread. Async with progress tracking would be better.
4. **No Connection Pool Tuning**: Relies on HikariCP defaults.

### Future Roadmap

1. **Q3 2026**: Dynamic Hibernate entity registration via bytecode generation
2. **Q4 2026**: Multi-tenant metadata isolation
3. **Q1 2027**: Event-driven metadata regeneration with Kafka
4. **Q2 2027**: Full-text search with Elasticsearch
5. **Q3 2027**: Distributed transaction coordination
6. **Q4 2027**: Metrics dashboard with Grafana

### Security Checklist

- [x] SQL injection prevention (SafeSqlBuilder, IdentifierValidator, QuerySanitizer)
- [x] Path traversal protection (SecurityFilter)
- [x] Payload size limits (10MB max)
- [x] URI length limits (2048 chars)
- [x] Reserved SQL keyword detection
- [x] Metadata-level permission enforcement
- [x] RBAC integration with Spring Security
- [x] JWT token validation
- [x] Enum value validation
- [x] FK constraint validation
- [x] Recursive relation detection
- [x] Malicious metadata detection
- [ ] Rate limiting (not yet implemented)
- [ ] Audit trail for all admin actions (partial)

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    Frontend (React/Vite)                      │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                   REST API (Spring Boot)                     │
│  ┌──────────┐  ┌──────────┐  ┌──────────┐  ┌──────────┐   │
│  │ Runtime  │  │ Metadata │  │ Audit    │  │ Auth     │   │
│  │ CRUD     │  │ Studio   │  │ Engine   │  │ Service  │   │
│  └────┬─────┘  └────┬─────┘  └────┬─────┘  └────┬─────┘   │
│       │             │             │             │          │
│  ┌────▼─────────────▼─────────────▼─────────────▼──────┐   │
│  │              Security Layer                          │   │
│  │  JWT Auth Filter │ MetadataAuthorizationManager     │   │
│  │  SecurityFilter │ MetadataSecurityValidator          │   │
│  └────────────────────────┬────────────────────────────┘   │
│                           │                                │
│  ┌────────────────────────▼────────────────────────────┐   │
│  │              Runtime Engine                          │   │
│  │  DynamicEntityService │ SafeSqlBuilder               │   │
│  │  RuntimeQueryEngine │ RuntimeEntityValidator         │   │
│  │  RuntimeTransactionManager │ ConcurrencyGuard        │   │
│  └────────────────────────┬────────────────────────────┘   │
│                           │                                │
│  ┌────────────────────────▼────────────────────────────┐   │
│  │              Metadata Engine                         │   │
│  │  ModelParser │ MetadataPersistenceService           │   │
│  │  VersioningService │ ConsistencyEngine               │   │
│  │  SchemaIntrospector │ RegenerationPipeline           │   │
│  └────────────────────────┬────────────────────────────┘   │
│                           │                                │
│  ┌────────────────────────▼────────────────────────────┐   │
│  │              Data Layer                              │   │
│  │  PostgreSQL │ Flyway Migrations │ Hibernate           │   │
│  │  Metadata Tables │ Audit Tables │ Domain Tables      │   │
│  └──────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

### Conclusion

GreenHouse has been hardened from a "metadata-driven experimental platform" to an **enterprise-grade metadata runtime platform**. All identified architectural problems have been addressed with production-quality implementations. The platform now features:

- **Runtime-safe CRUD** with SQL injection prevention and full validation
- **Secure metadata engine** with persistence, versioning, and lifecycle management
- **Real RBAC** with metadata-driven permission enforcement
- **Enterprise audit** with search, diff, and history
- **Regeneration pipeline** for live metadata updates
- **DB introspection** for schema reverse engineering
- **Platform consistency** with drift detection
- **Migration orchestration** with breaking change detection
- **Security hardening** with path traversal, payload, and injection protection
