package com.kikidan.domain.model.saju

import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.Gender

data class PartnerSajuInput(
    val name: String,
    val gender: Gender,
    val relationshipTypeCode: String,
    val birth: Birth,
)
