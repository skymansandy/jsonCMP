# Quick Start

## JSON Viewer

Render JSON as a read-only, syntax-highlighted, foldable tree:

```kotlin
@OptIn(ExperimentalJsonCmpApi::class)
@Composable
fun JsonViewerScreen() {
    val state = rememberJsonViewerState(
        json = """{"name": "John", "age": 30}""",
    )

    JsonViewerCMP(
        modifier = Modifier.fillMaxSize(),
        state = state,
    )
}
```

The viewer responds to changes in the `json` parameter — passing a new value triggers a re-parse and updates the display.

## JSON Editor

Edit JSON with real-time validation, formatting, and sorting:

```kotlin
@OptIn(ExperimentalJsonCmpApi::class)
@Composable
fun JsonEditorScreen() {
    val state = rememberJsonEditorState(
        initialJson = """{"name": "John", "age": 30}""",
    )

    JsonEditorCMP(
        modifier = Modifier.fillMaxSize(),
        state = state,
    )

    // React to changes by observing state properties
    LaunchedEffect(state.error) {
        state.error?.let { error ->
            println("Parse error: ${error.message}")
        }
    }
}
```

The editor owns its own text state — `initialJson` is used only once to seed the content. To load entirely new content, wrap the composable in a `key(documentId)` block.

## Search Highlighting

Pass a search query to highlight matching text:

```kotlin
JsonViewerCMP(
    state = state,
    searchQuery = "John",
)
```

## Observing State

Both `JsonViewerState` and `JsonEditorState` expose observable Compose state properties:

```kotlin
val state = rememberJsonViewerState(json = myJson)

// Read in composition or via snapshotFlow
state.json       // current raw JSON text
state.parsedJson // parsed JsonNode tree, or null if invalid
state.error      // parse error, or null if valid
```
