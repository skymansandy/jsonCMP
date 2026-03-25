package dev.skymansandy.jsoncmpsample.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.JsonCMP
import dev.skymansandy.jsoncmp.config.JsonEditorState
import dev.skymansandy.jsoncmp.config.JsonTheme
import dev.skymansandy.jsoncmpsample.data.sampleJson
import jsoncmp.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class)
@Composable
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(),
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text("JsonCMP Sample")
                    },
                )
            },
        ) {
            var searchQuery by remember { mutableStateOf("") }
            var selectedTheme by remember { mutableStateOf<JsonTheme>(JsonTheme.Dark) }
            val themes = remember {
                listOf(
                    JsonTheme.Dark,
                    JsonTheme.Light,
                    JsonTheme.Monokai,
                    JsonTheme.Dracula,
                    JsonTheme.SolarizedDark,
                )
            }

            var largeJson by remember { mutableStateOf(sampleJson) }
            LaunchedEffect(Unit) {
                withContext(Dispatchers.Default) {
                    val bytes = Res.readBytes("files/5mbjson.json")
                    largeJson = bytes.decodeToString()
                }
            }

            val state by remember(largeJson) {
                mutableStateOf(
                    JsonEditorState(
                        initialJson = largeJson,
                        isEditing = false,
                    ),
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    themes.forEach { theme ->
                        FilterChip(
                            selected = selectedTheme == theme,
                            onClick = { selectedTheme = theme },
                            label = { Text(theme.label) },
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.weight(1f),
                        label = { Text("Search") },
                        singleLine = true,
                    )
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Switch(
                            checked = state.isEditing,
                            onCheckedChange = { state.isEditing = it },
                        )
                        Text(
                            text = if (state.isEditing) "Edit" else "View",
                            style = typography.labelSmall,
                        )
                    }
                }

                JsonCMP(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    state = state,
                    searchQuery = searchQuery,
                    theme = selectedTheme,
                )
            }
        }
    }
}
