package com.kikidan.auth.model

sealed interface LoginState {
    data object Loading : LoginState

    data object Success : LoginState

    data object Failure : LoginState
}
