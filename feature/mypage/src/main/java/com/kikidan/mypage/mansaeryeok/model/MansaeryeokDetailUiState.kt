package com.kikidan.mypage.mansaeryeok.model

sealed interface MansaeryeokDetailUiState {
    data object Loading : MansaeryeokDetailUiState

    data class Success(
        val model: MansaeryeokDetailUiModel,
    ) : MansaeryeokDetailUiState

    data class Fail(
        val e: Throwable,
    ) : MansaeryeokDetailUiState
}
