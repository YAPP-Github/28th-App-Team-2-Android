package com.kikidan.sajucontents

import androidx.lifecycle.ViewModel
import com.kikidan.domain.model.saju.PartnerSajuInput
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.usecase.saju.RegisterPartnerSajuUseCase
import com.kikidan.sajucontents.model.CompatibilityPartnerFormSideEffect
import com.kikidan.sajucontents.model.CompatibilityPartnerFormState
import com.kikidan.sajucontents.model.SavePartnerState
import dagger.hilt.android.lifecycle.HiltViewModel
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.time.LocalDate
import javax.inject.Inject

private const val MAX_NAME_LENGTH = 10

@HiltViewModel
class CompatibilityPartnerFormViewModel
    @Inject
    constructor(
        private val registerPartnerSajuUseCase: RegisterPartnerSajuUseCase,
    ) : ViewModel(),
        ContainerHost<CompatibilityPartnerFormState, CompatibilityPartnerFormSideEffect> {
        override val container: Container<CompatibilityPartnerFormState, CompatibilityPartnerFormSideEffect> =
            container(CompatibilityPartnerFormState())

        fun updateName(name: String) =
            intent {
                val errorRes =
                    if (name.length > MAX_NAME_LENGTH) R.string.compatibility_partner_form_name_error else null
                reduce { state.copy(name = name, nameErrorMessageRes = errorRes) }
            }

        fun selectGender(gender: Gender) = intent { reduce { state.copy(gender = gender) } }

        fun selectDateType(dateType: DateType) = intent { reduce { state.copy(dateType = dateType) } }

        fun updateBirthDate(birthDate: LocalDate?) = intent { reduce { state.copy(birthDate = birthDate) } }

        fun updateBirthTime(birthTime: BirthTime) = intent { reduce { state.copy(birthTime = birthTime) } }

        fun selectRelationshipType(code: String) = intent { reduce { state.copy(relationshipTypeCode = code) } }

        fun save() =
            intent {
                val birthDate = state.birthDate ?: return@intent
                if (!state.isSaveEnabled) return@intent
                reduce { state.copy(saveState = SavePartnerState.Loading) }

                val input =
                    PartnerSajuInput(
                        name = state.name,
                        gender = state.gender,
                        relationshipTypeCode = state.relationshipTypeCode,
                        birth = Birth(dateType = state.dateType, date = birthDate, time = state.birthTime),
                    )
                registerPartnerSajuUseCase(input)
                    .onSuccess {
                        reduce { state.copy(saveState = SavePartnerState.Success) }
                        postSideEffect(CompatibilityPartnerFormSideEffect.NavigateBack)
                    }.onFailure {
                        reduce { state.copy(saveState = SavePartnerState.Failure) }
                        postSideEffect(CompatibilityPartnerFormSideEffect.ShowSaveError)
                    }
            }
    }
