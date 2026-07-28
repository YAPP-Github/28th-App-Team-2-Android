package com.kikidan.auth

import com.kikidan.domain.model.auth.OAuthProviderType

/**
 * 서버 LoginRequest.oauthAccessToken에 실리는 자격증명 문자열.
 * KAKAO는 access token, GOOGLE은 ID token(JWT)이다.
 */
@JvmInline
value class OAuthToken(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "OAuth token must not be blank" }
    }

    /** 로그·크래시 리포트로 토큰이 새는 것을 막는다. */
    override fun toString(): String = "OAuthToken(REDACTED)"
}

data class OAuthCredential(
    val provider: OAuthProviderType,
    val token: OAuthToken,
)
