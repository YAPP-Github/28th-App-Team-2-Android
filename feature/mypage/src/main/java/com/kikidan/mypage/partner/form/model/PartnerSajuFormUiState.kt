package com.kikidan.mypage.partner.form.model

sealed interface PartnerSajuFormUiState {
    data object Loading : PartnerSajuFormUiState

    data class Success(
        val model: PartnerSajuFormUiModel,
        val isSaving: Boolean = false,
    ) : PartnerSajuFormUiState

    data class Fail(
        val e: Throwable,
    ) : PartnerSajuFormUiState
}
