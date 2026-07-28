package com.kikidan.data_remote.auth

import com.kikidan.data.auth.AuthTokenCacheInvalidator
import com.kikidan.data_remote.di.AuthenticatedClient
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.plugins.auth.authProvider
import io.ktor.client.plugins.auth.providers.BearerAuthProvider
import javax.inject.Inject

class BearerTokenCacheInvalidator
    @Inject
    constructor(
        @AuthenticatedClient private val lazyClient: Lazy<HttpClient>,
    ) : AuthTokenCacheInvalidator {
        override fun invalidate() {
            // Lazy<HttpClient>: 초기화 순서 안전을 위해 dagger.Lazy로 주입.
            // 로그인 직후 saveToken 시 호출되어 AuthTokenHolder의 인메모리 캐시(null)를 비운다.
            // 이후 요청에서 loadTokens가 재실행되어 DataStore에서 새 토큰을 읽는다.
            runCatching {
                lazyClient.get().authProvider<BearerAuthProvider>()?.clearToken()
            }
        }
    }
