package com.kikidan.sajucontents.model

sealed interface YearFortuneSideEffect {
    data class NavigateToResult(
        val id: String,
    ) : YearFortuneSideEffect

    data object ShowError : YearFortuneSideEffect
}
