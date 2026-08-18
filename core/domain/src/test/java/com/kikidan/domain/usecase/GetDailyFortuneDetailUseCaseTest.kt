package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeFortuneRepository
import com.kikidan.domain.model.fortune.DailyFortuneDetail
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneScore
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetDailyFortuneDetailUseCaseTest {
    private lateinit var fakeFortuneRepository: FakeFortuneRepository
    private lateinit var useCase: GetDailyFortuneDetailUseCase

    @Before
    fun setUp() {
        fakeFortuneRepository = FakeFortuneRepository()
        useCase = GetDailyFortuneDetailUseCase(fakeFortuneRepository)
    }

    @Test
    fun `성공 시 DailyFortuneDetail을 그대로 전달한다`() =
        runTest {
            val expected =
                DailyFortuneDetail(
                    id = "f-1",
                    totalScore = 72,
                    content = "오늘은 좋은 하루예요.",
                    luckyItems = listOf("노란색", "운동화"),
                    cautionaryItems = listOf("셔츠"),
                    title = "",
                    scores = listOf(FortuneScore(FortuneCategory.LOVE, 84, "la-1")),
                )
            fakeFortuneRepository.dailyFortuneDetailResult = Result.success(expected)

            val result = useCase("f-1")

            assertEquals("f-1", fakeFortuneRepository.lastRequestedDailyFortuneId)
            assertTrue(result.isSuccess)
            assertEquals(expected, result.getOrThrow())
        }

    @Test
    fun `실패 시 Result failure를 그대로 전파한다`() =
        runTest {
            val error = IllegalStateException("not found")
            fakeFortuneRepository.dailyFortuneDetailResult = Result.failure(error)

            val result = useCase("f-1")

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}
