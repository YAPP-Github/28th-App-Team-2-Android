package com.kikidan.data.repository

import com.kikidan.data.fake.FakeRemoteDeviceTokenDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class DeviceTokenRepositoryImplTest {
    private lateinit var fake: FakeRemoteDeviceTokenDataSource
    private lateinit var sut: DeviceTokenRepositoryImpl

    @Before
    fun setUp() {
        fake = FakeRemoteDeviceTokenDataSource()
        sut = DeviceTokenRepositoryImpl(fake)
    }

    @Test
    fun `registerDeviceToken이 성공하면 요청한 token으로 위임하고 Result success를 반환한다`() =
        runTest {
            val result = sut.registerDeviceToken("fcm-token")

            assertEquals("fcm-token", fake.lastPostedToken)
            assertTrue(result.isSuccess)
        }

    @Test
    fun `registerDeviceToken이 IOException을 throw하면 Result failure로 반환된다`() =
        runTest {
            fake.throwOnPostDeviceToken = IOException("network")

            val result = sut.registerDeviceToken("fcm-token")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test(expected = CancellationException::class)
    fun `registerDeviceToken이 CancellationException을 throw하면 그대로 전파된다`() =
        runTest {
            fake.throwOnPostDeviceToken = CancellationException("cancelled")
            sut.registerDeviceToken("fcm-token")
        }
}
