package com.kikidan.data.datasource

import com.kikidan.domain.model.auth.AuthToken

// rules/20-data: REST 엔드포인트와 1:1, 함수 접두사는 HTTP METHOD 연관 동사
interface AuthRemoteDataSource {
    suspend fun postRefresh(refreshToken: String): AuthToken
}
