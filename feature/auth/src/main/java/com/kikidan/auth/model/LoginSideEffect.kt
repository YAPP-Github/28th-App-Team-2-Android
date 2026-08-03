package com.kikidan.auth.model

import com.kikidan.domain.model.auth.LoginResult

sealed interface LoginSideEffect {
    data class LoginSucceeded(
        val result: LoginResult,
    ) : LoginSideEffect

    data class LoginFailed(
        val throwable: Throwable,
    ) : LoginSideEffect
}
