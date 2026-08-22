package com.kikidan.domain.model.dayfortune

import com.kikidan.domain.model.fortune.FortuneCategoryStar
import java.time.LocalDate

data class DayFortune(
    val id: String,
    val purpose: DayFortunePurpose,
    val targetDate: LocalDate,
    val score: Int,
    val title: String,
    val content: String,
    val categoryStars: List<FortuneCategoryStar>,
)

enum class DayFortunePurpose {
    CONTRACT_MOVING,
    BUSINESS_OPENING,
    TRAVEL,
    CONFESSION_DATING,
    EXAM_INTERVIEW,
}
