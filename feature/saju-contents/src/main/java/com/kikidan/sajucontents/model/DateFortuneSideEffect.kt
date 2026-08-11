package com.kikidan.sajucontents.model

sealed interface DateFortuneSideEffect {
    data object ShowToast : DateFortuneSideEffect

    data object ShowError : DateFortuneSideEffect

    data class NavigateToResult(
        val id: List<String>,
    ) : DateFortuneSideEffect

    data object NavigateBack : DateFortuneSideEffect
}
