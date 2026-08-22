package com.kikidan.data_remote.dto.compatibility

import com.kikidan.data_remote.dto.saju.ElementResponse
import com.kikidan.data_remote.dto.saju.RelationshipTypeResponse
import com.kikidan.data_remote.dto.saju.toOhaeng
import com.kikidan.domain.model.compatibility.Compatibility
import com.kikidan.domain.model.compatibility.CompatibilityOhaeng
import com.kikidan.domain.model.saju.RelationshipType
import kotlinx.serialization.Serializable

@Serializable
data class CompatibilityResponse(
    val id: String,
    val partnerName: String,
    val relationshipType: RelationshipTypeResponse,
    val score: Int,
    val headline: String,
    val subheadline: String,
    val summary: String,
    val totalAnalysis: String,
    val analysisBasis: String,
    val ohaengs: List<CompatibilityOhaengResponse>,
)

@Serializable
data class CompatibilityOhaengResponse(
    val element: ElementResponse,
    val percentage: Int,
)

fun CompatibilityResponse.toDomain(): Compatibility =
    Compatibility(
        id = id,
        partnerName = partnerName,
        relationshipType = RelationshipType(code = relationshipType.code, label = relationshipType.label),
        score = score,
        headline = headline,
        subheadline = subheadline,
        summary = summary,
        totalAnalysis = totalAnalysis,
        analysisBasis = analysisBasis,
        ohaengs = ohaengs.map { CompatibilityOhaeng(ohaeng = it.element.code.toOhaeng(), percentage = it.percentage) },
    )
