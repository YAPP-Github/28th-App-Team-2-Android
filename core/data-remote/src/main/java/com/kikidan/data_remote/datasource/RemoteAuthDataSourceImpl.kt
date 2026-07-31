package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteAuthDataSource
import com.kikidan.data_remote.dto.CommonResponse
import com.kikidan.data_remote.dto.auth.LoginResponse
import com.kikidan.data_remote.dto.auth.RefreshRequest
import com.kikidan.data_remote.dto.auth.RefreshResponse
import com.kikidan.data_remote.dto.auth.toDomain
import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class RemoteAuthDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteAuthDataSource {
        override suspend fun login(oauthCredential: OAuthCredential): LoginResult {
            val response =
                client
                    .get()
                    .post(LOGIN_URL) {
                    }.body<CommonResponse<LoginResponse>>()
            val loginResponse =
                requireNotNull(response.data) {
                    "login 응답의 data가 null입니다. code=${response.code}, message=${response.message}"
                }
            return loginResponse.toDomain()
        }

        override suspend fun postRefresh(refreshToken: String): AuthToken {
            val response =
                client
                    .get()
                    .post(REFRESH_URL) {
                        // 설정하지 않았을 때, refreshToken이 두 번 실행
                        attributes.put(AuthCircuitBreaker, Unit)
                        setBody(RefreshRequest(refreshToken))
                    }.body<CommonResponse<RefreshResponse>>()

            val refreshResponse =
                requireNotNull(response.data) {
                    "refresh 응답의 data가 null입니다. code=${response.code}, message=${response.message}"
                }
            return refreshResponse.toDomain()
        }

        companion object {
            private const val REFRESH_URL = "api/v1/auth/refresh"
            private const val LOGIN_URL = "api/v1/auth/login"
        }
    }
