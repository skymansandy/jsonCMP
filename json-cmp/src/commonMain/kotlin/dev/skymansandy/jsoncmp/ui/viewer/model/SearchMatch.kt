/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.viewer.model

/** A single search hit: visible line index and character offset within that line. */
internal data class SearchMatch(
    val lineIdx: Int,
    val charOffset: Int,
)
