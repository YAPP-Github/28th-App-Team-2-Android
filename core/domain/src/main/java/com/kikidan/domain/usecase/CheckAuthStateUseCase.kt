package com.kikidan.domain.usecase

import com.kikidan.domain.model.auth.AuthState
import com.kikidan.domain.repository.AuthRepository
import com.kikidan.domain.repository.TokenRepository
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long
import java.time.Instant
import javax.inject.Inject
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class CheckAuthStateUseCase
    @Inject
    constructor(
        private val tokenRepository: TokenRepository,
        private val authRepository: AuthRepository,
    ) {
        suspend operator fun invoke(): AuthState {
            val token = tokenRepository.getToken().getOrNull() ?: return AuthState.Unauthenticated
            if (!token.accessToken.isExpired()) return AuthState.Authenticated

            // TODO MemberRepository 생성 시, 추후 api 교체
            return authRepository
                .refresh(token.refreshToken)
                .fold(
                    onSuccess = { newToken ->
                        tokenRepository.saveToken(newToken)
                        AuthState.Authenticated
                    },
                    onFailure = {
                        tokenRepository.clearToken()
                        AuthState.Unauthenticated
                    },
                )
        }

        // accessToken JWT 만료 기간 확인
        @OptIn(ExperimentalEncodingApi::class)
        private fun String.isExpired(now: Instant = Instant.now()): Boolean {
            val exp =
                runCatching {
                    val payload = split(".")[1]
                    val json =
                        Base64.UrlSafe
                            .withPadding(Base64.PaddingOption.ABSENT_OPTIONAL)
                            .decode(payload)
                            .decodeToString()
                    Json
                        .parseToJsonElement(json)
                        .jsonObject["exp"]
                        ?.jsonPrimitive
                        ?.long
                }.getOrNull() ?: return false

            return now.epochSecond >= exp
        }
    }
