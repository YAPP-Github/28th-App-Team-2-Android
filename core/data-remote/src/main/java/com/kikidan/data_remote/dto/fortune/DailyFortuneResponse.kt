package com.kikidan.data_remote.dto.fortune

import com.kikidan.domain.model.fortune.FortuneScore
import kotlinx.serialization.Serializable

@Serializable
data class DailyFortuneResponse(
    val id: String,
    val luckActionScores: List<LuckActionScoreResponse> = emptyList(),
)

internal fun DailyFortuneResponse.toFortuneScores(): List<FortuneScore> = luckActionScores.map { it.toDomain() }
