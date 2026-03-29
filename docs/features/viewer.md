# JSON Viewer

`JsonViewerCMP` renders JSON as a read-only, syntax-highlighted tree with line numbers, code folding, and virtualized rendering.

## Syntax Highlighting

Each JSON token type is rendered in a distinct color:

- **Keys** — object property names
- **Strings** — string values
- **Numbers** — numeric values
- **Booleans** — `true` / `false`
- **Null** — `null` values
- **Punctuation** — braces, brackets, colons, commas

## Code Folding

Objects and arrays can be collapsed by clicking the fold indicator in the gutter. Collapsed sections show an ellipsis with the element count.

## Line Numbers

Line numbers are displayed in a gutter column with a subtle border separator.

## Search Highlighting

Pass a `searchQuery` to highlight matching text across the document:

```kotlin
JsonViewerCMP(
    state = state,
    searchQuery = "John",
    theme = JsonTheme.Dark,
)
```

Matches are highlighted with the `highlight` and `highlightFg` colors from the active theme.

## Reactive State

The viewer responds to changes in the `json` parameter passed to `rememberJsonViewerState` — new values trigger a re-parse and update the display:

```kotlin
var json by remember { mutableStateOf(initialJson) }
val state = rememberJsonViewerState(json = json)

// Updating `json` will automatically re-render the viewer
json = newJson
```
