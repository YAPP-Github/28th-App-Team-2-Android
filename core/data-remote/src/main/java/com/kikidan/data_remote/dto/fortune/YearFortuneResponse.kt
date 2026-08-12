package com.kikidan.data_remote.dto.fortune

import com.kikidan.domain.model.fortune.FortuneCategory
import com.kikidan.domain.model.fortune.FortuneCategoryStar
import com.kikidan.domain.model.fortune.YearFortune
import kotlinx.serialization.Serializable

@Serializable
data class YearFortuneResponse(
    val id: String,
    val year: Int,
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

fun YearFortuneResponse.toDomain(): YearFortune =
    YearFortune(
        id = id,
        year = year,
        score = score,
        title = title,
        content = content,
        categories = fortuneCategories.mapNotNull { it.toDomain() },
    )

// 서버가 새 카테고리를 추가해도 valueOf() 크래시 대신 알 수 없는 카테고리만 스킵한다.
fun FortuneCategoryStarResponse.toDomain(): FortuneCategoryStar? {
    val category = FortuneCategory.entries.find { it.name == fortuneCategory } ?: return null
    return FortuneCategoryStar(
        category = category,
        star = star,
    )
}
