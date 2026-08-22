package com.kikidan.sajucontents.model

import com.kikidan.domain.model.dayfortune.DayFortune
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class DateFortuneResultState(
    val resultState: DateFortuneResultLoadState? = null,
    val selectedResultIndex: Int = 0,
    val isShareDialogVisible: Boolean = false,
) {
    val results: ImmutableList<DayFortune>
        get() = (resultState as? DateFortuneResultLoadState.Success)?.results ?: persistentListOf()
}

sealed interface DateFortuneResultLoadState {
    data object Loading : DateFortuneResultLoadState

    data class Success(
        val results: ImmutableList<DayFortune>,
    ) : DateFortuneResultLoadState

    data object Failure : DateFortuneResultLoadState
}
