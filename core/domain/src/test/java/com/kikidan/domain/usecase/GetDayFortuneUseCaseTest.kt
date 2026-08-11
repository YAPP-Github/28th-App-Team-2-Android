package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeDayFortuneRepository
import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetDayFortuneUseCaseTest {
    private lateinit var fakeRepository: FakeDayFortuneRepository
    private lateinit var sut: GetDayFortuneUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeDayFortuneRepository()
        sut = GetDayFortuneUseCase(fakeRepository)
    }

    @Test
    fun `id를_그대로_Repository에_전달하고_결과를_그대로_반환한다`() =
        runTest {
            // given
            val expected =
                DayFortune(
                    id = "id-1",
                    purpose = DayFortunePurpose.TRAVEL,
                    targetDate = LocalDate.of(2026, 8, 10),
                    score = 85,
                    title = "title",
                    content = "content",
                    categoryStars = emptyList(),
                )
            fakeRepository.getResult = Result.success(expected)

            // when
            val result = sut("id-1")

            // then
            assertEquals(expected, result.getOrNull())
            assertEquals("id-1", fakeRepository.lastGetId)
        }

    @Test
    fun `Repository가_실패하면_실패가_그대로_전파된다`() =
        runTest {
            // given
            val expected = IllegalStateException("서버 오류")
            fakeRepository.getResult = Result.failure(expected)

            // when
            val result = sut("id-1")

            // then
            assertEquals(expected, result.exceptionOrNull())
        }
}
