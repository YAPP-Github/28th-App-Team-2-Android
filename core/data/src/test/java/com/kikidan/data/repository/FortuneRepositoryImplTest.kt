package com.kikidan.data.repository

import com.kikidan.data.fake.FakeRemoteFortuneDataSource
import com.kikidan.domain.model.fortune.DailyFortuneHistoryEntry
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneScore
import com.kikidan.domain.model.fortune.LuckAction
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException
import java.time.LocalDate

class FortuneRepositoryImplTest {
    private lateinit var fake: FakeRemoteFortuneDataSource
    private lateinit var sut: FortuneRepositoryImpl

    @Before
    fun setUp() {
        fake = FakeRemoteFortuneDataSource()
        sut = FortuneRepositoryImpl(fake)
    }

    @Test
    fun `getTodayFortuneScores가 성공하면 Result success로 반환된다`() =
        runTest {
            fake.scores = listOf(FortuneScore(FortuneCategory.LOVE, 21))

            val result = sut.getTodayFortuneScores()

            assertEquals(Result.success(fake.scores), result)
        }

    @Test
    fun `getTodayFortuneScores가 IOException을 throw하면 Result failure로 반환된다`() =
        runTest {
            fake.throwOnGetTodayFortuneScores = IOException("network")

            val result = sut.getTodayFortuneScores()

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test(expected = CancellationException::class)
    fun `getTodayFortuneScores가 CancellationException을 throw하면 그대로 전파된다`() =
        runTest {
            fake.throwOnGetTodayFortuneScores = CancellationException("cancelled")
            sut.getTodayFortuneScores()
        }

    @Test
    fun `getFortuneRecordForDate는 이번달과 지난달 history를 합쳐 날짜가 일치하는 항목을 찾는다`() =
        runTest {
            val date = LocalDate.of(2026, 7, 24)
            fake.history =
                listOf(
                    DailyFortuneHistoryEntry(
                        id = "f-1",
                        fortuneDate = date,
                        actions = listOf(LuckAction("a-1", FortuneCategory.LOVE, "제목", true)),
                    ),
                )
            fake.detailScores = listOf(FortuneScore(FortuneCategory.LOVE, 21))

            val result = sut.getFortuneRecordForDate(date)

            // 이번 달(오늘 기준) + 지난달 전체, 두 번 조회해서 합친다.
            assertEquals(2, fake.requestedToValues.size)
            assertEquals("f-1", fake.lastRequestedDailyFortuneId)
            assertTrue(result.isSuccess)
            val record = result.getOrThrow()
            assertEquals(1, record?.scores?.size)
            assertEquals(1, record?.actions?.size)
        }

    @Test
    fun `getFortuneRecordForDate는 일치하는 날짜가 없으면 success(null)을 반환한다`() =
        runTest {
            val date = LocalDate.of(2026, 7, 24)
            fake.history = listOf(DailyFortuneHistoryEntry("f-1", date.minusDays(1), emptyList()))

            val result = sut.getFortuneRecordForDate(date)

            assertTrue(result.isSuccess)
            assertEquals(null, result.getOrThrow())
        }

    @Test
    fun `history 조회는 같은 to를 다시 요청해도 캐시로 처리되어 네트워크 요청이 늘지 않는다`() =
        runTest {
            val date = LocalDate.of(2026, 7, 24)
            fake.history = listOf(DailyFortuneHistoryEntry("f-1", date, emptyList()))

            sut.getFortuneRecordForDate(date)
            val requestedAfterFirstCall = fake.requestedToValues.size
            sut.getEarliestFortuneDate()

            assertEquals(requestedAfterFirstCall, fake.requestedToValues.size)
        }

    @Test
    fun `getFortuneRecordForDate가 IOException을 throw하면 Result failure로 반환된다`() =
        runTest {
            fake.throwOnGetFortuneHistory = IOException("network")

            val result = sut.getFortuneRecordForDate(LocalDate.now())

            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is IOException)
        }

    @Test
    fun `getEarliestFortuneDate는 조회된 항목 중 가장 오래된 날짜를 반환한다`() =
        runTest {
            fake.history =
                listOf(
                    DailyFortuneHistoryEntry("f-1", LocalDate.of(2026, 7, 20), emptyList()),
                    DailyFortuneHistoryEntry("f-2", LocalDate.of(2026, 7, 10), emptyList()),
                )

            val result = sut.getEarliestFortuneDate()

            assertEquals(Result.success(LocalDate.of(2026, 7, 10)), result)
        }

    @Test
    fun `getEarliestFortuneDate는 기록이 전혀 없으면 success(null)을 반환한다`() =
        runTest {
            fake.history = emptyList()

            val result = sut.getEarliestFortuneDate()

            assertEquals(Result.success(null), result)
        }
}
