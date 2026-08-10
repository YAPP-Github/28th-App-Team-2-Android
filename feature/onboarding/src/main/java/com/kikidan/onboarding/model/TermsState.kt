package com.kikidan.onboarding.model

import androidx.compose.runtime.Immutable

enum class TermsDialog {
    EXIT_CONFIRM,
}

@Immutable
data class TermsState(
    val termsAgreement: TermsAgreementUiModel = TermsAgreementUiModel(),
    val dialog: TermsDialog? = null,
) {
    val canProceed: Boolean
        get() = termsAgreement.allRequiredSelected
}
