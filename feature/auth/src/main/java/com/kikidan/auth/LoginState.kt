package com.kikidan.auth

sealed interface LoginState {
    val step: LoginStep

    data class Loading(
        override val step: LoginStep,
    ) : LoginState

    data class Success(
        override val step: LoginStep,
    ) : LoginState

    data class Failure(
        override val step: LoginStep,
    ) : LoginState
}
