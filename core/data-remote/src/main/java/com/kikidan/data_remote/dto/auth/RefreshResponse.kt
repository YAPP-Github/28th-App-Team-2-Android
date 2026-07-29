package com.kikidan.data_remote.dto.auth

import com.kikidan.domain.model.auth.AuthToken
import kotlinx.serialization.Serializable

@Serializable
data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)

internal fun RefreshResponse.toDomain(): AuthToken =
    AuthToken(
        accessToken = accessToken,
        refreshToken = refreshToken,
    )
