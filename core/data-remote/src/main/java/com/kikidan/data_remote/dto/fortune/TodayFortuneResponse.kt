package com.kikidan.data_remote.dto.fortune

import com.kikidan.domain.model.fortune.FortuneScore
import kotlinx.serialization.Serializable

@Serializable
data class TodayFortuneResponse(
    val id: String,
    val fortuneDate: String,
    val score: Int,
    val title: String,
    val luckActionScores: List<LuckActionScoreResponse> = emptyList(),
)

internal fun TodayFortuneResponse.toFortuneScores(): List<FortuneScore> = luckActionScores.map { it.toDomain() }
