package com.kikidan.auth.kakao

import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kikidan.auth.OAuthException
import com.kikidan.domain.model.auth.OAuthProviderType.KAKAO
import java.io.IOException

internal fun Throwable.toOAuthException(): OAuthException =
    when (this) {
        is ClientError if reason == ClientErrorCause.Cancelled -> OAuthException.Cancelled(KAKAO, this)
        is IOException -> OAuthException.Network(KAKAO, this)
        else -> OAuthException.Unknown(KAKAO, this)
    }
