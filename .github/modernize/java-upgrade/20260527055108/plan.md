# Upgrade Plan: GreenHouse (20260527055108)

- **Generated**: 2026-05-27 05:51:08
- **HEAD Branch**: unknown
- **HEAD Commit ID**: unknown

## Available Tools

**JDKs**
- Java 22.0.2: C:\Users\Daniel Calderon\.jdks\openjdk-22.0.2\bin
- Java 26.0.1: C:\Program Files\Java\jdk-26.0.1\bin
- Java 21: **<TO_BE_INSTALLED>** (required by step 1 and final validation)

**Build Tools**
- Maven 3.9.16: C:\Users\Daniel Calderon\Downloads\apache-maven-3.9.16-bin\apache-maven-3.9.16\bin
- Maven Wrapper: not present in repository

## Guidelines

> Note: You can add any specific guidelines or constraints for the upgrade process here if needed, bullet points are preferred.

## Options

- Working branch: appmod/java-upgrade-20260527055108
- Run tests before and after the upgrade: true

## Upgrade Goals

- Upgrade the backend Java project to the latest LTS Java runtime (Java 21)

## Technology Stack

| Technology/Dependency | Current | Min Compatible | Why Incompatible |
| --------------------- | ------- | -------------- | ---------------- |
| Java | 21 | 21 | User requested latest LTS runtime |
| Spring Boot | 3.3.5 | 3.3.5 | Already compatible with Java 21 |
| Maven | 3.9.16 | 3.9.16 | Compatible with Java 21 |
| springdoc-openapi | 2.6.0 | 2.6.0 | Compatible with current Spring Boot version |

## Derived Upgrades

- No dependency upgrades are required for the Java 21 target because the backend already targets Java 21 and uses Spring Boot 3.3.5, which is already Java 21 compatible.

## Impact Analysis

### Dependency Changes

| File | Dependency | Current | Action | Target | Reason |
|------|------------|---------|--------|--------|--------|
| backend/pom.xml | `<java.version>` | 21 | none | 21 | Project already targets latest LTS Java |

### Source Code Changes

| File | Location | Current | Required Change | Reason |
|------|----------|---------|----------------|--------|
| None | N/A | N/A | N/A | No source changes required for the requested Java runtime upgrade |

### Configuration Changes

| File | Property/Setting | Current | Required Change | Reason |
|------|------------------|---------|----------------|--------|
| None | N/A | N/A | N/A | No configuration changes required for Java 21 runtime upgrade |

### CI/CD Changes

| File | Location | Current | Required Change |
|------|----------|---------|-----------------|
| None | N/A | N/A | N/A |

### Risks & Warnings

- The project currently targets Java 21 but the host system does not have Java 21 installed. I will install JDK 21 during execution to validate the actual target runtime.
- HEAD branch and commit information could not be recovered from the version control tool output, so the plan uses `unknown` for those values.

## Upgrade Steps

- Step 1: Setup Environment
  - **Rationale**: Ensure the actual Java 21 runtime is available for baseline verification and final validation.
  - **Changes to Make**: Install JDK 21, verify Maven 3.9.16 availability, no pom changes.
  - **Verification**: `java -version && mvn -version` with JDK 21 available

- Step 2: Baseline Verification
  - **Rationale**: Confirm the current project configuration compiles and tests successfully on Java 21 before any changes.
  - **Changes to Make**: None to source or pom; run baseline compile and tests.
  - **Verification**: `mvn clean test-compile -q && mvn clean test -q` using JDK 21

- Step 3: Final Validation
  - **Rationale**: Confirm the upgrade goal is already satisfied and ensure all tests pass with the actual Java 21 runtime.
  - **Changes to Make**: None; verify current configuration and runtime.
  - **Verification**: `mvn clean test -q` using JDK 21
