package com.kikidan.home.model

sealed interface FortuneReportSideEffect {
    data class Error(
        val e: Throwable,
    ) : FortuneReportSideEffect
}
