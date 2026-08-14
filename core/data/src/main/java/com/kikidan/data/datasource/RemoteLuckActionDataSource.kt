package com.kikidan.data.datasource

import com.kikidan.domain.model.fortune.LuckAction
import com.kikidan.domain.model.fortune.LuckActionDetail

interface RemoteLuckActionDataSource {
    suspend fun getTodayLuckActions(): List<LuckAction>

    suspend fun patchAchievement(luckActionId: String): LuckAction

    suspend fun getLuckAction(luckActionId: String): LuckActionDetail
}
