package com.kikidan.data_remote.client

import android.util.Log
import com.kikidan.data.datasource.AuthRemoteDataSource
import com.kikidan.data.datasource.TokenDataSource
import com.kikidan.data_remote.BuildConfig
import com.kikidan.data_remote.api.AuthApi
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.url
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal fun HttpClientConfig<*>.installTodakunDefaults(
    json: Json,
    baseUrl: String,
) {
    install(ContentNegotiation) {
        json(json)
    }
    install(Logging) {
        // android.util.Log.v를 직접 사용 — Logger.ANDROID와 동일한 동작.
        // testOptions.isReturnDefaultValues = true로 유닛 테스트에서 stub 처리.
        logger =
            object : Logger {
                override fun log(message: String) {
                    Log.v("Ktor", message)
                }
            }
        level = if (BuildConfig.DEBUG) LogLevel.ALL else LogLevel.NONE
        sanitizeHeader { header -> header.equals(HttpHeaders.Authorization, ignoreCase = true) }
    }
    install(HttpTimeout) {
        requestTimeoutMillis = 15_000
        connectTimeoutMillis = 10_000
        socketTimeoutMillis = 15_000
    }
    install(DefaultRequest) {
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }
    expectSuccess = true
}

internal fun HttpClientConfig<*>.installBearerAuth(
    tokenDataSource: TokenDataSource,
    authRemoteDataSource: AuthRemoteDataSource,
) {
    install(Auth) {
        bearer {
            loadTokens {
                tokenDataSource.getToken()?.let { token ->
                    BearerTokens(token.accessToken, token.refreshToken)
                }
            }

            sendWithoutRequest { request ->
                // URLBuilder.build()로 Url 객체를 얻어 encodedPath를 읽는다.
                // Ktor 3.x에서 URLBuilder에 encodedPath 프로퍼티가 없어 build() 경유.
                val path =
                    request.url
                        .build()
                        .encodedPath
                        .trimStart('/')
                AuthApi.NO_AUTH_PATHS.none { noAuthPath -> path == noAuthPath }
            }

            refreshTokens {
                val oldRefreshToken = oldTokens?.refreshToken
                if (oldRefreshToken == null) {
                    tokenDataSource.clearToken()
                    return@refreshTokens null
                }

                runCatching { authRemoteDataSource.postRefresh(oldRefreshToken) }
                    .fold(
                        onSuccess = { newToken ->
                            // rotation: 새 토큰 저장이 먼저다(AC-4).
                            // 저장 실패 시 계정 영구 실패 → 예외 propagate.
                            tokenDataSource.saveToken(newToken)
                            BearerTokens(newToken.accessToken, newToken.refreshToken)
                        },
                        onFailure = {
                            tokenDataSource.clearToken()
                            null
                        },
                    )
            }
        }
    }
}
