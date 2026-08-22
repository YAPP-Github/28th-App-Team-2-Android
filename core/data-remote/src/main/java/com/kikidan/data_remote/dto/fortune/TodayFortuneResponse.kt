package com.kikidan.data_remote.dto.fortune

import com.kikidan.domain.model.fortune.TodayFortune
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class TodayFortuneResponse(
    val id: String,
    val fortuneDate: String,
    val score: Int,
    val title: String,
    val luckActionScores: List<LuckActionScoreResponse> = emptyList(),
)

internal fun TodayFortuneResponse.toDomain(): TodayFortune =
    TodayFortune(
        id = id,
        date = LocalDate.parse(fortuneDate),
        totalScore = score,
        scoreLabel = title,
        scores = luckActionScores.map { it.toDomain() },
    )
