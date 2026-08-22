package com.kikidan.data.repository

import com.kikidan.data.datasource.RemoteCompatibilityDataSource
import com.kikidan.domain.model.compatibility.Compatibility
import com.kikidan.domain.repository.CompatibilityRepository
import com.kikidan.domain.util.runCatchingCancellable
import javax.inject.Inject

class CompatibilityRepositoryImpl
    @Inject
    constructor(
        private val remoteCompatibilityDataSource: RemoteCompatibilityDataSource,
    ) : CompatibilityRepository {
        override suspend fun createCompatibility(partnerLinkId: String): Result<Compatibility> =
            runCatchingCancellable { remoteCompatibilityDataSource.postCompatibility(partnerLinkId) }

        override suspend fun getCompatibility(compatibilityId: String): Result<Compatibility> =
            runCatchingCancellable { remoteCompatibilityDataSource.getCompatibility(compatibilityId) }
    }
