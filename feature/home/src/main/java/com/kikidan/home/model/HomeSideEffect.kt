package com.kikidan.home.model

sealed interface HomeSideEffect {
    data class Error(
        val e: Throwable,
    ) : HomeSideEffect
}
