package com.kikidan.luckaction

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.GetEarliestFortuneDateUseCase
import com.kikidan.domain.usecase.GetLuckActionPageUseCase
import com.kikidan.domain.usecase.ToggleLuckActionUseCase
import com.kikidan.luckaction.model.FortuneScoreUiModel
import com.kikidan.luckaction.model.LuckActionItemUiModel
import com.kikidan.luckaction.model.LuckActionSideEffect
import com.kikidan.luckaction.model.LuckActionUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.syntax.Syntax
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class LuckActionViewModel
    @Inject
    constructor(
        private val getLuckActionPage: GetLuckActionPageUseCase,
        private val getEarliestFortuneDate: GetEarliestFortuneDateUseCase,
        private val toggleLuckAction: ToggleLuckActionUseCase,
    ) : ViewModel(),
        ContainerHost<LuckActionUiState, LuckActionSideEffect> {
        override val container = container<LuckActionUiState, LuckActionSideEffect>(LuckActionUiState.Loading)

        fun load() =
            intent {
                getEarliestFortuneDate()
                    .onSuccess { earliestDate ->
                        fetch(LocalDate.now(), earliestDate)
                    }.onFailure {
                        postSideEffect(LuckActionSideEffect.Error(it))
                    }
            }

        fun goToPrevDate() =
            intent {
                val current = state as? LuckActionUiState.Success ?: return@intent
                if (current.canGoToPrevDate) fetch(current.date.minusDays(1), current.earliestDate)
            }

        fun goToNextDate() =
            intent {
                val current = state as? LuckActionUiState.Success ?: return@intent
                val next = current.date.plusDays(1)
                if (!next.isAfter(LocalDate.now())) fetch(next, current.earliestDate)
            }

        fun toggleAction(id: String) =
            intent {
                val current = state as? LuckActionUiState.Success ?: return@intent
                toggleLuckAction(id)
                    .onSuccess { updated ->
                        reduce {
                            current.copy(
                                actions =
                                    current.actions
                                        .map { if (it.id == id) it.copy(achieved = updated.achieved) else it }
                                        .toPersistentList(),
                                completionOverlayCategory =
                                    if (updated.achieved) updated.category else current.completionOverlayCategory,
                            )
                        }
                    }.onFailure { postSideEffect(LuckActionSideEffect.Error(it)) }
            }

        fun dismissCompleteOverlay() =
            intent {
                val current = state as? LuckActionUiState.Success ?: return@intent
                reduce { current.copy(completionOverlayCategory = null) }
            }

        // 이전에 성공적으로 불러온 화면이 있으면(Success) 그 위에서 isRefreshing만 켠 채 유지하고,
        // 최초 로딩(Loading)에서만 전체 화면 로딩/실패 상태로 전환한다.
        // 실패 시에도 이미 보여주던 날짜/목록은 그대로 두고 토스트만 띄운다(같은 이유).
        // earliestDate는 세션 동안 바뀌지 않으므로 load()에서 한 번만 조회하고, 날짜 이동 시에는
        // 상태에 들고 있는 값을 그대로 재사용한다(매 클릭마다 다시 조회하지 않는다).
        private suspend fun Syntax<LuckActionUiState, LuckActionSideEffect>.fetch(
            date: LocalDate,
            earliestDate: LocalDate?,
        ) {
            val previous = state as? LuckActionUiState.Success
            val canGoToPrevDate = earliestDate != null && date.isAfter(earliestDate)
            val canGoToNextDate = date.isBefore(LocalDate.now())

            reduce { previous?.copy(isRefreshing = true) ?: LuckActionUiState.Loading }

            getLuckActionPage(date)
                .onSuccess { page ->
                    // 중간에 날짜가 빵꾸가 났을 때 실행되는 방어 경로.
                    if (page == null) {
                        reduce {
                            previous?.copy(
                                isRefreshing = false,
                                canGoToPrevDate = false,
                            ) ?: LuckActionUiState.Failure
                        }
                    } else {
                        reduce {
                            LuckActionUiState.Success(
                                date = date,
                                canGoToPrevDate = canGoToPrevDate,
                                canGoToNextDate = canGoToNextDate,
                                earliestDate = earliestDate,
                                scores =
                                    page.scores
                                        .sortedBy { it.category.ordinal }
                                        .map { FortuneScoreUiModel(it.category, it.score) }
                                        .toPersistentList(),
                                actions =
                                    page.actions
                                        .sortedBy { it.category.ordinal }
                                        .map { LuckActionItemUiModel(it.id, it.category, it.title, it.achieved) }
                                        .toPersistentList(),
                            )
                        }
                    }
                }.onFailure {
                    postSideEffect(LuckActionSideEffect.Error(it))
                    reduce { previous?.copy(isRefreshing = false) ?: LuckActionUiState.Failure }
                }
        }
    }
