package com.kikidan.onboarding.model

sealed interface TermsSideEffect {
    data object Exit : TermsSideEffect
}
