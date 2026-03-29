/*
 * Copyright (c) 2026 skymansandy. All rights reserved.
 */

package dev.skymansandy.jsoncmp.ui.editor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.skymansandy.jsoncmp.domain.parser.JsonError
import dev.skymansandy.jsoncmp.theme.LocalJsonCmpColors
import dev.skymansandy.jsoncmp.theme.monoStyle

/** Displays a parse error in a red banner; hidden when [error] is null. */
@Composable
internal fun ErrorBanner(
    modifier: Modifier = Modifier,
    error: JsonError?,
) {
    if (error == null) return
    val colors = LocalJsonCmpColors.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(colors.errorBackground)
            .padding(horizontal = 12.dp, vertical = 6.dp),
    ) {
        Text(
            text = "\u26A0",
            style = monoStyle,
            color = colors.errorForeground,
            modifier = Modifier.padding(end = 8.dp),
        )

        Text(
            text = error.message,
            style = monoStyle.copy(fontSize = 11.sp),
            color = colors.errorForeground,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

// ── Previews ──

@Preview
@Composable
private fun Preview_ErrorBanner() {
    MaterialTheme {
        ErrorBanner(
            modifier = Modifier.fillMaxWidth(),
            error = JsonError(
                message = "Unexpected token '}' at position 23",
                position = 23,
            ),
        )
    }
}

@Preview
@Composable
private fun Preview_ErrorBannerNull() {
    MaterialTheme {
        ErrorBanner(
            modifier = Modifier.fillMaxWidth(),
            error = null,
        )
    }
}
