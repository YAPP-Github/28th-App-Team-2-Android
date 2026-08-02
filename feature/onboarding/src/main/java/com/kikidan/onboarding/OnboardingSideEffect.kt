package com.kikidan.onboarding

sealed interface OnboardingSideEffect {
    data object NavigateToHome : OnboardingSideEffect

    data object Exit : OnboardingSideEffect

    data object InvalidInput : OnboardingSideEffect

    data object Failure : OnboardingSideEffect
}
