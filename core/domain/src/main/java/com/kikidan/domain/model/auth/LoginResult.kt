package com.kikidan.domain.model.auth

data class LoginResult(
    val authToken: AuthToken?,
    val onboardingToken: String?,
    val newMember: Boolean,
)
