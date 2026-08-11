package com.kikidan.data.repository

import com.kikidan.data.fake.FakeRemoteDayFortuneDataSource
import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class DayFortuneRepositoryImplTest {
    private lateinit var fakeRemoteDayFortuneDataSource: FakeRemoteDayFortuneDataSource
    private lateinit var sut: DayFortuneRepositoryImpl

    private val purpose = DayFortunePurpose.TRAVEL
    private val targetDates = listOf(LocalDate.of(2026, 8, 10))

    @Before
    fun setUp() {
        fakeRemoteDayFortuneDataSource = FakeRemoteDayFortuneDataSource()
        sut = DayFortuneRepositoryImpl(fakeRemoteDayFortuneDataSource)
    }

    @Test
    fun `DataSource가_정상_반환하면_Result_success로_동일한_값이_반환된다`() =
        runTest {
            // given
            val expected =
                listOf(
                    DayFortune(
                        id = "id-1",
                        purpose = purpose,
                        targetDate = targetDates.first(),
                        score = 85,
                        title = "title",
                        content = "content",
                        categoryStars = emptyList(),
                    ),
                )
            fakeRemoteDayFortuneDataSource.dayFortunes = expected

            // when
            val result = sut.createDayFortunes(purpose, targetDates)

            // then
            assertEquals(expected, result.getOrNull())
        }

    @Test
    fun `DataSource가_IOException을_throw하면_Result_failure로_반환되고_예외가_누수되지_않는다`() =
        runTest {
            // given
            fakeRemoteDayFortuneDataSource.throwOnPost = IOException("network error")

            // when
            val result = sut.createDayFortunes(purpose, targetDates)

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test(expected = CancellationException::class)
    fun `DataSource가_CancellationException을_throw하면_Result로_감싸지지_않고_그대로_전파된다`() =
        runTest {
            // given
            fakeRemoteDayFortuneDataSource.throwOnPost = CancellationException("cancelled")

            // when
            sut.createDayFortunes(purpose, targetDates)
        }

    @Test
    fun `getDayFortune_DataSource가_정상_반환하면_Result_success로_동일한_값이_반환된다`() =
        runTest {
            // given
            val expected =
                DayFortune(
                    id = "id-1",
                    purpose = purpose,
                    targetDate = targetDates.first(),
                    score = 85,
                    title = "title",
                    content = "content",
                    categoryStars = emptyList(),
                )
            fakeRemoteDayFortuneDataSource.dayFortune = expected

            // when
            val result = sut.getDayFortune("id-1")

            // then
            assertEquals(expected, result.getOrNull())
        }

    @Test
    fun `getDayFortune_DataSource가_IOException을_throw하면_Result_failure로_반환되고_예외가_누수되지_않는다`() =
        runTest {
            // given
            fakeRemoteDayFortuneDataSource.throwOnGet = IOException("network error")

            // when
            val result = sut.getDayFortune("id-1")

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }
}
