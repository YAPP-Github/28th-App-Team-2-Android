package com.kikidan.onboarding

import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.auth.Job
import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.RelationshipStatus
import com.kikidan.domain.model.onboarding.OnboardingTerm
import com.kikidan.domain.model.onboarding.UserName
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.usecase.SignUpUseCase
import com.kikidan.onboarding.model.OnboardingDialog
import com.kikidan.onboarding.model.OnboardingSheet
import com.kikidan.onboarding.model.OnboardingSideEffect
import com.kikidan.onboarding.model.OnboardingState
import com.kikidan.onboarding.model.OnboardingStep
import com.kikidan.onboarding.model.toDomain
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel
    @Inject
    constructor(
        private val signUpUseCase: SignUpUseCase,
    ) : ViewModel(),
        ContainerHost<OnboardingState, OnboardingSideEffect> {
        override val container = container<OnboardingState, OnboardingSideEffect>(OnboardingState())

        fun clickNext() =
            intent {
                if (!state.canProceed) return@intent
                val next = state.step.next
                next?.let { reduce { state.copy(step = next) } }
            }

        fun clickBack() =
            intent {
                when (state.step) {
                    OnboardingStep.TERMS -> reduce { state.copy(dialog = OnboardingDialog.EXIT_CONFIRM) }
                    else -> state.step.previous?.let { previous -> reduce { state.copy(step = previous) } }
                }
            }

        fun dismissDialog() =
            intent {
                reduce { state.copy(dialog = null) }
            }

        fun confirmExit() =
            intent {
                reduce { OnboardingState() }
                postSideEffect(OnboardingSideEffect.Exit)
            }

        fun confirmComplete(onboardingToken: OnboardingToken) =
            intent {
                if (state.isSubmitting) return@intent
                val signupSubmission = state.toDomain()
                if (signupSubmission == null) {
                    postSideEffect(OnboardingSideEffect.InvalidInput)
                    return@intent
                }
                reduce { state.copy(isSubmitting = true) }
                signUpUseCase(
                    signupSubmission = signupSubmission,
                    onboardingToken = onboardingToken,
                ).onSuccess {
                    reduce { state.copy(isSubmitting = false, step = OnboardingStep.COMPLETE) }
                    postSideEffect(OnboardingSideEffect.PermissionRequest)
                }.onFailure { e ->
                    reduce { state.copy(isSubmitting = false) }
                    postSideEffect(OnboardingSideEffect.Failure(e))
                }
            }

        fun changeTerm(term: OnboardingTerm) =
            intent {
                val updated = state.termsAgreement.toggle(term)
                reduce { state.copy(termsAgreement = updated) }
            }

        fun changeAllTerms(agreed: Boolean) =
            intent {
                reduce { state.copy(termsAgreement = state.termsAgreement.withAll(agreed)) }
            }

        fun changeName(name: String) =
            intent {
                reduce { state.copy(username = UserName.from(name)) }
            }

        fun selectGender(gender: Gender) =
            intent {
                reduce { state.copy(gender = gender) }
            }

        fun selectCalendarType(calendarType: DateType) =
            intent {
                reduce { state.copy(calendarType = calendarType) }
            }

        fun openSheet(sheet: OnboardingSheet) =
            intent {
                reduce { state.copy(sheet = sheet) }
            }

        fun dismissSheet() =
            intent {
                reduce { state.copy(sheet = null) }
            }

        fun changeBirthDate(date: LocalDate) =
            intent {
                reduce { state.copy(birthDate = date) }
            }

        fun clearBirthDate() =
            intent {
                reduce { state.copy(birthDate = null) }
            }

        fun changeBirthTime(birthTime: BirthTime) =
            intent {
                reduce { state.copy(birthTime = birthTime) }
            }

        fun clearBirthTime() =
            intent {
                reduce { state.copy(birthTime = null) }
            }

        fun changeBirthTimeUnknown(unknown: Boolean) =
            intent {
                reduce {
                    state.copy(
                        birthTime = if (unknown) BirthTime.UNKNOWN else null,
                        sheet = if (unknown && state.sheet == OnboardingSheet.BIRTH_TIME) null else state.sheet,
                    )
                }
            }

        fun selectLifeStage(lifeStage: Job) =
            intent {
                reduce { state.copy(lifeStage = lifeStage) }
            }

        fun selectRelationshipStatus(relationshipStatus: RelationshipStatus) =
            intent {
                reduce { state.copy(relationshipStatus = relationshipStatus) }
            }
    }
