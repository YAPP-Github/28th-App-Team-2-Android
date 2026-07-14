package com.kikidan.designsystem.component.wheelpicker

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import com.kikidan.designsystem.theme.TodakunTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class BirthDateWheelPickerTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun `년월일_컬럼이_렌더링된다`() {
        // given
        composeTestRule.setContent {
            TodakunTheme {
                BirthDateWheelPicker(
                    year = 2000,
                    month = 1,
                    day = 1,
                    onYearChange = {},
                    onMonthChange = {},
                    onDayChange = {},
                    onSaveClick = {},
                    onDismissRequest = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when & then
        // 연/월/일은 서로 다른 접미사("년"/"월"/"일")가 붙어 텍스트가 겹치지 않는다.
        composeTestRule.onNodeWithText("2000년").assertIsDisplayed()
        composeTestRule.onNodeWithText("01월").assertIsDisplayed()
        composeTestRule.onNodeWithText("1일").assertIsDisplayed()
    }

    @Test
    fun `실존하는_윤년_날짜는_토스트_없이_저장된다`() {
        // given
        var saved = false
        composeTestRule.setContent {
            TodakunTheme {
                // 2000-02-29 는 윤년이라 실존하는 날짜다.
                BirthDateWheelPicker(
                    year = 2000,
                    month = 2,
                    day = 29,
                    onYearChange = {},
                    onMonthChange = {},
                    onDayChange = {},
                    onSaveClick = { saved = true },
                    onDismissRequest = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        composeTestRule.onNodeWithText("저장").performClick()
        composeTestRule.waitForIdle()

        // then
        assertTrue(saved)
    }

    @Test
    fun `표시_범위보다_큰_연도를_입력하면_최대_연도로_보정된다`() {
        // given
        var year = 2000
        composeTestRule.setContent {
            TodakunTheme {
                BirthDateWheelPicker(
                    year = year,
                    month = 1,
                    day = 1,
                    onYearChange = { year = it },
                    onMonthChange = {},
                    onDayChange = {},
                    onSaveClick = {},
                    onDismissRequest = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        // 연도 컬럼의 중심 항목("2000년") 탭 -> 직접입력 전환
        composeTestRule.onNodeWithText("2000년").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("2999")
        composeTestRule.onNode(hasSetTextAction()).performImeAction()
        composeTestRule.waitForIdle()

        // then
        // yearRange 기본값(1900..LocalDate.now().year)의 상한으로 보정된다.
        assertEquals(LocalDate.now().year, year)
    }

    @Test
    fun `표시_범위보다_큰_월을_입력하면_12월로_보정된다`() {
        // given
        var month = 1
        composeTestRule.setContent {
            TodakunTheme {
                BirthDateWheelPicker(
                    year = 2000,
                    month = month,
                    day = 1,
                    onYearChange = {},
                    onMonthChange = { month = it },
                    onDayChange = {},
                    onSaveClick = {},
                    onDismissRequest = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        // 월 컬럼의 중심 항목("01월") 탭 -> 직접입력 전환
        composeTestRule.onNodeWithText("01월").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("13")
        composeTestRule.onNode(hasSetTextAction()).performImeAction()
        composeTestRule.waitForIdle()

        // then
        assertEquals(12, month)
    }

    @Test
    fun `표시_범위보다_큰_일을_입력하면_31일로_보정된다`() {
        // given
        var day = 1
        composeTestRule.setContent {
            TodakunTheme {
                BirthDateWheelPicker(
                    year = 2000,
                    month = 1,
                    day = day,
                    onYearChange = {},
                    onMonthChange = {},
                    onDayChange = { day = it },
                    onSaveClick = {},
                    onDismissRequest = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        // 일 컬럼의 중심 항목("1일") 탭 -> 직접입력 전환
        composeTestRule.onNodeWithText("1일").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("35")
        composeTestRule.onNode(hasSetTextAction()).performImeAction()
        composeTestRule.waitForIdle()

        // then
        assertEquals(31, day)
    }

    @Test
    fun `월_컬럼을_스크롤_선택하면_다른_컬럼과_무관하게_월_값만_보고된다`() {
        // given
        var month = 6
        composeTestRule.setContent {
            TodakunTheme {
                BirthDateWheelPicker(
                    year = 2000,
                    month = month,
                    day = 15,
                    onYearChange = {},
                    onMonthChange = { month = it },
                    onDayChange = {},
                    onSaveClick = {},
                    onDismissRequest = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        // 월 컬럼의 중심(6월)에서 1칸 떨어진 7월("07월") 탭.
        composeTestRule.onNodeWithText("07월").performClick()
        composeTestRule.waitForIdle()

        // then
        assertEquals(7, month)
    }
}
