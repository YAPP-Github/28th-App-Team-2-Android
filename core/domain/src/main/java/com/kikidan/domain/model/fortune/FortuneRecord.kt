package com.kikidan.domain.model.fortune

data class FortuneRecord(
    val scores: List<FortuneScore>,
    val actions: List<LuckAction>,
)
