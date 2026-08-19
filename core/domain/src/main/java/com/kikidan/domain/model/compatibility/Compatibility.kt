package com.kikidan.domain.model.compatibility

import com.kikidan.domain.model.saju.Ohaeng
import com.kikidan.domain.model.saju.RelationshipType

data class Compatibility(
    val id: String,
    val partnerName: String,
    val relationshipType: RelationshipType,
    val score: Int,
    val headline: String,
    val subheadline: String,
    val summary: String,
    val totalAnalysis: String,
    val analysisBasis: String,
    val ohaengs: List<CompatibilityOhaeng>,
)

data class CompatibilityOhaeng(
    val ohaeng: Ohaeng,
    val percentage: Int,
)
