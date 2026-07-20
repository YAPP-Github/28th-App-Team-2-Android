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
    fun `maxInputDigits까지_입력하면_바깥을_클릭하기_전에_이미_값이_커밋된다`() {
        // given
        // 자동 이동(auto-advance): 컬럼의 maxInputDigits(시=2자리)까지 채우는 순간, IME Done/포커스
        // 아웃 없이도 값이 즉시 커밋되고 다음 컬럼으로 포커스가 넘어간다. 따라서 그 뒤에 바깥(scrim)을
        // 탭해 시트를 닫아도, 커밋된 값은 그대로 유지된다. (커밋의 원인은 "바깥 탭"이 아니라
        // maxInputDigits 도달이다.)
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
        // 시(hour) 컬럼 중심 항목 탭 -> 직접입력 전환 후 2자리("15")를 채우면 자동 커밋된다.
        composeTestRule.wheelColumn(0).onChildren().filterToOne(hasText("09")).performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNode(hasSetTextAction()).performTextReplacement("15")
        composeTestRule.waitForIdle()

        // 이미 커밋된 뒤 바깥(scrim)을 탭해 시트를 닫는다.
        composeTestRule.onNode(isDialog()).performTouchInput { click(Offset(10f, 10f)) }
        composeTestRule.waitForIdle()

        // then
        assertTrue(dismissed)
        assertEquals(15, hour) // "15"는 maxInputDigits 도달 시 이미 커밋되었다.
    }
}
