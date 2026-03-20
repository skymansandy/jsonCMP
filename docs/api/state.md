# JsonEditorState

State holder for the JsonCMP component. Manages raw JSON text, parsed tree, error state, folding, and display mode.

## Creating State

```kotlin
@Composable
fun MyScreen() {
    val state = rememberJsonEditorState(
        initialJson = """{"key": "value"}""",
        isEditing = false,
    )
}
```

## Properties

| Property | Type | Description |
|----------|------|-------------|
| `rawJson` | `String` | Current raw JSON text (read-only externally) |
| `parsedJson` | `JsonNode?` | Parsed JSON tree, or null if invalid |
| `error` | `JsonError?` | Parse error, or null if valid |
| `isCompact` | `Boolean` | Whether the JSON is in compact format |
| `isEditing` | `Boolean` | Toggle between editor and viewer mode (read-write) |

## Methods

### `updateRawJson(newJson: String)`

Replace the JSON content and re-parse.

### `format(compact: Boolean)`

Re-format the current parsed JSON. `compact = true` produces minified output, `compact = false` produces indented output.

### `sortKeys(ascending: Boolean)`

Sort all object keys recursively. Updates both `parsedJson` and `rawJson`.

### `collapseAll()`

Collapse all foldable nodes (objects and arrays).

### `expandAll()`

Expand all foldable nodes.
