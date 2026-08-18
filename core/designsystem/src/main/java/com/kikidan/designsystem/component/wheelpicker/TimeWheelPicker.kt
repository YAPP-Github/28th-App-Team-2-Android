package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme
import java.time.LocalTime

@Composable
fun TimeWheelPicker(
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onSaveClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    hour: Int = LocalTime.now().hour,
    minute: Int = LocalTime.now().minute,
    minuteStep: Int = 1,
) {
    var clampedHour by remember { mutableIntStateOf(hour) }
    var clampedMinute by remember { mutableIntStateOf(minute) }

    WheelPickerBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        val hourItems =
            remember { TimeWheelPickerDefault.HourRange.map { it.toString().padStart(2, '0') } }
        val minuteItems =
            remember(minuteStep) {
                (TimeWheelPickerDefault.MinuteRange step minuteStep).map { it.toString().padStart(2, '0') }
            }

        TodakunWheelPicker(
            title = stringResource(R.string.wheel_picker_time_title),
            onSaveClick = onSaveClick,
            columns =
                listOf(
                    WheelPickerColumnState(
                        items = hourItems,
                        selectedIndex = clampedHour - TimeWheelPickerDefault.HourRange.first,
                        maxInputDigits = 2,
                    ),
                    WheelPickerColumnState(
                        items = minuteItems,
                        selectedIndex = (clampedMinute - TimeWheelPickerDefault.MinuteRange.first) / minuteStep,
                        maxInputDigits = 2,
                    ),
                ),
            onWheelPickerColumnSelect = { columnIndex, selectedIndex ->
                when (columnIndex) {
                    0 -> {
                        val newHour = TimeWheelPickerDefault.HourRange.first + selectedIndex
                        clampedHour = newHour
                        onHourChange(TimeWheelPickerDefault.HourRange.first + selectedIndex)
                    }

                    1 -> {
                        val newMinute = TimeWheelPickerDefault.MinuteRange.first + selectedIndex * minuteStep
                        clampedMinute = newMinute
                        onMinuteChange(clampedMinute)
                    }
                }
            },
            directInputEnabled = true,
            onColumnDirectInputCommit = { columnIndex, rawDigits ->
                val typed = rawDigits.take(2).toIntOrNull()
                if (typed != null) {
                    when (columnIndex) {
                        0 -> {
                            val newHour = typed.coerceAtMost(TimeWheelPickerDefault.HourRange.last)
                            clampedHour = newHour
                            onHourChange(newHour)
                        }

                        1 -> {
                            val clamped = typed.coerceAtMost(TimeWheelPickerDefault.MinuteRange.last)
                            val newMinute = clamped.roundToStep(minuteStep)
                            clampedMinute = newMinute
                            onMinuteChange(newMinute)
                        }
                    }
                }
            },
        )
    }
}

private fun Int.roundToStep(step: Int): Int {
    if (step <= 1) return this
    return ((this + step / 2) / step * step).coerceAtMost(TimeWheelPickerDefault.MinuteRange.last)
}

private object TimeWheelPickerDefault {
    val HourRange = 0..23
    val MinuteRange = 0..59
}

@Preview(showBackground = true)
@Composable
private fun TimeWheelPickerPreview() {
    TodakunTheme {
        TimeWheelPicker(
            onHourChange = {},
            onMinuteChange = {},
            onSaveClick = {},
            onDismissRequest = {},
        )
    }
}
