package com.kikidan.auth.model

sealed interface SplashSideEffect {
    data object NavigateToLogin : SplashSideEffect

    data object AlreadyAuthenticated : SplashSideEffect
}
