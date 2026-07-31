package com.kikidan.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.auth.AuthState
import com.kikidan.domain.model.auth.LoginResult
import com.kikidan.domain.model.auth.OAuthProviderType
import com.kikidan.domain.usecase.CheckAuthStateUseCase
import com.kikidan.domain.usecase.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

data class LoginState(
    val step: LoginStep = LoginStep.SPLASH,
    val isLoading: Boolean = false,
)

sealed interface LoginSideEffect {
    data class LoginSucceeded(
        val result: LoginResult,
    ) : LoginSideEffect

    data class LoginFailed(
        val throwable: Throwable,
    ) : LoginSideEffect

    data object AlreadyAuthenticated : LoginSideEffect
}

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val oAuthTokenProviderRegistry: OAuthTokenProviderRegistry,
        private val loginUseCase: LoginUseCase,
        private val checkAuthStateUseCase: CheckAuthStateUseCase,
    ) : ViewModel(),
        ContainerHost<LoginState, LoginSideEffect> {
        override val container: Container<LoginState, LoginSideEffect> = container(LoginState())

        /**
         * 스플래시 진입 시 1회 호출. 로컬 토큰·서버 세션을 확인하는 동안에도 스플래시가
         * 너무 짧게 깜빡이지 않도록 최소 노출 시간([SPLASH_MIN_DURATION_MILLIS])을 함께 보장한다.
         */
        fun checkAuthState() =
            intent {
                val authState =
                    coroutineScope {
                        val deferred = async { checkAuthStateUseCase() }
                        delay(SPLASH_MIN_DURATION_MILLIS)
                        deferred.await()
                    }
                when (authState) {
                    AuthState.Authenticated -> postSideEffect(LoginSideEffect.AlreadyAuthenticated)
                    AuthState.Unauthenticated -> reduce { state.copy(step = LoginStep.LOGIN) }
                }
            }

        fun login(
            provider: OAuthProviderType,
            activity: Activity,
        ) = intent {
            reduce { state.copy(isLoading = true) }

            val credential =
                try {
                    oAuthTokenProviderRegistry[provider].authorize(activity)
                } catch (e: OAuthException) {
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(LoginSideEffect.LoginFailed(e))
                    return@intent
                }

            loginUseCase(credential)
                .onSuccess { result ->
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(LoginSideEffect.LoginSucceeded(result))
                }.onFailure { error ->
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(LoginSideEffect.LoginFailed(error))
                }
        }

        companion object {
            private const val SPLASH_MIN_DURATION_MILLIS = 1_500L
        }
    }
