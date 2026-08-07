package com.kikidan.luckaction.model

import com.kikidan.domain.model.fortune.FortuneCategory
import java.time.LocalDate

sealed interface LuckActionUiState {
    data object Loading : LuckActionUiState

    data class Success(
        val date: LocalDate,
        val canGoToPrevDate: Boolean,
        val isRefreshing: Boolean = false,
        val scores: List<FortuneScoreUiModel> = emptyList(),
        val actions: List<LuckActionItemUiModel> = emptyList(),
        val completionOverlayCategory: FortuneCategory? = null,
    ) : LuckActionUiState

    data object Failure : LuckActionUiState
}

data class FortuneScoreUiModel(
    val category: FortuneCategory,
    val score: Int,
)

data class LuckActionItemUiModel(
    val id: String,
    val category: FortuneCategory,
    val title: String,
    val achieved: Boolean,
)
