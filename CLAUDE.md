# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
mvn clean package                          # Build all modules
mvn clean install                          # Build + install to local repo
mvn -pl validator-core test                # Test a single module
mvn -pl validator-core -Dtest=Testing test # Run a single test class
mvn -pl validator-core -Dtest=Testing#simpleOk test  # Run a single test method
make package                               # Makefile shortcut for clean package
```

Java 17+ required. CI tests on Java 17, 21, 25.

## Project Structure

Five Maven modules with linear dependency chain:

```
validator-api    → Public API: interfaces, JAXB-generated model (from vefa-validator.xsd)
validator-core   → Validation engine, Guice-based plugin system, all declarations/checkers
validator-tester → CLI utilities for testing validation artifacts
validator-build  → Tools for building/compiling validation artifacts
validator-dist   → Distribution packaging, Docker image
```

Each module depends on all modules above it.

## Architecture

**Guice Plugin System** (`validator-core/.../module/ValidatorModule.java`): Multibinder-based registration of pluggable components:
- `IDeclaration` — document type detectors (UBL, SBDH, ESPD, ASiC-E, etc.)
- `ICheckerFactory` — validation rule executors (XSD, Schematron, Schematron-XSLT)
- `ITrigger` — nested/secondary validation (e.g. ASiC-E container contents)
- `IConfigurationProvider` — configuration suppliers

**Validation Flow**:
1. `ValidatorBuilder.newValidator().build()` → creates `Validator` (expensive, singleton, thread-safe)
2. `validator.validate(path)` → `ValidationInstance` reads 50KB sample, tries each `IDeclaration.verify()`
3. Matched declaration runs `detect()` to find standardId/profileId
4. Loads matching `Configuration` (XML-based, supports inheritance via `config.xml`)
5. Runs checkers (XSD, Schematron) and triggers (nested validation)
6. Returns `IValidation` with `Report` containing sections and assertions

**Result Model** (JAXB from `validator-api/src/main/xsd/vefa-validator.xsd`):
`Report` → `Section[]` → `Assertion[]`, each with a `FlagType` (OK, WARNING, ERROR, FATAL, UNKNOWN).

**Validation Artifacts** are external (not in this repo). Loaded via `IArtifactsSource`:
- `RepositorySource` — HTTP (production: `https://anskaffelser.dev/repo/validator/current/`)
- `DirectorySource` — local filesystem
- `ClasspathSource` — test resources

## Key Conventions

- License: MPL 2.0 (not Apache 2.0 — this project has its own license header)
- Group ID: `com.helger.vefa` (forked from `no.difi.vefa`)
- Test framework: JUnit 4 (not JUnit 5)
- DI framework: Google Guice 7
- XML processing: Saxon 12.9, ph-schematron
- Utilities: ph-commons (`ICommonsList`, `CommonsArrayList`, etc.)
