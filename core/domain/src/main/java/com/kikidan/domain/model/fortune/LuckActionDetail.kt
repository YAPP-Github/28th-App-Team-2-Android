package com.kikidan.domain.model.fortune

data class LuckActionDetail(
    val id: String,
    val category: FortuneCategory,
    val score: Int,
    val title: String,
    val content: String,
    val achieved: Boolean,
)
