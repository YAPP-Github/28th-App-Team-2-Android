package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme

/**
 * 태어난 시각 선택(단일 휠, 문자열 라벨) 변형. Figma `WheelPicker_single` (node-id 383:1630) 재현.
 *
 * [WheelPickerBottomSheet]로 감싼 다이얼로그 카드로 화면 중앙에 렌더링된다(stateless) — 표시 여부는
 * 호출부가 `if (visible) { SajuBirthTimeWheelPicker(..., onDismissRequest = { visible = false }) }`
 * 형태로 제어한다. [onDismissRequest]는 아래로 드래그(핸들바/타이틀 영역)/바깥 탭/뒤로가기 중
 * 어떤 경로로 닫히든 호출된다. [onSaveClick] 이후 다이얼로그를 닫는 것은 호출부 책임이다(자동으로
 * 닫히지 않는다).
 *
 * 이미 포맷된 문자열 라벨 컬럼을 그대로 받는 얇은 wrapper이며 직접입력은 지원하지 않는다.
 */
@Composable
fun SajuBirthTimeWheelPicker(
    onSaveClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    column: WheelPickerColumnState = rememberSajuBirthWheelPickerState(),
) {
    WheelPickerBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        WheelPicker(
            title = stringResource(R.string.wheel_picker_saju_birth_time_title),
            onSaveClick = onSaveClick,
            columns = listOf(column),
            onWheelPickerColumnSelected = { _, selectedIndex -> onSelectedIndexChange(selectedIndex) },
            directInputEnabled = false,
        )
    }
}

@Composable
private fun rememberSajuBirthWheelPickerState(): WheelPickerColumnState {
    val items = stringArrayResource(R.array.wheel_picker_saju_birth_times)
    return remember(items) {
        WheelPickerColumnState(
            items = items.toList(),
            selectedIndex = 2,
        )
    }
}
