package dev.skymansandy.jsoncmp.component.common

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.helper.constants.foldGlyphSize
import dev.skymansandy.jsoncmp.helper.constants.typography.monoStyle
import dev.skymansandy.jsoncmp.helper.mock.previewColors

@Composable
internal fun GutterCell(
    lineNumber: Int,
    numDigits: Int,
    colors: JsonCmpColors,
    foldId: Int? = null,
    isFolded: Boolean = false,
    onFoldToggle: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    val foldGlyph = when {
        foldId == null -> ""
        isFolded -> "▶"
        else -> "▼"
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = lineNumber.toString().padStart(numDigits),
            style = monoStyle,
            color = colors.lineNumber,
            modifier = Modifier.padding(
                start = 12.dp,
                end = 6.dp,
            ),
        )
        Box(
            modifier = Modifier
                .size(foldGlyphSize)
                .then(
                    if (foldId != null) {
                        Modifier.pointerInput(foldId) { detectTapGestures { onFoldToggle() } }
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (foldId != null) {
                Text(
                    text = foldGlyph,
                    style = monoStyle,
                    color = colors.foldHint,
                )
            }
        }
    }
}

// ── Previews ──

@Preview
@Composable
private fun Preview_GutterCell() {
    MaterialTheme {
        GutterCell(
            lineNumber = 2,
            numDigits = 2,
            colors = previewColors,
        )
    }
}

@Preview
@Composable
private fun Preview_GutterCellFoldable() {
    MaterialTheme {
        GutterCell(
            lineNumber = 5,
            numDigits = 2,
            colors = previewColors,
            foldId = 1,
            isFolded = false,
            onFoldToggle = {},
        )
    }
}

@Preview
@Composable
private fun Preview_GutterCellFolded() {
    MaterialTheme {
        GutterCell(
            lineNumber = 5,
            numDigits = 2,
            colors = previewColors,
            foldId = 1,
            isFolded = true,
            onFoldToggle = {},
        )
    }
}
