package com.kikidan.onboarding

import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.onboarding.OnboardingTerm
import com.kikidan.onboarding.model.TermsDialog
import com.kikidan.onboarding.model.TermsSideEffect
import com.kikidan.onboarding.model.TermsState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject

@HiltViewModel
class TermsViewModel
    @Inject
    constructor() :
    ViewModel(),
        ContainerHost<TermsState, TermsSideEffect> {
        override val container = container<TermsState, TermsSideEffect>(TermsState())

        fun changeTerm(term: OnboardingTerm) =
            intent {
                reduce { state.copy(termsAgreement = state.termsAgreement.toggle(term)) }
            }

        fun changeAllTerms(agreed: Boolean) =
            intent {
                reduce { state.copy(termsAgreement = state.termsAgreement.withAll(agreed)) }
            }

        fun clickBack() =
            intent {
                reduce { state.copy(dialog = TermsDialog.EXIT_CONFIRM) }
            }

        fun dismissDialog() =
            intent {
                reduce { state.copy(dialog = null) }
            }

        fun confirmExit() =
            intent {
                reduce { TermsState() }
                postSideEffect(TermsSideEffect.Exit)
            }
    }
