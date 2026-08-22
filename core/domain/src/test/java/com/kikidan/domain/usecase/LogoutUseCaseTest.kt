package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeAuthRepository
import com.kikidan.domain.fake.FakeTokenRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class LogoutUseCaseTest {
    private lateinit var fakeAuthRepository: FakeAuthRepository
    private lateinit var fakeTokenRepository: FakeTokenRepository
    private lateinit var useCase: LogoutUseCase

    @Before
    fun setUp() {
        fakeAuthRepository = FakeAuthRepository()
        fakeTokenRepository = FakeTokenRepository()
        useCase = LogoutUseCase(fakeAuthRepository, fakeTokenRepository)
    }

    @Test
    fun `성공 시 서버 로그아웃과 로컬 토큰 삭제가 모두 호출되고 Result success를 반환한다`() =
        runTest {
            val result = useCase()

            assertTrue(fakeAuthRepository.logoutCalled)
            assertTrue(fakeTokenRepository.clearTokenCalled)
            assertTrue(result.isSuccess)
        }

    @Test
    fun `서버 로그아웃이 실패해도 로컬 토큰은 삭제되고 그 결과를 반환한다`() =
        runTest {
            fakeAuthRepository.logoutResult = Result.failure(IllegalStateException("network"))

            val result = useCase()

            assertTrue(fakeAuthRepository.logoutCalled)
            assertTrue(fakeTokenRepository.clearTokenCalled)
            assertTrue(result.isSuccess)
        }

    @Test
    fun `로컬 토큰 삭제가 실패하면 Result failure를 그대로 전파한다`() =
        runTest {
            val error = IllegalStateException("disk error")
            fakeTokenRepository.clearTokenResult = Result.failure(error)

            val result = useCase()

            assertTrue(result.isFailure)
        }
}
