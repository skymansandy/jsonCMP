package dev.skymansandy.jsoncmpsample.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.skymansandy.jsoncmp.domain.ExperimentalJsonCmpApi
import dev.skymansandy.jsoncmp.theme.JsonTheme
import dev.skymansandy.jsoncmp.ui.editor.JsonEditorCMP
import dev.skymansandy.jsoncmp.ui.editor.rememberJsonEditorState
import dev.skymansandy.jsoncmp.ui.viewer.JsonViewerCMP
import dev.skymansandy.jsoncmp.ui.viewer.rememberJsonViewerState
import dev.skymansandy.jsoncmpsample.data.sampleJson
import jsoncmp.composeapp.generated.resources.Res
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalMaterial3Api::class, ExperimentalResourceApi::class, ExperimentalJsonCmpApi::class)
@Composable
fun App() {
    MaterialTheme(
        colorScheme = darkColorScheme(),
    ) {
        var fiveMbJson by remember { mutableStateOf("") }
        var showInvalidJson by remember { mutableStateOf(false) }
        var searchQuery by remember { mutableStateOf("") }
        var selectedTheme by remember { mutableStateOf<JsonTheme>(JsonTheme.Dark) }
        var showOverflowMenu by remember { mutableStateOf(false) }
        var selectedTab by remember { mutableIntStateOf(0) }
        var searchExpanded by remember { mutableStateOf(false) }

        val themes = remember {
            listOf(
                JsonTheme.Dark,
                JsonTheme.Light,
                JsonTheme.Monokai,
                JsonTheme.Dracula,
                JsonTheme.SolarizedDark,
            )
        }

        LaunchedEffect(Unit) {
            withContext(Dispatchers.Default) {
                val bytes = Res.readBytes("files/5mbjson.json")
                fiveMbJson = bytes.decodeToString()
            }
        }

        val viewerJson = if (showInvalidJson) "$fiveMbJson!!!" else fiveMbJson
        val viewerState = rememberJsonViewerState(json = viewerJson)

        val editorJson = if (showInvalidJson) fiveMbJson else sampleJson
        val editorState = rememberJsonEditorState(initialJson = editorJson)

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (searchExpanded && selectedTab == 0) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Search JSON...") },
                                singleLine = true,
                                trailingIcon = {
                                    IconButton(onClick = {
                                        searchQuery = ""
                                        searchExpanded = false
                                    }) {
                                        Icon(Icons.Default.Close, contentDescription = "Close search")
                                    }
                                },
                            )
                        } else {
                            Text("JsonCMP Sample")
                        }
                    },
                    actions = {
                        if (!searchExpanded && selectedTab == 0) {
                            IconButton(onClick = { searchExpanded = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search")
                            }
                        }
                        IconButton(onClick = { showInvalidJson = !showInvalidJson }) {
                            Icon(
                                imageVector = if (showInvalidJson) Icons.Default.BugReport else Icons.Default.CheckCircle,
                                contentDescription = if (showInvalidJson) "Showing invalid JSON" else "Showing valid JSON",
                            )
                        }
                        IconButton(onClick = { showOverflowMenu = !showOverflowMenu }) {
                            Icon(Icons.Default.MoreVert, contentDescription = "More options")
                        }
                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false },
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                                Row(
                                    modifier = Modifier
                                        .horizontalScroll(rememberScrollState()),
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
                            }
                        }
                    },
                )
            },
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = {
                            Text(
                                if (showInvalidJson) "Viewer: Invalid JSON"
                                else "Viewer: Valid 5MB JSON",
                            )
                        },
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = {
                            Text(
                                if (showInvalidJson) "Editor: Large JSON (50KB limit)"
                                else "Editor: Sample JSON",
                            )
                        },
                    )
                }

                when (selectedTab) {
                    0 -> JsonViewerCMP(
                        state = viewerState,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        searchQuery = searchQuery,
                        theme = selectedTheme,
                    )
                    1 -> JsonEditorCMP(
                        state = editorState,
                        modifier = Modifier.fillMaxWidth().weight(1f),
                        searchQuery = searchQuery,
                        theme = selectedTheme,
                    )
                }
            }
        }
    }
}
