package com.kikidan.data_remote.dto.auth

import com.kikidan.domain.model.auth.OnboardingToken
import com.kikidan.domain.model.auth.SignupSubmission
import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.User
import kotlinx.serialization.Serializable

@Serializable
data class SignupRequest(
    val birthDate: String,
    val birthTime: String,
    val calendarType: String,
    val gender: String,
    val job: String,
    val name: String,
    val onboardingToken: String,
    val relationshipStatus: String,
)

fun SignupSubmission.toSignupRequest(onboardingToken: OnboardingToken): SignupRequest =
    SignupRequest(
        birthDate = birth.date.toString(),
        birthTime = birth.time.toApiValue(),
        calendarType = birth.dateType.name,
        gender = gender.name,
        job = job.name,
        name = name,
        onboardingToken = onboardingToken.value,
        relationshipStatus = relationshipStatus.name,
    )

/** 서버 스펙(예: JASI)과 도메인 enum 이름(예: JA)이 달라 별도 매핑이 필요하다. */
private fun BirthTime.toApiValue(): String =
    when (this) {
        BirthTime.JA -> "JASI"
        BirthTime.CHUK -> "CHUKSI"
        BirthTime.IN -> "INSI"
        BirthTime.MYO -> "MYOSI"
        BirthTime.JIN -> "JINSI"
        BirthTime.SA -> "SASI"
        BirthTime.O -> "OSI"
        BirthTime.MI -> "MISI"
        BirthTime.SIN -> "SINSI"
        BirthTime.YU -> "YUSI"
        BirthTime.SUL -> "SULSI"
        BirthTime.HAE -> "HAESI"
        BirthTime.UNKNOWN -> "UNKNOWN"
    }
