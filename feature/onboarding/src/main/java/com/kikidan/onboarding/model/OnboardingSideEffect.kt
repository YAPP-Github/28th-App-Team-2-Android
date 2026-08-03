package com.kikidan.onboarding.model

sealed interface OnboardingSideEffect {
    data object NavigateToHome : OnboardingSideEffect

    data object Exit : OnboardingSideEffect

    data object InvalidInput : OnboardingSideEffect

    data object PermissionRequest : OnboardingSideEffect

    data class Failure(
        val throwable: Throwable,
    ) : OnboardingSideEffect
}
