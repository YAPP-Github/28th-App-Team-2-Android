package com.kikidan.data_remote.dto.dayfortune

import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneCategoryStar
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class DayFortuneResponse(
    val id: String,
    val purpose: String,
    val targetDate: String,
    val score: Int,
    val title: String,
    val content: String,
    val fortuneCategories: List<FortuneCategoryStarResponse>,
)

@Serializable
data class FortuneCategoryStarResponse(
    val fortuneCategory: String,
    val star: Int,
)

fun DayFortuneResponse.toDomain(): DayFortune =
    DayFortune(
        id = id,
        purpose =
            DayFortunePurpose.entries.find { it.name == purpose }
                ?: throw IllegalArgumentException("알 수 없는 purpose 입니다: $purpose"),
        targetDate = LocalDate.parse(targetDate),
        score = score,
        title = title,
        content = content,
        categoryStars = fortuneCategories.mapNotNull { it.toDomainOrNull() },
    )

private fun FortuneCategoryStarResponse.toDomainOrNull(): FortuneCategoryStar? {
    val category = FortuneCategory.entries.find { it.name == fortuneCategory } ?: return null
    return FortuneCategoryStar(category = category, star = star)
}
