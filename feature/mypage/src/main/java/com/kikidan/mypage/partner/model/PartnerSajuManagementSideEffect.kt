package com.kikidan.mypage.partner.model

sealed interface PartnerSajuManagementSideEffect {
    data object NavigateToAddPartner : PartnerSajuManagementSideEffect

    data object ShowMaxPartnerLimitSnackbar : PartnerSajuManagementSideEffect
}
