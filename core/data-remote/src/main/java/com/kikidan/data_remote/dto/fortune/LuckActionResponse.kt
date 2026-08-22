package com.kikidan.data_remote.dto.fortune

import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.LuckAction
import com.kikidan.domain.model.fortune.LuckActionDetail
import kotlinx.serialization.Serializable

@Serializable
data class LuckActionResponse(
    val id: String,
    val fortuneCategory: String,
    val score: Int,
    val title: String,
    val content: String,
    val achieved: Boolean,
)

internal fun LuckActionResponse.toDomain(): LuckAction =
    LuckAction(
        id = id,
        category = FortuneCategory.valueOf(fortuneCategory),
        title = title,
        achieved = achieved,
    )

internal fun LuckActionResponse.toDetail(): LuckActionDetail =
    LuckActionDetail(
        id = id,
        category = FortuneCategory.valueOf(fortuneCategory),
        score = score,
        title = title,
        content = content,
        achieved = achieved,
    )
