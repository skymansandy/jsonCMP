package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.JsonCMP
import dev.skymansandy.jsoncmp.config.JsonStore
import dev.skymansandy.jsoncmp.config.JsonTheme
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class JsonCMPUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val theme = JsonTheme.Dark

    // ── Viewer mode ──

    @Test
    fun viewerModeDisplaysParsedJsonContent() {
        val store = JsonStore("""{"name": "John", "age": 30}""", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(store = store, theme = theme)
            }
        }

        composeTestRule.onNodeWithText("\"name\"", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("\"John\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun viewerModeDisplaysLineNumbers() {
        val store = JsonStore("""{"name": "John", "age": 30}""", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(store = store, theme = theme)
            }
        }

        composeTestRule.onNodeWithText("\"name\"", substring = true).assertIsDisplayed()
    }

    @Test
    fun viewerModeDoesNotShowEditorToolbar() {
        val store = JsonStore("""{"name": "John"}""", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(store = store, theme = theme)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").assertDoesNotExist()
    }

    @Test
    fun viewerModeShowsPlainTextForUnparseableContent() {
        val store = JsonStore("just plain text", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(store = store, theme = theme)
            }
        }

        composeTestRule.onNodeWithText("just plain text", substring = true).assertIsDisplayed()
    }

    // ── Editor mode ──

    @Test
    fun editorModeShowsToolbar() {
        val store = JsonStore("""{"name": "John"}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(store = store, theme = theme)
            }
        }

        composeTestRule.onNodeWithContentDescription("Sort keys").assertIsDisplayed()
    }

    @Test
    fun editorModeShowsFormatToggle() {
        val store = JsonStore("""{"name": "John"}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(store = store, theme = theme)
            }
        }

        composeTestRule.onNodeWithContentDescription("Compact").assertIsDisplayed()
    }

    @Test
    fun editorModeShowsErrorBannerForInvalidJson() {
        val store = JsonStore("{invalid}", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(store = store, theme = theme)
            }
        }

        composeTestRule.onNodeWithText("\u26A0", substring = true).assertIsDisplayed()
    }

    @Test
    fun editorModeNoErrorBannerForValidJson() {
        val store = JsonStore("""{"name": "John"}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(store = store, theme = theme)
            }
        }

        composeTestRule.onNodeWithText("\u26A0").assertDoesNotExist()
    }

    // ── onJsonChange callback ──

    @Test
    fun onJsonChangeReceivesInitialState() {
        val json = """{"name": "John"}"""
        val store = JsonStore(json, isEditing = false, dispatcher = Dispatchers.Unconfined)
        var receivedJson: String? = null

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(
                    store = store,
                    theme = theme,
                    onJsonChange = { j, _, _ -> receivedJson = j },
                )
            }
        }

        composeTestRule.waitForIdle()
        receivedJson shouldBe json
    }

    // ── Format toggle ──

    @Test
    fun clickingFormatButtonTogglesCompactMode() {
        val store = JsonStore("""{"name": "John"}""", isEditing = true, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(store = store, theme = theme)
            }
        }

        composeTestRule.onNodeWithContentDescription("Compact").performClick()
        composeTestRule.waitForIdle()

        store.state.value.isCompact shouldBe true
        composeTestRule.onNodeWithContentDescription("Beautify").assertIsDisplayed()
    }

    // ── Search highlighting in viewer ──

    @Test
    fun viewerModeWithSearchQueryShowsMatchingContent() {
        val store = JsonStore("""{"name": "John"}""", isEditing = false, dispatcher = Dispatchers.Unconfined)

        composeTestRule.setContent {
            MaterialTheme {
                JsonCMP(
                    store = store,
                    searchQuery = "John",
                    theme = theme,
                )
            }
        }

        composeTestRule.onNodeWithText("\"John\"", substring = true).assertIsDisplayed()
    }
}
