package com.kikidan.data_remote.dto.fortune

import com.kikidan.domain.model.fortune.DailyFortuneDetail
import com.kikidan.domain.model.fortune.FortuneScore
import kotlinx.serialization.Serializable

@Serializable
data class DailyFortuneResponse(
    val id: String,
    val score: Int = 0,
    val title: String = "",
    val content: String = "",
    val luckyItems: List<String> = emptyList(),
    val cautionaryItems: List<String> = emptyList(),
    val luckActionScores: List<LuckActionScoreResponse> = emptyList(),
)

internal fun DailyFortuneResponse.toFortuneScores(): List<FortuneScore> = luckActionScores.map { it.toDomain() }

internal fun DailyFortuneResponse.toDetail(): DailyFortuneDetail =
    DailyFortuneDetail(
        id = id,
        title = title,
        totalScore = score,
        content = content,
        luckyItems = luckyItems,
        cautionaryItems = cautionaryItems,
        scores = toFortuneScores(),
    )
