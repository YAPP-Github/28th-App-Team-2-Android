package com.kikidan.home

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.GetHomeFortuneUseCase
import com.kikidan.domain.usecase.GetLuckActionDetailUseCase
import com.kikidan.home.model.CategoryScoreUiModel
import com.kikidan.home.model.DetailSheetUiState
import com.kikidan.home.model.HomeSideEffect
import com.kikidan.home.model.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class HomeViewModel
    @Inject
    constructor(
        private val getHomeFortune: GetHomeFortuneUseCase,
        private val getLuckActionDetail: GetLuckActionDetailUseCase,
    ) : ViewModel(),
        ContainerHost<HomeState, HomeSideEffect> {
        override val container = container<HomeState, HomeSideEffect>(HomeState.Loading)

        fun load(greeting: String) =
            intent {
                getHomeFortune()
                    .onSuccess { fortune ->
                        reduce {
                            HomeState.Success(
                                totalScore = fortune.totalScore,
                                scoreLabel = fortune.scoreLabel,
                                greeting = greeting,
                                categories =
                                    fortune.scores
                                        .sortedBy { it.category.ordinal }
                                        .map { CategoryScoreUiModel(it.luckActionId, it.category, it.score) }
                                        .toPersistentList(),
                            )
                        }
                    }.onFailure {
                        postSideEffect(HomeSideEffect.Error(it))
                        reduce { HomeState.Failure }
                    }
            }

        fun openDetail(luckActionId: String) =
            intent {
                val current = state as? HomeState.Success ?: return@intent
                val category = current.categories.first { it.luckActionId == luckActionId }.category
                reduce { (state as? HomeState.Success)?.copy(detail = DetailSheetUiState.Loading(category)) ?: state }
                getLuckActionDetail(luckActionId)
                    .onSuccess { detail ->
                        reduce {
                            (state as? HomeState.Success)?.copy(
                                detail =
                                    DetailSheetUiState.Success(
                                        category = detail.category,
                                        score = detail.score,
                                        actionTitle = detail.title,
                                        content = detail.content,
                                    ),
                            ) ?: state
                        }
                    }.onFailure {
                        postSideEffect(HomeSideEffect.Error(it))
                        reduce { (state as? HomeState.Success)?.copy(detail = null) ?: state }
                    }
            }

        fun closeDetail() =
            intent {
                reduce { (state as? HomeState.Success)?.copy(detail = null) ?: state }
            }
    }
