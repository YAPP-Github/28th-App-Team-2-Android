package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeLuckActionRepository
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.LuckAction
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ToggleLuckActionUseCaseTest {
    private lateinit var fakeRepository: FakeLuckActionRepository
    private lateinit var useCase: ToggleLuckActionUseCase

    @Before
    fun setUp() {
        fakeRepository = FakeLuckActionRepository()
        useCase = ToggleLuckActionUseCase(fakeRepository)
    }

    @Test
    fun `토글 성공 시 갱신된 LuckAction을 반환하고 id를 그대로 전달한다`() =
        runTest {
            val updated = LuckAction("1", FortuneCategory.LOVE, "제목", achieved = true)
            fakeRepository.toggleResult = Result.success(updated)

            val result = useCase("1")

            assertEquals("1", fakeRepository.lastToggledId)
            assertTrue(result.isSuccess)
            assertEquals(updated, result.getOrThrow())
        }

    @Test
    fun `토글 실패 시 failure를 그대로 전달한다`() =
        runTest {
            val error = IllegalStateException("토글 실패")
            fakeRepository.toggleResult = Result.failure(error)

            val result = useCase("1")

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}
