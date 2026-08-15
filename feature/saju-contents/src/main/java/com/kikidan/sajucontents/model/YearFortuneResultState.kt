package com.kikidan.sajucontents.model

import com.kikidan.domain.model.fortune.YearFortune

data class YearFortuneResultState(
    val fortuneResult: YearFortune? = null,
    val isLoading: Boolean = false,
    val isShareDialogVisible: Boolean = false,
)
