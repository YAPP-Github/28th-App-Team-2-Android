package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeFortuneRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GetEarliestFortuneDateUseCaseTest {
    @Test
    fun `Repository가 반환한 날짜를 그대로 전달한다`() =
        runTest {
            val date = LocalDate.of(2026, 7, 1)
            val fakeRepository = FakeFortuneRepository().apply { earliestDateResult = Result.success(date) }
            val useCase = GetEarliestFortuneDateUseCase(fakeRepository)

            val result = useCase()

            assertEquals(Result.success(date), result)
        }

    @Test
    fun `기록이 없으면 success(null)을 전달한다`() =
        runTest {
            val fakeRepository = FakeFortuneRepository().apply { earliestDateResult = Result.success(null) }
            val useCase = GetEarliestFortuneDateUseCase(fakeRepository)

            val result = useCase()

            assertEquals(Result.success(null), result)
        }
}
