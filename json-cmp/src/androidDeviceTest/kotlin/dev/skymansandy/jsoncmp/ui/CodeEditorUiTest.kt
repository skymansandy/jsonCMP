package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.component.editor.JsonEditor
import dev.skymansandy.jsoncmp.config.JsonAction
import dev.skymansandy.jsoncmp.config.JsonStore
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CodeEditorUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonCmpColors.Dark

    @Test
    fun displaysJsonText() {
        val store = JsonStore("""{"name": "John"}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonEditor(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\"name\"", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("\"John\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysLineNumbers() {
        val json = "{\n  \"name\": \"John\",\n  \"age\": 30\n}"
        val store = JsonStore(json, isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonEditor(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("4", substring = true).assertIsDisplayed()
    }

    @Test
    fun syncsStateWhenFormatChangesExternally() {
        val store = JsonStore("""{"a": 1, "b": 2}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonEditor(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        store.dispatch(JsonAction.Format(compact = false))
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("\"a\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysWithSearchQueryWithoutCrash() {
        val store = JsonStore("""{"name": "John"}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonEditor(state = state, onAction = store::dispatch, searchQuery = "John", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\"John\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysCompactJsonContent() {
        val store = JsonStore("""{"a":1}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonEditor(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("{\"a\":1}", substring = true).assertIsDisplayed()
    }

    @Test
    fun rendersMultilineJsonWithCorrectLineCount() {
        val json = """
            {
              "a": 1,
              "b": 2,
              "c": 3,
              "d": 4,
              "e": 5,
              "f": 6,
              "g": 7,
              "h": 8,
              "i": 9,
              "j": 10
            }
        """.trimIndent()
        val store = JsonStore(json, isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonEditor(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("12", substring = true).assertIsDisplayed()
    }
}
