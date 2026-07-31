package com.kikidan.domain.model.auth

@JvmInline
value class OnboardingToken(
    val value: String,
) {
    init {
        require(value.isNotBlank()) { "OAuth token must not be blank" }
    }

    /** 로그·크래시 리포트로 토큰이 새는 것을 막는다. */
    override fun toString(): String = "OAuthToken(REDACTED)"
}
