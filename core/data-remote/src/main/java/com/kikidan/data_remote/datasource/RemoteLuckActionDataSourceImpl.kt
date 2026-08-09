package com.kikidan.data_remote.datasource

import com.kikidan.data.datasource.RemoteLuckActionDataSource
import com.kikidan.data_remote.dto.fortune.LuckActionResponse
import com.kikidan.data_remote.dto.fortune.TodayLuckActionResponse
import com.kikidan.data_remote.dto.fortune.toDomain
import com.kikidan.data_remote.util.bodyNotNull
import com.kikidan.domain.model.fortune.LuckAction
import dagger.Lazy
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.patch
import javax.inject.Inject

class RemoteLuckActionDataSourceImpl
    @Inject
    constructor(
        private val client: Lazy<HttpClient>,
    ) : RemoteLuckActionDataSource {
        override suspend fun getTodayLuckActions(): List<LuckAction> =
            client
                .get()
                .get(TODAY_LUCK_ACTIONS_URL)
                .bodyNotNull<List<TodayLuckActionResponse>>()
                .map { it.toDomain() }

        override suspend fun patchAchievement(luckActionId: String): LuckAction =
            client
                .get()
                .patch(achievementUrl(luckActionId))
                .bodyNotNull<LuckActionResponse>()
                .toDomain()

        companion object {
            private const val LUCK_ACTIONS_URL = "api/v1/luck-actions"
            private const val TODAY_LUCK_ACTIONS_URL = "$LUCK_ACTIONS_URL/today"

            private fun achievementUrl(luckActionId: String) = "$LUCK_ACTIONS_URL/$luckActionId/achievement"
        }
    }
