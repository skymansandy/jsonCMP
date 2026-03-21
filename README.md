# JsonCMP

<p align="center">
  <img src="docs/icon.svg" width="128" height="128" alt="JsonCMP Icon"/>
</p>

[![Build](https://github.com/skymansandy/jsonCMP/actions/workflows/deploy.yml/badge.svg)](https://github.com/skymansandy/jsonCMP/actions/workflows/deploy.yml) [![Coverage](https://img.shields.io/badge/coverage-100%25-brightgreen)](https://github.com/skymansandy/jsonCMP/actions/workflows/deploy.yml) [![Maven Central](https://img.shields.io/badge/maven--central-1.0.0--alpha1-blue)](https://central.sonatype.com/artifact/dev.skymansandy/json-cmp)

Kotlin Multiplatform Compose JSON viewer and editor component for Android, iOS, and JVM Desktop.

## Features

- **JSON Viewer** — Syntax-highlighted, foldable JSON tree with line numbers
- **JSON Editor** — Editable JSON with real-time validation, formatting, and sorting
- **Search** — Highlight matching text across the JSON document
- **Multiple Themes** — Dark, Light, Monokai, Dracula, Solarized Dark
- **KMP** — Android, iOS, and JVM Desktop support via Compose Multiplatform

## Installation

Add `mavenCentral()` to your repositories in `settings.gradle.kts`:

```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
    }
}
```

Then add the dependency:

```kotlin
// build.gradle.kts
dependencies {
    implementation("dev.skymansandy:json-cmp:1.0.0-alpha1")
}
```

## Quick Start

```kotlin
@Composable
fun MyScreen() {
    val state = rememberJsonEditorState(
        initialJson = """{"name": "John", "age": 30}""",
    )

    JsonCMP(
        modifier = Modifier.fillMaxSize(),
        state = state,
    )
}
```

## Editor Mode

```kotlin
@Composable
fun MyEditor() {
    val state = rememberJsonEditorState(
        initialJson = """{"name": "John", "age": 30}""",
        isEditing = true,
    )

    JsonCMP(
        modifier = Modifier.fillMaxSize(),
        state = state,
        onJsonChange = { json, parsed, error ->
            // React to JSON changes
        },
    )
}
```

## Themes

```kotlin
JsonCMP(
    state = state,
    colors = JsonCmpColors.Monokai, // Dark, Light, Monokai, Dracula, SolarizedDark
)
```

## API

### JsonCMP

```kotlin
@Composable
fun JsonCMP(
    modifier: Modifier = Modifier,
    state: JsonEditorState,
    searchQuery: String = "",
    colors: JsonCmpColors = JsonCmpColors.Dark,
    onJsonChange: (json: String, parsed: JsonNode?, error: JsonError?) -> Unit = { _, _, _ -> },
)
```

### JsonEditorState

```kotlin
val state = rememberJsonEditorState(
    initialJson = "...",
    isEditing = false,
)

state.updateRawJson(newJson)     // Update JSON content
state.format(compact = false)    // Pretty-print or minify
state.sortKeys(ascending = true) // Sort object keys
state.collapseAll()              // Collapse all foldable nodes
state.expandAll()                // Expand all nodes
state.isEditing = true           // Toggle editor mode
```
