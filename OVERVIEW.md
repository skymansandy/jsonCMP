# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

JsonCMP is a Kotlin Multiplatform (KMP) library providing JSON viewer and editor composables for Android, iOS, and JVM Desktop. Published to Maven Central as `dev.skymansandy:json-cmp` (currently 1.0.0-RC2). All public APIs are marked `@ExperimentalJsonCmpApi`.

## Build Commands

```bash
# Build
./gradlew build                                    # Full build
./gradlew :json-cmp:jvmTest                        # Run JVM tests
./gradlew :json-cmp:iosSimulatorArm64Test           # Run iOS simulator tests
./gradlew :json-cmp:jvmTest --tests "fully.qualified.TestClass"  # Single test class

# Code Quality
./gradlew detektAll                                 # Detekt static analysis (strict: 0 issues allowed)
./gradlew apiCheck                                  # Binary compatibility validation

# Coverage
./gradlew :json-cmp:jvmTest koverXmlReport          # JVM tests + coverage report

# Sample App
./gradlew :androidApp:assembleRelease               # Build sample Android APK

# Publishing (requires credentials via env vars)
./gradlew publishAndReleaseToMavenCentral --no-configuration-cache

# Documentation
mkdocs build                                        # Build docs site (requires: pip install mkdocs-material)
```

## Architecture

**Modules:**
- `json-cmp` — Core KMP library (the published artifact)
- `composeApp` — Multiplatform Compose sample app (Android/iOS/Desktop)
- `androidApp` — Native Android sample app

**Library structure (`json-cmp/src/commonMain`):**
- `domain/` — Core logic: JSON parser, serializer, syntax highlighter, Redux-like state store (`JsonHolderImpl` dispatching `JsonAction`), line-based virtualization, `JsonNode` tree model
- `ui/viewer/` — `JsonViewerCMP` composable + `JsonViewerState` (read-only, virtualized)
- `ui/editor/` — `JsonEditorCMP` composable + `JsonEditorState` (editable, with validation)
- `ui/common/` — Shared UI components (highlighter, gutter, line view)
- `theme/` — Built-in themes (Dark, Light, Monokai, Dracula, Solarized Dark) + `JsonTheme.Custom`

**Key patterns:**
- Observable state holders (`@Stable`, `mutableStateOf`, Flow) — no callback-based API
- Virtualized rendering via line calculation for large JSON
- `rememberJsonViewerState` / `rememberJsonEditorState` use Compose retain API for lifecycle

**Platform targets:** Android (minSdk 24, compileSdk 36), iOS (Arm64 + Simulator), JVM

## Code Quality

- **Detekt** with auto-correct, strict mode (maxIssues: 0), baseline in `detekt-baseline.xml`
- `@Composable` functions are exempt from LongMethod and LongParameterList rules
- **Binary compatibility validator** tracks API surface at `json-cmp/api/jvm/json-cmp.api`
- Kotlin code style: `official`

## CI Pipeline (PR to main)

1. API compatibility check (`apiCheck`)
2. Detekt code quality
3. JVM tests
4. iOS simulator tests

## Key Versions

Managed in `gradle/libs.versions.toml`: Kotlin 2.3.10, Compose Multiplatform 1.10.2, AGP 9.1.0, Gradle 9.3.1
