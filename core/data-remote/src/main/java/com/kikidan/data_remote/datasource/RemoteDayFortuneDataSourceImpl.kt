package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteDayFortuneDataSource
import com.kikidan.data_remote.dto.dayfortune.CreateDayFortuneRequest
import com.kikidan.data_remote.dto.dayfortune.DayFortuneResponse
import com.kikidan.data_remote.dto.dayfortune.toDomain
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.dayfortune.DayFortune
import com.kikidan.domain.model.dayfortune.DayFortunePurpose
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RemoteDayFortuneDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteDayFortuneDataSource {
        override suspend fun postDayFortunes(
            purpose: DayFortunePurpose,
            targetDates: List<LocalDate>,
        ): List<DayFortune> =
            client
                .get()
                .post(DAY_FORTUNES_URL) {
                    setBody(
                        CreateDayFortuneRequest(
                            purpose = purpose.name,
                            targetDates = targetDates.map { it.format(DateTimeFormatter.ISO_LOCAL_DATE) },
                        ),
                    )
                }.bodyNotNull<List<DayFortuneResponse>>()
                .map { it.toDomain() }

        companion object {
            private const val DAY_FORTUNES_URL = "api/v1/day-fortunes"
        }
    }
