package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeDayFortuneRepository
import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class CreateDayFortunesUseCaseTest {
    private lateinit var fakeRepository: FakeDayFortuneRepository
    private lateinit var sut: CreateDayFortunesUseCase

    private val purpose = DayFortunePurpose.TRAVEL

    @Before
    fun setUp() {
        fakeRepository = FakeDayFortuneRepository()
        sut = CreateDayFortunesUseCase(fakeRepository)
    }

    @Test
    fun `날짜가_0개면_실패하고_Repository를_호출하지_않는다`() =
        runTest {
            // when
            val result = sut(purpose, emptyList())

            // then
            assertTrue(result.isFailure)
            assertEquals(0, fakeRepository.callCount)
        }

    @Test
    fun `날짜가_상한을_초과하면_실패하고_Repository를_호출하지_않는다`() =
        runTest {
            // given
            val today = LocalDate.now()
            val sixDates = (0 until DateFortuneDefaults.MAX_TARGET_DATES + 1).map { today.plusDays(it.toLong()) }

            // when
            val result = sut(purpose, sixDates)

            // then
            assertTrue(result.isFailure)
            assertEquals(0, fakeRepository.callCount)
        }

    @Test
    fun `과거_날짜가_포함되면_실패하고_Repository를_호출하지_않는다`() =
        runTest {
            // given
            val pastDate = LocalDate.now().minusDays(1)

            // when
            val result = sut(purpose, listOf(pastDate))

            // then
            assertTrue(result.isFailure)
            assertEquals(0, fakeRepository.callCount)
        }

    @Test
    fun `오늘_날짜는_통과한다`() =
        runTest {
            // given
            val today = LocalDate.now()
            fakeRepository.result = Result.success(emptyList())

            // when
            val result = sut(purpose, listOf(today))

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, fakeRepository.callCount)
        }

    @Test
    fun `날짜가_정확히_5개면_통과한다`() =
        runTest {
            // given
            val today = LocalDate.now()
            val fiveDates = (0 until DateFortuneDefaults.MAX_TARGET_DATES).map { today.plusDays(it.toLong()) }
            fakeRepository.result = Result.success(emptyList())

            // when
            val result = sut(purpose, fiveDates)

            // then
            assertTrue(result.isSuccess)
            assertEquals(1, fakeRepository.callCount)
        }

    @Test
    fun `유효한_입력이면_Repository의_반환값이_그대로_전달된다`() =
        runTest {
            // given
            val today = LocalDate.now()
            val expected =
                listOf(
                    DayFortune(
                        id = "id-1",
                        purpose = purpose,
                        targetDate = today,
                        score = 85,
                        title = "title",
                        content = "content",
                        categoryStars = emptyList(),
                    ),
                )
            fakeRepository.result = Result.success(expected)

            // when
            val result = sut(purpose, listOf(today))

            // then
            assertEquals(expected, result.getOrNull())
            assertEquals(purpose, fakeRepository.lastPurpose)
            assertEquals(listOf(today), fakeRepository.lastTargetDates)
        }

    @Test
    fun `Repository가_실패하면_실패가_그대로_전파된다`() =
        runTest {
            // given
            val today = LocalDate.now()
            val expected = IllegalStateException("서버 오류")
            fakeRepository.result = Result.failure(expected)

            // when
            val result = sut(purpose, listOf(today))

            // then
            assertTrue(result.isFailure)
            assertEquals(expected, result.exceptionOrNull())
        }
}
