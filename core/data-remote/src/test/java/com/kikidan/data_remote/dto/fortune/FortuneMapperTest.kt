package com.kikidan.data_remote.dto.fortune

import com.kikidan.domain.model.fortune.FortuneCategory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FortuneMapperTest {
    @Test
    fun `LuckActionScoreResponse가 FortuneScore 도메인으로 변환된다`() {
        val response = LuckActionScoreResponse(id = "s-1", fortuneCategory = "LOVE", score = 21)

        val domain = response.toDomain()

        assertEquals(FortuneCategory.LOVE, domain.category)
        assertEquals(21, domain.score)
    }

    @Test
    fun `TodayFortuneResponse의 luckActionScores가 FortuneScore 목록으로 변환된다`() {
        val response =
            TodayFortuneResponse(
                id = "f-1",
                fortuneDate = "2026-07-24",
                score = 60,
                title = "오늘의 운세",
                luckActionScores =
                    listOf(
                        LuckActionScoreResponse("s-1", "RELATIONSHIP", 84),
                        LuckActionScoreResponse("s-2", "MONEY", 93),
                    ),
            )

        val domain = response.toFortuneScores()

        assertEquals(2, domain.size)
        assertEquals(FortuneCategory.RELATIONSHIP, domain[0].category)
        assertEquals(93, domain[1].score)
    }

    @Test
    fun `TodayLuckActionResponse가 LuckAction 도메인으로 변환된다`() {
        val response =
            TodayLuckActionResponse(
                id = "a-1",
                fortuneCategory = "HEALTH",
                score = 5,
                title = "산책하기",
                achieved = false,
            )

        val domain = response.toDomain()

        assertEquals("a-1", domain.id)
        assertEquals(FortuneCategory.HEALTH, domain.category)
        assertEquals("산책하기", domain.title)
        assertEquals(false, domain.achieved)
    }

    @Test
    fun `LuckActionResponse가 LuckAction 도메인으로 변환된다`() {
        val response =
            LuckActionResponse(
                id = "a-1",
                fortuneCategory = "ACHIEVEMENT",
                score = 5,
                title = "업무 끝내기",
                content = "설명",
                achieved = true,
            )

        val domain = response.toDomain()

        assertEquals("a-1", domain.id)
        assertEquals(FortuneCategory.ACHIEVEMENT, domain.category)
        assertEquals(true, domain.achieved)
    }

    @Test
    fun `LuckActionSummaryResponse가 LuckAction 도메인으로 변환된다`() {
        val response = LuckActionSummaryResponse(id = "a-1", fortuneCategory = "MONEY", title = "제목", achieved = true)

        val domain = response.toDomain()

        assertEquals("a-1", domain.id)
        assertEquals(FortuneCategory.MONEY, domain.category)
        assertEquals(true, domain.achieved)
    }

    @Test
    fun `DailyFortuneHistoryResponse가 DailyFortuneHistoryEntry 도메인으로 변환된다`() {
        val response =
            DailyFortuneHistoryResponse(
                id = "f-1",
                fortuneDate = "2026-07-24",
                luckActions = listOf(LuckActionSummaryResponse("a-1", "LOVE", "제목", false)),
            )

        val domain = response.toDomain()

        assertEquals("f-1", domain.id)
        assertEquals(LocalDate.of(2026, 7, 24), domain.fortuneDate)
        assertEquals(1, domain.actions.size)
    }

    @Test
    fun `DailyFortuneResponse의 luckActionScores가 FortuneScore 목록으로 변환된다`() {
        val response =
            DailyFortuneResponse(
                id = "f-1",
                luckActionScores = listOf(LuckActionScoreResponse("s-1", "HEALTH", 60)),
            )

        val domain = response.toFortuneScores()

        assertEquals(1, domain.size)
        assertEquals(FortuneCategory.HEALTH, domain[0].category)
    }
}
