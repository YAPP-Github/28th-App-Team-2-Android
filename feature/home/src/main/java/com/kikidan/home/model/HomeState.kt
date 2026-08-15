package com.kikidan.home.model

import com.kikidan.domain.model.fortune.FortuneCategory
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf

sealed interface HomeState {
    data object Loading : HomeState

    data class Success(
        val totalScore: Int,
        val scoreLabel: String,
        val fortuneId: String = "",
        val categories: PersistentList<CategoryScoreUiModel> = persistentListOf(),
        val detail: DetailSheetUiState? = null,
    ) : HomeState

    data object Failure : HomeState
}


data class CategoryScoreUiModel(
    val luckActionId: String,
    val category: FortuneCategory,
    val score: Int,
)

sealed interface DetailSheetUiState {
    data class Loading(
        val category: FortuneCategory,
    ) : DetailSheetUiState

    data class Success(
        val category: FortuneCategory,
        val score: Int,
        val actionTitle: String,
        val content: String,
    ) : DetailSheetUiState
}
