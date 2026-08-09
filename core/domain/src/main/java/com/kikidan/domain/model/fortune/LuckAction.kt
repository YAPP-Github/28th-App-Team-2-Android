package com.kikidan.domain.model.fortune

data class LuckAction(
    val id: String,
    val category: FortuneCategory,
    val title: String,
    val achieved: Boolean,
)
