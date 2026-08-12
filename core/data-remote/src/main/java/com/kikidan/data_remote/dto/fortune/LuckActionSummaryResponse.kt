package com.kikidan.data_remote.dto.fortune

import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.LuckAction
import kotlinx.serialization.Serializable

@Serializable
data class LuckActionSummaryResponse(
    val id: String,
    val fortuneCategory: String,
    val title: String,
    val achieved: Boolean,
)

internal fun LuckActionSummaryResponse.toDomain(): LuckAction =
    LuckAction(
        id = id,
        category = FortuneCategory.valueOf(fortuneCategory),
        title = title,
        achieved = achieved,
    )
