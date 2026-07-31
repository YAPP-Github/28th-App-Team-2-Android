package com.kikidan.data_remote.dto.auth

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val onboardingToken: String,
    val newMember: Boolean,
)

fun LoginResponse.toDomain(): LoginResult =
    LoginResult(
        authToken =
            AuthToken(
                accessToken = accessToken,
                refreshToken = refreshToken,
            ),
        onboardingToken = onboardingToken,
        newMember = newMember,
    )
