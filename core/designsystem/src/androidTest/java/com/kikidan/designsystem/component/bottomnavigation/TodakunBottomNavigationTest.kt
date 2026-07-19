package com.kikidan.designsystem.component.bottomnavigation

import android.content.Context
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.isNotSelected
import androidx.compose.ui.test.isSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.core.app.ApplicationProvider
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TodakunBottomNavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun setContent(
        selectedItem: TodakunNavItem = TodakunNavItem.LUCKY,
        onItemSelected: (TodakunNavItem) -> Unit = {},
    ) {
        composeTestRule.setContent {
            TodakunTheme {
                TodakunBottomNavigation(
                    selectedItem = selectedItem,
                    onItemSelected = onItemSelected,
                )
            }
        }
    }

    @Test
    fun allFourLabels_areDisplayed() {
        setContent()

        composeTestRule.onNodeWithText(label(R.string.bottom_nav_fortune)).assertExists()
        composeTestRule.onNodeWithText(label(R.string.bottom_nav_todak)).assertExists()
        composeTestRule.onNodeWithText(label(R.string.bottom_nav_lucky_action)).assertExists()
        composeTestRule.onNodeWithText(label(R.string.bottom_nav_my)).assertExists()
    }

    @Test
    fun clickingLuckyTab_invokesCallbackWithLucky() {
        var clicked: TodakunNavItem? = null
        setContent(onItemSelected = { clicked = it })

        composeTestRule.onNodeWithText(label(R.string.bottom_nav_fortune)).performClick()

        assertEquals(TodakunNavItem.LUCKY, clicked)
    }

    @Test
    fun clickingMyTab_invokesCallbackWithMy() {
        var clicked: TodakunNavItem? = null
        setContent(onItemSelected = { clicked = it })

        composeTestRule.onNodeWithText(label(R.string.bottom_nav_my)).performClick()

        assertEquals(TodakunNavItem.MY, clicked)
    }

    @Test
    fun selectedTab_exposesSelectedSemantics_othersDoNot() {
        setContent(selectedItem = TodakunNavItem.AI)

        composeTestRule.onNodeWithText(label(R.string.bottom_nav_todak)).assert(isSelected())
        composeTestRule.onNodeWithText(label(R.string.bottom_nav_fortune)).assert(isNotSelected())
        composeTestRule.onNodeWithText(label(R.string.bottom_nav_lucky_action)).assert(isNotSelected())
        composeTestRule.onNodeWithText(label(R.string.bottom_nav_my)).assert(isNotSelected())
    }

    private fun label(resId: Int): String =
        ApplicationProvider.getApplicationContext<Context>().getString(resId)
}
