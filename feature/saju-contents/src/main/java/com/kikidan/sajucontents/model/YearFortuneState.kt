package com.kikidan.sajucontents.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.time.Year

private val DEFAULT_YEARS: ImmutableList<Int> = (2021..2031).toList().toPersistentList()

data class YearFortuneState(
    val years: ImmutableList<Int> = DEFAULT_YEARS,
    val selectedYear: Int = Year.now().value,
    val currentYear: Int = Year.now().value,
    val submitState: YearFortuneSubmitState? = null,
)

sealed interface YearFortuneSubmitState {
    data object Loading : YearFortuneSubmitState

    data object Success : YearFortuneSubmitState

    data object Failure : YearFortuneSubmitState
}
