package com.kikidan.sajucontents.model

sealed interface YearFortuneResultSideEffect {
    data object ShowError : YearFortuneResultSideEffect
}
