package com.kikidan.mypage.home

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.mypage.GetMyPageInfoUseCase
import com.kikidan.mypage.home.model.MyPageHomeSideEffect
import com.kikidan.mypage.home.model.MyPageHomeUiModel
import com.kikidan.mypage.home.model.MyPageHomeUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class MyPageHomeViewModel
    @Inject
    constructor(
        private val getMyPageInfoUseCase: GetMyPageInfoUseCase,
    ) : ViewModel(),
        ContainerHost<MyPageHomeUiState, MyPageHomeSideEffect> {
        override val container: Container<MyPageHomeUiState, MyPageHomeSideEffect> =
            container(MyPageHomeUiState.Loading) {
                repeatOnSubscription {
                    loadMyPageInfo()
                }
            }

        fun loadMyPageInfo() =
            intent {
                getMyPageInfoUseCase()
                    .onSuccess { info ->
                        reduce { MyPageHomeUiState.Success(MyPageHomeUiModel(info.user, info.sajuPalja)) }
                    }.onFailure { throwable ->
                        reduce { MyPageHomeUiState.Fail(throwable) }
                    }
            }
    }
