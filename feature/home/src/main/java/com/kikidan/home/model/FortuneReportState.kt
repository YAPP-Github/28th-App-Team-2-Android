package com.kikidan.home.model

import kotlinx.collections.immutable.PersistentList

sealed interface FortuneReportState {
    data object Loading : FortuneReportState

    data class Success(
        val totalScore: Int,
        val content: String,
        val title: String,
        val categories: PersistentList<CategoryScoreUiModel>,
        val luckyItems: PersistentList<String>,
        val cautionaryItems: PersistentList<String>,
        val detail: DetailSheetUiState? = null
    ) : FortuneReportState

    data object Failure : FortuneReportState
}
