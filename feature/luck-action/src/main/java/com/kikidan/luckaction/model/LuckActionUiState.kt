package com.kikidan.luckaction.model

import com.kikidan.domain.model.fortune.FortuneCategory
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import java.time.LocalDate

sealed interface LuckActionUiState {
    data object Loading : LuckActionUiState

    data class Success(
        val date: LocalDate,
        val canGoToPrevDate: Boolean,
        val canGoToNextDate: Boolean,
        val earliestDate: LocalDate? = null,
        val isRefreshing: Boolean = false,
        val scores: PersistentList<FortuneScoreUiModel> = persistentListOf(),
        val actions: PersistentList<LuckActionItemUiModel> = persistentListOf(),
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
