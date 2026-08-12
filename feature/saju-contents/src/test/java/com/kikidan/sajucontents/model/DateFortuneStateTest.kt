package com.kikidan.sajucontents.model

import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.model.user.Gender
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class DateFortuneStateTest {
    @Test
    fun `목적이_없으면_canSubmit은_false다`() {
        val state = DateFortuneState(selectedPurpose = null, selectedDates = persistentListOf(LocalDate.now()))

        assertFalse(state.canSubmit)
    }

    @Test
    fun `날짜가_0개면_canSubmit은_false다`() {
        val state = DateFortuneState(selectedPurpose = DayFortunePurpose.TRAVEL, selectedDates = persistentListOf())

        assertFalse(state.canSubmit)
    }

    @Test
    fun `제출_중이면_canSubmit은_false다`() {
        val state =
            DateFortuneState(
                selectedPurpose = DayFortunePurpose.TRAVEL,
                selectedDates = persistentListOf(LocalDate.now()),
                isLoading = true,
            )

        assertFalse(state.canSubmit)
    }

    @Test
    fun `목적과_날짜가_있으면_성별_미선택이어도_canSubmit은_true다`() {
        // 서버로 보내지 않는 값이라 필수 조건에서 제외한다 (블로커 B-2 회귀 방지).
        val state =
            DateFortuneState(
                selectedPurpose = DayFortunePurpose.TRAVEL,
                selectedGender = null,
                selectedDates = persistentListOf(LocalDate.now()),
            )

        assertTrue(state.canSubmit)
    }

    @Test
    fun `목적_날짜_성별이_모두_있으면_canSubmit은_true다`() {
        val state =
            DateFortuneState(
                selectedPurpose = DayFortunePurpose.TRAVEL,
                selectedGender = Gender.MALE,
                selectedDates = persistentListOf(LocalDate.now()),
            )

        assertTrue(state.canSubmit)
    }
}
