package com.kikidan.domain.repository

import com.kikidan.domain.model.compatibility.Compatibility

interface CompatibilityRepository {
    suspend fun createCompatibility(partnerLinkId: String): Result<Compatibility>

    suspend fun getCompatibility(compatibilityId: String): Result<Compatibility>
}
