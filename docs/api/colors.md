# JsonTheme & JsonCmpColors

## JsonTheme

Sealed class wrapping a `JsonCmpColors` palette with a display label. Passed as the `theme` parameter to `JsonViewerCMP` and `JsonEditorCMP`.

### Built-in Themes

```kotlin
JsonTheme.Dark           // VS Code Dark+
JsonTheme.Light          // VS Code Light+
JsonTheme.Monokai        // Monokai
JsonTheme.Dracula        // Dracula
JsonTheme.SolarizedDark  // Solarized Dark
```

### Custom Theme

```kotlin
JsonTheme.Custom(myJsonCmpColors)
```

---

## JsonCmpColors

Data class defining the color scheme for all JSON elements and UI chrome.

### Properties

| Property | Description |
|----------|-------------|
| `key` | Object key names |
| `string` | String values |
| `number` | Numeric values |
| `booleanColor` | Boolean values (`true`/`false`) |
| `nullColor` | Null values |
| `punctuation` | Braces, brackets, colons, commas |
| `lineNumber` | Line numbers in the gutter |
| `foldHint` | Fold/unfold indicator |
| `background` | Main content background |
| `gutterBackground` | Line number gutter background |
| `highlight` | Search match highlight background |
| `highlightFg` | Search match highlight foreground |
| `gutterBorder` | Border between gutter and content |
| `foldEllipsis` | Collapsed section ellipsis text |
| `errorBackground` | Error banner background |
| `errorForeground` | Error banner text |

### Built-in Presets

```kotlin
JsonCmpColors.Dark           // VS Code Dark+
JsonCmpColors.Light          // VS Code Light+
JsonCmpColors.Monokai        // Monokai
JsonCmpColors.Dracula        // Dracula
JsonCmpColors.SolarizedDark  // Solarized Dark
```
