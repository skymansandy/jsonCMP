package dev.skymansandy.jsoncmpsample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.JsonCMP
import dev.skymansandy.jsoncmp.config.rememberJsonEditorState
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors

private val sampleJson = """
{
    "name": "JsonCMP",
    "version": "1.0.0",
    "description": "Kotlin Multiplatform Compose JSON viewer and editor",
    "platforms": ["Android", "iOS", "JVM Desktop"],
    "features": {
        "viewer": true,
        "editor": true,
        "search": true,
        "folding": true,
        "sorting": true
    },
    "themes": [
        {"name": "Dark", "type": "dark"},
        {"name": "Light", "type": "light"},
        {"name": "Monokai", "type": "dark"},
        {"name": "Dracula", "type": "dark"},
        {"name": "Solarized Dark", "type": "dark"}
    ],
    "author": {
        "name": "skymansandy",
        "github": "https://github.com/skymansandy"
    },
    "license": "Apache-2.0",
    "stars": null
}
""".trimIndent()

private enum class ThemeOption(val label: String, val colors: JsonCmpColors) {
    Dark("Dark", JsonCmpColors.Dark),
    Light("Light", JsonCmpColors.Light),
    Monokai("Monokai", JsonCmpColors.Monokai),
    Dracula("Dracula", JsonCmpColors.Dracula),
    SolarizedDark("Solarized", JsonCmpColors.SolarizedDark),
}

@Composable
fun App() {

    MaterialTheme(
        colorScheme = darkColorScheme(),
    ) {
        var searchQuery by remember { mutableStateOf("") }
        var selectedTheme by remember { mutableStateOf(ThemeOption.Dark) }
        val state = rememberJsonEditorState(
            initialJson = sampleJson,
            isEditing = true,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(selectedTheme.colors.background),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                ThemeOption.entries.forEach { theme ->
                    FilterChip(
                        selected = selectedTheme == theme,
                        onClick = { selectedTheme = theme },
                        label = { Text(theme.label) },
                    )
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                label = { Text("Search") },
                singleLine = true,
            )

            JsonCMP(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                state = state,
                searchQuery = searchQuery,
                colors = selectedTheme.colors,
            )
        }
    }
}
