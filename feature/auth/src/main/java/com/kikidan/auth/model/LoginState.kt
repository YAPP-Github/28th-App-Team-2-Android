package com.kikidan.auth.model

sealed interface LoginState {
    data object Idle : LoginState

    data object Loading : LoginState

    data object Success : LoginState

    data object Failure : LoginState
}
