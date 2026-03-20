# JsonCMP

Kotlin Multiplatform Compose JSON viewer and editor component for Android, iOS, and JVM Desktop.

## Features

- **JSON Viewer** — Syntax-highlighted, foldable JSON tree with line numbers
- **JSON Editor** — Editable JSON with real-time validation, formatting, and sorting
- **Search** — Highlight matching text across the JSON document
- **Multiple Themes** — Dark, Light, Monokai, Dracula, Solarized Dark
- **KMP** — Android, iOS, and JVM Desktop support via Compose Multiplatform

## Installation

```kotlin
// build.gradle.kts
dependencies {
    implementation("dev.skymansandy:json-cmp:<version>")
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
