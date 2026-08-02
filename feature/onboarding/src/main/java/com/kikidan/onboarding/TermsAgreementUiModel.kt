package com.kikidan.onboarding

import com.kikidan.domain.model.onboarding.OnboardingTerm

data class TermsAgreementUiModel(
    val selected: Set<OnboardingTerm> = emptySet(),
) {
    val allRequiredSelected: Boolean
        get() = OnboardingTerm.entries.none { it.required && it !in selected }

    val allSelected: Boolean
        get() = selected.size == OnboardingTerm.entries.size

    fun toggle(term: OnboardingTerm): TermsAgreementUiModel =
        copy(selected = if (isAgreed(term)) selected - term else selected + term)

    fun withAll(value: Boolean): TermsAgreementUiModel =
        copy(selected = if (value) OnboardingTerm.entries.toSet() else emptySet())

    fun isAgreed(onboardingTerm: OnboardingTerm) = onboardingTerm in selected
}
