package dev.skymansandy.jsoncmp.ui.viewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.domain.store.JsonHolderState
import dev.skymansandy.jsoncmp.ui.common.PlainText
import dev.skymansandy.jsoncmp.ui.theme.JsonCmpColors
import dev.skymansandy.jsoncmp.ui.theme.monoStyle

/** Shown when no parsed lines exist — loading spinner, plain text fallback, or truncated preview. */
@Composable
internal fun JsonViewerEmptyState(
    modifier: Modifier,
    state: JsonHolderState,
    searchQuery: String,
    colors: JsonCmpColors,
) {
    if (state.isParsing) {
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
    } else if (state.raw.length <= 100 * 1024) {
        PlainText(
            modifier = modifier,
            text = state.raw,
            searchQuery = searchQuery,
            colors = colors,
        )
    } else {
        val truncatedText = state.raw.take(100 * 1024)
        Column(modifier = modifier.fillMaxHeight()) {
            Text(
                text = "Invalid JSON. Showing truncated preview (first 100 KB of ${state.raw.length / 1024} KB)",
                style = monoStyle,
                color = colors.highlightFg,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.highlight)
                    .padding(horizontal = 12.dp, vertical = 4.dp),
            )
            Box(modifier = Modifier.weight(1f)) {
                PlainText(
                    text = truncatedText,
                    searchQuery = searchQuery,
                    colors = colors,
                )
            }
        }
    }
}
