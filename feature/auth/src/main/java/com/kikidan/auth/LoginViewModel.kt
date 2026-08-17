package com.kikidan.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.kikidan.auth.model.LoginSideEffect
import com.kikidan.auth.model.LoginState
import com.kikidan.domain.model.auth.OAuthProviderType
import com.kikidan.domain.usecase.LoginUseCase
import com.kikidan.domain.usecase.RegisterDeviceTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class LoginViewModel
    @Inject
    constructor(
        private val registerDeviceToken: RegisterDeviceTokenUseCase,
        private val oAuthTokenProviderRegistry: OAuthTokenProviderRegistry,
        private val loginUseCase: LoginUseCase,
    ) : ViewModel(),
        ContainerHost<LoginState, LoginSideEffect> {
        override val container: Container<LoginState, LoginSideEffect> =
            container(LoginState.Idle)

        fun login(
            provider: OAuthProviderType,
            activity: Activity,
        ) = intent {
            reduce { LoginState.Loading }

            val credential =
                try {
                    oAuthTokenProviderRegistry[provider].authorize(activity)
                } catch (e: OAuthException) {
                    reduce { LoginState.Failure }
                    postSideEffect(LoginSideEffect.LoginFailed(e))
                    return@intent
                }

            loginUseCase(credential)
                .onSuccess { result ->
                    registerMessagingToken()
                    reduce { LoginState.Success }
                    postSideEffect(LoginSideEffect.LoginSucceeded(result))
                }.onFailure { error ->
                    reduce { LoginState.Failure }
                    postSideEffect(LoginSideEffect.LoginFailed(error))
                }
        }

        private fun registerMessagingToken() {
            viewModelScope.launch {
                val token = FirebaseMessaging.getInstance().token.await()
                registerDeviceToken(token)
            }
        }
    }
