# JsonCMP

The main Composable entry point.

## Signature

```kotlin
@Composable
fun JsonCMP(
    modifier: Modifier = Modifier,
    state: JsonEditorState,
    searchQuery: String = "",
    colors: JsonCmpColors = JsonCmpColors.Dark,
    onJsonChange: (
        json: String,
        parsed: JsonNode?,
        error: JsonError?,
    ) -> Unit = { _, _, _ -> },
)
```

## Parameters

| Parameter | Type | Default | Description |
|-----------|------|---------|-------------|
| `modifier` | `Modifier` | `Modifier` | Compose modifier for the root layout |
| `state` | `JsonEditorState` | — | State holder created via `rememberJsonEditorState()` |
| `searchQuery` | `String` | `""` | Text to highlight in the JSON content |
| `colors` | `JsonCmpColors` | `Dark` | Color theme for syntax highlighting |
| `onJsonChange` | Lambda | no-op | Callback fired when JSON content changes |

## Behavior

- When `state.isEditing` is `true`: renders the editor toolbar, error banner, and editable code editor
- When `state.isEditing` is `false`: renders the read-only JSON viewer with folding
- `onJsonChange` is triggered via `LaunchedEffect` whenever `rawJson`, `parsedJson`, or `error` changes
