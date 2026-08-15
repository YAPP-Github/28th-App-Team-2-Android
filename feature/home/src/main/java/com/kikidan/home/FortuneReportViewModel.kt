package com.kikidan.home

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.GetDailyFortuneDetailUseCase
import com.kikidan.home.model.CategoryScoreUiModel
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
    }
