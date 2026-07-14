package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.LocalTodakunColor
import com.kikidan.designsystem.theme.LocalTodakunTypography

/**
 * [WheelPicker]의 컬럼 하나를 표현하는 stateless 데이터.
 *
 * @param items 이미 포맷된 표시용 라벨(예: "08", "자시").
 * @param selectedIndex 현재 선택된 항목의 인덱스.
 * @param width 컬럼 고정 너비. [Dp.Unspecified]면 컬럼끼리 균등 분배(weight)한다.
 * @param maxInputDigits 직접입력 활성화 시 이 컬럼이 허용하는 최대 입력 자리수(예: year=4, 나머지=2).
 */
@Immutable
data class WheelPickerColumnState(
    val items: List<String>,
    val selectedIndex: Int,
    val width: Dp = Dp.Unspecified,
    val maxInputDigits: Int = 2,
)

@Composable
internal fun WheelPicker(
    title: String,
    onSaveClick: () -> Unit,
    columns: List<WheelPickerColumnState>,
    onWheelPickerColumnSelected: (columnIndex: Int, selectedIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    visibleCount: Int = 5,
    directInputEnabled: Boolean = false,
    onColumnDirectInputCommitted: (columnIndex: Int, rawDigits: String) -> Unit = { _, _ -> },
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
            .padding(top = 12.dp, bottom = 40.dp, start = 30.dp, end = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        WheelPickerHeader(
            title = title,
            onSaveClick = onSaveClick,
        )

        Spacer(modifier = Modifier.height(28.dp))

        Box(contentAlignment = Alignment.Center) {
            WheelPickerHighlight()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                Spacer(modifier.weight(1f))
                columns.forEachIndexed { columnIndex, columnState ->
                    WheelPickerColumn(
                        modifier = Modifier.weight(1f),
                        items = columnState.items,
                        selectedIndex = columnState.selectedIndex,
                        onSelectedIndexChange = { idx ->
                            onWheelPickerColumnSelected(
                                columnIndex,
                                idx
                            )
                        },
                        visibleCount = visibleCount,
                        directInputEnabled = directInputEnabled,
                        maxInputDigits = columnState.maxInputDigits,
                        onDirectInputCommitted = { raw ->
                            onColumnDirectInputCommitted(
                                columnIndex,
                                raw
                            )
                        },
                    )
                    Spacer(modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun WheelPickerHeader(
    title: String,
    onSaveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val typography = LocalTodakunTypography.current
    val colors = LocalTodakunColor.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text = title, style = typography.heading4Bold, color = colors.black)
        Text(
            text = stringResource(R.string.wheel_picker_save),
            style = typography.body2SemiBold,
            color = colors.primary600,
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onSaveClick() },
        )
    }
}

@Composable
private fun WheelPickerHighlight(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(WheelPickerDefaults.CenterContainerHeight)
            .clip(WheelPickerDefaults.WheelPickerHighlightShape)
            .background(LocalTodakunColor.current.primary50),
    )
}

internal object WheelPickerDefaults {
    val CenterContainerHeight: Dp
        @Composable get() =
            LocalTodakunTypography.current.body1Medium.toLineHeight() + HighlightVerticalPadding

    val HighlightVerticalPadding = 24.dp

    val WheelPickerHighlightShape = RoundedCornerShape(8.dp)
}

@Composable
private fun TextStyle.toLineHeight(): Dp {
    val density = LocalDensity.current
    return with(density) {
        val lineHeight = lineHeight
        if (lineHeight.isSp) lineHeight.toDp() else fontSize.toDp()
    }
}
