package com.kikidan.data_remote.dto.saju

import com.kikidan.data_remote.dto.common.toApiValue
import com.kikidan.data_remote.dto.common.toBirthTime
import com.kikidan.domain.model.saju.PartnerSaju
import com.kikidan.domain.model.saju.PartnerSajuInput
import com.kikidan.domain.model.saju.RelationshipType
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class RelationshipTypeResponse(
    val code: String,
    val label: String,
)

@Serializable
data class PartnerSajuSummaryResponse(
    val linkId: String,
    val relationshipType: RelationshipTypeResponse,
    val name: String,
    val gender: String,
    val birthDate: String,
    val calendarType: String,
    val birthTime: String,
    val isTimeUnknown: Boolean,
)

@Serializable
data class PartnerSajuDetailResponse(
    val linkId: String,
    val relationshipType: RelationshipTypeResponse,
    val name: String,
    val gender: String,
    val birthDate: String,
    val calendarType: String,
    val birthTime: String,
    val isTimeUnknown: Boolean,
)

@Serializable
data class RegisterPartnerSajuRequest(
    val name: String,
    val gender: String,
    val calendarType: String,
    val birthDate: String,
    val birthTime: String,
    val relationshipType: String,
)

@Serializable
data class UpdatePartnerSajuRequest(
    val name: String,
    val gender: String,
    val calendarType: String,
    val birthDate: String,
    val birthTime: String,
    val relationshipType: String,
)

@Serializable
data class RegisterPartnerSajuResponse(
    val linkId: String,
)

internal fun PartnerSajuSummaryResponse.toDomain(): PartnerSaju =
    toPartnerSaju(linkId, name, gender, relationshipType, birthDate, calendarType, birthTime, isTimeUnknown)

internal fun PartnerSajuDetailResponse.toDomain(): PartnerSaju =
    toPartnerSaju(linkId, name, gender, relationshipType, birthDate, calendarType, birthTime, isTimeUnknown)

private fun toPartnerSaju(
    linkId: String,
    name: String,
    gender: String,
    relationshipType: RelationshipTypeResponse,
    birthDate: String,
    calendarType: String,
    birthTime: String,
    isTimeUnknown: Boolean,
): PartnerSaju =
    PartnerSaju(
        linkId = linkId,
        name = name,
        gender = Gender.valueOf(gender),
        relationshipType = RelationshipType(code = relationshipType.code, label = relationshipType.label),
        birth =
            Birth(
                dateType = DateType.valueOf(calendarType),
                date = LocalDate.parse(birthDate),
                time = if (isTimeUnknown) BirthTime.UNKNOWN else birthTime.toBirthTime(),
            ),
    )

internal fun PartnerSajuInput.toRegisterRequest(): RegisterPartnerSajuRequest =
    RegisterPartnerSajuRequest(
        name = name,
        gender = gender.name,
        calendarType = birth.dateType.name,
        birthDate = birth.date.toString(),
        birthTime = birth.time.toApiValue(),
        relationshipType = relationshipTypeCode,
    )

internal fun PartnerSajuInput.toUpdateRequest(): UpdatePartnerSajuRequest =
    UpdatePartnerSajuRequest(
        name = name,
        gender = gender.name,
        calendarType = birth.dateType.name,
        birthDate = birth.date.toString(),
        birthTime = birth.time.toApiValue(),
        relationshipType = relationshipTypeCode,
    )
