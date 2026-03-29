/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.viewer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import dev.skymansandy.jsoncmp.domain.ExperimentalJsonCmpApi
import dev.skymansandy.jsoncmp.theme.JsonTheme
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors

/**
 * Read-only JSON viewer composable with virtualized rendering.
 *
 * Observe state via [JsonViewerState.json], [JsonViewerState.parsedJson],
 * and [JsonViewerState.error] — no callbacks needed.
 *
 * @param state state holder created via [rememberJsonViewerState].
 * @param modifier layout modifier.
 * @param searchQuery optional search query to highlight matches.
 * @param theme visual theme for the viewer.
 */
@ExperimentalJsonCmpApi
@Composable
fun JsonViewerCMP(
    modifier: Modifier = Modifier,
    state: JsonViewerState,
    searchQuery: String = "",
    theme: JsonTheme = JsonTheme.Dark,
) {
    val storeState by state.store.state.collectAsState()

    CompositionLocalProvider(LocalJsonCmpColors provides theme.colors) {
        JsonViewer(
            modifier = modifier,
            state = storeState,
            onAction = state.store::dispatch,
            searchQuery = searchQuery,
        )
    }
}
