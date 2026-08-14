package com.kikidan.domain.model.fortune

import java.time.LocalDate

data class TodayFortune(
    val id: String,
    val date: LocalDate,
    val totalScore: Int,
    val scoreLabel: String,
    val scores: List<FortuneScore>,
)
