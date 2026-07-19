package com.kikidan.designsystem.component.header

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.kikidan.designsystem.theme.TodakunTheme
import org.junit.Rule
import org.junit.Test

class HeaderTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun mainHeader_showsTitleAndSubtext() {
        composeTestRule.setContent {
            TodakunTheme {
                TodakunMainHeader(
                    title = "타이틀",
                    subtext = "서브텍스트",
                )
            }
        }

        composeTestRule.onNodeWithText("타이틀").assertIsDisplayed()
        composeTestRule.onNodeWithText("서브텍스트").assertIsDisplayed()
    }

    @Test
    fun mainHeader_subtextNull_doesNotShowSubtext() {
        composeTestRule.setContent {
            TodakunTheme {
                TodakunMainHeader(
                    title = "타이틀",
                    subtext = null,
                )
            }
        }

        composeTestRule.onNodeWithText("타이틀").assertIsDisplayed()
        composeTestRule.onAllNodesWithText("서브텍스트").assertCountEquals(0)
    }

    @Test
    fun mainHeader_bellClick_invokesCallback() {
        var clicked = false
        composeTestRule.setContent {
            TodakunTheme {
                TodakunMainHeader(
                    title = "타이틀",
                    onBellClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("알림").performClick()
        assert(clicked)
    }

    @Test
    fun subHeader_showsTitle() {
        composeTestRule.setContent {
            TodakunTheme {
                TodakunSubHeader(title = "타이틀")
            }
        }

        composeTestRule.onNodeWithText("타이틀").assertIsDisplayed()
    }

    @Test
    fun subHeader_backClick_invokesCallback() {
        var clicked = false
        composeTestRule.setContent {
            TodakunTheme {
                TodakunSubHeader(
                    title = "타이틀",
                    onBackClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("뒤로 가기").performClick()
        assert(clicked)
    }

    @Test
    fun subHeader_onCloseClickProvided_showsCloseAndInvokesCallback() {
        var clicked = false
        composeTestRule.setContent {
            TodakunTheme {
                TodakunSubHeader(
                    title = "타이틀",
                    onCloseClick = { clicked = true },
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("닫기").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("닫기").performClick()
        assert(clicked)
    }

    @Test
    fun subHeader_onCloseClickNull_doesNotShowClose() {
        composeTestRule.setContent {
            TodakunTheme {
                TodakunSubHeader(
                    title = "타이틀",
                    onCloseClick = null,
                )
            }
        }

        composeTestRule.onAllNodesWithContentDescription("닫기").assertCountEquals(0)
    }
}
