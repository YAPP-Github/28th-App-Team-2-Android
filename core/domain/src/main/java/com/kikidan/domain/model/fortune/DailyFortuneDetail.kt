package com.kikidan.domain.model.fortune

data class DailyFortuneDetail(
    val id: String,
    val totalScore: Int,
    val title: String,
    val content: String,
    val luckyItems: List<String>,
    val cautionaryItems: List<String>,
    val scores: List<FortuneScore>,
)
