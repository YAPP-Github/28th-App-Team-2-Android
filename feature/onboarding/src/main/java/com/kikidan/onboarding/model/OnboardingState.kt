package com.kikidan.onboarding.model

import androidx.compose.runtime.Immutable
import com.kikidan.domain.model.auth.Job
import com.kikidan.domain.model.auth.RelationshipStatus
import com.kikidan.domain.model.auth.SignupSubmission
import com.kikidan.domain.model.onboarding.UserName
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import java.time.LocalDate

enum class OnboardingSheet {
    BIRTH_DATE,
    BIRTH_TIME,
}

enum class OnboardingDialog {
    EXIT_CONFIRM,
    SIGN_UP_COMPLETE,
}

@Immutable
data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.TERMS,
    val termsAgreement: TermsAgreementUiModel = TermsAgreementUiModel(),
    val username: UserName = UserName.Invalid.Empty,
    val gender: Gender? = null,
    val calendarType: DateType? = null,
    val birthDate: LocalDate? = null,
    val birthTime: BirthTime? = null,
    val lifeStage: Job? = null,
    val relationshipStatus: RelationshipStatus? = null,
    val sheet: OnboardingSheet? = null,
    val dialog: OnboardingDialog? = null,
    val isSubmitting: Boolean = false,
) {
    val canProceed: Boolean
        get() =
            when (step) {
                OnboardingStep.TERMS -> {
                    termsAgreement.allRequiredSelected
                }

                OnboardingStep.NAME -> {
                    username is UserName.Valid
                }

                OnboardingStep.BIRTH_INFO -> {
                    gender != null &&
                        calendarType != null &&
                        birthDate != null &&
                        birthTime != null
                }

                OnboardingStep.EXTRA_QUESTION -> {
                    lifeStage != null && relationshipStatus != null
                }
            }
}

fun OnboardingState.toDomain(): SignupSubmission? {
    return SignupSubmission(
        name = (username as? UserName.Valid)?.name ?: return null,
        job = lifeStage ?: return null,
        relationshipStatus = relationshipStatus ?: return null,
        gender = gender ?: return null,
        birth =
            Birth(
                dateType = calendarType ?: return null,
                date = birthDate ?: return null,
                time = birthTime ?: return null,
            ),
    )
}
