# GreenHouse — Hardening & Integration Audit

## Critical Issues Found

### 1. Generated Code Not Compiled Into Runtime
**Severity: CRITICAL**  
**Files:** `generation/JavaGeneratorEngine.java`  
The `JavaGeneratorEngine` writes `.java` files to `generated/` directory, but Spring Boot does not scan or compile them. The generated code is **dead on disk** — never loaded into the JVM.

**Fix needed:** Dynamic compilation + Spring bean registration OR direct runtime endpoints.

### 2. Audit Engine Is In-Memory Only
**Severity: HIGH**  
**File:** `audit/AuditTrailService.java`  
`CopyOnWriteArrayList<AuditEntry>` stores everything in heap memory. Lost on restart. No persistence, no querying, no pagination.

**Fix needed:** JPA entities for audit_log and audit_snapshot tables.

### 3. Permission Evaluator Not Wired Into Spring Security
**Severity: HIGH**  
**File:** `auth/MetadataPermissionEvaluator.java`  
The evaluator exists but is never called from any security filter, interceptor, or guard. It's an unused class.

**Fix needed:** WebSecurityConfig integration + method security + route filtering.

### 4. MigrationPlanner Has No History Tracking
**Severity: MEDIUM**  
**File:** `migration/MigrationPlanner.java`  
Migrations are generated but never tracked. No `migration_history` table, no applied migrations state.

**Fix needed:** Migration tracking entity + history viewer.

### 5. MetadataStudio Is Read-Only
**Severity: MEDIUM**  
**File:** `MetadataStudio.jsx`  
The studio shows the model but cannot edit it. No entity create/edit, no field editor, no relationship editor.

**Fix needed:** Create entity/field endpoints + frontend editor forms.

### 6. Duplicate Frontend Model Loading
**Severity: MEDIUM**  
**File:** `modelParser.js`, `modelSync.js`  
Two separate mechanisms load the model: `loadModel()` async from API and `getCachedModel()` from localStorage/bundle. Race conditions possible.

**Fix needed:** Unified loading strategy with proper cache invalidation.

### 7. No Consistency Between Static and Async Paths
**Severity: MEDIUM**  
Some components use sync `modelParser` functions (e.g., `ERDViewer`), others use async `loadModel()`. Mixed patterns cause confusion.

**Fix needed:** All components should use a consistent loading pattern.

### 8. SQL Injection Risk Mitigated But Not Eliminated
**Severity: LOW**  
**File:** `SqlGenerator.java`  
`quoteIdent()` correctly escapes double quotes, but generated SQL is never validated before being saved to disk.

**Fix needed:** Pre-validation of generated SQL.

---

## Action Plan

| Priority | Issue | Fix |
|----------|-------|-----|
| CRITICAL | 1. Generated code not in runtime | Dynamic CRUD controller at runtime |
| HIGH | 2. Audit in-memory | JPA audit entities + tables |
| HIGH | 3. Permissions not wired | Spring Security integration |
| MEDIUM | 4. Migration no history | Migration tracking entity |
| MEDIUM | 5. Studio read-only | Entity editor endpoints |
| MEDIUM | 6. Duplicate loading | Unified model loader |
| MEDIUM | 7. Sync/async mismatch | Standardize on sync+refresh |
| LOW | 8. SQL validation | Pre-validation step |
