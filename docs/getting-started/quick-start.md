# Quick Start

## Viewer Mode

Render JSON as a read-only, syntax-highlighted, foldable tree:

```kotlin
@Composable
fun JsonViewerScreen() {
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

Enable inline editing with real-time validation:

```kotlin
@Composable
fun JsonEditorScreen() {
    val state = rememberJsonEditorState(
        initialJson = """{"name": "John", "age": 30}""",
        isEditing = true,
    )

    JsonCMP(
        modifier = Modifier.fillMaxSize(),
        state = state,
        onJsonChange = { json, parsed, error ->
            if (error != null) {
                println("Parse error: ${error.message}")
            }
        },
    )
}
```

## Search Highlighting

Pass a search query to highlight matching text:

```kotlin
JsonCMP(
    state = state,
    searchQuery = "John",
)
```

## Programmatic Control

Use `JsonEditorState` to control the component:

```kotlin
val state = rememberJsonEditorState(initialJson = myJson)

// Format
state.format(compact = false) // pretty-print
state.format(compact = true)  // minify

// Sort
state.sortKeys(ascending = true)

// Folding
state.collapseAll()
state.expandAll()

// Update content
state.updateRawJson(newJson)

// Toggle mode
state.isEditing = true
```
