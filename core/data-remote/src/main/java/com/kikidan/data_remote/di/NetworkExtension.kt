package com.kikidan.data_remote.di

import com.kikidan.data.datasource.LocalTokenDataSource
import com.kikidan.data.datasource.RemoteAuthDataSource
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.sse.SSE
import io.ktor.http.ContentType
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
    install(HttpTimeout) {
        requestTimeoutMillis = 100_000
        connectTimeoutMillis = 100_000
        socketTimeoutMillis = 100_000
    }
    install(DefaultRequest) {
        url(baseUrl)
        contentType(ContentType.Application.Json)
    }
    install(SSE)
    expectSuccess = true
}

internal fun HttpClientConfig<*>.installBearerAuth(
    localTokenDataSource: LocalTokenDataSource,
    remoteAuthDataSource: RemoteAuthDataSource,
) {
    install(Auth) {
        bearer {
            loadTokens {
                localTokenDataSource.getToken()?.let { token ->
                    BearerTokens(token.accessToken, token.refreshToken)
                }
            }

            sendWithoutRequest { request ->
                val path =
                    request.url
                        .build()
                        .encodedPath
                        .trimStart('/')
                NO_AUTH_PATHS.none { noAuthPath -> path == noAuthPath }
            }

            refreshTokens {
                val oldRefreshToken = oldTokens?.refreshToken
                if (oldRefreshToken == null) {
                    localTokenDataSource.clearToken()
                    return@refreshTokens null
                }

                runCatching { remoteAuthDataSource.postRefresh(oldRefreshToken) }
                    .fold(
                        onSuccess = { newToken ->
                            localTokenDataSource.saveToken(newToken)
                            BearerTokens(newToken.accessToken, newToken.refreshToken)
                        },
                        onFailure = {
                            localTokenDataSource.clearToken()
                            null
                        },
                    )
            }
        }
    }
}

internal val TodakunJson: Json =
    Json {
        ignoreUnknownKeys = true
        isLenient = true
        explicitNulls = false
        coerceInputValues = true
    }

private val NO_AUTH_PATHS =
    setOf(
        "api/v1/auth/refresh",
        "api/v1/auth/login",
        "api/v1/auth/signup",
        "api/v1/terms",
    )
