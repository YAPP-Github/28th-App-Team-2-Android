package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeLuckActionRepository
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.LuckActionDetail
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GetLuckActionDetailUseCaseTest {
    private lateinit var fakeLuckActionRepository: FakeLuckActionRepository
    private lateinit var useCase: GetLuckActionDetailUseCase

    @Before
    fun setUp() {
        fakeLuckActionRepository = FakeLuckActionRepository()
        useCase = GetLuckActionDetailUseCase(fakeLuckActionRepository)
    }

    @Test
    fun `성공 시 LuckActionDetail을 그대로 전달한다`() =
        runTest {
            val expected =
                LuckActionDetail(
                    id = "a-1",
                    category = FortuneCategory.LOVE,
                    score = 21,
                    title = "제목",
                    content = "내용",
                    achieved = false,
                )
            fakeLuckActionRepository.luckActionDetailResult = Result.success(expected)

            val result = useCase("a-1")

            assertEquals("a-1", fakeLuckActionRepository.lastRequestedDetailId)
            assertTrue(result.isSuccess)
            assertEquals(expected, result.getOrThrow())
        }

    @Test
    fun `실패 시 Result failure를 그대로 전파한다`() =
        runTest {
            val error = IllegalStateException("not found")
            fakeLuckActionRepository.luckActionDetailResult = Result.failure(error)

            val result = useCase("a-1")

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}
