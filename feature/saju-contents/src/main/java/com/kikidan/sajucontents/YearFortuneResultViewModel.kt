package com.kikidan.sajucontents

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.GetYearFortuneUseCase
import com.kikidan.sajucontents.model.YearFortuneResultSideEffect
import com.kikidan.sajucontents.model.YearFortuneResultState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

// id를 직접 전달받아 스스로 데이터를 불러온다. 공유 딥링크로 결과 화면에 바로 진입할 수 있도록
// 연도 선택 화면(YearFortuneViewModel)과 완전히 분리된 인스턴스로 존재한다.
@HiltViewModel
class YearFortuneResultViewModel
    @Inject
    constructor(
        private val getYearFortune: GetYearFortuneUseCase,
    ) : ViewModel(),
        ContainerHost<YearFortuneResultState, YearFortuneResultSideEffect> {
        override val container: Container<YearFortuneResultState, YearFortuneResultSideEffect> =
            container(YearFortuneResultState())

        fun load(id: String) =
            intent {
                if (state.isLoading) return@intent
                reduce { state.copy(isLoading = true) }

                getYearFortune(id).fold(
                    onSuccess = { result ->
                        reduce { state.copy(isLoading = false, fortuneResult = result) }
                    },
                    onFailure = {
                        reduce { state.copy(isLoading = false) }
                        postSideEffect(YearFortuneResultSideEffect.ShowError)
                    },
                )
            }

        fun showShareDialog() =
            intent {
                reduce { state.copy(isShareDialogVisible = true) }
            }

        fun hideShareDialog() =
            intent {
                reduce { state.copy(isShareDialogVisible = false) }
            }

        fun notifyShareUnavailable() =
            intent {
                reduce { state.copy(isShareDialogVisible = false) }
                postSideEffect(YearFortuneResultSideEffect.ShowShareError)
            }

        fun notifyUrlCopied() =
            intent {
                reduce { state.copy(isShareDialogVisible = false) }
                postSideEffect(YearFortuneResultSideEffect.ShowUrlCopied)
            }
    }
