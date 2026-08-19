package com.kikidan.sajucontents.model

sealed interface CompatibilityEntrySideEffect {
    data object ShowLoadError : CompatibilityEntrySideEffect

    data object ShowCreateError : CompatibilityEntrySideEffect

    data object NavigateToPartnerForm : CompatibilityEntrySideEffect

    data class NavigateToResult(
        val compatibilityId: String,
        val partnerLinkId: String,
    ) : CompatibilityEntrySideEffect
}
