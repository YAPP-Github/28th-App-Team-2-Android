package com.kikidan.data_remote.dto.user

import com.kikidan.data_remote.dto.common.toBirthTime
import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import com.kikidan.domain.model.user.Job
import com.kikidan.domain.model.user.RelationshipStatus
import com.kikidan.domain.model.user.User
import kotlinx.serialization.Serializable
import java.time.LocalDate

@Serializable
data class MyProfileResponse(
    val id: String,
    val name: String,
    val gender: String,
    val birthDate: String,
    val birthTime: String,
    val isTimeUnknown: Boolean,
    val calendarType: String,
    val job: String,
    val relationshipStatus: String,
)

internal fun MyProfileResponse.toDomain(): User =
    User(
        id = id,
        name = name,
        gender = Gender.valueOf(gender),
        job = Job.valueOf(job),
        relationshipStatus = RelationshipStatus.valueOf(relationshipStatus),
        birth =
            Birth(
                dateType = DateType.valueOf(calendarType),
                date = LocalDate.parse(birthDate),
                time = if (isTimeUnknown) BirthTime.UNKNOWN else birthTime.toBirthTime(),
            ),
    )
