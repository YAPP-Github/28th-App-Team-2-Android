package com.kikidan.data.repository

import com.kikidan.data.fake.FakeRemoteLuckActionDataSource
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class LuckActionRepositoryImplTest {
    private lateinit var fake: FakeRemoteLuckActionDataSource
    private lateinit var sut: LuckActionRepositoryImpl

    @Before
    fun setUp() {
        fake = FakeRemoteLuckActionDataSource()
        sut = LuckActionRepositoryImpl(fake)
    }

    @Test
    fun `getTodayLuckActions가 성공하면 Result success로 반환된다`() =
        runTest {
            val result = sut.getTodayLuckActions()
            assertEquals(Result.success(fake.actions), result)
        }

    @Test
    fun `toggleAchievement가 id를 그대로 전달하고 갱신된 LuckAction을 반환한다`() =
        runTest {
            val result = sut.toggleAchievement("42")

            assertEquals("42", fake.lastPatchedId)
            assertEquals(Result.success(fake.toggled), result)
        }

    @Test
    fun `toggleAchievement가 IOException을 throw하면 Result failure로 반환된다`() =
        runTest {
            fake.throwOnPatchAchievement = IOException("network")

            val result = sut.toggleAchievement("42")

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test(expected = CancellationException::class)
    fun `toggleAchievement가 CancellationException을 throw하면 그대로 전파된다`() =
        runTest {
            fake.throwOnPatchAchievement = CancellationException("cancelled")
            sut.toggleAchievement("42")
        }
}
