# JSON Viewer

The viewer renders JSON as a read-only, syntax-highlighted tree with line numbers and code folding.

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

```kotlin
// Programmatic control
state.collapseAll()
state.expandAll()
```

## Line Numbers

Line numbers are displayed in a gutter column with a subtle border separator.

## Search Highlighting

Pass a `searchQuery` to highlight matching text across the document:

```kotlin
JsonCMP(
    state = state,
    searchQuery = "John",
    colors = JsonCmpColors.Dark,
)
```

Matches are highlighted with the `highlight` and `highlightFg` colors from the active theme.
