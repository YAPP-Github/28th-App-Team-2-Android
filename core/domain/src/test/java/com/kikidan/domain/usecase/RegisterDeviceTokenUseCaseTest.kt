package com.kikidan.domain.usecase

import com.kikidan.domain.fake.FakeDeviceTokenRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class RegisterDeviceTokenUseCaseTest {
    private lateinit var fakeDeviceTokenRepository: FakeDeviceTokenRepository
    private lateinit var useCase: RegisterDeviceTokenUseCase

    @Before
    fun setUp() {
        fakeDeviceTokenRepository = FakeDeviceTokenRepository()
        useCase = RegisterDeviceTokenUseCase(fakeDeviceTokenRepository)
    }

    @Test
    fun `성공 시 요청한 token으로 위임한다`() =
        runTest {
            val result = useCase("fcm-token")

            assertEquals("fcm-token", fakeDeviceTokenRepository.lastRegisteredToken)
            assertTrue(result.isSuccess)
        }

    @Test
    fun `실패 시 Result failure를 그대로 전파한다`() =
        runTest {
            val error = IllegalStateException("network")
            fakeDeviceTokenRepository.registerResult = Result.failure(error)

            val result = useCase("fcm-token")

            assertTrue(result.isFailure)
            assertEquals(error, result.exceptionOrNull())
        }
}
