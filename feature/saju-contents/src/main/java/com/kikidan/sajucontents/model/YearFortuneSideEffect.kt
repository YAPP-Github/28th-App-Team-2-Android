package com.kikidan.sajucontents.model

sealed interface YearFortuneSideEffect {
    data class NavigateToResult(
        val year: Int,
    ) : YearFortuneSideEffect

    data object ShowError : YearFortuneSideEffect
}
