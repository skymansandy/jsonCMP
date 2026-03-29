package dev.skymansandy.jsoncmp.ui.viewer

/** A single search hit: visible line index and character offset within that line. */
internal data class SearchMatch(
    val lineIdx: Int,
    val charOffset: Int,
)
