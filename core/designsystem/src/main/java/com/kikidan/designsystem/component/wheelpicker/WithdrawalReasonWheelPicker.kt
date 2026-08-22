package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme

@Composable
fun WithdrawalReasonWheelPicker(
    onSaveClick: () -> Unit,
    onDismissRequest: () -> Unit,
    onWithdrawalReasonChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialSelectedIndex: Int = 0,
) {
    val items = stringArrayResource(R.array.wheel_picker_withdrawal_reasons)

    WheelPickerBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        TodakunWheelPicker(
            title = stringResource(R.string.wheel_picker_withdrawal_reason_title),
            onSaveClick = onSaveClick,
            columns =
                listOf(
                    WheelPickerColumnState(
                        items = items.toList(),
                        selectedIndex = initialSelectedIndex,
                    ),
                ),
            onWheelPickerColumnSelect = { _, selectedIndex -> onWithdrawalReasonChange(items[selectedIndex]) },
            directInputEnabled = false,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun WithdrawalReasonWheelPickerPreview() {
    TodakunTheme {
        WithdrawalReasonWheelPicker(
            onSaveClick = {},
            onDismissRequest = {},
            onWithdrawalReasonChange = {},
        )
    }
}
