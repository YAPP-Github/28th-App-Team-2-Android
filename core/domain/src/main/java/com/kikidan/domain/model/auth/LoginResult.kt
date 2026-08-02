package com.kikidan.domain.model.auth

data class LoginResult(
    val authToken: AuthToken?,
    val onboardingToken: OnboardingToken?,
    val newMember: Boolean,
)
