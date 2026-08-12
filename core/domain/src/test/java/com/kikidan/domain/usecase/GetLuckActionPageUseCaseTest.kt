package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeFortuneRepository
import com.kikidan.domain.fake.FakeLuckActionRepository
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneRecord
import com.kikidan.domain.model.fortune.FortuneScore
import com.kikidan.domain.model.fortune.LuckAction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetLuckActionPageUseCaseTest {
    private lateinit var fakeFortuneRepository: FakeFortuneRepository
    private lateinit var fakeLuckActionRepository: FakeLuckActionRepository
    private lateinit var useCase: GetLuckActionPageUseCase

    @Before
    fun setUp() {
        fakeFortuneRepository = FakeFortuneRepository()
        fakeLuckActionRepository = FakeLuckActionRepository()
        useCase = GetLuckActionPageUseCase(fakeFortuneRepository, fakeLuckActionRepository)
    }

    @Test
    fun `오늘 날짜를 요청하면 today 전용 API로 점수와 액션을 합친다`() =
        runTest {
            fakeFortuneRepository.scoresResult = Result.success(listOf(FortuneScore(FortuneCategory.LOVE, 21)))
            fakeLuckActionRepository.actionsResult =
                Result.success(listOf(LuckAction("1", FortuneCategory.LOVE, "제목", false)))

            val result = useCase(LocalDate.now())

            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow()?.scores?.size)
            assertEquals(1, result.getOrThrow()?.actions?.size)
        }

    @Test
    fun `과거 날짜를 요청하면 getFortuneRecordForDate로 조회한다`() =
        runTest {
            val pastDate = LocalDate.now().minusDays(1)
            fakeFortuneRepository.recordResult =
                Result.success(
                    FortuneRecord(
                        scores = listOf(FortuneScore(FortuneCategory.MONEY, 93)),
                        actions = listOf(LuckAction("2", FortuneCategory.MONEY, "제목2", true)),
                    ),
                )

            val result = useCase(pastDate)

            assertEquals(pastDate, fakeFortuneRepository.lastRequestedDate)
            assertTrue(result.isSuccess)
            assertEquals(1, result.getOrThrow()?.scores?.size)
            assertEquals(1, result.getOrThrow()?.actions?.size)
        }

    @Test
    fun `과거 날짜에 기록이 없으면 success(null)을 반환한다`() =
        runTest {
            fakeFortuneRepository.recordResult = Result.success(null)

            val result = useCase(LocalDate.now().minusDays(1))

            assertTrue(result.isSuccess)
            assertNull(result.getOrThrow())
        }

    @Test
    fun `과거 날짜 조회가 실패하면 failure를 그대로 전달한다`() =
        runTest {
            val error = IllegalStateException("기록 없음")
            fakeFortuneRepository.recordResult = Result.failure(error)

            val result = useCase(LocalDate.now().minusDays(1))

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }

    @Test
    fun `오늘 날짜에서 점수 조회가 실패하면 액션을 조회하지 않고 failure를 반환한다`() =
        runTest {
            val error = IllegalStateException("점수 조회 실패")
            fakeFortuneRepository.scoresResult = Result.failure(error)

            val result = useCase(LocalDate.now())

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}
