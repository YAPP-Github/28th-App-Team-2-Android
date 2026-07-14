package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.test.SemanticsNodeInteractionsProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.click
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasScrollAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import com.kikidan.designsystem.theme.TodakunTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class TimeWheelPickerTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    // 시/분 컬럼은 둘 다 "00".."59" 형태라 텍스트만으로는 값이 겹칠 수 있다. testTag 없이는
    // 스크롤 가능한(LazyColumn) 노드를 등장 순서(컬럼 인덱스: 0=시, 1=분)로 좁혀서 식별한다.
    private fun SemanticsNodeInteractionsProvider.wheelColumn(index: Int) =
        onAllNodes(hasScrollAction())[index]

    @Test
    fun `시와_분_컬럼이_렌더링된다`() {
        // given
        composeTestRule.setContent {
            TodakunTheme {
                TimeWheelPicker(
                    hour = 9,
                    minute = 30,
                    onHourChange = {},
                    onMinuteChange = {},
                    onSaveClick = {},
                    onDismissRequest = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when & then
        composeTestRule.onAllNodes(hasScrollAction()).assertCountEquals(2)
    }

    @Test
    fun `24를_입력하면_시가_23으로_보정된다`() {
        // given
        var hour = 9
        composeTestRule.setContent {
            TodakunTheme {
                TimeWheelPicker(
                    hour = hour,
                    minute = 30,
                    onHourChange = { hour = it },
                    onMinuteChange = {},
                    onSaveClick = {},
                    onDismissRequest = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        // 시(컬럼 0)의 중심 항목("09") 탭 -> 직접입력 전환
        composeTestRule.wheelColumn(0).onChildren().filterToOne(hasText("09")).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("24")
        composeTestRule.onNode(hasSetTextAction()).performImeAction()
        composeTestRule.waitForIdle()

        // then
        assertEquals(23, hour)
    }

    @Test
    fun `60을_입력하면_분이_59로_보정된다`() {
        // given
        var minute = 30
        composeTestRule.setContent {
            TodakunTheme {
                TimeWheelPicker(
                    hour = 9,
                    minute = minute,
                    onHourChange = {},
                    onMinuteChange = { minute = it },
                    onSaveClick = {},
                    onDismissRequest = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        // 분(컬럼 1)의 중심 항목("30") 탭 -> 직접입력 전환
        composeTestRule.wheelColumn(1).onChildren().filterToOne(hasText("30")).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("60")
        composeTestRule.onNode(hasSetTextAction()).performImeAction()
        composeTestRule.waitForIdle()

        // then
        assertEquals(59, minute)
    }

    @Test
    fun `4자리를_입력하면_앞_2자리만_적용된_후_보정된다`() {
        // given
        // 3자리 이상 입력 시 앞 2자리만 적용되어 동일 규칙(min(typed, 23))이 적용됨을 확인.
        // 예: "9999" -> 타이핑 중 "99"로 길이 제한 -> 커밋 시 23으로 클램프.
        var hour = 9
        composeTestRule.setContent {
            TodakunTheme {
                TimeWheelPicker(
                    hour = hour,
                    minute = 30,
                    onHourChange = { hour = it },
                    onMinuteChange = {},
                    onSaveClick = {},
                    onDismissRequest = {},
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        composeTestRule.wheelColumn(0).onChildren().filterToOne(hasText("09")).performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("9999")
        composeTestRule.onNode(hasSetTextAction()).performImeAction()
        composeTestRule.waitForIdle()

        // then
        assertEquals(23, hour)
    }

    @Test
    fun `편집_중_바깥을_클릭하면_값이_커밋되지_않고_다이얼로그가_닫힌다`() {
        // given
        // onDismissRequest는 순수 콜백일 뿐 다이얼로그를 자동으로 언마운트하지 않는다(호출부가
        // 상태를 바꿔야 실제로 컴포지션에서 제거됨). 이 테스트는 호출부 상태 변경 없이 콜백만
        // 관찰하므로, 다이얼로그/텍스트필드는 여전히 포커스를 유지한 채 남아있고 커밋 경로(IME
        // Done/포커스 아웃)를 타지 않았음을 확인한다 -- 즉 "바깥 탭"이라는 행위 자체가 편집 중인
        // 값을 커밋시키는 부수효과를 만들지 않아야 한다.
        var hour = 9
        var dismissed = false
        composeTestRule.setContent {
            TodakunTheme {
                TimeWheelPicker(
                    hour = hour,
                    minute = 30,
                    onHourChange = { hour = it },
                    onMinuteChange = {},
                    onSaveClick = {},
                    onDismissRequest = { dismissed = true },
                )
            }
        }
        composeTestRule.waitForIdle()

        // when
        // 시(hour) 컬럼 중심 항목 탭 -> 직접입력 전환. 아직 IME Done/포커스 아웃으로 커밋하지 않은 채
        // 값만 입력해 둔다.
        composeTestRule.wheelColumn(0).onChildren().filterToOne(hasText("09")).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("15")

        // 커밋되기 전에 바깥(scrim)을 탭해 시트를 닫는다.
        composeTestRule.onNode(isDialog()).performTouchInput { click(Offset(10f, 10f)) }
        composeTestRule.waitForIdle()

        // then
        assertTrue(dismissed)
        assertEquals(9, hour) // 편집 중이던 "15"는 커밋되지 않고 폐기된다.
    }
}
