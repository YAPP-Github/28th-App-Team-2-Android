package com.kikidan.domain.model.auth

data class OAuthCredential(
    val provider: OAuthProviderType,
    val token: OAuthToken,
)
