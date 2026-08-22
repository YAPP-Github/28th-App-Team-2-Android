package com.kikidan.luckaction.model

sealed interface LuckActionSideEffect {
    data class Error(
        val e: Throwable,
    ) : LuckActionSideEffect
}
