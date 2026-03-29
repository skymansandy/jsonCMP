package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.component.editor.EditorToolbar
import dev.skymansandy.jsoncmp.config.JsonAction
import dev.skymansandy.jsoncmp.config.JsonStore
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import dev.skymansandy.jsoncmp.model.JsonNode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class EditorToolbarUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonCmpColors.Dark

    @Test
    fun showsFormatButtonWithCompactDescriptionWhenNotCompact() {
        val store = JsonStore("""{"a": 1}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                EditorToolbar(state = state, onAction = store::dispatch, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Compact").assertIsDisplayed()
    }

    @Test
    fun showsFormatButtonWithBeautifyDescriptionWhenCompact() {
        val store = JsonStore("""{"a": 1}""", isEditing = true, dispatcher = Dispatchers.Unconfined)
        store.dispatch(JsonAction.Format(compact = true))

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                EditorToolbar(state = state, onAction = store::dispatch, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Beautify").assertIsDisplayed()
    }

    @Test
    fun showsSortButton() {
        val store = JsonStore("""{"a": 1}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                EditorToolbar(state = state, onAction = store::dispatch, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").assertIsDisplayed()
    }

    @Test
    fun clickingFormatTogglesCompactMode() {
        val store = JsonStore("""{"a": 1}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                EditorToolbar(state = state, onAction = store::dispatch, colors = colors)
            }
        }

        store.state.value.isCompact shouldBe false
        composeTestRule.onNodeWithContentDescription("Compact").performClick()
        composeTestRule.waitForIdle()

        store.state.value.isCompact shouldBe true
        composeTestRule.onNodeWithContentDescription("Beautify").assertIsDisplayed()
    }

    @Test
    fun clickingFormatTwiceRoundtrips() {
        val store = JsonStore("""{"a": 1}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                EditorToolbar(state = state, onAction = store::dispatch, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Compact").performClick()
        composeTestRule.waitForIdle()
        store.state.value.isCompact shouldBe true

        composeTestRule.onNodeWithContentDescription("Beautify").performClick()
        composeTestRule.waitForIdle()
        store.state.value.isCompact shouldBe false
    }

    @Test
    fun clickingSortOpensBottomSheet() {
        val store = JsonStore("""{"a": 1}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                EditorToolbar(state = state, onAction = store::dispatch, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithText("Sort Keys").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sort Ascending (A \u2192 Z)").assertIsDisplayed()
        composeTestRule.onNodeWithText("Sort Descending (Z \u2192 A)").assertIsDisplayed()
    }

    @Test
    fun sortAscendingReordersKeys() {
        val store = JsonStore("""{"c": 3, "a": 1, "b": 2}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                EditorToolbar(state = state, onAction = store::dispatch, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sort Ascending (A \u2192 Z)").performClick()
        composeTestRule.waitForIdle()

        val obj = store.state.value.parsedJson as JsonNode.JObject
        obj.fields.map { it.first } shouldBe listOf("a", "b", "c")
    }

    @Test
    fun sortDescendingReordersKeys() {
        val store = JsonStore("""{"a": 1, "b": 2, "c": 3}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            val state by store.state.collectAsState()
            MaterialTheme {
                EditorToolbar(state = state, onAction = store::dispatch, colors = colors)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithText("Sort Descending (Z \u2192 A)").performClick()
        composeTestRule.waitForIdle()

        val obj = store.state.value.parsedJson as JsonNode.JObject
        obj.fields.map { it.first } shouldBe listOf("c", "b", "a")
    }
}
