package com.kikidan.sajucontents.model

sealed interface DateFortuneResultSideEffect {
    data object ShowError : DateFortuneResultSideEffect

    data object ShowShareError : DateFortuneResultSideEffect

    data object ShowUrlCopied : DateFortuneResultSideEffect
}
