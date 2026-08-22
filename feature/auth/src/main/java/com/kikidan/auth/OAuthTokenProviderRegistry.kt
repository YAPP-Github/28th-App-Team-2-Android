package com.kikidan.auth

import android.content.Context
import com.kakao.sdk.common.KakaoSdk
import com.kikidan.domain.model.auth.OAuthProviderType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OAuthTokenProviderRegistry
    @Inject
    constructor(
        private val providers: Map<OAuthProviderType, @JvmSuppressWildcards OAuthTokenProvider>,
    ) {
        /**
         * 프로바이더 SDK를 초기화한다. **Application.onCreate()에서 반드시 호출해야 한다.**
         * @param context Application 컨텍스트.
         */
        fun init(context: Context) {
            KakaoSdk.init(context, BuildConfig.KAKAO_NATIVE_APP_KEY)
        }

        operator fun get(type: OAuthProviderType): OAuthTokenProvider =
            providers[type] ?: error("No OAuthTokenProvider registered for $type")
    }
