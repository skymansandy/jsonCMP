package dev.skymansandy.jsoncmp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.theme.monoStyle

/** Fallback text display for invalid/unparseable JSON with optional search highlighting. */
@Composable
internal fun PlainText(
    modifier: Modifier = Modifier,
    text: String,
    searchQuery: String,
) {
    val colors = LocalJsonCmpColors.current

    val annotated = remember(text, searchQuery, colors) {
        buildAnnotatedString {
            append(text)
            addStyle(SpanStyle(color = colors.punctuation), 0, text.length)
            if (searchQuery.isNotBlank()) {
                val lowerText = text.lowercase()
                val lowerQuery = searchQuery.lowercase()
                var idx = lowerText.indexOf(lowerQuery)
                while (idx >= 0) {
                    addStyle(
                        SpanStyle(background = colors.highlight, color = colors.highlightFg),
                        idx,
                        idx + lowerQuery.length,
                    )
                    idx = lowerText.indexOf(lowerQuery, idx + lowerQuery.length)
                }
            }
        }
    }

    SelectionContainer(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background),
    ) {
        Text(
            text = annotated,
            style = monoStyle,
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 4.dp),
        )
    }
}

// ── Previews ──
@Preview
@Composable
private fun Preview_PlainText() {
    MaterialTheme {
        PlainText(
            text = "This is plain, unparseable text content",
            searchQuery = "",
        )
    }
}

@Preview
@Composable
private fun Preview_PlainTextWithSearch() {
    MaterialTheme {
        PlainText(
            text = "This is plain, unparseable text content",
            searchQuery = "plain",
        )
    }
}
