# Metadata Lifecycle — GreenHouse

## Overview

The model metadata follows a defined lifecycle from creation to consumption across the full stack.

## Lifecycle Diagram

```
modelo.json (source of truth)
    │
    ├── 1. LOAD ──→ ModelParser.java
    │                  │
    │                  ├── Thread-safe AtomicReference cache
    │                  ├── ReentrantReadWriteLock for concurrent access
    │                  └── Immutable records (List.copyOf, unmodifiable)
    │
    ├── 2. VALIDATE ──→ ModelValidator.java
    │                       │
    │                       ├── 16+ validation checks
    │                       ├── Duplicates, FKs, enums, cycles
    │                       └── Reserved SQL keywords
    │
    ├── 3. SERVE ──→ MetadataController.java (REST API)
    │                   │
    │                   ├── GET /api/metadata/model
    │                   ├── GET /api/metadata/validate
    │                   ├── GET /api/metadata/checksum
    │                   └── POST /api/metadata/reload
    │
    ├── 4. GENERATE SQL ──→ SqlGenerator.java
    │                          │
    │                          ├── Quoted identifiers (SQL injection safe)
    │                          ├── PostgreSQL-compatible
    │                          ├── FK constraints, indexes, timestamps
    │                          └── Soft delete support
    │
    ├── 5. GENERATE FLYWAY ──→ FlywayGenerator.java
    │                             │
    │                             ├── V1 initial schema
    │                             ├── Incremental diff migrations
    │                             └── Detect schema changes
    │
    ├── 6. GENERATE JAVA ──→ JavaGenerator.java
    │                           │
    │                           ├── @Entity, @Table, @Column
    │                           ├── @ManyToOne, @OneToMany
    │                           ├── @Enumerated, @JoinColumn
    │                           ├── Repository, Service, Controller
    │                           └── Request/Response DTOs
    │
    ├── 7. DIFF ──→ DiffEngine.java
    │                 │
    │                 ├── Compare modelo_actual vs modelo_anterior
    │                 ├── Breaking change detection
    │                 ├── Field type changes
    │                 └── Entity add/remove detection
    │
    ├── 8. CONSUME (Frontend) ──→ modelParser.js
    │                               │
    │                               ├── ERD Viewer (React Flow)
    │                               ├── Data Dictionary
    │                               ├── Metadata Dashboard
    │                               ├── Validation Page
    │                               └── Code Generator UI
    │
    └── 9. SYNC CHECK ──→ modelSync.js
                            │
                            ├── SHA-256 checksum comparison
                            ├── Frontend vs Backend version check
                            └── Warning on mismatch
```

## Hot Reload Flow

```
File change detected (ModelWatcher @Scheduled 5s)
    │
    ├── parser.reload()
    │       │
    │       ├── Acquires write lock
    │       ├── Reads modelo.json from disk
    │       ├── Parses with Jackson
    │       ├── Creates immutable copy
    │       ├── Updates AtomicReference
    │       └── Releases write lock
    │
    └── Next API call sees new model immediately
```

## Thread Safety Architecture

```
┌─────────────────────────────────────────┐
│           ModelParser                    │
│  ┌─────────────────────────────────┐    │
│  │  AtomicReference<ModelDef>      │ ←──│── Concurrent reads/writes
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │  ReentrantReadWriteLock         │ ←──│── Reload coordination
│  └─────────────────────────────────┘    │
│  ┌─────────────────────────────────┐    │
│  │  Immutable records (Java 21)    │ ←──│── No shared mutable state
│  └─────────────────────────────────┘    │
└─────────────────────────────────────────┘
```

## Security Measures

| Risk | Mitigation |
|------|------------|
| SQL injection | All identifiers quoted with `"name"` escaping |
| Path traversal | Entity names validated before use |
| JSON injection | Jackson strict parsing |
| Stack overflow | Cycle detection in validator |
| ReDoS | Simple regex patterns only |
| Cache poisoning | AtomicReference + immutable records |
| Race conditions | ReadWriteLock + AtomicReference |
