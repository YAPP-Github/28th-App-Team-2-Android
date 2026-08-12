package com.kikidan.domain.model.fortune

import java.time.LocalDate

data class DailyFortuneHistoryEntry(
    val id: String,
    val fortuneDate: LocalDate,
    val actions: List<LuckAction>,
)
