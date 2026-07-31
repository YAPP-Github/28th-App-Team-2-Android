package com.kikidan.data_remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    val birthDate: String,
    val birthTime: String,
    val calendarType: String,
    val favoriteFortuneCategories: List<String>,
    val gender: String,
    val job: String,
    val name: String,
    val onboardingToken: String,
    val relationshipStatus: String
)
