package com.kikidan.onboarding.model

import com.kikidan.domain.model.onboarding.OnboardingTerm
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.collections.immutable.toImmutableSet
import kotlinx.collections.immutable.toPersistentSet

data class TermsAgreementUiModel(
    val selected: PersistentSet<OnboardingTerm> = persistentSetOf<OnboardingTerm>(),
) {
    val allRequiredSelected: Boolean
        get() = OnboardingTerm.entries.none { it.required && it !in selected }

    val allSelected: Boolean
        get() = selected.size == OnboardingTerm.entries.size

    fun toggle(term: OnboardingTerm): TermsAgreementUiModel =
        copy(selected = if (isAgreed(term)) selected.removing(term) else selected.adding(term))

    fun withAll(value: Boolean): TermsAgreementUiModel =
        copy(selected = if (value) OnboardingTerm.entries.toPersistentSet() else persistentSetOf())

    fun isAgreed(onboardingTerm: OnboardingTerm) = onboardingTerm in selected
}
