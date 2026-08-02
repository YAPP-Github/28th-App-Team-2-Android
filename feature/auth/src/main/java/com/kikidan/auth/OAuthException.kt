package com.kikidan.auth

import com.kikidan.domain.model.auth.OAuthProviderType

/**
 * OAuth 로그인 흐름에서 발생하는 예외.
 *
 * ⚠️ [Cancelled]는 [kotlin.coroutines.cancellation.CancellationException]을 상속하지 않는다.
 * 상속하면 코루틴 취소로 취급되어 runCatching/try-catch를 조용히 통과해
 * ViewModel이 "사용자가 취소했다"를 인지하지 못한 채 아무 일도 안 일어난다.
 */
sealed class OAuthException(
    val provider: OAuthProviderType?,
    message: String?,
    cause: Throwable?,
) : Exception(message, cause) {
    class Cancelled(
        provider: OAuthProviderType?,
        cause: Throwable? = null,
    ) : OAuthException(provider, "User cancelled OAuth login", cause)

    class NoCredential(
        provider: OAuthProviderType?,
        cause: Throwable? = null,
    ) : OAuthException(provider, "No credential available", cause)

    class Network(
        provider: OAuthProviderType?,
        cause: Throwable? = null,
    ) : OAuthException(provider, "Network error during OAuth", cause)

    class Unknown(
        provider: OAuthProviderType?,
        cause: Throwable? = null,
    ) : OAuthException(provider, "Unknown OAuth error", cause)
}
