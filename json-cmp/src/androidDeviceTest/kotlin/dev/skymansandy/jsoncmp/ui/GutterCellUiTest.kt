package dev.skymansandy.jsoncmp.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import dev.skymansandy.jsoncmp.component.common.GutterCell
import dev.skymansandy.jsoncmp.helper.constants.colors.JsonCmpColors
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class GutterCellUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val colors = JsonCmpColors.Dark

    @Test
    fun displaysLineNumber() {
        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    lineNumber = 5,
                    numDigits = 2,
                    colors = colors,
                )
            }
        }

        composeTestRule.onNodeWithText(" 5").assertIsDisplayed()
    }

    @Test
    fun padsLineNumberToNumDigits() {
        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    lineNumber = 5,
                    numDigits = 3,
                    colors = colors,
                )
            }
        }

        composeTestRule.onNodeWithText("  5").assertIsDisplayed()
    }

    @Test
    fun showsExpandedGlyphForFoldableLine() {
        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    lineNumber = 3,
                    numDigits = 2,
                    colors = colors,
                    foldId = 1,
                    isFolded = false,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("\u25BC").assertIsDisplayed()
    }

    @Test
    fun showsCollapsedGlyphWhenFolded() {
        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    lineNumber = 3,
                    numDigits = 2,
                    colors = colors,
                    foldId = 1,
                    isFolded = true,
                    onFoldToggle = {},
                )
            }
        }

        composeTestRule.onNodeWithText("\u25B6").assertIsDisplayed()
    }

    @Test
    fun noFoldGlyphForNonFoldableLine() {
        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    lineNumber = 5,
                    numDigits = 2,
                    colors = colors,
                )
            }
        }

        composeTestRule.onNodeWithText("\u25BC").assertDoesNotExist()
        composeTestRule.onNodeWithText("\u25B6").assertDoesNotExist()
    }

    @Test
    fun foldToggleCallbackFiresOnClick() {
        var toggled = false

        composeTestRule.setContent {
            MaterialTheme {
                GutterCell(
                    lineNumber = 3,
                    numDigits = 2,
                    colors = colors,
                    foldId = 1,
                    isFolded = false,
                    onFoldToggle = { toggled = true },
                )
            }
        }

        composeTestRule.onNodeWithText("\u25BC").performClick()
        composeTestRule.waitForIdle()

        toggled shouldBe true
    }
}
