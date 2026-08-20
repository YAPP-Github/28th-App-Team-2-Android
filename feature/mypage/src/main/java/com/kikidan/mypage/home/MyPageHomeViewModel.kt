package com.kikidan.mypage.home

import androidx.lifecycle.ViewModel
import com.kikidan.domain.usecase.LogoutUseCase
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
        private val logoutUseCase: LogoutUseCase,
        private val appVersionChecker: AppVersionChecker,
    ) : ViewModel(),
        ContainerHost<MyPageHomeUiState, MyPageHomeSideEffect> {
        override val container: Container<MyPageHomeUiState, MyPageHomeSideEffect> =
            container(MyPageHomeUiState.Loading) {
                loadMyPageInfo()
            }

        fun loadMyPageInfo() =
            intent {
                getMyPageInfoUseCase()
                    .onSuccess { info ->
                        val isLatestVersion = appVersionChecker.isLatestVersion()
                        reduce {
                            MyPageHomeUiState.Success(
                                MyPageHomeUiModel(
                                    user = info.user,
                                    sajuPalja = info.sajuPalja,
                                    appVersionName = appVersionChecker.getCurrentVersionName(),
                                    isLatestVersion = isLatestVersion,
                                ),
                            )
                        }
                    }.onFailure { throwable ->
                        reduce { MyPageHomeUiState.Fail(throwable) }
                    }
            }

        fun logout() =
            intent {
                logoutUseCase()
                    .onSuccess { postSideEffect(MyPageHomeSideEffect.NavigateToLogin) }
                    .onFailure { postSideEffect(MyPageHomeSideEffect.ShowLogoutError) }
            }
    }
