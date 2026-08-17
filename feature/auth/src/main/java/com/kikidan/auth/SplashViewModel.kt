package com.kikidan.auth

import androidx.lifecycle.ViewModel
import com.kikidan.auth.model.SplashSideEffect
import com.kikidan.domain.model.auth.AuthState
import com.kikidan.domain.usecase.CheckAuthStateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class SplashViewModel
    @Inject
    constructor(
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
                    AuthState.Authenticated -> postSideEffect(SplashSideEffect.AlreadyAuthenticated)
                    AuthState.Unauthenticated -> postSideEffect(SplashSideEffect.NavigateToLogin)
                }
            }

        companion object {
            private const val SPLASH_MIN_DURATION_MILLIS = 1_500L
        }
    }
