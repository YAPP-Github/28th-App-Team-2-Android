package com.kikidan.data_remote.dto.auth

import com.kikidan.domain.model.auth.AuthToken
import kotlinx.serialization.Serializable

@Serializable
data class SignupResponse(
    val accessToken: String,
    val refreshToken: String,
)

fun SignupResponse.toDomain(): AuthToken =
    AuthToken(
        accessToken = accessToken,
        refreshToken = refreshToken,
    )
