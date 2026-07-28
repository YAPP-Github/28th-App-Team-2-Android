package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.AuthRemoteDataSource
import com.kikidan.data_remote.api.AuthApi
import com.kikidan.data_remote.di.TokenRefreshClient
import com.kikidan.data_remote.dto.CommonResponse
import com.kikidan.data_remote.dto.auth.RefreshRequest
import com.kikidan.data_remote.dto.auth.RefreshResponse
import com.kikidan.data_remote.mapper.toDomain
import com.kikidan.domain.model.auth.AuthToken
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

// rules/20-data: try-catch 금지, 예외 그대로 throw.
class AuthRemoteDataSourceImpl
    @Inject
    constructor(
        @TokenRefreshClient private val client: HttpClient,
    ) : AuthRemoteDataSource {
        override suspend fun postRefresh(refreshToken: String): AuthToken {
            val response =
                client
                    .post(AuthApi.REFRESH) {
                        setBody(RefreshRequest(refreshToken))
                    }.body<CommonResponse<RefreshResponse>>()

            val refreshResponse =
                requireNotNull(response.data) {
                    "refresh 응답의 data가 null입니다. code=${response.code}, message=${response.message}"
                }
            return refreshResponse.toDomain()
        }
    }
