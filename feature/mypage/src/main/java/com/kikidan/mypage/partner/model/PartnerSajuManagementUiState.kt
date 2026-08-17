package com.kikidan.mypage.partner.model

sealed interface PartnerSajuManagementUiState {
    data object Loading : PartnerSajuManagementUiState

    data class Success(
        val model: PartnerSajuManagementUiModel,
    ) : PartnerSajuManagementUiState

    data class Fail(
        val e: Throwable,
    ) : PartnerSajuManagementUiState
}
