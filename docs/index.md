# JsonCMP

Kotlin Multiplatform Compose JSON viewer and editor component. Syntax-highlighted JSON rendering with code folding, inline editing, real-time validation, formatting, sorting, and search highlighting. Ships as a single Composable for Android, iOS, and JVM Desktop.

!!! warning "Experimental"
    This library is in an experimental state. APIs may change without notice between releases. Use in production at your own discretion.

## Platform Support

| Platform | Supported |
|----------|:---------:|
| **Android** | ✅ |
| **iOS** | ✅ |
| **JVM Desktop** | ✅ |

## JSON Viewer

Drop-in read-only JSON renderer with:

- Syntax highlighting (keys, strings, numbers, booleans, null, punctuation)
- Line numbers with gutter
- Code folding for objects and arrays
- Search text highlighting
- Collapse/expand all

```kotlin
@Composable
fun MyScreen() {
    val state = rememberJsonEditorState(initialJson = myJson)

    JsonCMP(
        modifier = Modifier.fillMaxSize(),
        state = state,
        searchQuery = "name",
    )
}
```

## JSON Editor

Full editing mode with real-time validation:

- Inline text editing with live parse feedback
- Error banner showing parse errors with line/column position
- Format (pretty-print or minify)
- Sort keys ascending/descending
- Toolbar with format, sort, collapse/expand controls

```kotlin
@Composable
fun MyEditor() {
    val state = rememberJsonEditorState(
        initialJson = myJson,
        isEditing = true,
    )

    JsonCMP(
        modifier = Modifier.fillMaxSize(),
        state = state,
        onJsonChange = { json, parsed, error ->
            // Handle changes
        },
    )
}
```

## Themes

Five built-in color themes inspired by popular code editors:

| Theme | Style |
|-------|-------|
| `JsonCmpColors.Dark` | VS Code Dark+ (default) |
| `JsonCmpColors.Light` | VS Code Light+ |
| `JsonCmpColors.Monokai` | Monokai |
| `JsonCmpColors.Dracula` | Dracula |
| `JsonCmpColors.SolarizedDark` | Solarized Dark |

Custom themes are supported by creating your own `JsonCmpColors` instance.
