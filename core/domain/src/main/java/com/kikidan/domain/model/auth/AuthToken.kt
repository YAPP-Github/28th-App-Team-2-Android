package com.kikidan.domain.model.auth

data class AuthToken(
    val accessToken: String,
    val refreshToken: String,
)
