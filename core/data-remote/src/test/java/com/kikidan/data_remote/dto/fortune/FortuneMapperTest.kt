package com.kikidan.data_remote.dto.fortune

import com.kikidan.domain.model.fortune.FortuneCategory
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class FortuneMapperTest {
    @Test
    fun `LuckActionScoreResponse가 FortuneScore 도메인으로 변환되고 luckActionId가 채워진다`() {
        val response = LuckActionScoreResponse(id = "s-1", fortuneCategory = "LOVE", score = 21)

        val domain = response.toDomain()

        assertEquals(FortuneCategory.LOVE, domain.category)
        assertEquals(21, domain.score)
        assertEquals("s-1", domain.luckActionId)
    }

    @Test
    fun `TodayFortuneResponse가 TodayFortune 도메인으로 변환된다`() {
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

        val domain = response.toDomain()

        assertEquals("f-1", domain.id)
        assertEquals(LocalDate.of(2026, 7, 24), domain.date)
        assertEquals(60, domain.totalScore)
        assertEquals("오늘의 운세", domain.scoreLabel)
        assertEquals(2, domain.scores.size)
        assertEquals(FortuneCategory.RELATIONSHIP, domain.scores[0].category)
        assertEquals(93, domain.scores[1].score)
        assertEquals("s-2", domain.scores[1].luckActionId)
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
    fun `LuckActionResponse가 LuckActionDetail 도메인으로 변환된다`() {
        val response =
            LuckActionResponse(
                id = "a-1",
                fortuneCategory = "LOVE",
                score = 80,
                title = "사랑 액션",
                content = "오늘 소중한 사람에게 연락해보세요",
                achieved = false,
            )

        val domain = response.toDetail()

        assertEquals("a-1", domain.id)
        assertEquals(FortuneCategory.LOVE, domain.category)
        assertEquals(80, domain.score)
        assertEquals("사랑 액션", domain.title)
        assertEquals("오늘 소중한 사람에게 연락해보세요", domain.content)
        assertEquals(false, domain.achieved)
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

    @Test
    fun `DailyFortuneResponse가 DailyFortuneDetail 도메인으로 변환된다`() {
        val response =
            DailyFortuneResponse(
                id = "f-1",
                score = 72,
                content = "오늘은 좋은 하루예요.",
                luckyItems = listOf("노란색", "운동화"),
                cautionaryItems = listOf("셔츠"),
                luckActionScores = listOf(LuckActionScoreResponse("s-1", "MONEY", 90)),
            )

        val domain = response.toDetail()

        assertEquals("f-1", domain.id)
        assertEquals(72, domain.totalScore)
        assertEquals("오늘은 좋은 하루예요.", domain.content)
        assertEquals(listOf("노란색", "운동화"), domain.luckyItems)
        assertEquals(listOf("셔츠"), domain.cautionaryItems)
        assertEquals(1, domain.scores.size)
        assertEquals(FortuneCategory.MONEY, domain.scores[0].category)
    }
}
