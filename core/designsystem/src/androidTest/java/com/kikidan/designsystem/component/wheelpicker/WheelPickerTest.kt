package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.test.espresso.Espresso
import com.kikidan.designsystem.theme.TodakunTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class WheelPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private fun numberColumn(range: IntRange, selected: Int) = WheelPickerColumnState(
        items = range.map { it.toString().padStart(2, '0') },
        selectedIndex = selected - range.first,
    )

    // 컬럼마다 testTag가 없으므로, 스크롤 가능한(LazyColumn) 노드를 등장 순서(컬럼 인덱스)로 찾는다.
    private fun SemanticsNodeInteractionsProvider.wheelColumn(index: Int) =
        onAllNodes(hasScrollAction())[index]

    @Test
    fun `컬럼별_선택은_다른_컬럼과_혼동되지_않고_올바른_컬럼_인덱스와_선택_인덱스를_보고한다`() {
        // given
        val events = mutableListOf<Pair<Int, Int>>()
        composeTestRule.setContent {
            TodakunTheme {
                WheelPicker(
                    title = "테스트",
                    onSaveClick = {},
                    columns = listOf(
                        numberColumn(0..23, 5),
                        numberColumn(0..59, 10),
                        numberColumn(1..31, 15),
                    ),
                    onWheelPickerColumnSelected = { columnIndex, selectedIndex ->
                        events += columnIndex to selectedIndex
                    },
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        // 컬럼1(분)의 인덱스 9 항목("09") 탭. 컬럼은 testTag 대신 등장 순서로 범위를 좁힌다.
        composeTestRule.wheelColumn(1).onChildren().filterToOne(hasText("09")).performClick()
        composeTestRule.waitForIdle()

        // 컬럼0(시)의 인덱스 4 항목("04", 중심 5에서 1칸 거리 -- 뷰포트 내 보장) 탭.
        // 다른 컬럼과 혼동되지 않아야 한다.
        composeTestRule.wheelColumn(0).onChildren().filterToOne(hasText("04")).performClick()
        composeTestRule.waitForIdle()

        // then
        assertEquals(listOf(1 to 9, 0 to 4), events)
    }

    @Test
    fun `저장_버튼을_탭하면_onSaveClick이_호출된다`() {
        // given
        var saved = false
        composeTestRule.setContent {
            TodakunTheme {
                WheelPicker(
                    title = "테스트",
                    onSaveClick = { saved = true },
                    columns = listOf(numberColumn(0..23, 5)),
                    onWheelPickerColumnSelected = { _, _ -> },
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        composeTestRule.onNodeWithText("저장").performClick()

        // then
        assertTrue(saved)
    }

    @Test
    fun `SajuBirthTimeWheelPicker는_단일_컬럼만_렌더링한다`() {
        // given
        composeTestRule.setContent {
            TodakunTheme {
                SajuBirthTimeWheelPicker(
                    onSaveClick = {},
                    onDismissRequest = {},
                    onSelectedIndexChange = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when & then
        // 스크롤 가능한(LazyColumn) 휠 컬럼이 정확히 1개만 렌더링된다.
        composeTestRule.onAllNodes(hasScrollAction()).assertCountEquals(1)
    }

    @Test
    fun `바깥을_클릭하면_onDismissRequest가_호출된다`() {
        // given
        var dismissed = false
        composeTestRule.setContent {
            TodakunTheme {
                SajuBirthTimeWheelPicker(
                    onSaveClick = {},
                    onDismissRequest = { dismissed = true },
                    onSelectedIndexChange = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        // 카드는 화면 중앙에 표시되므로, 화면 좌상단 모서리(카드 바깥) 탭이 "바깥 클릭"에 해당한다.
        composeTestRule.onNode(isDialog()).performTouchInput { click(Offset(10f, 10f)) }
        composeTestRule.waitForIdle()

        // then
        assertTrue(dismissed)
    }

    @Test
    fun `뒤로가기를_누르면_onDismissRequest가_호출된다`() {
        // given
        var dismissed = false
        composeTestRule.setContent {
            TodakunTheme {
                SajuBirthTimeWheelPicker(
                    onSaveClick = {},
                    onDismissRequest = { dismissed = true },
                    onSelectedIndexChange = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        Espresso.pressBack()
        composeTestRule.waitForIdle()

        // then
        assertTrue(dismissed)
    }
}
