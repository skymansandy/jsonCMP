# JSON Editor

The editor adds inline text editing with real-time validation, formatting, and sorting on top of the viewer.

## Enabling Editor Mode

```kotlin
val state = rememberJsonEditorState(
    initialJson = myJson,
    isEditing = true,
)
```

Or toggle at runtime:

```kotlin
state.isEditing = true
```

## Toolbar

The editor toolbar provides:

- **Format** — Toggle between pretty-print and compact (minified) output
- **Sort** — Sort object keys ascending or descending (recursive)
- **Collapse All / Expand All** — Fold or unfold all nodes

## Error Banner

When the JSON is invalid, an error banner appears below the toolbar showing:

- Error message
- Line and column position of the error

## Real-time Validation

The `onJsonChange` callback fires on every edit with the current JSON string, parsed `JsonNode?` (null if invalid), and `JsonError?`:

```kotlin
JsonCMP(
    state = state,
    onJsonChange = { json, parsed, error ->
        if (error != null) {
            showError(error.message)
        } else {
            saveJson(json)
        }
    },
)
```

## Formatting

```kotlin
state.format(compact = false) // Pretty-print with indentation
state.format(compact = true)  // Minified single-line output
```

## Sorting

Sort all object keys recursively:

```kotlin
state.sortKeys(ascending = true)  // A → Z
state.sortKeys(ascending = false) // Z → A
```
