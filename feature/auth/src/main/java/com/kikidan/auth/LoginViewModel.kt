package com.kikidan.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.auth.AuthState
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

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val oAuthTokenProviderRegistry: OAuthTokenProviderRegistry,
        private val loginUseCase: LoginUseCase,
        private val checkAuthStateUseCase: CheckAuthStateUseCase,
    ) : ViewModel(),
        ContainerHost<LoginState, LoginSideEffect> {
        override val container: Container<LoginState, LoginSideEffect> =
            container(LoginState.Loading(LoginStep.SPLASH))

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
                    AuthState.Unauthenticated -> reduce { LoginState.Success(LoginStep.LOGIN) }
                }
            }

        fun login(
            provider: OAuthProviderType,
            activity: Activity,
        ) = intent {
            reduce { LoginState.Loading(LoginStep.LOGIN) }

            val credential =
                try {
                    oAuthTokenProviderRegistry[provider].authorize(activity)
                } catch (e: OAuthException) {
                    reduce { LoginState.Failure(LoginStep.LOGIN) }
                    postSideEffect(LoginSideEffect.LoginFailed(e))
                    return@intent
                }

            loginUseCase(credential)
                .onSuccess { result ->
                    reduce { LoginState.Success(LoginStep.LOGIN) }
                    postSideEffect(LoginSideEffect.LoginSucceeded(result))
                }.onFailure { error ->
                    reduce { LoginState.Failure(LoginStep.LOGIN) }
                    postSideEffect(LoginSideEffect.LoginFailed(error))
                }
        }

        companion object {
            private const val SPLASH_MIN_DURATION_MILLIS = 1_500L
        }
    }
