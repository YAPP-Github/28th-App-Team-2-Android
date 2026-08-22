package com.kikidan.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.kikidan.auth.model.SplashSideEffect
import com.kikidan.domain.model.auth.AuthState
import com.kikidan.domain.usecase.CheckAuthStateUseCase
import com.kikidan.domain.usecase.RegisterDeviceTokenUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
        private val registerDeviceToken: RegisterDeviceTokenUseCase,
        private val checkAuthStateUseCase: CheckAuthStateUseCase,
    ) : ViewModel(),
        ContainerHost<Unit, SplashSideEffect> {
        override val container: Container<Unit, SplashSideEffect> = container(Unit)

        fun checkAuthState() =
            intent {
                val authState =
                    coroutineScope {
                        val deferred = async { checkAuthStateUseCase() }
                        delay(SPLASH_MIN_DURATION_MILLIS)
                        deferred.await()
                    }
                when (authState) {
                    AuthState.Authenticated -> {
                        registerMessagingToken()
                        postSideEffect(SplashSideEffect.AlreadyAuthenticated)
                    }

                    AuthState.Unauthenticated -> {
                        postSideEffect(SplashSideEffect.NavigateToLogin)
                    }
                }
            }

        private fun registerMessagingToken() {
            viewModelScope.launch {
                val token = FirebaseMessaging.getInstance().token.await()
                // TODO 올바른 상태 보장을 위해 예외 발생, 추후 ErrorScreen으로 이동
                registerDeviceToken(token).getOrThrow()
            }
        }

        companion object {
            private const val SPLASH_MIN_DURATION_MILLIS = 1_500L
        }
    }
