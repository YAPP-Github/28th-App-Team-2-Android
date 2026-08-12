package com.kikidan.sajucontents

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.GetYearFortuneUseCase
import com.kikidan.sajucontents.model.YearFortuneSideEffect
import com.kikidan.sajucontents.model.YearFortuneState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

// internal: 실패 시 상태에 반영되는 정확한 메시지를 유닛 테스트(YearFortuneViewModelTest)에서 직접 검증하기 위해 노출한다.
internal const val LOAD_ERROR_MESSAGE = "연도별 운세를 불러오지 못했어요. 다시 시도해주세요."

@HiltViewModel
class YearFortuneViewModel
    @Inject
    constructor(
        private val getYearFortune: GetYearFortuneUseCase,
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
                reduce { state.copy(isLoading = true, error = null) }

                getYearFortune(year).fold(
                    onSuccess = { result ->
                        reduce { state.copy(isLoading = false, fortuneResult = result) }
                        postSideEffect(YearFortuneSideEffect.NavigateToResult(year))
                    },
                    onFailure = {
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(YearFortuneSideEffect.ShowError)
                    },
                )
            }

        // 결과 화면(YearFortuneResultRoute)이 자신의 ViewModel 인스턴스로 스스로 데이터를 불러오기 위한 액션.
        // 연도 선택 화면의 ViewModel과 별개 인스턴스여도 무방하도록 year를 직접 전달받는다.
        fun load(year: Int) =
            intent {
                if (state.isLoading) return@intent
                reduce { state.copy(selectedYear = year, isLoading = true, error = null) }

                getYearFortune(year).fold(
                    onSuccess = { result ->
                        reduce { state.copy(isLoading = false, fortuneResult = result) }
                    },
                    onFailure = {
                        reduce { state.copy(isLoading = false, error = LOAD_ERROR_MESSAGE) }
                    },
                )
            }

        fun onShareIconClick() =
            intent {
                reduce { state.copy(isShareSheetVisible = true) }
            }

        fun onShareSheetDismiss() =
            intent {
                reduce { state.copy(isShareSheetVisible = false) }
            }
    }
