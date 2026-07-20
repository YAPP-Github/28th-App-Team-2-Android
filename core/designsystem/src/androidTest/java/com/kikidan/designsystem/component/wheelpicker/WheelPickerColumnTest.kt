package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import com.kikidan.designsystem.theme.TodakunTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Rule
import org.junit.Test

class WheelPickerColumnTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    private val hours = (0..23).map { it.toString().padStart(2, '0') }

    @Test
    fun `중심_항목은_선택된_인덱스를_반영한다`() {
        // given
        composeTestRule.setContent {
            TodakunTheme {
                WheelPickerColumn(
                    items = hours,
                    selectedIndex = 5,
                    onSelectedIndexChange = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when & then
        // 항목 값(hours[5] = "05")은 컬럼 내에서 유일하므로 텍스트로 바로 식별한다.
        composeTestRule.onNodeWithText("05").assertIsSelected()
        composeTestRule.onNodeWithText("06").assertIsNotSelected()
    }

    @Test
    fun `중심이_아닌_항목을_탭하면_해당_인덱스로_콜백이_정확히_한_번_호출된다`() {
        // given
        var callCount = 0
        var receivedIndex = -1
        composeTestRule.setContent {
            TodakunTheme {
                WheelPickerColumn(
                    items = hours,
                    selectedIndex = 5,
                    onSelectedIndexChange = { index ->
                        callCount++
                        receivedIndex = index
                    },
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        // 중심(5)에서 1칸 떨어진 항목만 탭한다 — 2칸 이상 떨어진 항목은 LazyColumn 뷰포트
        // 밖(미구성)일 수 있어 계측 테스트에서 노드를 못 찾고 실패할 위험이 있다.
        composeTestRule.onNodeWithText("06").performClick()
        composeTestRule.waitForIdle()

        // then
        assertEquals(1, callCount)
        assertEquals(6, receivedIndex)
    }

    @Test
    fun `visibleCount가_짝수이면_예외가_발생한다`() {
        // given & when & then
        assertThrows(IllegalArgumentException::class.java) {
            composeTestRule.setContent {
                TodakunTheme {
                    WheelPickerColumn(
                        items = hours,
                        selectedIndex = 0,
                        onSelectedIndexChange = {},
                        visibleCount = 4,
                    )
                }
            }
            composeTestRule.waitForIdle()
        }
    }
}
