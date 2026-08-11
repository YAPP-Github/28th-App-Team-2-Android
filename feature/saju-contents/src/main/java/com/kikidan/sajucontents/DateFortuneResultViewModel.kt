package com.kikidan.sajucontents

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.GetDayFortuneUseCase
import com.kikidan.sajucontents.model.DateFortuneResultSideEffect
import com.kikidan.sajucontents.model.DateFortuneResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class DateFortuneResultViewModel
    @Inject
    constructor(
        private val getDayFortune: GetDayFortuneUseCase,
    ) : ViewModel(),
        ContainerHost<DateFortuneResultState, DateFortuneResultSideEffect> {
        override val container: Container<DateFortuneResultState, DateFortuneResultSideEffect> =
            container(DateFortuneResultState())

        fun loadResults(ids: List<String>) =
            intent {
                reduce { state.copy(isLoading = true) }

                // 탭 순서(선택한 날짜 순서)를 유지하기 위해 병렬 조회 후 요청한 id 순서 그대로 매핑한다.
                val results = coroutineScope { ids.map { id -> async { getDayFortune(id) } }.awaitAll() }
                val failure = results.firstOrNull { it.isFailure }
                if (failure != null) {
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(DateFortuneResultSideEffect.ShowError)
                    return@intent
                }

                reduce {
                    state.copy(
                        isLoading = false,
                        results =
                            results
                                .map { it.getOrThrow() }
                                .sortedByDescending { it.score }
                                .take(3)
                                .toPersistentList(),
                        selectedResultIndex = 0,
                    )
                }
            }

        fun selectTabResult(index: Int) =
            intent {
                reduce { state.copy(selectedResultIndex = index) }
            }
    }
