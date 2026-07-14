package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import com.kikidan.designsystem.R

@Composable
fun SajuBirthTimeWheelPicker(
    onSaveClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onSelectedIndexChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val items = stringArrayResource(R.array.wheel_picker_saju_birth_times)

    WheelPickerBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        WheelPicker(
            title = stringResource(R.string.wheel_picker_saju_birth_time_title),
            onSaveClick = onSaveClick,
            columns = listOf(
                WheelPickerColumnState(
                    items = items.toList(),
                    selectedIndex = 2,
                )
            ),
            onWheelPickerColumnSelected = { _, selectedIndex -> onSelectedIndexChange(selectedIndex) },
            directInputEnabled = false,
        )
    }
}
