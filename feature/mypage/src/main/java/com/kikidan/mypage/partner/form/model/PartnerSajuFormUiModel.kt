package com.kikidan.mypage.partner.form.model

import com.kikidan.domain.model.user.BirthTime
import com.kikidan.domain.model.user.DateType
import com.kikidan.domain.model.user.Gender
import java.time.LocalDate

data class PartnerSajuFormUiModel(
    val linkId: String?,
    val name: String,
    val gender: Gender,
    val dateType: DateType,
    val birthDate: LocalDate?,
    val birthTime: BirthTime,
    val relationshipTypeCode: String,
)
