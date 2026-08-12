package com.kikidan.sajucontents.model

import com.kikidan.domain.model.fortune.YearFortune
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList
import java.time.Year

private val DEFAULT_YEARS: ImmutableList<Int> = (2021..2031).toList().toPersistentList()

data class YearFortuneState(
    val years: ImmutableList<Int> = DEFAULT_YEARS,
    val selectedYear: Int = Year.now().value,
    val currentYear: Int = Year.now().value,
    val fortuneResult: YearFortune? = null,
    val isLoading: Boolean = false,
    val isShareSheetVisible: Boolean = false,
    val error: String? = null,
)
