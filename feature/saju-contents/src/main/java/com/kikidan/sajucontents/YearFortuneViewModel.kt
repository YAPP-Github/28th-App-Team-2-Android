package com.kikidan.sajucontents

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.CreateYearFortuneUseCase
import com.kikidan.sajucontents.model.YearFortuneSideEffect
import com.kikidan.sajucontents.model.YearFortuneState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class YearFortuneViewModel
    @Inject
    constructor(
        private val createYearFortune: CreateYearFortuneUseCase,
    ) : ViewModel(),
        ContainerHost<YearFortuneState, YearFortuneSideEffect> {
        override val container: Container<YearFortuneState, YearFortuneSideEffect> =
            container(YearFortuneState())

        fun onYearSelect(year: Int) =
            intent {
                reduce { state.copy(selectedYear = year) }
            }

        fun onSubmit() =
            intent {
                if (state.isLoading) return@intent
                val year = state.selectedYear
                reduce { state.copy(isLoading = true) }

                createYearFortune(year).fold(
                    onSuccess = { result ->
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(YearFortuneSideEffect.NavigateToResult(result.id))
                    },
                    onFailure = {
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(YearFortuneSideEffect.ShowError)
                    },
                )
            }
    }
