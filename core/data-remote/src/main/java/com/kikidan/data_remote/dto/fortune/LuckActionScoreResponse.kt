package com.kikidan.data_remote.dto.fortune

import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneScore
import kotlinx.serialization.Serializable

@Serializable
data class LuckActionScoreResponse(
    val id: String,
    val fortuneCategory: String,
    val score: Int,
)

internal fun LuckActionScoreResponse.toDomain(): FortuneScore =
    FortuneScore(
        category = FortuneCategory.valueOf(fortuneCategory),
        score = score,
        luckActionId = id,
    )
