package com.kikidan.mypage.partner.form.model

sealed interface PartnerSajuFormSideEffect {
    data object NavigateBack : PartnerSajuFormSideEffect
}
