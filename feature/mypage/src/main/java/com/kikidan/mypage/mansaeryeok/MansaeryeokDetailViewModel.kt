package com.kikidan.mypage.mansaeryeok

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.mypage.GetMansaeryeokDetailUseCase
import com.kikidan.mypage.mansaeryeok.model.MansaeryeokDetailSideEffect
import com.kikidan.mypage.mansaeryeok.model.MansaeryeokDetailUiState
import com.kikidan.mypage.mansaeryeok.model.toUiModel
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class MansaeryeokDetailViewModel
    @Inject
    constructor(
        private val getMansaeryeokDetailUseCase: GetMansaeryeokDetailUseCase,
    ) : ViewModel(),
        ContainerHost<MansaeryeokDetailUiState, MansaeryeokDetailSideEffect> {
        override val container: Container<MansaeryeokDetailUiState, MansaeryeokDetailSideEffect> =
            container(MansaeryeokDetailUiState.Loading) {
                loadMansaeryeokDetail()
            }

        fun loadMansaeryeokDetail() =
            intent {
                getMansaeryeokDetailUseCase()
                    .onSuccess { detail ->
                        reduce { MansaeryeokDetailUiState.Success(detail.chart.toUiModel(detail.user)) }
                    }.onFailure { throwable ->
                        reduce { MansaeryeokDetailUiState.Fail(throwable) }
                    }
            }
    }
