package com.kikidan.domain.model.saju

import com.kikidan.domain.model.user.Birth
import com.kikidan.domain.model.user.Gender

data class PartnerSaju(
    val linkId: String,
    val name: String,
    val gender: Gender,
    val relationshipType: RelationshipType,
    val birth: Birth,
)

data class RelationshipType(
    val code: String,
    val label: String,
)
