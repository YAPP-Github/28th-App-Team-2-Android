package com.kikidan.mypage.edit.model

sealed interface MyPageEditUiState {
    data object Loading : MyPageEditUiState

    data class Success(
        val model: MyPageEditUiModel,
    ) : MyPageEditUiState

    data class Fail(
        val e: Throwable,
    ) : MyPageEditUiState
}
