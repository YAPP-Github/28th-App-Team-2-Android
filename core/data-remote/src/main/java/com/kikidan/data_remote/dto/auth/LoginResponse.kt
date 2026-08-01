package com.kikidan.data_remote.dto.auth

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
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
        onboardingToken = onboardingToken,
        newMember = isNewMember,
    )
