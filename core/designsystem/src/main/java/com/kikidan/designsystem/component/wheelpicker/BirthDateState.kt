package com.kikidan.designsystem.component.wheelpicker

import androidx.compose.runtime.Immutable
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class BirthDateState(
    val year: Int,
    val month: Int,
    val day: Int,
    val yearRange: IntRange,
) {
    fun withYear(year: Int): BirthDateState =
        copy(year = year.coerceIn(yearRange)).clampDay()

    fun withMonth(month: Int): BirthDateState =
        copy(month = month.coerceIn(1, 12)).clampDay()

    fun withDay(day: Int): BirthDateState {
        val maxDay = YearMonth.of(year, month).lengthOfMonth()
        return copy(day = day.coerceIn(1, maxDay))
    }

    private fun clampDay(): BirthDateState {
        val maxDay = YearMonth.of(year, month).lengthOfMonth()
        return if (day > maxDay) copy(day = maxDay) else this
    }

    companion object {
        fun of(date: LocalDate, yearRange: IntRange): BirthDateState =
            BirthDateState(date.year, date.monthValue, date.dayOfMonth, yearRange)
    }
}
