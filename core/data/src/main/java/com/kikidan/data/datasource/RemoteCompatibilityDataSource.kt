package com.kikidan.data.datasource

import com.kikidan.domain.model.compatibility.Compatibility

interface RemoteCompatibilityDataSource {
    suspend fun postCompatibility(partnerLinkId: String): Compatibility

    suspend fun getCompatibility(compatibilityId: String): Compatibility
}
