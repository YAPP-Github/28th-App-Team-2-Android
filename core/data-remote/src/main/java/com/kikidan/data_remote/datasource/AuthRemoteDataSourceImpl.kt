package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.AuthRemoteDataSource
import com.kikidan.data_remote.auth.AuthApi
import com.kikidan.data_remote.dto.CommonResponse
import com.kikidan.data_remote.dto.auth.RefreshRequest
import com.kikidan.data_remote.dto.auth.RefreshResponse
import com.kikidan.data_remote.mapper.toDomain
import com.kikidan.domain.model.auth.AuthToken
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.auth.AuthCircuitBreaker
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import javax.inject.Inject

class AuthRemoteDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : AuthRemoteDataSource {
        override suspend fun postRefresh(refreshToken: String): AuthToken {
            val response =
                client
                    .get()
                    .post(AuthApi.REFRESH) {
                        attributes.put(AuthCircuitBreaker, Unit)
                        setBody(RefreshRequest(refreshToken))
                    }.body<CommonResponse<RefreshResponse>>()

            val refreshResponse =
                requireNotNull(response.data) {
                    "refresh 응답의 data가 null입니다. code=${response.code}, message=${response.message}"
                }
            return refreshResponse.toDomain()
        }
    }
