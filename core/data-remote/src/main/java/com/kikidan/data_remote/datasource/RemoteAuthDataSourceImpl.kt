package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteAuthDataSource
import com.kikidan.data_remote.dto.CommonResponse
import com.kikidan.data_remote.dto.auth.LoginRequest
import com.kikidan.data_remote.dto.auth.LoginResponse
import com.kikidan.data_remote.dto.auth.RefreshRequest
import com.kikidan.data_remote.dto.auth.RefreshResponse
import com.kikidan.data_remote.dto.auth.SignupRequest
import com.kikidan.data_remote.dto.auth.SignupResponse
import com.kikidan.data_remote.dto.auth.toDomain
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.auth.AuthToken
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthCredential
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.user.User
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
    override suspend fun postLogin(oauthCredential: OAuthCredential): LoginResult =
        client
            .get()
            .post(LOGIN_URL) {
                setBody(
                    LoginRequest(
                        provider = oauthCredential.provider.toString(),
                        oauthAccessToken = oauthCredential.token.value,
                    ),
                )
            }.bodyNotNull<LoginResponse>().toDomain()


    override suspend fun postSignup(user: User, onboardingToken: OnboardingToken): AuthToken = client
        .get()
        .post(SIGNUP_URL) {
            setBody(
                SignupRequest(

                ),
            )
        }.bodyNotNull<SignupResponse>().toDomain()


    override suspend fun postLogout() {
        client
            .get()
            .post(LOGOUT_URL)
            .body<CommonResponse<Unit>>()
    }

    override suspend fun postRefresh(refreshToken: String): AuthToken = client
        .get()
        .post(REFRESH_URL) {
            // 설정하지 않았을 때, refreshToken이 두 번 실행
            attributes.put(AuthCircuitBreaker, Unit)
            setBody(RefreshRequest(refreshToken))
        }.bodyNotNull<RefreshResponse>().toDomain()


    companion object {
        private const val REFRESH_URL = "api/v1/auth/refresh"
        private const val LOGIN_URL = "api/v1/auth/login"
        private const val SIGNUP_URL = "api/v1/auth/signup"

        private const val LOGOUT_URL = "api/v1/auth/logout"

    }
}
