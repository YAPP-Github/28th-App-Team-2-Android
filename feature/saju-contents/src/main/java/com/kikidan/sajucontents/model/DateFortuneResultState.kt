package com.kikidan.sajucontents.model

import com.kikidan.domain.model.dayfortune.DayFortune
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class DateFortuneResultState(
    val isLoading: Boolean = false,
    val results: ImmutableList<DayFortune> = persistentListOf(),
    val selectedResultIndex: Int = 0,
)
