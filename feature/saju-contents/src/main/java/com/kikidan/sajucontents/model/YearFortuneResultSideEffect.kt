package com.kikidan.sajucontents.model

sealed interface YearFortuneResultSideEffect {
    data object ShowError : YearFortuneResultSideEffect

    data object ShowShareError : YearFortuneResultSideEffect

    data object ShowUrlCopied : YearFortuneResultSideEffect
}
