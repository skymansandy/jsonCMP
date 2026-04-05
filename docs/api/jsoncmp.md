# JsonViewerCMP & JsonEditorCMP

Two separate composable entry points for viewing and editing JSON.

## JsonViewerCMP

Read-only JSON viewer with virtualized rendering. Virtually no size limit.

```kotlin
@ExperimentalJsonCmpApi
@Composable
fun JsonViewerCMP(
    modifier: Modifier = Modifier,
    state: JsonViewerState,
    searchQuery: String = "",
    theme: JsonTheme = JsonTheme.Dark,
)
```

### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `modifier` | `Modifier` | `Modifier` | Compose modifier for the root layout |
| `state` | `JsonViewerState` | — | State holder created via `rememberJsonViewerState()` |
| `searchQuery` | `String` | `""` | Text to highlight in the JSON content |
| `theme` | `JsonTheme` | `Dark` | Color theme for syntax highlighting |

### Behavior

- Renders a read-only JSON tree with syntax highlighting, line numbers, and code folding
- Responds to changes in the `json` parameter of `rememberJsonViewerState` — new values trigger a re-parse

---

## JsonEditorCMP

JSON editor with real-time validation, formatting, and sorting. Enforces a 50 KB write limit — content is truncated if it exceeds this threshold.

```kotlin
@ExperimentalJsonCmpApi
@Composable
fun JsonEditorCMP(
    modifier: Modifier = Modifier,
    state: JsonEditorState,
    searchQuery: String = "",
    theme: JsonTheme = JsonTheme.Dark,
)
```

### Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `modifier` | `Modifier` | `Modifier` | Compose modifier for the root layout |
| `state` | `JsonEditorState` | — | State holder created via `rememberJsonEditorState()` |
| `searchQuery` | `String` | `""` | Text to highlight in the JSON content |
| `theme` | `JsonTheme` | `Dark` | Color theme for syntax highlighting |

### Behavior

- Renders the editor toolbar (format, sort, collapse/expand), error banner, and editable code editor
- `initialJson` seeds the editor once — the editor owns its text state internally
- Observe changes via `state.json`, `state.parsedJson`, and `state.error`
