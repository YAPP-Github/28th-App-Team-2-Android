package com.kikidan.sajucontents.model

sealed interface DateFortuneSideEffect {
    data class ShowToast(
        val messageRes: Int,
    ) : DateFortuneSideEffect

    data class ShowError(
        val messageRes: Int,
    ) : DateFortuneSideEffect

    data object NavigateToResult : DateFortuneSideEffect

    data object NavigateBack : DateFortuneSideEffect
}
