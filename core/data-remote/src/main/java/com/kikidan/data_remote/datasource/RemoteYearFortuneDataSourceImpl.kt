package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteYearFortuneDataSource
import com.kikidan.data_remote.dto.fortune.YearFortuneResponse
import com.kikidan.data_remote.dto.fortune.toDomain
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.fortune.YearFortune
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import javax.inject.Inject

class RemoteYearFortuneDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteYearFortuneDataSource {
        override suspend fun postYearFortune(year: Int): YearFortune =
            client
                .get()
                .post("api/v1/year-fortunes/$year")
                .bodyNotNull<YearFortuneResponse>()
                .toDomain()
    }
