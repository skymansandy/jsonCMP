/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.viewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.domain.store.JsonHolderState
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.ui.common.PlainText

/** Shown when no parsed lines exist — loading spinner or lazy plain-text fallback. */
@Composable
internal fun JsonViewerEmptyState(
    modifier: Modifier = Modifier,
    state: JsonHolderState,
    searchQuery: String,
) {
    val colors = LocalJsonCmpColors.current

    when {
        state.isParsing ->
            Box(
                modifier = modifier.fillMaxSize().background(colors.background),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = colors.punctuation,
                    strokeWidth = 2.dp,
                )
            }

        else ->
            PlainText(
                modifier = modifier,
                text = state.raw,
                searchQuery = searchQuery,
            )
    }
}
