package com.kikidan.designsystem.component.wheelpicker

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme
import java.time.LocalDate
import java.time.YearMonth


@Composable
fun BirthDateWheelPicker(
    onYearChange: (Int) -> Unit,
    onMonthChange: (Int) -> Unit,
    onDayChange: (Int) -> Unit,
    onSaveClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    yearRange: IntRange = 1900..LocalDate.now().year,
    year: Int = LocalDate.now().year,
    month: Int = LocalDate.now().monthValue,
    day: Int = LocalDate.now().dayOfMonth,
) {
    val context = LocalContext.current
    val invalidDateMessage = stringResource(R.string.wheel_picker_birth_date_invalid)
    val yearPostfix = stringResource(R.string.wheel_picker_year_postfix)
    val monthPostfix = stringResource(R.string.wheel_picker_month_postfix)
    val datePostfix = stringResource(R.string.wheel_picker_date_postfix)

    WheelPickerBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        var clampedYear by remember { mutableIntStateOf(year.coerceIn(yearRange)) }
        var clampedMonth by remember { mutableIntStateOf(month.coerceIn(BirthDateWheelPickerDefaults.MonthRange)) }
        var clampedDay by remember { mutableIntStateOf(day.coerceIn(BirthDateWheelPickerDefaults.DayRange)) }

        val yearItems = remember(yearRange) { yearRange.map { it.toString() + yearPostfix } }
        val monthItems = remember {
            BirthDateWheelPickerDefaults.MonthRange.map {
                it.toString().padStart(2, '0') + monthPostfix
            }
        }

        WheelPicker(
            title = stringResource(R.string.wheel_picker_birth_date_title),
            onSaveClick = {
                val validDate = runCatching { LocalDate.of(year, month, day) }
                if (validDate.isSuccess) {
                    onSaveClick()
                } else {
                    Toast.makeText(context, invalidDateMessage, Toast.LENGTH_SHORT).show()
                }
            },
            columns = listOf(
                WheelPickerColumnState(
                    items = yearItems,
                    selectedIndex = clampedYear - yearRange.first,
                    width = 60.dp,
                    maxInputDigits = 4,
                ),
                WheelPickerColumnState(
                    items = monthItems,
                    selectedIndex = clampedMonth - BirthDateWheelPickerDefaults.MonthRange.first,
                    width = 40.dp,
                    maxInputDigits = 2,
                ),
                WheelPickerColumnState(
                    items = (1..YearMonth.of(clampedYear, clampedMonth)
                        .lengthOfMonth()).map { it.toString() + datePostfix },
                    selectedIndex = clampedDay - BirthDateWheelPickerDefaults.DayRange.first,
                    width = 40.dp,
                    maxInputDigits = 2,
                ),
            ),
            onWheelPickerColumnSelected = { columnIndex, selectedIndex ->
                when (columnIndex) {
                    0 -> {
                        val newYear = yearRange.first + selectedIndex
                        clampedYear = newYear
                        onYearChange(newYear)
                    }

                    1 -> {
                        val newMonth = BirthDateWheelPickerDefaults.MonthRange.first + selectedIndex
                        clampedMonth = newMonth
                        onMonthChange(newMonth)
                    }

                    2 -> {
                        val newDay = BirthDateWheelPickerDefaults.DayRange.first + selectedIndex
                        clampedDay = newDay
                        onDayChange(newDay)
                    }
                }
            },
            directInputEnabled = true,
            onColumnDirectInputCommitted = { columnIndex, rawDigits ->
                when (columnIndex) {
                    0 -> {
                        val typed = rawDigits.take(4).toIntOrNull()
                        if (typed != null) {
                            val newYear = typed.coerceIn(yearRange)
                            clampedYear = newYear
                            onYearChange(newYear)
                        }
                    }

                    1 -> {
                        val typed = rawDigits.take(2).toIntOrNull()
                        if (typed != null) {
                            val newMonth = typed.coerceIn(BirthDateWheelPickerDefaults.MonthRange)
                            clampedMonth = newMonth
                            onMonthChange(typed.coerceIn(BirthDateWheelPickerDefaults.MonthRange))
                        }
                    }

                    2 -> {
                        val typed = rawDigits.take(2).toIntOrNull()
                        if (typed != null) {
                            val newDay = typed.coerceIn(BirthDateWheelPickerDefaults.DayRange)
                            clampedDay = newDay
                            onDayChange(newDay)
                        }
                    }
                }
            },
        )
    }
}

private object BirthDateWheelPickerDefaults {
    val MonthRange = 1..12
    val DayRange = 1..31
}

@Preview(showBackground = true)
@Composable
private fun BirthDateWheelPickerPreview() {
    TodakunTheme {
        BirthDateWheelPicker(
            onYearChange = {},
            onMonthChange = {},
            onDayChange = {},
            onSaveClick = {},
            onDismissRequest = {},
        )
    }
}