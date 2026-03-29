package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.component.viewer.JsonViewer
import dev.skymansandy.jsoncmp.config.JsonAction
import dev.skymansandy.jsoncmp.config.JsonStore
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonViewerUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonCmpColors.Dark

    @Test
    fun displaysJsonKeys() {
        val store = JsonStore("""{"name": "John", "age": 30}""", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonViewer(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\"name\"", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("\"age\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysJsonValues() {
        val store = JsonStore("""{"name": "John"}""", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonViewer(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\"John\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysLineNumbersForMultiLineJson() {
        val store = JsonStore(
            """{"a": 1, "b": 2, "c": 3, "d": 4, "e": 5, "f": 6, "g": 7, "h": 8, "i": 9, "j": 10, "k": 11}""",
            isEditing = false,
            dispatcher = Dispatchers.Unconfined,
        )

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonViewer(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("13", substring = true).assertIsDisplayed()
    }

    @Test
    fun rendersNestedObjectsWithFoldStructure() {
        val store = JsonStore("""{"obj": {"key": "val"}}""", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonViewer(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("\"obj\"", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("\"key\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun showsPlainTextFallbackForUnparseableContent() {
        val store = JsonStore("not json at all", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonViewer(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("not json at all", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysEmptyObjectInline() {
        val store = JsonStore("""{"empty": {}}""", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonViewer(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("{}", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysEmptyArrayInline() {
        val store = JsonStore("""{"empty": []}""", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonViewer(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("[]", substring = true).assertIsDisplayed()
    }

    @Test
    fun foldCollapseHidesChildren() {
        val store = JsonStore(
            """{"user": {"name": "John", "city": "NYC"}}""",
            isEditing = false,
            dispatcher = Dispatchers.Unconfined,
        )

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonViewer(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        store.dispatch(JsonAction.CollapseAll)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("...", substring = true).assertIsDisplayed()
    }

    @Test
    fun collapseThenExpandShowsChildrenAgain() {
        val store = JsonStore(
            """{"name": "John", "age": 30}""",
            isEditing = false,
            dispatcher = Dispatchers.Unconfined,
        )

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonViewer(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        store.dispatch(JsonAction.CollapseAll)
        composeTestRule.waitForIdle()
        store.dispatch(JsonAction.ExpandAll)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("\"name\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysBooleanValues() {
        val store = JsonStore("""{"flag": true}""", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonViewer(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("true", substring = true).assertIsDisplayed()
    }

    @Test
    fun displaysNullValues() {
        val store = JsonStore("""{"val": null}""", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                JsonViewer(state = state, onAction = store::dispatch, searchQuery = "", colors = colors)
            }
        }

        composeTestRule.onNodeWithText("null", substring = true).assertIsDisplayed()
    }
}
