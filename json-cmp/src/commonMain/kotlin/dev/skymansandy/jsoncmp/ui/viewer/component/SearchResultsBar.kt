package dev.skymansandy.jsoncmp.ui.viewer.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.ui.theme.JsonCmpColors
import dev.skymansandy.jsoncmp.ui.theme.monoStyle

/** "X of Y" match counter with previous/next navigation buttons. */
@Composable
internal fun SearchResultsBar(
    totalMatches: Int,
    currentMatch: Int,
    colors: JsonCmpColors,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
) {
    val hasMatches = totalMatches > 0
    val label = if (hasMatches) {
        "${currentMatch + 1} of $totalMatches"
    } else {
        "No results"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.gutterBackground)
            .padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = monoStyle,
            color = if (hasMatches) colors.punctuation else colors.lineNumber,
        )

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = onPrevious,
            enabled = hasMatches,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = colors.punctuation,
                disabledContentColor = colors.lineNumber,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowUp,
                contentDescription = "Previous match",
                modifier = Modifier.size(18.dp),
            )
        }

        IconButton(
            onClick = onNext,
            enabled = hasMatches,
            modifier = Modifier.size(32.dp),
            colors = IconButtonDefaults.iconButtonColors(
                contentColor = colors.punctuation,
                disabledContentColor = colors.lineNumber,
            ),
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowDown,
                contentDescription = "Next match",
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
