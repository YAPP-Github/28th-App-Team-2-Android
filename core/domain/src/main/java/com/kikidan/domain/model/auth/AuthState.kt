package com.kikidan.domain.model.auth

sealed interface AuthState {
    data object Authenticated : AuthState

    data object Unauthenticated : AuthState
}
