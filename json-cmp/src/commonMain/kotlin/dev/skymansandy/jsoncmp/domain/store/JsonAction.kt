package dev.skymansandy.jsoncmp.domain.store

/** User-driven actions dispatched to [JsonStore]. */
sealed class JsonAction {

    /** Replace the raw JSON text and trigger a reparse. */
    data class UpdateJson(val raw: String) : JsonAction()

    /** Toggle between pretty-printed and minified output. */
    data class Format(val compact: Boolean) : JsonAction()

    /** Sort all object keys alphabetically (ascending or descending). */
    data class SortKeys(val ascending: Boolean) : JsonAction()

    /** Expand or collapse a single foldable block by its [foldId]. */
    data class ToggleFold(val foldId: Int) : JsonAction()

    /** Collapse every foldable block. */
    data object CollapseAll : JsonAction()

    /** Expand every foldable block. */
    data object ExpandAll : JsonAction()
}
