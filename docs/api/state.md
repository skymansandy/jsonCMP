# State

State holders for `JsonViewerCMP` and `JsonEditorCMP`. Both expose observable Compose state properties — no callbacks needed.

## JsonViewerState

### Creating State

```kotlin
@OptIn(ExperimentalJsonCmpApi::class)
@Composable
fun MyScreen() {
    val state = rememberJsonViewerState(json = """{"key": "value"}""")
}
```

The viewer responds to changes in the `json` parameter — passing a new value triggers a re-parse and updates the display.

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `json` | `String` | Current raw JSON text being displayed |
| `parsedJson` | `JsonNode?` | Parsed JSON tree, or null if invalid |
| `error` | `JsonError?` | Parse error, or null if valid |

---

## JsonEditorState

### Creating State

```kotlin
@OptIn(ExperimentalJsonCmpApi::class)
@Composable
fun MyScreen() {
    val state = rememberJsonEditorState(initialJson = """{"key": "value"}""")
}
```

`initialJson` is used only once to seed the editor. The editor owns its own text state internally. To load entirely new content, wrap the composable in a `key(documentId)` block.

### Properties

| Property | Type | Description |
|----------|------|-------------|
| `json` | `String` | Current raw JSON text in the editor |
| `parsedJson` | `JsonNode?` | Parsed JSON tree, or null if invalid |
| `error` | `JsonError?` | Parse error, or null if valid |
