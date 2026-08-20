package com.kikidan.sajucontents.model

import com.kikidan.domain.model.fortune.YearFortune

data class YearFortuneResultState(
    val resultState: YearFortuneResultLoadState? = null,
    val isShareDialogVisible: Boolean = false,
) {
    val fortuneResult: YearFortune?
        get() = (resultState as? YearFortuneResultLoadState.Success)?.fortune
}

sealed interface YearFortuneResultLoadState {
    data object Loading : YearFortuneResultLoadState

    data class Success(
        val fortune: YearFortune,
    ) : YearFortuneResultLoadState

    data object Failure : YearFortuneResultLoadState
}
