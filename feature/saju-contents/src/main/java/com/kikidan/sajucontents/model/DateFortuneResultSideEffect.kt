package com.kikidan.sajucontents.model

sealed interface DateFortuneResultSideEffect {
    data object ShowError : DateFortuneResultSideEffect
}
