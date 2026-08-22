package com.kikidan.domain.model.fortune

data class YearFortune(
    val id: String,
    val year: Int,
    val score: Int,
    val title: String,
    val content: String,
    val categories: List<FortuneCategoryStar>,
)
