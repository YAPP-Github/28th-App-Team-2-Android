package com.kikidan.domain.repository

import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential

interface AuthRepository {
    suspend fun login(credential: OAuthCredential): Result<LoginResult>

    suspend fun refresh(refreshToken: String): Result<AuthToken>
}
