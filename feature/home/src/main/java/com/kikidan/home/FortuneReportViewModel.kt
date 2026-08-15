package com.kikidan.home

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.GetDailyFortuneDetailUseCase
import com.kikidan.domain.usecase.GetLuckActionDetailUseCase
import com.kikidan.home.model.CategoryScoreUiModel
import com.kikidan.home.model.DetailSheetUiState
import com.kikidan.home.model.FortuneReportSideEffect
import com.kikidan.home.model.FortuneReportState
import com.kikidan.home.model.HomeSideEffect
import com.kikidan.home.model.HomeState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class FortuneReportViewModel
    @Inject
    constructor(
        private val getDailyFortuneDetail: GetDailyFortuneDetailUseCase,
        private val getLuckActionDetail: GetLuckActionDetailUseCase
    ) : ViewModel(),
        ContainerHost<FortuneReportState, FortuneReportSideEffect> {
        override val container = container<FortuneReportState, FortuneReportSideEffect>(FortuneReportState.Loading)

        fun load(fortuneId: String) =
            intent {
                reduce { FortuneReportState.Loading }
                getDailyFortuneDetail(fortuneId)
                    .onSuccess { detail ->
                        reduce {
                            FortuneReportState.Success(
                                totalScore = detail.totalScore,
                                content = detail.content,
                                title = detail.title,
                                categories =
                                    detail.scores
                                        .sortedBy { it.category.ordinal }
                                        .map { CategoryScoreUiModel(it.luckActionId, it.category, it.score) }
                                        .toPersistentList(),
                                luckyItems = detail.luckyItems.toPersistentList(),
                                cautionaryItems = detail.cautionaryItems.toPersistentList(),
                            )
                        }
                    }.onFailure {
                        postSideEffect(FortuneReportSideEffect.Error(it))
                        reduce { FortuneReportState.Failure }
                    }
            }

    fun openDetail(luckActionId: String) =
        intent {
            val current = state as? FortuneReportState.Success ?: return@intent
            val category = current.categories.first { it.luckActionId == luckActionId }.category
            reduce { (state as? FortuneReportState.Success)?.copy(detail = DetailSheetUiState.Loading(category)) ?: state }
            getLuckActionDetail(luckActionId)
                .onSuccess { detail ->
                    reduce {
                        (state as? FortuneReportState.Success)?.copy(
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
                    postSideEffect(FortuneReportSideEffect.Error(it))
                    reduce { (state as? FortuneReportState.Success)?.copy(detail = null) ?: state }
                }
        }

    fun closeDetail() =
        intent {
            reduce { (state as? FortuneReportState.Success)?.copy(detail = null) ?: state }
        }


    }
