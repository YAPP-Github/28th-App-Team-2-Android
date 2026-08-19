package com.kikidan.sajucontents.model

import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import java.time.LocalDate

data class CompatibilityPartnerFormState(
    val name: String = "",
    val nameErrorMessageRes: Int? = null,
    val gender: Gender = Gender.MALE,
    val dateType: DateType = DateType.SOLAR,
    val birthDate: LocalDate? = null,
    val birthTime: BirthTime = BirthTime.UNKNOWN,
    val relationshipTypeCode: String = CompatibilityRelationshipType.LOVER.code,
    val isSaving: Boolean = false,
) {
    val isSaveEnabled: Boolean
        get() = name.isNotBlank() && nameErrorMessageRes == null && birthDate != null && !isSaving
}

sealed interface CompatibilityPartnerFormSideEffect {
    data object NavigateBack : CompatibilityPartnerFormSideEffect

    data object ShowSaveError : CompatibilityPartnerFormSideEffect
}
