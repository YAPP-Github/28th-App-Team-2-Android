package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.kikidan.designsystem.R
import com.kikidan.designsystem.theme.TodakunTheme
import java.time.LocalDate
import java.time.YearMonth

@Composable
fun BirthDateWheelPicker(
    birthDateState: BirthDateState,
    onBirthDateChange: (BirthDateState) -> Unit,
    onSaveClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val yearRange = birthDateState.yearRange
    val yearPostfix = stringResource(R.string.wheel_picker_year_postfix)
    val monthPostfix = stringResource(R.string.wheel_picker_month_postfix)
    val datePostfix = stringResource(R.string.wheel_picker_date_postfix)

    WheelPickerBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
    ) {
        val yearItems = remember(yearRange) { yearRange.map { it.toString() + yearPostfix } }
        val monthItems = remember {
            BirthDateWheelPickerDefaults.MonthRange.map {
                it.toString().padStart(2, '0') + monthPostfix
            }
        }
        val dayItems = remember(birthDateState.year, birthDateState.month) {
            (1..YearMonth.of(birthDateState.year, birthDateState.month).lengthOfMonth())
                .map { it.toString() + datePostfix }
        }

        TodakunWheelPicker(
            title = stringResource(R.string.wheel_picker_birth_date_title),
            onSaveClick = onSaveClick,
            columns = listOf(
                WheelPickerColumnState(
                    items = yearItems,
                    selectedIndex = birthDateState.year - yearRange.first,
                    maxInputDigits = 4,
                ),
                WheelPickerColumnState(
                    items = monthItems,
                    selectedIndex = birthDateState.month - BirthDateWheelPickerDefaults.MonthRange.first,
                    maxInputDigits = 2,
                ),
                WheelPickerColumnState(
                    items = dayItems,
                    selectedIndex = birthDateState.day - 1,
                    maxInputDigits = 2,
                ),
            ),
            onWheelPickerColumnSelected = { columnIndex, selectedIndex ->
                when (columnIndex) {
                    0 -> onBirthDateChange(
                        birthDateState.withYear(yearRange.first + selectedIndex)
                    )

                    1 -> onBirthDateChange(
                        birthDateState.withMonth(
                            BirthDateWheelPickerDefaults.MonthRange.first + selectedIndex
                        )
                    )

                    2 -> onBirthDateChange(birthDateState.withDay(selectedIndex + 1))
                }
            },
            directInputEnabled = true,
            onColumnDirectInputCommitted = { columnIndex, rawDigits ->
                when (columnIndex) {
                    0 -> rawDigits.take(4).toIntOrNull()?.let {
                        onBirthDateChange(birthDateState.withYear(it))
                    }

                    1 -> rawDigits.take(2).toIntOrNull()?.let {
                        onBirthDateChange(birthDateState.withMonth(it))
                    }

                    2 -> rawDigits.take(2).toIntOrNull()?.let {
                        onBirthDateChange(birthDateState.withDay(it))
                    }
                }
            },
        )
    }
}

private object BirthDateWheelPickerDefaults {
    val MonthRange = 1..12
}

@Preview(showBackground = true)
@Composable
private fun BirthDateWheelPickerPreview() {
    TodakunTheme {
        BirthDateWheelPicker(
            birthDateState = BirthDateState.of(LocalDate.now(), 1900..LocalDate.now().year),
            onBirthDateChange = {},
            onSaveClick = {},
            onDismissRequest = {},
        )
    }
}
