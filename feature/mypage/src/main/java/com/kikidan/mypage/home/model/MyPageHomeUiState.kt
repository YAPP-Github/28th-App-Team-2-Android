package com.kikidan.mypage.home.model

sealed interface MyPageHomeUiState {
    data object Loading : MyPageHomeUiState

    data class Success(
        val model: MyPageHomeUiModel,
    ) : MyPageHomeUiState

    data class Fail(
        val e: Throwable,
    ) : MyPageHomeUiState
}
