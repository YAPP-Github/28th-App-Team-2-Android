package com.kikidan.data.repository

import com.kikidan.data.fake.FakeRemoteYearFortuneDataSource
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneCategoryStar
import com.kikidan.domain.model.fortune.YearFortune
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class YearFortuneRepositoryImplTest {
    private lateinit var fakeRemoteFortuneDataSource: FakeRemoteYearFortuneDataSource
    private lateinit var sut: YearFortuneRepositoryImpl

    @Before
    fun setUp() {
        fakeRemoteFortuneDataSource = FakeRemoteYearFortuneDataSource()
        sut = YearFortuneRepositoryImpl(fakeRemoteFortuneDataSource)
    }

    @Test
    fun `createYearFortune이_성공하면_DataSource의_YearFortune이_그대로_Result_success로_반환된다`() =
        runTest {
            // given
            val expected =
                YearFortune(
                    id = "f-2",
                    year = 2027,
                    score = 90,
                    title = "title-2",
                    content = "content-2",
                    categories = listOf(FortuneCategoryStar(FortuneCategory.HEALTH, 5)),
                )
            fakeRemoteFortuneDataSource.yearFortune = expected

            // when
            val result = sut.createYearFortune(2027)

            // then
            assertEquals(expected, result.getOrNull())
        }

    @Test
    fun `DataSource의_postYearFortune이_예외를_throw하면_Result_failure로_반환되고_예외가_누수되지_않는다`() =
        runTest {
            // given
            fakeRemoteFortuneDataSource.throwOnPostYearFortune = IOException("network error")

            // when
            val result = sut.createYearFortune(2026)

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test(expected = CancellationException::class)
    fun `DataSource의_postYearFortune이_CancellationException을_throw하면_Result로_감싸지지_않고_그대로_전파된다`() =
        runTest {
            // given
            fakeRemoteFortuneDataSource.throwOnPostYearFortune = CancellationException("cancelled")

            // when
            sut.createYearFortune(2026)
        }

    @Test
    fun `getYearFortune이_성공하면_DataSource의_YearFortune이_그대로_Result_success로_반환된다`() =
        runTest {
            // given
            val expected =
                YearFortune(
                    id = "f-2",
                    year = 2027,
                    score = 90,
                    title = "title-2",
                    content = "content-2",
                    categories = listOf(FortuneCategoryStar(FortuneCategory.HEALTH, 5)),
                )
            fakeRemoteFortuneDataSource.yearFortune = expected

            // when
            val result = sut.getYearFortune("f-2")

            // then
            assertEquals(expected, result.getOrNull())
        }

    @Test
    fun `DataSource의_getYearFortune이_예외를_throw하면_Result_failure로_반환되고_예외가_누수되지_않는다`() =
        runTest {
            // given
            fakeRemoteFortuneDataSource.throwOnGetYearFortune = IOException("network error")

            // when
            val result = sut.getYearFortune("f-1")

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test(expected = CancellationException::class)
    fun `DataSource의_getYearFortune이_CancellationException을_throw하면_Result로_감싸지지_않고_그대로_전파된다`() =
        runTest {
            // given
            fakeRemoteFortuneDataSource.throwOnGetYearFortune = CancellationException("cancelled")

            // when
            sut.getYearFortune("f-1")
        }
}
