package com.kikidan.data_remote.dto.dayfortune

import kotlinx.serialization.Serializable

@Serializable
data class CreateDayFortuneRequest(
    val purpose: String,
    val targetDates: List<String>,
)
