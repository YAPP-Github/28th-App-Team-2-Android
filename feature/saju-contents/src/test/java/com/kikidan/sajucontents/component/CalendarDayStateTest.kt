package com.kikidan.sajucontents.component

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class CalendarDayStateTest {
    private val today = LocalDate.of(2026, 8, 10)

    @Test
    fun `과거_날짜는_PAST로_판정된다`() {
        val pastDate = today.minusDays(1)

        val state = resolveCalendarDayState(pastDate, today, emptyList())

        assertEquals(CalendarDayState.PAST, state)
    }

    @Test
    fun `오늘_날짜는_선택되지_않았으면_TODAY로_판정된다`() {
        val state = resolveCalendarDayState(today, today, emptyList())

        assertEquals(CalendarDayState.TODAY, state)
    }

    @Test
    fun `미래_날짜이면서_미선택이면_DEFAULT로_판정된다`() {
        val futureDate = today.plusDays(3)

        val state = resolveCalendarDayState(futureDate, today, emptyList())

        assertEquals(CalendarDayState.DEFAULT, state)
    }

    @Test
    fun `미래_날짜이면서_선택되었으면_SELECTED로_판정된다`() {
        val futureDate = today.plusDays(3)

        val state = resolveCalendarDayState(futureDate, today, listOf(futureDate))

        assertEquals(CalendarDayState.SELECTED, state)
    }

    @Test
    fun `과거_날짜는_선택_목록에_있어도_PAST가_우선한다`() {
        val pastDate = today.minusDays(1)

        val state = resolveCalendarDayState(pastDate, today, listOf(pastDate))

        assertEquals(CalendarDayState.PAST, state)
    }
}
