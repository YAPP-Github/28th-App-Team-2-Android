package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteCompatibilityDataSource
import com.kikidan.data_remote.dto.compatibility.CompatibilityResponse
import com.kikidan.data_remote.dto.compatibility.toDomain
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.compatibility.Compatibility
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import javax.inject.Inject

class RemoteCompatibilityDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteCompatibilityDataSource {
        override suspend fun postCompatibility(partnerLinkId: String): Compatibility =
            client
                .get()
                .post("$COMPATIBILITIES_URL/$partnerLinkId")
                .bodyNotNull<CompatibilityResponse>()
                .toDomain()

        override suspend fun getCompatibility(compatibilityId: String): Compatibility =
            client
                .get()
                .get("$COMPATIBILITIES_URL/$compatibilityId")
                .bodyNotNull<CompatibilityResponse>()
                .toDomain()

        companion object {
            private const val COMPATIBILITIES_URL = "api/v1/compatibilities"
        }
    }
