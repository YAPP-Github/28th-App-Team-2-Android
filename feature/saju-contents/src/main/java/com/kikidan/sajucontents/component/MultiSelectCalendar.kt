package com.kikidan.sajucontents.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kikidan.designsystem.theme.TodakunColor
import com.kikidan.designsystem.theme.TodakunTypography
import com.kikidan.sajucontents.R
import kotlinx.collections.immutable.ImmutableList
import java.time.LocalDate
import java.time.YearMonth

/** 캘린더 날짜 셀의 시각 상태 4종. 우선순위: 과거 > 선택 > 오늘 > 기본. */
enum class CalendarDayState {
    PAST,
    SELECTED,
    TODAY,
    DEFAULT,
}

/**
 * 날짜 하나의 캘린더 셀 상태를 판정하는 순수 함수.
 * Compose에 의존하지 않아 유닛 테스트로 직접 검증한다.
 */
fun resolveCalendarDayState(
    date: LocalDate,
    today: LocalDate,
    selectedDates: List<LocalDate>,
): CalendarDayState =
    when {
        date.isBefore(today) -> CalendarDayState.PAST
        selectedDates.contains(date) -> CalendarDayState.SELECTED
        date.isEqual(today) -> CalendarDayState.TODAY
        else -> CalendarDayState.DEFAULT
    }

private const val MONTH_COUNT = 12
private const val DAYS_IN_WEEK = 7
private val CellSize = 48.dp

@Composable
fun MultiSelectCalendar(
    selectedDates: ImmutableList<LocalDate>,
    onDateToggle: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val months =
        remember(today) {
            val start = YearMonth.from(today)
            (0 until MONTH_COUNT).map { start.plusMonths(it.toLong()) }
        }

    LazyColumn(modifier = modifier) {
        items(months, key = { it.toString() }) { month ->
            MonthSection(
                month = month,
                today = today,
                selectedDates = selectedDates,
                onDateToggle = onDateToggle,
            )
        }
    }
}

@Composable
private fun MonthSection(
    month: YearMonth,
    today: LocalDate,
    selectedDates: ImmutableList<LocalDate>,
    onDateToggle: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth().padding(vertical = 12.dp)) {
        Text(
            text = "${month.year}.${month.monthValue}",
            style = TodakunTypography.heading4Bold,
            color = TodakunColor.gray975,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        WeekDaysHeader()

        val firstDayOfMonth = month.atDay(1)
        val daysInMonth = month.lengthOfMonth()
        // 일요일 시작 기준 선행 빈 칸 개수 (MONDAY=1..SUNDAY=7 → SUNDAY만 0)
        val leadingBlanks = firstDayOfMonth.dayOfWeek.value % DAYS_IN_WEEK
        val totalCells = leadingBlanks + daysInMonth
        val weekCount = (totalCells + DAYS_IN_WEEK - 1) / DAYS_IN_WEEK

        for (week in 0 until weekCount) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0 until DAYS_IN_WEEK) {
                    val dayNumber = week * DAYS_IN_WEEK + dayOfWeek - leadingBlanks + 1
                    Box(
                        modifier = Modifier.weight(1f).height(CellSize),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (dayNumber in 1..daysInMonth) {
                            val date = month.atDay(dayNumber)
                            DayCell(
                                date = date,
                                state = resolveCalendarDayState(date, today, selectedDates),
                                onClick = onDateToggle,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WeekDaysHeader(modifier: Modifier = Modifier) {
    val labels =
        listOf(
            R.string.date_fortune_weekday_sun,
            R.string.date_fortune_weekday_mon,
            R.string.date_fortune_weekday_tue,
            R.string.date_fortune_weekday_wed,
            R.string.date_fortune_weekday_thu,
            R.string.date_fortune_weekday_fri,
            R.string.date_fortune_weekday_sat,
        )
    Row(modifier = modifier.fillMaxWidth()) {
        labels.forEach { labelRes ->
            Text(
                text = stringResource(labelRes),
                style = TodakunTypography.caption1Medium,
                color = TodakunColor.gray700,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f).height(CellSize),
            )
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate,
    state: CalendarDayState,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = state != CalendarDayState.PAST
    val textColor =
        when (state) {
            CalendarDayState.PAST -> TodakunColor.gray300
            CalendarDayState.SELECTED -> TodakunColor.gray975
            CalendarDayState.TODAY -> TodakunColor.primary600
            CalendarDayState.DEFAULT -> TodakunColor.gray975
        }

    Column(
        modifier =
            modifier
                .size(CellSize)
                .clip(CircleShape)
                .background(if (state == CalendarDayState.SELECTED) TodakunColor.primary400 else TodakunColor.white)
                .then(
                    if (enabled) {
                        Modifier.clickable { onClick(date) }
                    } else {
                        Modifier
                    },
                ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            style = TodakunTypography.body2Medium,
            color = textColor,
            textAlign = TextAlign.Center,
        )
        if (state == CalendarDayState.TODAY) {
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = stringResource(R.string.date_fortune_today_label),
                style = TodakunTypography.caption3Medium,
                color = TodakunColor.primary600,
                textAlign = TextAlign.Center,
            )
        }
    }
}
