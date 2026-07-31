package com.kikidan.data_remote.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val provider: String,
    val oauthAccessToken: String,
)
