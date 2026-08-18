package com.kikidan.mypage.edit.model

import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import java.time.LocalDate

data class MyPageEditUiModel(
    val id: String,
    val name: String,
    val gender: Gender,
    val dateType: DateType,
    val birthDate: LocalDate,
    val birthTime: BirthTime,
    val lifeStatus: LifeStatus,
    val relationshipStatus: RelationshipStatus,
)
