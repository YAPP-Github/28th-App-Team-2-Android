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
import com.kikidan.designsystem.R
import com.kizitonwose.calendar.compose.VerticalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.PersistentList
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/** 캘린더 날짜 셀의 시각 상태 4종. 우선순위: 과거 > 선택 > 오늘 > 기본. */
internal enum class CalendarDayState {
    PAST,
    SELECTED,
    TODAY,
    DEFAULT,
}

/**
 * 날짜 하나의 캘린더 셀 상태를 판정하는 순수 함수.
 * Compose에 의존하지 않아 유닛 테스트로 직접 검증한다.
 */
internal fun resolveCalendarDayState(
    date: LocalDate,
    today: LocalDate,
    selectedDates: ImmutableList<LocalDate>,
): CalendarDayState =
    when {
        date.isBefore(today) -> CalendarDayState.PAST
        selectedDates.contains(date) -> CalendarDayState.SELECTED
        date.isEqual(today) -> CalendarDayState.TODAY
        else -> CalendarDayState.DEFAULT
    }

private const val MONTH_COUNT = 12L

@Composable
internal fun MultiSelectCalendar(
    selectedDates: ImmutableList<LocalDate>,
    onDateToggle: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    val startMonth = remember(today) { YearMonth.from(today) }
    val endMonth = remember(startMonth) { startMonth.plusMonths(MONTH_COUNT - 1) }
    val calendarState =
        rememberCalendarState(
            startMonth = startMonth,
            endMonth = endMonth,
            firstVisibleMonth = startMonth,
            firstDayOfWeek = DayOfWeek.SUNDAY,
        )

    VerticalCalendar(
        state = calendarState,
        modifier = modifier,
        monthHeader = { month ->
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${month.yearMonth.year}.${month.yearMonth.monthValue}",
                    style = TodakunTypography.body2SemiBold,
                    color = TodakunColor.gray975,
                )
                Spacer(modifier = Modifier.height(32.dp))
                WeekDaysHeader()
            }
        },
        dayContent = { day ->
            if (day.position == DayPosition.MonthDate) {
                DayCell(
                    day = day,
                    state = resolveCalendarDayState(day.date, today, selectedDates),
                    onClick = onDateToggle,
                    modifier = Modifier.align(Alignment.Center),
                )
            }
        },
    )
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
                modifier = Modifier.weight(1f).height(44.dp),
            )
        }
    }
}

@Composable
private fun DayCell(
    day: CalendarDay,
    state: CalendarDayState,
    onClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
) {
    val enabled = state != CalendarDayState.PAST
    val textColor =
        when (state) {
            CalendarDayState.PAST -> TodakunColor.gray300
            CalendarDayState.SELECTED -> TodakunColor.white
            CalendarDayState.TODAY -> TodakunColor.primary600
            CalendarDayState.DEFAULT -> TodakunColor.gray975
        }

    Box(
        modifier =
            modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(if (state == CalendarDayState.SELECTED) TodakunColor.primary400 else TodakunColor.white)
                .then(
                    if (enabled) {
                        Modifier.clickable { onClick(day.date) }
                    } else {
                        Modifier
                    },
                ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = day.date.dayOfMonth.toString(),
            style = TodakunTypography.body2Medium,
            color = textColor,
            textAlign = TextAlign.Center,
        )
        if (state == CalendarDayState.TODAY) {
            Text(
                modifier = Modifier.align(Alignment.BottomCenter),
                text = stringResource(R.string.date_fortune_today_label),
                style = TodakunTypography.caption3Medium,
                color = TodakunColor.primary600,
                textAlign = TextAlign.Center,
            )
        }
    }
}
