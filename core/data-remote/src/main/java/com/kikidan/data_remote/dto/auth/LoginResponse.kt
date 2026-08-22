package com.kikidan.data_remote.dto.auth

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OnboardingToken
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken: String?,
    val refreshToken: String?,
    val onboardingToken: String?,
    val isNewMember: Boolean,
)

fun LoginResponse.toDomain(): LoginResult =
    LoginResult(
        authToken =
            if (accessToken != null && refreshToken != null) {
                AuthToken(
                    accessToken = accessToken,
                    refreshToken = refreshToken,
                )
            } else {
                null
            },
        onboardingToken = if (onboardingToken != null) OnboardingToken(onboardingToken) else null,
        newMember = isNewMember,
    )
